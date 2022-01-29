package com.shyii.ams_core.amshook

import android.annotation.SuppressLint
import android.os.Build
import com.shyii.ams_core.ActivityManagerServiceHook
import java.lang.reflect.Proxy

/**
 * @Author zhanfeng
 * @Date 1/29/22 3:27 下午
 * @Desc
 */
class ActivityManagerNativeHooker {

    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            try {
                // 1.获取ActivityManagerNative的Class对象
                // package android.app
                // public abstract class ActivityManagerNative
                val activityManagerNativeClazz =
                    Class.forName("android.app.ActivityManagerNative")

                // 2.获取 ActivityManagerNative的 私有属性gDefault
                // private static final Singleton<IActivityManager> gDefault
                // 3.对私有属性gDefault,解除私有限定
                val singletonField = activityManagerNativeClazz.getDeclaredField("gDefault")
                    .also { it.isAccessible = true }

                // 4.获得gDefaultField中对应的属性值(被static修饰了),既得到Singleton<IActivityManager>对象的实例
                // 所有静态对象的反射可以通过传null获取
                // private static final Singleton<IActivityManager> gDefault
                handleIActivityManager(singletonField[null])
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     *  for 15<= apiLevel <= 28
     */
    @SuppressLint("DiscouragedPrivateApi")
    private fun handleIActivityManager(
        iActivityManagerSingletonObj: Any?
    ) {
        try {
            // 5.获取private static final Singleton<IActivityManager> IActivityManagerSingleton对象中的属性private T mInstance的值
            // 既,为了获取一个IActivityManager的实例对象
            // private static final Singleton<IActivityManager> IActivityManagerSingleton = new Singleton<IActivityManager>(){...}

            // 6.获取Singleton类对象
            // package android.util
            // public abstract class Singleton<T>
            val singletonClazz = Class.forName("android.util.Singleton")

            // 7.获取mInstance属性
            // private T mInstance;
            // 8.取消Java的权限检查
            val mInstanceField =
                singletonClazz.getDeclaredField("mInstance").also { it.isAccessible = true }

            // 9.获取mInstance属性的值,既IActivityManager的实例
            // 从private static final Singleton<IActivityManager> IActivityManagerSingleton实例对象中获取mInstance属性对应的值,既IActivityManager
            val iActivityManager = mInstanceField[iActivityManagerSingletonObj]

            // 10.获取IActivityManager接口的类对象
            // package android.app
            // public interface IActivityManager
            val iActivityManagerClazz = Class.forName("android.app.IActivityManager")

            // 11.创建一个IActivityManager接口的代理对象
            val iActivityManagerProxy = Proxy.newProxyInstance(
                Thread.currentThread().contextClassLoader, arrayOf(iActivityManagerClazz),
                ActivityManagerServiceHook.IActivityInvocationHandler(iActivityManager)
            )

            // 12.重新赋值
            // 给mInstance属性,赋新值
            // 给Singleton<IActivityManager> IActivityManagerSingleton实例对象的属性private T mInstance赋新值
            mInstanceField[iActivityManagerSingletonObj] = iActivityManagerProxy
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}