package me.singleneuron.locknotification

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.lang.Exception

class NotificationListener : NotificationListenerService() {

    private var mOnGotNotificationListener : OnGotNotificationListener? = null
    private var isConnected = false

    override fun onBind(intent: Intent?): IBinder? {
        return if (intent!=null && intent.hasExtra("bindByActivity")) {
            //Log.d("锁定通知","BindByActivity")
            NotificationListenerBinder()
        } else {
            //Log.d("锁定通知","BindBySystem")
            super.onBind(intent)
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        //Log.d("锁定通知","OnListenerConnected")
        isConnected = true
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        //Log.d("锁定通知","OnListenerDisconnected")
        isConnected = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (!isConnected) return
        if (mOnGotNotificationListener==null) return
        //Log.d("锁定通知","OnNotificationPosted")
        try {
            mOnGotNotificationListener!!.onGotNotification(activeNotifications)
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (!isConnected) return
        if (mOnGotNotificationListener==null) return
        //Log.d("锁定通知","OnNotificationRemoved")
        try {
            mOnGotNotificationListener!!.onGotNotification(activeNotifications)
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    inner class NotificationListenerBinder : Binder() {
        public fun getService() : NotificationListener = this@NotificationListener
    }

    public fun setOnGotNotificationListener(onGotNotificationListener: OnGotNotificationListener) {
        mOnGotNotificationListener = onGotNotificationListener
        if (!isConnected) return
        if (mOnGotNotificationListener==null) return
        try {
            mOnGotNotificationListener!!.onGotNotification(activeNotifications)
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    public fun removeOnGotNotificationListener() {
        mOnGotNotificationListener = null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        super.onUnbind(intent)
        if (intent!=null && intent.hasExtra("bindByActivity")) mOnGotNotificationListener = null
        return false
    }

}
