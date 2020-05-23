package me.singleneuron.locknotification.Fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.singleneuron.locknotification.R
import me.singleneuron.locknotification.databinding.ConfigFragmentBinding

class ConfigFragment : Fragment() {

    private lateinit var binding : ConfigFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = ConfigFragmentBinding.inflate(inflater)
        //val view = inflater.inflate(R.layout.config_fragment,container,false)
        childFragmentManager.beginTransaction().replace(R.id.content_frame2, ConfigsFragment()).addToBackStack(ConfigsFragment::class.java.simpleName).commit()
        val floatingActionButton = binding.floatingActionButton
        floatingActionButton.setColorFilter(Color.WHITE)
        floatingActionButton.rippleColor = ContextCompat.getColor(requireContext(), R.color.colorPrimaryVariant)
        floatingActionButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out).replace(R.id.content_frame, AddConfigFragment(), "addConfigFragment").addToBackStack(AddConfigFragment::class.java.simpleName).commit()
        }
        return binding.root
    }

}