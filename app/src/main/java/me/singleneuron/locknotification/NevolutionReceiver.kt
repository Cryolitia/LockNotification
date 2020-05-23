package me.singleneuron.locknotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK

class NevolutionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context!!.startActivity(Intent().setClassName(context, MainActivity::class.java.name).addFlags(FLAG_ACTIVITY_NEW_TASK))
    }
}