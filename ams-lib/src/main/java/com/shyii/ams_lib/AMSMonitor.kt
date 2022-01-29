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
    private const val MAX_BUNDLE_SIZE = 512 * 1024
    private var bundleSizeThreshold = (80 * MAX_BUNDLE_SIZE) / 100

    fun init(app: Application) {
        initBundleSizeThreshold(app)
        hookStartActivity()
        app.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks by noOpDelegate() {
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                val size = outState.sizeAsParcel()
                if (size > bundleSizeThreshold) {
                    Log.e(TAG, "onActivitySaveInstanceState >> bundle.sizeAsParcel:$size")
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
                        val size = (args[index] as Intent).extras?.sizeAsParcel() ?: 0
                        if (size > bundleSizeThreshold) {
                            Log.e(TAG, "startActivity >> bundle.sizeAsParcel:$size")
                        }
                        break
                    }
                }
            }
        }
        ActivityManagerServiceHook.hookAMS()
    }

    private fun initBundleSizeThreshold(app: Application) {
        var bundleThresholdPercent =
            app.resources.getInteger(R.integer.bundle_size_threshold_percent)
        if (bundleThresholdPercent < 0 || bundleThresholdPercent > 100) {
            bundleThresholdPercent = 80
        }
        bundleSizeThreshold = (bundleThresholdPercent * MAX_BUNDLE_SIZE) / 100
    }

}