package me.singleneuron.locknotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.JsonParser
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import me.singleneuron.locknotification.SharedPreferences.ContentProviderPreference
import me.singleneuron.locknotification.Utils.ConfigUtil
import me.singleneuron.locknotification.Utils.GeneralUtils
import me.singleneuron.locknotification.Utils.HookStatue
import me.singleneuron.locknotification.Utils.LogUtils

@Keep
class InitHook : IXposedHookLoadPackage {

    companion object{
        var configUtil : ConfigUtil? = null
    }

    @Throws(Throwable::class)
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            XposedHelpers.findAndHookMethod(HookStatue::class.java.name,lpparam.classLoader,"isEnabled",object : XC_MethodReplacement(){
                override fun replaceHookedMethod(param: MethodHookParam?): Any {
                    return true
                }
            })
            return
        }

        XposedHelpers.findAndHookMethod(Notification.Builder::class.java,"build",object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                val notification = param!!.result as Notification
                val context = GeneralUtils.getContext()
                val configJsonString = ContentProviderPreference(ContentProvider.CONTENT_PROVIDER_JSON,null,context).originalJsonString
                if (configJsonString.isNullOrBlank()) return
                val jsonObject = JsonParser.parseString(configJsonString).asJsonObject
                jsonObject.keySet().forEach {key ->
                    val configUtil : ConfigUtil = Gson().fromJson(jsonObject.getAsJsonObject(key), ConfigUtil::class.java)
                    if (!configUtil.configHit(notification.extras, lpparam.packageName,
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) notification.channelId
                                else null)
                    ) return@forEach
                    if (configUtil.lockNotification) {
                        notification.flags = notification.flags or NotificationCompat.FLAG_NO_CLEAR or NotificationCompat.FLAG_ONGOING_EVENT
                    }
                    if (configUtil.throwInAnotherChannel) {
                        @Suppress("DEPRECATION")
                        notification.priority = configUtil.property
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val channel = NotificationChannel(configUtil.channelKey,configUtil.channelTitle,configUtil.importance)
                            (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
                            XposedHelpers.setObjectField(notification,"mChannelId",configUtil.channelKey)
                        }
                    }
                    if (configUtil.getVisibility()!=NotificationCompat.VISIBILITY_PUBLIC) {
                        notification.visibility = configUtil.getVisibility()
                    }
                    notification.extras.putString("LOCK_NOTIFICATION_APPLY",key)
                    param.result = notification
                    LogUtils.addLogByContentProvider(key, lpparam.packageName, context)
                    return
                }
            }
        })
    }
}