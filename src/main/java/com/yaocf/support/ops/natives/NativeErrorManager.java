package com.yaocf.support.ops.natives;

import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import com.yaocf.support.ops.natives.NativeErrorHandler;
import com.yaocf.support.ops.natives.SysSignalException;

/**
 * <pre>
 * Used to
 * <pre/>
 * created by:   yaochunfeng
 * on:           2022/9/23 15:52
 * Email:        yaochunfeng@wondersgroup.com
 */
public class NativeErrorManager {
    static {
        System.loadLibrary("native_error_handler");
    }

    private static Context mContext;
    private static NativeErrorHandler mNativeErrorHandler;

    public static void signalError() throws SysSignalException {
        throw new SysSignalException();
    }

    /**
     * 一般只需要处理7、6、8、4、5、11
     */
    public static void initSignal(int[] signals, Context appContext, NativeErrorHandler nativeErrorHandler) {
        initWithSignals(signals);
        mContext = appContext;
        mNativeErrorHandler = nativeErrorHandler;
    }

    //通知native层去替换指定的信号的处理函数。
    public static native void initWithSignals(int[] signals);
    //测试用的!!!用来raise一个指定的信号
    public static native void raiseNativeSignal(int signals);

    //当native捕获到信号量时，接收native的调用，以处理错误信号。这里也可以进行一些联动，比如让java返回一些标记，以此来让native层做一些响应。
    public static void callNativeException(int signal, String nativeStackTrace) {
        Log.i("native_error_handler", "callNativeException $signal");
        // TODO 获取java堆栈
        if (mNativeErrorHandler != null && !mNativeErrorHandler.onSysSignalRec(mContext, signal, nativeStackTrace, Thread.currentThread())) {
            // 默认处理 重启app(避免由于没处理，导致黑屏卡死)
            Intent killIntent =
                    mContext.getPackageManager().getLaunchIntentForPackage(mContext.getPackageName());
            // context 可能为application等无任务栈的context，需要添加任务栈标记FLAG_ACTIVITY_NEW_TASK
            killIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            killIntent.setAction("restart");
            mContext.startActivity(killIntent);
            Process.killProcess(Process.myPid());
            System.exit(0);
        }
    }
}
