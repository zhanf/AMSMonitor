package com.shyii.ams_core

import android.os.Build
import com.shyii.ams_core.amshook.ActivityManagerHooker
import com.shyii.ams_core.amshook.ActivityManagerNativeHooker
import com.shyii.ams_core.amshook.IActivityTaskManagerHooker
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * @Author zhanfeng
 * @Date 1/25/22 1:20 下午
 * @see <a href="https://github.com/androidmalin/AndroidComponentPlugin">AndroidComponentPlugin</a>
 */
object ActivityManagerServiceHook {

    /**
     * 对IActivityManager接口中的startActivity方法进行动态代理,发生在app的进程中
     * [android.app.Activity.startActivity]
     * [android.app.Activity.startActivityForResult]
     * android.app.Instrumentation#execStartActivity()
     * Activity#startActivityForResult-->Instrumentation#execStartActivity-->ActivityManager.getService().startActivity()-->
     * IActivityManager public int startActivity(android.app.IApplicationThread caller, java.lang.String callingPackage, android.content.Intent intent, java.lang.String resolvedType, android.os.IBinder resultTo, java.lang.String resultWho, int requestCode, int flags, android.app.ProfilerInfo profilerInfo, android.os.Bundle options) throws android.os.RemoteException;
     *
     * @param context          context
     * @param subActivityClazz 在AndroidManifest.xml中注册了的Activity
     */
    fun hookAMS() {
        val apiLevel = Build.VERSION.SDK_INT
        when {
            apiLevel >= Build.VERSION_CODES.Q -> {
                IActivityTaskManagerHooker()
            }
            apiLevel >= Build.VERSION_CODES.O -> {
                ActivityManagerHooker()
            }
            else -> {
                ActivityManagerNativeHooker()
            }
        }
    }

    /**
     * 对IActivityManager/IActivityTaskManager接口进行动态代理
     */
    internal class IActivityInvocationHandler(
        private val mIActivityManager: Any?
    ) : InvocationHandler {
        @Throws(InvocationTargetException::class, IllegalAccessException::class)
        override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
            invokeCallBacks.forEach {
                it.invoke(proxy, method, args)
            }
            return method.invoke(mIActivityManager, *(args ?: arrayOfNulls<Any>(0)))
        }
    }

    private val invokeCallBacks =
        mutableListOf<(proxy: Any, method: Method, args: Array<Any>?) -> Unit>()

    fun registerInvokeCallback(block: (proxy: Any, method: Method, args: Array<Any>?) -> Unit) {
        if (!invokeCallBacks.contains(block)) {
            invokeCallBacks.add(block)
        }
    }

    fun unRegisterInvokeCallback(block: (proxy: Any, method: Method, args: Array<Any>?) -> Unit): Boolean {
        return invokeCallBacks.remove(block)
    }
}