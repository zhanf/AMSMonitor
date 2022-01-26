package com.shyii.ams_lib

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * @Author zhanfeng
 * @Date 1/25/22 10:34 下午
 * @Desc
 */
internal class AMSContentProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        val application = context!!.applicationContext as Application
        AMSMonitor.init(application)
        return true
    }

    override fun query(
        uri: Uri,
        projectionArg: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?
    ): Int = 0
}