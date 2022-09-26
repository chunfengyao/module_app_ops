package com.yaocf.support.util

import android.os.Handler
import android.os.Looper
import com.yaocf.support.ops.DefaultExceptionHandler
import java.lang.Exception
import java.lang.RuntimeException
import java.util.concurrent.Future
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

/**
 * <pre>
 * Used to 线程和线程池的相关工具。
 * <pre></pre>
 * created by:   yaochunfeng
 * on:           2020/5/4 11:14
 * Email:        yaochunfeng@wondersgroup.com
</pre> */
public class ThreadUtils {
    /**
     * 一般不需要调用release！！！
     */
    fun releaseThreadPool() {
        threadPool.shutdown()
    }

    companion object {
        private const val LOG_TAG = "线程工具类"
        private val mMainLooperHandler: Handler = Handler(Looper.getMainLooper())
        private var threadPoolInstance: ScheduledThreadPoolExecutor? = null
        private val threadPool: ScheduledThreadPoolExecutor = if (threadPoolInstance == null) ScheduledThreadPoolExecutor(
                Math.max(8, Runtime.getRuntime().availableProcessors() * 2),
                threadFactory("manniuInPoolThread-", false)
            ).also { threadPoolInstance = it } else threadPoolInstance!!

        fun threadFactory(name: String, daemon: Boolean): ThreadFactory {
            return ThreadFactory { runnable: Runnable ->
                val result = Thread(
                    {
                        try {
                            runnable.run()
                        } catch (e: Exception) {
//                    ToastUtil.showCenterOnDebug("线程池中的某个线程发生了问题，请查看控制台或者日志文件！。");
                            DefaultExceptionHandler.getInstance()
                                .uncaughtException(Thread.currentThread(), e)
                        }
                    }, name + System.currentTimeMillis()
                )
                result.isDaemon = daemon
                result
            }
        }

        /**
         * 让线程池中的子线程执行异步任务
         * 在任务数超过最大值，或者线程池Shutdown时将抛出异常
         *
         * @param runnable Runnable
         */
        fun submit(runnable: Runnable): Future<*> {
            return schedule(runnable, 0, TimeUnit.NANOSECONDS)
        }

        /**
         * 延迟让线程池中的子线程执行异步任务
         * 在任务数超过最大值，或者线程池Shutdown时将抛出异常
         * 参数列表详见
         * [ScheduledThreadPoolExecutor.schedule]
         */
        fun schedule(runnable: Runnable, delay: Long, unit: TimeUnit?): Future<*> {
            //正常情况是不会走到if里面的！！！除非有for循环在批量创建子线程，但是这个是大概率会把cpu卡住的。
            if (threadPool.queue.size == threadPool.maximumPoolSize || threadPool.isShutdown) {
//            ToastUtil.showCenterOnDebug("线程池爆满警告，请查看是否开启了过多的耗时线程!");
                DefaultExceptionHandler.getInstance().uncaughtException(
                    Thread.currentThread(), RuntimeException("线程池爆满警告，请查看是否开启了过多的耗时线程!")
                )
                //重置一下线程池，并且抛弃之前的线程池的引用（TODO 优化此处的逻辑，要在App退出时销毁线程池的！）
                threadPoolInstance = null
            }
            return threadPool.schedule(runnable, delay, unit)
        }

        /**
         * 延迟让线程池中的子线程执行异步任务
         * 在任务数超过最大值，或者线程池Shutdown时将抛出异常
         * 参数列表详见
         * [ScheduledThreadPoolExecutor.schedule]
         */
        fun scheduleAtFixedRate(
            runnable: Runnable,
            initialDelay: Long,
            period: Long,
            unit: TimeUnit?
        ): Future<*> {
            //正常情况是不会走到if里面的！！！除非有for循环在批量创建子线程，但是这个是大概率会把cpu卡住的。
            if (threadPool.queue.size == threadPool.maximumPoolSize || threadPool.isShutdown) {
//            ToastUtil.showCenterOnDebug("线程池爆满警告，请查看是否开启了过多的耗时线程!");
                DefaultExceptionHandler.getInstance().uncaughtException(
                    Thread.currentThread(), RuntimeException("线程池爆满警告，请查看是否开启了过多的耗时线程!")
                )
                //重置一下线程池，并且抛弃之前的线程池的引用（TODO 优化此处的逻辑，要在App退出时销毁线程池的！）
                threadPoolInstance = null
            }
            return threadPool.scheduleAtFixedRate(runnable, initialDelay, period, unit)
        }

        /**
         * 在主线程延迟执行。
         *
         * @param runnable Runnable
         * @param delayed  时长 Millis
         */
        fun runOnUiThread(runnable: Runnable, delayed: Long) {
            mMainLooperHandler.postDelayed(runnable, delayed)
        }

        fun isUIThread(thread: Thread?): Boolean {
            return mMainLooperHandler.looper.thread === thread
        }

        /**
         * 在主线程执行。
         *
         * @param runnable Runnable
         */
        fun runOnUiThread(runnable: Runnable) {
            //如果已经是UI线程了，直接运行！
            if (isUIThread(Thread.currentThread())) {
                runnable.run()
            } else {
                mMainLooperHandler.post(runnable)
            }
        }

        /**
         * 在主线程并立刻执行。
         *
         * @param runnable Runnable
         */
        fun runOnUiThreadImediatly(runnable: Runnable) {
            //如果已经是UI线程了，直接运行！
            if (isUIThread(Thread.currentThread())) {
                runnable.run()
            } else {
                mMainLooperHandler.postAtFrontOfQueue(runnable)
            }
        }
    }
}