package me.singleneuron.locknotification.Fragment

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.oasisfeng.nevo.sdk.NevoDecoratorService
import me.singleneuron.locknotification.*
import me.singleneuron.locknotification.Utils.GeneralUtils
import me.singleneuron.locknotification.Utils.HookStatue
import me.singleneuron.locknotification.Utils.LogUtils
import me.singleneuron.locknotification.databinding.SettingFragmentBinding


class SettingFragment : Fragment(), OnGotNotificationListener {

    private lateinit var binding : SettingFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = SettingFragmentBinding.inflate(inflater)
        childFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out).replace(R.id.content_frame3, SettingsFragment()).addToBackStack(SettingsFragment::class.java.simpleName).commit()

        val cardView1 = binding.customCardView1
        val statue = HookStatue.getStatue(activity)
        cardView1.title = getString(HookStatue.getStatueName(statue))
        cardView1.enable = HookStatue.isActive(statue)

        if (statue.name.contains("taichi", true)) {
            if (HookStatue.isActive(statue)) {
                cardView1.setOnClickListener {
                    requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out).replace(R.id.content_frame, LogFragment(), "logFragment").addToBackStack(LogFragment::class.java.simpleName).commit()
                }
                cardView1.apply {
                    setOnClickListener {
                        //activity!!.toast("跳转到太极")
                        val t = Intent("me.weishu.exp.ACTION_MODULE_MANAGE")
                        t.data = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                        t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        try {
                            startActivity(t)
                        } catch (e: ActivityNotFoundException) {
                            //ignore
                        }
                    }
                    cardView.isClickable = true
                }
            } else {
                cardView1.setOnClickListener {
                    //activity!!.toast("跳转到太极")
                    val t = Intent("me.weishu.exp.ACTION_MODULE_MANAGE")
                    t.data = Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                    t.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        startActivity(t)
                    } catch (e: ActivityNotFoundException) {
                        //ignore
                    }
                }
            }
        } else {
            cardView1.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out).replace(R.id.content_frame, LogFragment(), "logFragment").addToBackStack(LogFragment::class.java.simpleName).commit()
            }
        }

        val cardView3 = binding.customCardView3
        var nevoInstalled = false
        var nevoActivated = false
        try {
            requireContext().packageManager.getApplicationInfo("com.oasisfeng.nevo",0)
            nevoInstalled = true
            nevoActivated = isDecoratorRunning()
        } catch (e:Exception) {
            //ignored
        }
        if (nevoInstalled) {
            if (nevoActivated) {
                cardView3.apply {
                    title = context.getString(R.string.nevolution_activated)
                    enable = true
                    setOnClickListener {
                        requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out).replace(R.id.content_frame, LogFragment(), "logFragment").addToBackStack(LogFragment::class.java.simpleName).commit()
                    }
                }
            } else {
                cardView3.apply {
                    title = context.getString(R.string.nevolution_not_activated)
                    enable = false
                    setOnClickListener {
                        activate()
                    }
                }
            }
        } else {
            cardView3.apply {
                title = context.getString(R.string.nevolution_not_install)
                enable = false
                setOnClickListener {
                    installNevolution()
                }
            }
        }

        if (HookStatue.isActive(statue)&&(!nevoActivated)) cardView3.visibility = View.GONE
        if ((!HookStatue.isActive(statue))&&nevoActivated) cardView1.visibility = View.GONE

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        LogUtils.cleanLog(
            GeneralUtils.getSharedPreferenceOnUI(context).getInt("logMaxLine", 1000),
            requireActivity()
        )
        val string = String.format(getString(R.string.already_apply_count),LogUtils.getLineCount(requireActivity()))
        binding.customCardView1.summary = string
        binding.customCardView3.summary = string

        if (!MainActivity.notificationsArray.isNullOrEmpty()) {
            binding.customCardView2.summary = String.format(getString(R.string.now_showing_count),MainActivity.notificationsArray!!.size)
        }

        val cardView2 = binding.customCardView2
        val isGranted: Boolean = isEnabled()
        cardView2.title = if (isGranted) getString(R.string.notification_allowed) else getString(R.string.notification_not_allowed)
        cardView2.enable = isGranted
        if (isGranted) {
            cardView2.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out).replace(R.id.content_frame, ShowingNotificationFragment(), "showingNotificationFragment").addToBackStack(ShowingNotificationFragment::class.java.simpleName).commit()
            }
        } else {
            cardView2.setOnClickListener {
                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                startActivity(intent)
            }
        }

        MainActivity.isSettingFragmentBind = true
        MainActivity.settingFragmentListener = this

    }

    private fun isEnabled(): Boolean {
        val pkgName: String = requireContext().packageName
        val flat: String =
            Settings.Secure.getString(requireContext().contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun onPause() {
        super.onPause()
        MainActivity.isSettingFragmentBind = false
        MainActivity.settingFragmentListener = null
    }

    override fun onGotNotification(array: Array<StatusBarNotification?>) {
        if (context==null) return
        binding.customCardView2.summary = String.format(getString(R.string.now_showing_count),array.size)
    }

    private fun isDecoratorRunning(): Boolean {
        val service: Intent = Intent(requireContext(), NevolutionService::class.java).setAction(NevoDecoratorService.ACTION_DECORATOR_SERVICE)
        return mDummyReceiver.peekService(requireContext(), service) != null
    }

    private val mDummyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, i: Intent?) {}
    }

    private fun installNevolution() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.oasisfeng.nevo")))
        } catch (ignored: ActivityNotFoundException) { }
    }

    private fun activate() {
        try {
            startActivity(Intent("com.oasisfeng.nevo.action.ACTIVATE_DECORATOR").setPackage("com.oasisfeng.nevo").putExtra("nevo.decorator", ComponentName(requireContext(), NevolutionService::class.java)))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage("com.oasisfeng.nevo"))
        }
    }


}