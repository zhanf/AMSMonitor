package com.shyii.ams_lib

import android.app.Application

/**
 * @Author zhanfeng
 * @Date 2/8/22 2:04 下午
 * @Desc
 */
object AMSConfig {

    private const val MAX_BUNDLE_SIZE = 512 * 1024
    private const val BUNDLE_SIZE_THRESHOLD_PERCENT = 50
    internal var bundleSizeThreshold = (BUNDLE_SIZE_THRESHOLD_PERCENT * MAX_BUNDLE_SIZE) / 100
        private set

    fun initConfig(app: Application) {
        initBundleSizeThreshold(app)
    }

    private fun initBundleSizeThreshold(app: Application) {
        var bundleThresholdPercent =
            app.resources.getInteger(R.integer.bundle_size_threshold_percent)
        if (bundleThresholdPercent < 0 || bundleThresholdPercent > 100) {
            bundleThresholdPercent = BUNDLE_SIZE_THRESHOLD_PERCENT
        }
        bundleSizeThreshold = (bundleThresholdPercent * MAX_BUNDLE_SIZE) / 100
    }
}