package me.singleneuron.locknotification.Fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.preference.EditTextPreference
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonArray as JsonArray

import me.singleneuron.locknotification.R
import me.singleneuron.locknotification.Utils.ConfigUtil
import me.singleneuron.locknotification.databinding.AddConfigFragmentBinding
import java.io.File

class AddConfigFragment : Fragment() {

    companion object{
        public fun newInstance(mConfigUtil: ConfigUtil) : AddConfigFragment {
            val addConfigFragment = AddConfigFragment()
            addConfigFragment.configUtil = mConfigUtil
            return addConfigFragment
        }
    }

    var jsonObject = JsonObject()
    var configUtil = ConfigUtil()
    private lateinit var binding: AddConfigFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        //Log.d("addConfigFragment",configUtil.toString())
        val jsonFile = File(requireContext().filesDir.absolutePath + File.separator + "config.json")
        if (jsonFile.exists()) {
            jsonObject = JsonParser.parseString(jsonFile.readText()).asJsonObject
        }
        binding = AddConfigFragmentBinding.inflate(inflater)
        //val view = inflater.inflate(R.layout.add_config_fragment, container, false)
        childFragmentManager.beginTransaction().replace(R.id.content_frame3, AddConfigDetailFragment.newInstance(configUtil)).addToBackStack(AddConfigDetailFragment::class.java.simpleName).commit()
        val floatingActionButton = binding.floatingActionButton2
        floatingActionButton.setColorFilter(Color.WHITE)
        floatingActionButton.rippleColor = ContextCompat.getColor(requireContext(), R.color.colorPrimaryVariant)
        floatingActionButton.setOnClickListener {
            if (AddConfigDetailFragment.configUtil.configKey.isNullOrEmpty()) {
                Toast.makeText(requireContext(),R.string.id_should_not_empty,Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (AddConfigDetailFragment.configUtil.throwInAnotherChannel && AddConfigDetailFragment.configUtil.channelKey.isNullOrEmpty()) {
                Toast.makeText(requireContext(),R.string.channel_id_should_not_empty,Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (AddConfigDetailFragment.configUtil.throwInAnotherChannel && AddConfigDetailFragment.configUtil.channelImportance.isNullOrEmpty()) {
                Toast.makeText(requireContext(),R.string.channel_importance_should_not_empty,Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            jsonObject.add(AddConfigDetailFragment.configUtil.configKey,Gson().toJsonTree(AddConfigDetailFragment.configUtil).asJsonObject)
            jsonFile.writeText(jsonObject.toString())
            requireActivity().onBackPressed()
        }
        return binding.root
    }

}
