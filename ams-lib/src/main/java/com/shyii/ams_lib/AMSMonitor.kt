package com.shyii.ams_lib

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.shyii.ams_core.ActivityManagerServiceHook
import java.lang.reflect.Method

/**
 * @Author zhanfeng
 * @Date 1/25/22 10:09 下午
 * @Desc
 */
object AMSMonitor {

    private const val TAG = "ActivityManagerHook"

    fun init(app: Application?) {
        hookStartActivity()
        app?.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks by noOpDelegate() {
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                val size = outState.sizeAsParcel()
                Log.e(TAG, "onActivitySaveInstanceState >> bundle.sizeAsParcel:$size")
            }
        })
    }

    private fun hookStartActivity() {
        Reflection()
        ActivityManagerServiceHook.registerInvokeCallback { _: Any, method: Method, args: Array<Any>? ->
            if (method.name == "startActivity" && args != null && args.isNotEmpty()) {
                for (index in args.indices) {
                    if (args[index] is Intent) {
                        val size = (args[index] as Intent).extras?.sizeAsParcel() ?: 0
                        Log.e(TAG, "startActivity >> bundle.sizeAsParcel:$size")
                        break
                    }
                }
            }
        }
        ActivityManagerServiceHook.hookAMS()
    }

}