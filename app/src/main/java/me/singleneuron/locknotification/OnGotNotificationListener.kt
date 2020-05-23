package me.singleneuron.locknotification

import android.service.notification.StatusBarNotification

interface OnGotNotificationListener {
    public fun onGotNotification(array: Array<StatusBarNotification?>)
}