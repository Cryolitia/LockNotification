package me.singleneuron.locknotification.Fragment

import android.app.Notification
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import androidx.preference.*
import me.singleneuron.locknotification.R
import org.json.JSONObject

@Keep
class NotificationDetailFragment private constructor() : PreferenceFragmentCompat() {

    companion object {
        fun newInstance(extras:Bundle): NotificationDetailFragment {
            val notificationDetailFragment = NotificationDetailFragment()
            notificationDetailFragment.arguments = extras
            return notificationDetailFragment
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.notification_detail)
        if (arguments == null) throw IllegalAccessException("Arguments should not be null,please use newInstance to get a DetailFragment object and set the param as the packageName")
        val detailPreference : PreferenceScreen = findPreference("notificationDetailFragment")!!
        addPreferenceFromBundle(requireArguments(),detailPreference)
    }

    private fun addPreferenceFromBundle(bundle:Bundle, preferenceGroup: PreferenceGroup) {
        try {
            for (key in bundle.keySet()) {
                if (bundle.get(key) is Bundle) {
                    val preferenceCategory = PreferenceCategory(requireContext())
                    preferenceCategory.title = key
                    if (!(bundle.get(key) as Bundle).isEmpty) addPreferenceFromBundle(bundle.get(key) as Bundle, preferenceCategory)
                    preferenceGroup.addPreference(preferenceCategory)
                    continue
                }
                val preference = Preference(requireContext())
                preference.title = key
                try {
                    preference.summary = bundle.get(key).toString()
                } catch (e:Exception) {
                    e.printStackTrace()
                    preference.summary = e.toString()
                }
                try {
                    if (key == NotificationCompat.EXTRA_LARGE_ICON_BIG)
                        preference.icon = (bundle.get(key) as Icon).loadDrawable(requireContext())
                } catch (e:Exception) {
                    e.printStackTrace()
                }
                try {
                    if (key == NotificationCompat.EXTRA_LARGE_ICON)
                        preference.icon = (bundle.get(key) as Icon).loadDrawable(requireContext())
                } catch (e:Exception) {
                    e.printStackTrace()
                }
                try {
                    if (key == NotificationCompat.EXTRA_PICTURE)
                        preference.icon = BitmapDrawable(resources, bundle.get(key) as Bitmap)
                } catch (e:Exception) {
                    e.printStackTrace()
                }
                preferenceGroup.addPreference(preference)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}