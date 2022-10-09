package com.yaocf.support.ops

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.Keep
import androidx.annotation.WorkerThread
import com.yaocf.support.ops.natives.NativeErrorHandler
import com.yaocf.support.ops.natives.NativeErrorManager
import com.yaocf.support.util.ThreadUtils
import java.io.*
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * created by:   yaochunfeng
 * on:           2020/3/31 14:25
 * Des:          Used to 处理默认的异常信息
 * Email:        yaochunfeng@wondersgroup.com
 *
 */
@Keep
class DefaultExceptionHandler private constructor() : Thread.UncaughtExceptionHandler {
    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
        //        ThreadUtils.schedule(this::cleanOutdatedLogFile, 5, TimeUnit.SECONDS);
    }

    /**
     * 清理过期日志文件。
     * //TODO 调整为保留最近的指定个数的日志文件，而不是只保留最近7天的，避免用户修改日期导致日志文件被抹除。 提示：File.listFiles()  Comparator    File.lastModified()    Arrays.sort()
     *
     */
    private fun cleanOutdatedLogFile() {
        //debug模式和测试环境不清理。
//        if (ManniuDynamicBuildConfig.isAppDebugable()){
//            return;
//        }
        //需要保留的日志天数。
        var keepLogsDay = 9
        val dateStrs: MutableList<String> = ArrayList()
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        val currentHours = SimpleDateFormat("HH", Locale.getDefault()).format(calendar.time)
        //如果超过中午12点，则保留多一天的日志。
        try {
            if (currentHours.toInt() > 12) {
                keepLogsDay += 1
            }
        } catch (e: Exception) {
            //do nothing
        }
        //保留当天的
        dateStrs.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
        //获取所有需要保留的日志文件的时间字符串
        for (i in 0 until keepLogsDay) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            dateStrs.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
        }
        val errorLogsDir = File(mAppContext!!.obbDir.absolutePath + ERROR_LOG)
        var logFiles = arrayOf<File?>()
        if (errorLogsDir.exists() && errorLogsDir.isDirectory) {
            logFiles = errorLogsDir.listFiles()
        }
        //遍历obb目录下的日志文件列表
        for (logFile in logFiles) {
            //只处理正常的文件。
            if (logFile != null && logFile.exists() && logFile.isFile) {
                var needDelete = true
                for (dateStr in dateStrs) {
                    //文件名包含时间字符串说明该文件在保留列表中。标记为要保留。
                    if (logFile.name.contains(dateStr)) {
                        needDelete = false
                    }
                }
                //如果要删除，在最后执行删除。
                if (needDelete) {
                    logFile.delete()
                }
            }
        }
    }

    /**
     * 注册默认的异常处理类
     *
     * @param handler            异常处理器
     * @param appContext         app的上下文。用来弹吐司提示。
     * @param enableCommonNitice 是否需要弹出常规提示（用于在测试环境提示测试人员有异常发生了。）
     */
    fun regist(appContext: Application, handler: ExceptionHandler, enableCommonNitice: Boolean
               , nativeErrorSignals:IntArray?, nativeErrorHandler:NativeErrorHandler?) {
        mHandler = handler
        mAppContext = appContext
        mEnableCommonNitice = enableCommonNitice
        //注册native层的异常拦截
        if (nativeErrorSignals != null){
            //这边以一个野线程的方式去启动（让so attach到一个独立的线程中），避免被线程池调度。TODO 所以，这里建议设置一下防止重入。
            Thread(object :Runnable{
                override fun run() {
                    NativeErrorManager.initSignal(nativeErrorSignals, appContext, nativeErrorHandler)
                }
            }).start()
        }
        //进行主线程拦截初始化
        Handler(Looper.getMainLooper())
            .postAtFrontOfQueue(Runnable { initLooper() })
    }

    private fun initLooper() {
        synchronized(Looper::class.java) {
            if (Looper.getMainLooper() == null) {
                Looper.prepareMainLooper()
            }
        }
        if (Looper.myLooper() == null) {
            Looper.prepare()
        }
        do {
            try {
                Looper.loop()
            } catch (throwable: Exception) {
                postException(throwable, Thread.currentThread())
                ThreadUtils.submit {
                    try {
                        dumpExceptionToLogFile(Thread.currentThread(), throwable)
                    } catch (e: IOException) {
                        Log.w(LOG_TAG, "在将异常写入到日志文件时发生了错误：\n", throwable);
                        postException(IOException("在将异常写入到日志文件时发生了错误", e), Thread.currentThread())
                    }
                }
                //无论是否开启了提示或者是否写入文件。都将错误信息打印一份到控制台。
                Log.e(LOG_TAG, "捕获到了一个未处理异常：\n", throwable);
                if (mEnableCommonNitice) {
                    showCenterIndependence("抱歉，App发生了一些错误，具体请查看日志文件。" + throwable.message)
                }
                if (Looper.myLooper() == null) {
                    Looper.prepare()
                }
            }
            //为了符合编码规范，这里加了一个不可能到达的条件。
        } while (System.currentTimeMillis() >= 0 || Thread.currentThread().id >= 0)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        postException(e, t)
        ThreadUtils.submit {
            try {
                dumpExceptionToLogFile(t, e)
            } catch (ex: IOException) {
                Log.w(LOG_TAG, "在将异常写入到日志文件时发生了错误：\n", ex);
                postException(IOException("在将异常写入到日志文件时发生了错误", e), Thread.currentThread())
            }
            if (t != null) {
                //中断这个异常线程。
                t.interrupt()
            }
        }
        //无论是否开启了提示或者是否写入文件。都将错误信息打印一份到控制台。
        Log.w(LOG_TAG, "捕获到了一个未处理异常：\n", e);
//        //最后再交由系统的或者第三方的处理一下。
//        if (defaultHandler != this && defaultHandler != null) {
//            defaultHandler.uncaughtException(t, new RuntimeException("已post到Bugly的异常。", e));
//        }
    }

    interface ExceptionHandler {
        /**
         * 异常发生后的回调
         * @param exception 异常
         * @param occuredOnUIThread true，这个异常是UI线程中发生的。
         * @return true, 则消耗掉这次异常。否则，表示这个异常需要继续往上抛！
         */
        @WorkerThread
        fun onExceptionOccured(exception: Throwable, occuredOnUIThread: Boolean)
    }

    companion object {
        private const val LOG_TAG = "异常捕获工具"
        private var mAppContext: Application? = null
        private var mEnableCommonNitice = false
        private const val ERROR_LOG = "/manniucommonlogs/"
        private var mHandler: ExceptionHandler = object : ExceptionHandler {
            override fun onExceptionOccured(
                exception: Throwable,
                occuredOnUIThread: Boolean
            ) {
                //默认空实现。
            }
        }

        private val instance = DefaultExceptionHandler()

        fun getInstance():DefaultExceptionHandler{
            return instance;
        }
        /**
         * 将异常信息推给异常处理
         *
         * @param throwable
         * @param thread
         */
        fun postException(throwable: Throwable, thread: Thread?) {
            ThreadUtils.submit {
                mHandler.onExceptionOccured(
                    throwable,
                    ThreadUtils.isUIThread(thread)
                )
            }
        }

        /**
         * 把异常信息写入到日志文件里面去。
         * obb目录一般不需要申请存储权限
         * TODO 完成加密存储，而不是直接明文存储。并且，最好是使用非固定密钥！
         *
         * @param thread
         * @param throwable
         */
        @Throws(IOException::class)
        fun dumpExceptionToLogFile(thread: Thread, throwable: Throwable) {
            var logFile: File
            val baseDir = mAppContext!!.obbDir.absolutePath
            //按天-小时分好。获取文件名。
            val date = SimpleDateFormat("yyyy-MM-dd_HH", Locale.getDefault())
                .format(Calendar.getInstance(TimeZone.getDefault()).time)
            run {
                //避免用户创建同名文件或者同名目录导致日志写入失败。
                var result = false
                val obbDir = File(baseDir)
                if (obbDir.isFile) {
                    result = obbDir.delete()
                }
                val logDir = File(baseDir + ERROR_LOG)
                if (logDir.isFile) {
                    result = logDir.delete()
                }
                logFile = File(baseDir + ERROR_LOG + date + ".txt")
                if (logFile.isDirectory) {
                    result = logFile.delete()
                }
                result = logDir.mkdirs()
                if (!logFile.exists()) {
                    result = logFile.createNewFile()
                }
                if (!logFile.setWritable(true)) {
                Log.v(LOG_TAG, "将日志文件设置为可写入失败。" + result);
                }
            }
            run {
                //将异常信息写入
                var errorHeader = """

---异常发生于: ${
                    DateFormat.getTimeInstance()
                        .format(Calendar.getInstance(TimeZone.getDefault()).time)
                } ---位于线程: $thread"""
                errorHeader += """

---异常原因: ${throwable.message} ---"""
                BufferedOutputStream(FileOutputStream(logFile, true)).use { stream ->
                    stream.write(errorHeader.toByteArray(StandardCharsets.UTF_8))
                    //获取完整堆栈
                    val sw = StringWriter()
                    throwable.printStackTrace(PrintWriter(sw))
                    errorHeader = sw.toString()
                    stream.write(errorHeader.toByteArray(StandardCharsets.UTF_8))
                    errorHeader = "\n\r--- 异常信息Dump结束. ---\n\r"
                    stream.write(errorHeader.toByteArray(StandardCharsets.UTF_8))
                    stream.flush()
                }
            }
        }

        /**
         * 中央toast(独立的，不会复用！注意不要频繁的调用。)
         */
        fun showCenterIndependence(res: String?) {
            if (TextUtils.isEmpty(res)) {
                return
            }
            Handler(Looper.getMainLooper())
                .post(
                    Runnable {
                        val toast: Toast = Toast.makeText(mAppContext, res, Toast.LENGTH_SHORT)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.setText(res)
                        toast.show()
                    }
                )
        }

        fun showCenterIndependence(id: Int) {
            showCenterIndependence(mAppContext!!.getString(id))
        }
    }
}
