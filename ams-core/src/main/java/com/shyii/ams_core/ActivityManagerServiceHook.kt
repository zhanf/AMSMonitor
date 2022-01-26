package com.shyii.ams_core

import android.annotation.SuppressLint
import android.os.Build
import java.lang.reflect.InvocationHandler
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

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
        try {
            val apiLevel = Build.VERSION.SDK_INT
            when {
                apiLevel >= Build.VERSION_CODES.Q -> {
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
                }
                apiLevel >= Build.VERSION_CODES.O -> {
                    // 1.获取ActivityManager的Class对象
                    // package android.app
                    // public class ActivityManager
                    val activityManagerClazz = Class.forName("android.app.ActivityManager")

                    // 2.获取ActivityManager的私有静态属性IActivityManagerSingleton
                    // private static final Singleton<IActivityManager> IActivityManagerSingleton
                    // 3.取消Java的权限检查
                    val iActivityManagerSingletonField =
                        activityManagerClazz.getDeclaredField("IActivityManagerSingleton")
                            .also { it.isAccessible = true }

                    // 4.获取IActivityManagerSingleton的实例对象
                    // private static final Singleton<IActivityManager> IActivityManagerSingleton
                    // 所有静态对象的反射可以通过传null获取,如果是非静态必须传实例
                    handleIActivityManager(
                        iActivityManagerSingletonField[null]
                    )
                }
                else -> {
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
                }
            }
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
                IActivityInvocationHandler(iActivityTaskManager)
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
                IActivityInvocationHandler(iActivityManager)
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

    /**
     * 对IActivityManager/IActivityTaskManager接口进行动态代理
     */
    private class IActivityInvocationHandler(
        private val mIActivityManager: Any?
    ) : InvocationHandler {
        @Throws(InvocationTargetException::class, IllegalAccessException::class)
        override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
            block.invoke(proxy, method, args)
            /*if (method.name == "startActivity" && args != null && args.isNotEmpty()) {
                for (index in args.indices) {
                    if (args[index] is Intent) {
                        val size = (args[index] as Intent).extras?.sizeAsParcel() ?: 0
                        Log.e(TAG, "bundle.sizeAsParcel:$size")
                        break
                    }
                }
            }*/
            // public abstract int android.app.IActivityManager.startActivity(android.app.IApplicationThread,java.lang.String,android.content.Intent,java.lang.String,android.os.IBinder,java.lang.String,int,int,android.app.ProfilerInfo,android.os.Bundle) throws android.os.RemoteException
            // public abstract int android.app.IActivityTaskManager.startActivity(whoThread, who.getBasePackageName(), intent,intent.resolveTypeIfNeeded(who.getContentResolver()),token, target != null ? target.mEmbeddedID : null,requestCode, 0, null, options);
            return method.invoke(mIActivityManager, *(args ?: arrayOfNulls<Any>(0)))
        }
    }

    var block: (proxy: Any, method: Method, args: Array<Any>?) -> Unit =
        { _: Any, _: Method, _: Array<Any>? -> }
}