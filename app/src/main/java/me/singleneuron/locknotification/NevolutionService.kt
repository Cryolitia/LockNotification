package me.singleneuron.locknotification

import android.app.NotificationChannel
import android.os.Build
import android.os.Process
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.oasisfeng.nevo.sdk.MutableStatusBarNotification
import com.oasisfeng.nevo.sdk.NevoDecoratorService
import me.singleneuron.locknotification.Utils.ConfigUtil
import me.singleneuron.locknotification.Utils.LogUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NevolutionService : NevoDecoratorService() {

    override fun apply(evolving: MutableStatusBarNotification?): Boolean {
        val notification = evolving!!.notification
        val configJsonString = File(filesDir.absolutePath + File.separator + "config.json").readText()
        if (configJsonString.isBlank()) return false
        val jsonObject = JsonParser.parseString(configJsonString).asJsonObject
        jsonObject.keySet().forEach {key ->
            val configUtil : ConfigUtil = Gson().fromJson(jsonObject.getAsJsonObject(key), ConfigUtil::class.java)
            if (!configUtil.configHit(notification.extras, evolving.packageName,
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
                    createNotificationChannels(evolving.packageName,Process.myUserHandle(), ArrayList<NotificationChannel>().apply {
                        add(channel)
                    })
                    notification.channelId = configUtil.channelKey
                }
            }
            if (configUtil.getVisibility()!=NotificationCompat.VISIBILITY_PUBLIC) {
                notification.visibility = configUtil.getVisibility()
            }
            notification.extras.putString("LOCK_NOTIFICATION_APPLY",key)
            val string: String = SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ", Locale.getDefault()).format(Date()) + key + " apply to " + packageName + "  \n"
            LogUtils.addLog(string, this)
            return true
        }
        return false
    }

}