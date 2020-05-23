package me.singleneuron.locknotification.Fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import me.singleneuron.locknotification.R
import me.singleneuron.locknotification.Utils.ConfigUtil
import java.io.File


class ConfigsFragment : PreferenceFragmentCompat() {

    companion object{
        public var jsonObject = JsonObject()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.configs)
    }

    override fun onResume() {
        super.onResume()
        addPreference()
    }

    private fun addPreference() {
        val preferenceCategory = findPreference<PreferenceCategory>("config")
        preferenceCategory!!.removeAll()
        val jsonFile = File(requireContext().filesDir.absolutePath + File.separator + "config.json")
        if (!jsonFile.exists()) return
        jsonObject = JsonParser.parseString(jsonFile.readText()).asJsonObject
        jsonObject.keySet().forEach {key ->
            val preference = Preference(requireContext())
            val jsonObject2 = jsonObject.getAsJsonObject(key)
            try {
                preference.title = jsonObject2.get("configName").asString
            } catch (e:Exception) {
                e.printStackTrace()
            }
            preference.summary = jsonObject2.toString()
            preference.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setItems(arrayOf(getString(R.string.edit),getString(R.string.delete),getString(R.string.copy))) { _, which ->
                        when(which) {
                            0 -> {
                                requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out).replace(R.id.content_frame, AddConfigFragment.newInstance(Gson().fromJson(jsonObject2,ConfigUtil::class.java))).addToBackStack(AddConfigFragment::class.java.simpleName).commit()
                            }
                            1 -> {
                                val snackBar = Snackbar.make(requireView().rootView,R.string.ask_delete,Snackbar.LENGTH_LONG)
                                    .setAction(R.string.OK) {
                                        jsonObject.remove(key)
                                        jsonFile.writeText(jsonObject.toString())
                                        addPreference()
                                    }
                                val snackBarView = snackBar.view
                                snackBarView.fitsSystemWindows = false
                                ViewCompat.setOnApplyWindowInsetsListener(snackBarView, null)
                                snackBar.show()
                            }
                            2 -> {
                                val clipboardManager : ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val string = jsonObject2.toString()
                                val clipData = ClipData.newPlainText("LockNotification",string)
                                clipboardManager.setPrimaryClip(clipData)
                                Toast.makeText(requireContext(),R.string.already_copy_to_clipbroad,Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .show()
                true
            }
            preferenceCategory.addPreference(preference)
        }
    }

}