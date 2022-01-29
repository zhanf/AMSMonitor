package com.shyii.ams_core.amshook

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import com.shyii.ams_core.ActivityManagerServiceHook
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy

/**
 * @Author zhanfeng
 * @Date 1/29/22 2:55 下午
 * @Desc
 */
@TargetApi(Build.VERSION_CODES.Q)
class IActivityTaskManagerHooker {

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                // 1.获取ActivityTaskManager的Class对象
                // package android.app;
                // public class ActivityTaskManager
                val activityTaskManagerClazz = Class.forName("android.app.ActivityTaskManager")

                // 2.获取ActivityTaskManager的私有静态成员变量IActivityTaskManagerSingleton
                // private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton
                // 3.取消Java的权限检查
                val iActivityTaskManagerSingletonField =
                    activityTaskManagerClazz.getDeclaredField("IActivityTaskManagerSingleton")
                        .also { it.isAccessible = true }

                // 4.获取IActivityManagerSingleton的实例对象
                // private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton
                // 所有静态对象的反射可以通过传null获取,如果是非静态必须传实例
                val iActivityTaskManagerSingletonObj =
                    iActivityTaskManagerSingletonField.get(null)

                // 5.
                handleIActivityTaskManager(
                    iActivityTaskManagerSingletonObj
                )
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
     * just for apiLevel >= 29
     */
    @SuppressLint("DiscouragedPrivateApi")
    private fun handleIActivityTaskManager(
        IActivityTaskManagerSingletonObj: Any?
    ) {
        try {
            // 5.获取private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton对象中的属性private T mInstance的值
            // 既,为了获取一个IActivityTaskManager的实例对象
            // private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton = new Singleton<IActivityTaskManager>() {...}

            // 6.获取Singleton类对象
            // package android.util
            // public abstract class Singleton<T>
            val singletonClazz = Class.forName("android.util.Singleton")

            // 7.获取mInstance属性
            // private T mInstance;
            // 8.取消Java的权限检查
            val mInstanceField =
                singletonClazz.getDeclaredField("mInstance").also { it.isAccessible = true }

            // 9.获取mInstance属性的值,既IActivityTaskManager的实例
            // 从private static final Singleton<IActivityTaskManager> IActivityTaskManagerSingleton实例对象中获取mInstance属性对应的值,既IActivityTaskManager
            var iActivityTaskManager = mInstanceField[IActivityTaskManagerSingletonObj]

            // 10.android10之后,从mInstanceField中取到的值为null,这里判断如果为null,就再次从get方法中再取一次
            if (iActivityTaskManager == null) {
                val getMethod =
                    singletonClazz.getDeclaredMethod("get").also { it.isAccessible = true }
                iActivityTaskManager = getMethod.invoke(IActivityTaskManagerSingletonObj)
            }

            // 11.获取IActivityTaskManager接口的类对象
            // package android.app
            // public interface IActivityTaskManager
            val iActivityTaskManagerClazz = Class.forName("android.app.IActivityTaskManager")

            // 12.创建一个IActivityTaskManager接口的代理对象
            val iActivityTaskManagerProxy = Proxy.newProxyInstance(
                Thread.currentThread().contextClassLoader, arrayOf(iActivityTaskManagerClazz),
                ActivityManagerServiceHook.IActivityInvocationHandler(iActivityTaskManager)
            )

            // 13.重新赋值
            // 给mInstance属性,赋新值
            // 给Singleton<IActivityManager> IActivityManagerSingleton实例对象的属性private T mInstance赋新值
            mInstanceField[IActivityTaskManagerSingletonObj] = iActivityTaskManagerProxy
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}