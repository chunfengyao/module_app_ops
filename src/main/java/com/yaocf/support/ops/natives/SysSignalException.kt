package com.yaocf.support.ops.natives

import androidx.annotation.Keep

/**
 * created by:   yaochunfeng
 * on:           2020/3/31 14:25
 * Des:          Used to 处理默认的异常信息
 * Email:        yaochunfeng@wondersgroup.com
 *
 */
@Keep
class SysSignalException(signal: Int) : Exception("SysSignalException:NativeErrorManager.initWithSignal:${signal} failed!please check the log!") {

}