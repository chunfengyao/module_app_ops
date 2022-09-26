#include <jni.h>
#include <string>
#include <signal.h>
#include <android/log.h>
#include "signal_utils.h"


#define TAG "native_error_handler"

/**
 * <pre>
 * Used to 用于接收系统的信号
 * <pre/>
 * created by:   yaochunfeng
 * on:           2021/6/7 3:19 下午
 * Email:        yaochunfeng@wondersgroup.com
 */

jobject currentObj;
JNIEnv *currentEnv = nullptr;

//uintptr_t getPc(const ucontext_t *uc) {
//#if (defined(__arm__))
//    return uc->uc_mcontext.arm_pc;
//#elif defined(__aarch64__)
//    return uc->uc_mcontext.pc;
//#elif (defined(__x86_64__))
//    return uc->uc_mcontext.gregs[REG_RIP];
//#else
//#error "unsupport"
//#endif
//}


void SigFunc(int sig_num, siginfo *info, void *ptr) {
    // 这里判空并不代表这个对象就是安全的，因为有可能是脏内存
    if (currentEnv == nullptr || currentObj == nullptr) {
        return;
    }
    __android_log_print(ANDROID_LOG_INFO, TAG, "%d catch", sig_num);
    __android_log_print(ANDROID_LOG_INFO, TAG, "crash info pid:%d ", info->si_pid);
    jclass main = currentEnv->FindClass("com/yaocf/support/ops/natives/NativeErrorManager");
    jmethodID id = currentEnv->GetStaticMethodID(main, "callNativeException", "(ILjava/lang/String;)V");
    //这里可以进行一些联动，比如让java返回一些标记，以此来让native层做一些响应。
    if (!id) {
        return;
    }

    jstring nativeStackTrace  = currentEnv->NewStringUTF(backtraceToLogcat().c_str());
    currentEnv->CallStaticVoidMethod(main, id, sig_num, nativeStackTrace);

    // 释放资源
//    currentEnv->DeleteGlobalRef(currentObj);
    currentEnv->DeleteLocalRef(nativeStackTrace);
}

extern "C" jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    jint result = -1;
    // 直接用vm进行赋值，不然不可靠
    backtraceToLogcat();
    if (vm->GetEnv((void **) &currentEnv, JNI_VERSION_1_4) != JNI_OK) {
        return result;
    }
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {

}


extern "C"
JNIEXPORT void JNICALL
Java_com_yaocf_support_ops_natives_NativeErrorManager_initWithSignals(JNIEnv *env,
                                                                        jclass thiz,
                                                                       jintArray signals) {
    // 必须生成全局变量，直接赋值的话只是局部变量，被回收后存在脏变量风险
    currentObj = env->NewGlobalRef(thiz);
    // 注意释放内存
    jint *signalsFromJava = env->GetIntArrayElements(signals, 0);
    int size = env->GetArrayLength(signals);
    bool needMask = false;

    for (int i = 0; i < size; i++) {
        if (signalsFromJava[i] == SIGQUIT) {
            needMask = true;
        }
    }

    do {
        sigset_t mask;
        sigset_t old;
        if (needMask) {
            sigemptyset(&mask);
            sigaddset(&mask, SIGQUIT);
            if (0 != pthread_sigmask(SIG_UNBLOCK, &mask, &old)) {
                break;
            }
        }

        struct sigaction sigc;
        //sigc.sa_handler = SigFunc;
        sigc.sa_sigaction = SigFunc;
        sigemptyset(&sigc.sa_mask);
        sigc.sa_flags = SA_SIGINFO;

        // 注册所有信号
        for (int i = 0; i < size; i++) {
            // 这里不需要旧的处理函数
            // 指定SIGKILL和SIGSTOP以外的所有信号
            int flag = sigaction(signalsFromJava[i], &sigc, nullptr);
            if (flag == -1) {
                __android_log_print(ANDROID_LOG_INFO, TAG, "register fail ===== signals[%d] ",
                                    i);
                // 异常处理
                jclass main = currentEnv->FindClass("com/yaocf/support/ops/natives/NativeErrorManager");
                jmethodID id = currentEnv->GetStaticMethodID(main, "signalError", "()V");
                env->CallStaticVoidMethod(main, id);
                // 失败后需要恢复原样
                if (needMask) {
                    pthread_sigmask(SIG_UNBLOCK, &old, nullptr);
                }
                break;
            }
        }


    } while (0);

    env->ReleaseIntArrayElements(signals, signalsFromJava, 0);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_yaocf_support_ops_natives_NativeErrorManager_raiseNativeSignal(JNIEnv *env,
                                              jclass clazz,
                                               jint signals) {
    //通知系统raise一个信号量
    raise(signals);
}