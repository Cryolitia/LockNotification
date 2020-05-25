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

    private var tempMap : HashMap<String,ConfigUtil> = HashMap()

    override fun apply(evolving: MutableStatusBarNotification?): Boolean {
        if (tempMap.isNullOrEmpty()) {
            val jsonString = File(filesDir.absolutePath + File.separator + "config.json").readText()
            if (jsonString.isBlank()) return false
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject
            if (jsonObject.keySet().isNullOrEmpty()) return false
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
        if (tempMap.isNullOrEmpty()) return false
        val notification = evolving!!.notification
        tempMap.keys.forEach {key ->
            if (tempMap[key] == null) return@forEach
            val configUtil: ConfigUtil = tempMap[key]!!
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