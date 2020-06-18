package me.singleneuron.locknotification.Fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.azhon.suspensionfab.FabAttributes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import me.singleneuron.locknotification.R
import me.singleneuron.locknotification.Utils.GeneralUtils
import me.singleneuron.locknotification.Utils.LogUtils.Companion.cleanLog
import me.singleneuron.locknotification.databinding.LogFragmentBinding
import java.io.File

class LogFragment : Fragment() {

    private lateinit var binding : LogFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = LogFragmentBinding.inflate(inflater)
        //val view = inflater.inflate(R.layout.log_fragment, container, false)

        val sharedPreferences = GeneralUtils.getSharedPreferenceOnUI(context)

        val logFile = File(requireActivity().filesDir.absolutePath + File.separator + "log.txt")
        if (!logFile.exists()) logFile.createNewFile()

        val textView = binding.textView
        textView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.textViewBackground))

        val colorPrimary = ContextCompat.getColor(requireActivity(), R.color.colorPrimary)
        val colorPrimaryVariant = ContextCompat.getColor(requireActivity(), R.color.colorPrimaryVariant)

        val suspensionFab = binding.fabTop
        val defaultFab = suspensionFab.findViewWithTag<FloatingActionButton>(0)
        defaultFab.backgroundTintList = ColorStateList.valueOf(colorPrimary)

        val deleteFAB = FabAttributes.Builder()
                .setBackgroundTint(colorPrimary)
                .setSrc(ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_black_24dp))
                .setFabSize(FloatingActionButton.SIZE_NORMAL)
                .setPressedTranslationZ(10)
                .setTag(1)
                .build()
        val refreshFAB = FabAttributes.Builder()
                .setBackgroundTint(colorPrimary)
                .setSrc(ContextCompat.getDrawable(requireContext(), R.drawable.ic_refresh_black_24dp))
                .setFabSize(FloatingActionButton.SIZE_NORMAL)
                .setPressedTranslationZ(10)
                .setTag(2)
                .build()
        val settingFAB = FabAttributes.Builder()
                .setBackgroundTint(colorPrimary)
                .setSrc(ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings_black_24dp))
                .setFabSize(FloatingActionButton.SIZE_NORMAL)
                .setPressedTranslationZ(10)
                .setTag(3)
                .build()
        suspensionFab.addFab(settingFAB, deleteFAB, refreshFAB)
        suspensionFab.setFabClickListener { _, tag ->
            when (tag) {
                1 -> {
                    val snackBar = Snackbar.make(binding.logContent, R.string.clean_all_log, Snackbar.LENGTH_LONG)
                            .setAction(getText(R.string.OK)) {
                                logFile.writeText("")
                                textView.text = ""
                            }
                    val snackBarView = snackBar.view
                    snackBarView.fitsSystemWindows = false
                    ViewCompat.setOnApplyWindowInsetsListener(snackBarView, null)
                    snackBar.show()
                }
                2 -> {
                    textView.text = logFile.readText()
                }
                3 -> {
                    val editText = EditText(context)
                    editText.inputType = InputType.TYPE_CLASS_NUMBER
                    editText.setText(sharedPreferences.getInt("logMaxLine", 1000).toString())
                    MaterialAlertDialogBuilder(requireContext())
                            .setTitle(R.string.max_remain)
                            .setView(editText)
                            .setPositiveButton(R.string.OK) { _, _ ->
                                val maxLine: Int = Integer.parseInt(editText.text.toString())
                                sharedPreferences.edit().putInt("logMaxLine", maxLine).apply()
                                cleanLog(maxLine, requireContext())
                                var logList = logFile.readLines()
                                logList = logList.reversed()
                                textView.text = logList.joinToString(separator = "\n")
                            }
                            .setNegativeButton(R.string.cancel, null)
                            .create()
                            .show()
                }
            }
        }
        for (i in 0..3) {
            suspensionFab.findViewWithTag<FloatingActionButton>(i).apply {
                setColorFilter(Color.WHITE)
                rippleColor = colorPrimaryVariant
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val logFile = File(requireActivity().filesDir.absolutePath + File.separator + "log.txt")
        val textView = binding.textView
        var logList = logFile.readLines()
        logList = logList.reversed()
        textView.text = logList.joinToString(separator = "\n")
    }

}
