package com.shyii.ams_lib

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.shizhi.shihuoapp.library.exception.ExceptionManager
import com.shizhi.shihuoapp.library.exception.SentryException
import com.shyii.ams_core.ActivityManagerServiceHook
import java.lang.StringBuilder
import java.lang.reflect.Method

/**
 * @Author zhanfeng
 * @Date 1/25/22 10:09 下午
 * @Desc
 */
object AMSMonitor {

    fun init(app: Application) {
        AMSConfig.initConfig(app)
        ExceptionManager.init(AMSExceptionReport())
        hookStartActivity()
        registerActivityLifecycleCallbacks(app)
    }

    private fun registerActivityLifecycleCallbacks(app: Application) {
        app.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks by noOpDelegate() {
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                val size = outState.sizeAsParcel()
                if (size > AMSConfig.bundleSizeThreshold) {
                    warningTransactionTooLargeException(size)
                }
            }
        })
    }

    private fun hookStartActivity() {
        Reflection()
        ActivityManagerServiceHook.registerInvokeCallback { _: Any, method: Method, args: Array<Any>? ->
            if (method.name == "startActivity" && args != null && args.isNotEmpty()) {
                for (index in args.indices) {
                    if (args[index] is Intent) {
                        (args[index] as Intent).extras?.sizeAsParcel()?.let {
                            if (it <= AMSConfig.bundleSizeThreshold) {
                                return@let
                            }
                            warningTransactionTooLargeException(it)
                        }
                        break
                    }
                }
            }
        }
        ActivityManagerServiceHook.hookAMS()
    }

    private fun warningTransactionTooLargeException(size: Int) {
        val sb = StringBuilder()
        Thread.currentThread().stackTrace.let {
            it.copyOfRange(Math.min(it.size, 3), it.size).forEach { stackTraceElement ->
                sb.append(stackTraceElement.toString()).append("\n")
            }
        }
        ExceptionManager.reporting(
            SentryException(
                "TransactionTooLargeException",
                SentryException.Level.WARNING, mapOf(
                    "size" to size.toString(),
                    "stackTrace" to sb.toString()
                )
            )
        )
    }
}