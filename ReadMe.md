## 说明文档。
#### 注册方式
```kotlin
class CodeBlock{
    private val nativeErrors = intArrayOf(
        SysSignalConst.SIGILL,
        SysSignalConst.SIGTRAP,
        SysSignalConst.SIGABRT,
        SysSignalConst.SIGBUS,
        SysSignalConst.SIGFPE,
        SysSignalConst.SIGSEGV,
    )
    fun init(){
        DefaultExceptionHandler.getInstance().regist(this,
                object:DefaultExceptionHandler.ExceptionHandler{
            override fun onExceptionOccured(
                    exception: Throwable,
                    occuredOnUIThread: Boolean
                ) {
                ToastUtil.showCenter(
                        "UI线程?${occuredOnUIThread},收到Java异常:${exception.message}"
                )
            }
        },
        false,
                nativeErrors,
                object : NativeErrorHandler {
            override fun onSysSignalRec(
                    context: Context,
                    signal: Int,
                    nativeStackTrace: String,
                    currentThread: Thread
                ): Boolean {
                ToastUtil.showCenter(
                        "收到Native异常:$nativeStackTrace"
                )
                return true
            }
        })
    }
}
```

#### 测试代码
```java
class CodeBlock{
    public void testNative() {
        //测试native的错误
        NativeErrorManager.raiseNativeSignal(SysSignalConst.SIGBUS);
    }
    public void testJava(){
        //测试java的错误
        throw new NullPointerException("测试异常");
    }
}
```

#### 注意点
- `com.yaocf.support.provider.ContextProvider`会在App启动时自动启动。
- 如果需要将注册代码放到onAttachBaseContext中，请将相关的所有类，加入到MultiDex的主Dex中（参见：[Android配置multidex](https://developer.android.com/studio/build/multidex?hl=zh-cn#keep)）
  - 该做法可以更大范围地捕捉和控制异常。
