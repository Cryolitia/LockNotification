package me.singleneuron.locknotification.Fragment

import android.app.Notification
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.fragment.app.Fragment
import androidx.core.app.NotificationCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import me.singleneuron.locknotification.MainActivity
import me.singleneuron.locknotification.OnGotNotificationListener
import me.singleneuron.locknotification.R
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class ShowingNotificationFragment : PreferenceFragmentCompat(), OnGotNotificationListener {

    var mArray : Array<StatusBarNotification?>? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.showing_notification)
        mArray = MainActivity.notificationsArray
        updateNotifacation()
    }

    override fun onResume() {
        super.onResume()
        MainActivity.isShowingNotificationFragmentBind = true
        MainActivity.showingNotificationFragmentListener = this
        mArray = MainActivity.notificationsArray
        updateNotifacation()
    }

    override fun onPause() {
        super.onPause()
        MainActivity.isShowingNotificationFragmentBind = false
        MainActivity.showingNotificationFragmentListener = null
    }

    override fun onGotNotification(array: Array<StatusBarNotification?>) {
        mArray = array
        if (context==null) return
        updateNotifacation()
    }

    private fun updateNotifacation() {
        val showingNotificationPreference = findPreference<PreferenceCategory>("showingNotification")
        showingNotificationPreference!!.removeAll()
        if (mArray.isNullOrEmpty()) return
        mArray!!.forEach {
            if (it!=null) {
                val notification = it.notification
                var packageName = it.packageName
                var title = ""
                var message = ""
                var icon : Drawable? = null
                try {
                    title = notification.extras.get(NotificationCompat.EXTRA_TITLE).toString()
                } catch (e:Exception) {
                    e.printStackTrace()
                }
                try {
                    message = notification.extras.get(NotificationCompat.EXTRA_TEXT).toString()
                } catch (e:Exception) {
                    e.printStackTrace()
                }
                try {
                    icon = requireContext().packageManager.getApplicationIcon(packageName)
                } catch (e:Exception) {
                    e.printStackTrace()
                }
                val preference = Preference(requireContext())
                preference.title = title
                preference.summary = message
                preference.icon = icon
                preference.setOnPreferenceClickListener {
                    val bundle : Bundle = notification.extras.clone() as Bundle
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        bundle.putString(Notification.EXTRA_CHANNEL_ID, notification.channelId)
                    }
                    bundle.putString("EXTRA_FLAGS", Integer.toBinaryString(notification.flags))
                    if (!notification.actions.isNullOrEmpty()) {
                        notification.actions.forEach { it1 ->
                            bundle.putBundle("action_"+it1.title, it1.extras)
                        }
                    }
                    requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out).replace(R.id.content_frame, NotificationDetailFragment.newInstance(bundle)).addToBackStack(NotificationDetailFragment::class.java.simpleName).commit()
                    true
                }
                showingNotificationPreference.addPreference(preference)
            }
        }
    }

}
