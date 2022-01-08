package me.singleneuron.locknotification.Fragment

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import me.singleneuron.locknotification.R
import me.singleneuron.locknotification.Utils.ConfigUtil
import me.singleneuron.locknotification.Utils.GeneralUtils
import java.lang.reflect.Field
import java.util.regex.Pattern

class AddConfigDetailFragment : PreferenceFragmentCompat() {

    companion object {
        public var configUtil = ConfigUtil()
        private val preferenceField: Array<Field> = ConfigUtil::class.java.fields
        public val preferenceKey = Array(preferenceField.size) {
            preferenceField[it].name
        }

        public fun newInstance(mConfigUtil: ConfigUtil) : AddConfigDetailFragment {
            val addConfigDetailFragment = AddConfigDetailFragment()
            configUtil = mConfigUtil
            return addConfigDetailFragment
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val editor = GeneralUtils.getSharedPreferenceOnUI(requireContext()).edit()
        preferenceKey.forEach {
            @Suppress("UNCHECKED_CAST")
            when (val value : Any? = ConfigUtil::class.java.getDeclaredField(it).get(configUtil)) {
                is Boolean -> editor.putBoolean(it,value)
                is Set<*>? -> if (value==null) editor.putStringSet(it, null) else editor.putStringSet(it, value as MutableSet<String>?)
                is String? -> if (value==null) editor.putString(it,"") else editor.putString(it,value)
            }
        }
        editor.commit()
        addPreferencesFromResource(R.xml.add_config)
        init()
        findPreference<Preference>("import")!!.setOnPreferenceClickListener {
            val editText = EditText(requireContext())
            MaterialAlertDialogBuilder(requireContext())
                .setView(editText)
                .setPositiveButton(R.string.OK) { _, _ ->
                    try {
                        configUtil = ConfigUtil.fromJson(editText.text.toString())
                        init()
                    } catch (e:Exception) {
                        Toast.makeText(requireContext(),e.toString(),Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton(R.string.cancel,null)
                .create()
                .show()
            true
        }
    }

    private fun init () {
        preferenceKey.forEach {
            val preference = findPreference<Preference>(it)
            val value = ConfigUtil::class.java.getDeclaredField(it).get(configUtil)
            if (value!=null) preference!!.summary = value.toString()
            preference!!.setOnPreferenceChangeListener { preference1, newValue ->
                if (it.contains("Key",true)) {
                    if (!Pattern.matches("^[a-z0-9A-Z_]+$",newValue as String)) {
                        Toast.makeText(requireContext(),preference1.title.toString() + getString(R.string.only_letters_and_numbers),Toast.LENGTH_LONG).show()
                        return@setOnPreferenceChangeListener false
                    }
                }
                preference1.summary = newValue.toString()
                ConfigUtil::class.java.getDeclaredField(it).set(configUtil,newValue)
                true
            }
        }
    }

}
