package com.yaocf.support.ops.natives

import android.content.Context

/**
 * created by:   yaochunfeng
 * on:           2020/3/31 14:25
 * Des:          Used to 处理默认的异常信息
 * Email:        yaochunfeng@wondersgroup.com
 *
 */
interface NativeErrorHandler {
    /**
     * @return true，则表示已经处理过该信号了，无需执行默认处理（重启App）
     */
    fun onSysSignalRec(context: Context, signal: Int, nativeStackTrace:String, currentThread:Thread):Boolean
}
