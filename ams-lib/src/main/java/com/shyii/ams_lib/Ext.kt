package com.shyii.ams_lib

import android.os.Bundle
import android.os.Parcel
import java.lang.reflect.Proxy

/**
 * @Author zhanfeng
 * @Date 1/20/22 4:31 下午
 * @Desc
 */
fun Bundle.sizeAsParcel(): Int {
    val parcel = Parcel.obtain()
    try {
        parcel.writeBundle(this)
        return parcel.dataSize()
    } finally {
        parcel.recycle()
    }
}

internal inline fun <reified T : Any> noOpDelegate(): T {
    val javaClass = T::class.java
    return Proxy.newProxyInstance(
        javaClass.classLoader, arrayOf(javaClass)
    ) { _, _, _ -> } as T
}