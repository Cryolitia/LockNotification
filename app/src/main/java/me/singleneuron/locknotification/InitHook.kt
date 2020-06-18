package me.singleneuron.locknotification

import android.annotation.SuppressLint
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
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

@Keep
class InitHook : IXposedHookLoadPackage {

    companion object{
        private var tempMap : HashMap<String,ConfigUtil> = HashMap()
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
            @SuppressLint("SdCardPath")
            override fun afterHookedMethod(param: MethodHookParam?) {
                try {
                    val context = GeneralUtils.getContext()
                    if (tempMap.isNullOrEmpty()) {
                        val jsonString = ContentProviderPreference(
                            ContentProvider.CONTENT_PROVIDER_JSON,
                            null,
                            context
                        ).originalJsonString
                        if (jsonString.isNullOrBlank()) return
                        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
                        if (jsonObject.keySet().isNullOrEmpty()) return
                        tempMap = HashMap<String,ConfigUtil>((jsonObject.size().toFloat()/0.75).toInt()+1)
                        jsonObject.keySet().forEach {
                            try {
                                val configUtil: ConfigUtil = Gson().fromJson(
                                    jsonObject!!.getAsJsonObject(it),
                                    ConfigUtil::class.java
                                )
                                tempMap[it] = configUtil
                            }catch (e:Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    if (tempMap.isNullOrEmpty()) return
                    val notification = param!!.result as Notification
                    tempMap.keys.forEach { key ->
                        try {
                            if (tempMap[key] == null) return@forEach
                            val configUtil: ConfigUtil = tempMap[key]!!
                            if (!configUtil.configHit(
                                    notification.extras, lpparam.packageName,
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) notification.channelId
                                    else null
                                )
                            ) return@forEach
                            if (configUtil.lockNotification) {
                                notification.flags =
                                    notification.flags or NotificationCompat.FLAG_NO_CLEAR or NotificationCompat.FLAG_ONGOING_EVENT
                            }
                            if (configUtil.throwInAnotherChannel) {
                                @Suppress("DEPRECATION")
                                notification.priority = configUtil.property
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val channel = NotificationChannel(
                                        configUtil.channelKey,
                                        configUtil.channelTitle,
                                        configUtil.importance
                                    )
                                    (context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                                        channel
                                    )
                                    XposedHelpers.setObjectField(
                                        notification,
                                        "mChannelId",
                                        configUtil.channelKey
                                    )
                                }
                            }
                            if (configUtil.getVisibility() != NotificationCompat.VISIBILITY_PUBLIC) {
                                notification.visibility = configUtil.getVisibility()
                            }
                            if (configUtil.replace)
                                configUtil.replace(notification)
                            notification.extras.putString("LOCK_NOTIFICATION_APPLY", key)
                            param.result = notification
                            LogUtils.addLogByContentProvider(key, lpparam.packageName, context)
                            return
                        } catch (e:Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e:Exception) {
                    if (BuildConfig.DEBUG) {
                        val stringWriter = StringWriter()
                        val printWriter = PrintWriter(stringWriter)
                        e.printStackTrace(printWriter)
                        File("/sdcard/locknotification_log.txt").appendText(stringWriter.toString())
                    }
                }
            }
        })
    }
}