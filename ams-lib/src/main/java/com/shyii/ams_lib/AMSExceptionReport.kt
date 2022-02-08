package com.shyii.ams_lib

import android.util.Log
import com.shizhi.shihuoapp.library.exception.ExceptionManager
import com.shizhi.shihuoapp.library.exception.SentryException

/**
 * @Author zhanfeng
 * @Date 2/8/22 10:39 上午
 * @Desc
 */
class AMSExceptionReport : ExceptionManager.ExceptionReport {
    override fun report(t: Throwable?) {
        if (t is SentryException) {
            Log.e("AMSExceptionReport", t.eventExtra.toString())
            return
        }
    }
}