package me.singleneuron.locknotification

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.service.notification.StatusBarNotification
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import me.singleneuron.locknotification.Fragment.SettingFragment
import me.singleneuron.locknotification.NotificationListener.NotificationListenerBinder
import me.singleneuron.locknotification.Utils.GeneralUtils
import me.singleneuron.locknotification.databinding.MainActivityBinding

@Keep
class MainActivity : AppCompatActivity(), ServiceConnection, OnGotNotificationListener {

    var isServiceBind = false
    var notificationListener: NotificationListener? = null
    private lateinit var binding : MainActivityBinding

    companion object {
        var nowNightMode = false
        var isSettingFragmentBind = false
        var isShowingNotificationFragmentBind = false
        var settingFragmentListener : OnGotNotificationListener? = null
        var showingNotificationFragmentListener : OnGotNotificationListener? =null
        var notificationsArray : Array<StatusBarNotification?>? = null

        init {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    var mToolbar: Toolbar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View, insets: WindowInsetsCompat ->
            v.setPadding(0, 0, 0, insets.tappableElementInsets.bottom)
            insets
        }
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = GeneralUtils.getSharedPreferenceOnUI(this).getBoolean("forceNight", false)
        val nightMode = if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        @ColorInt val colorInt = ContextCompat.getColor(this, R.color.toolbarBackground)

        //https://blog.csdn.net/maosidiaoxian/article/details/51734895
        val window = window
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        //设置状态栏颜色
        window.statusBarColor = colorInt
        mToolbar = binding.toolbarPreference
        mToolbar!!.title = resources.getString(R.string.app_name)
        //mToolbar.setBackgroundColor(colorInt);
        setSupportActionBar(mToolbar)
        val docker = getWindow().decorView
        var ui = docker.systemUiVisibility
        nowNightMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES || isNightMode
        if (!nowNightMode) {
            ui = ui or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ui = ui or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        } else {
            ui = ui and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ui = ui and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            }
        }
        docker.systemUiVisibility = ui
        //https://blog.csdn.net/polo2044/article/details/81708196
        val needRecreat = delegate.localNightMode != nightMode
        if (needRecreat) {
            delegate.localNightMode = nightMode
            recreate()
            return
        }
        val fragmentManager = supportFragmentManager
        var settingFragment = fragmentManager.findFragmentByTag("settingFragment")
        if (settingFragment == null) settingFragment = SettingFragment()
        if (settingFragment.isAdded) fragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out).show(settingFragment)
        else supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out)
            .replace(R.id.content_frame, settingFragment, "settingFragment")
            .commit()
        //if (BuildConfig.VERSION_NAME.contains("canary") || BuildConfig.VERSION_NAME.contains("NIGHTLY") || BuildConfig.VERSION_NAME.contains("beta") || BuildConfig.VERSION_NAME.contains("alpha") || BuildConfig.VERSION_NAME.contains("α") || BuildConfig.VERSION_NAME.contains("β")) Toast.makeText(this, "您正在使用未经完全测试的版本，使用风险自负\n测试版本不代表最终品质", Toast.LENGTH_LONG).show()
    }

    override fun onBackPressed() {
        if (!supportFragmentManager.popBackStackImmediate()) super.onBackPressed()
    }

    public override fun onStart() {
        super.onStart()
        bindService(Intent(this, NotificationListener::class.java).putExtra("bindByActivity",true), this, Context.BIND_AUTO_CREATE)
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (isServiceBind) notificationListener!!.removeOnGotNotificationListener()
        unbindService(this)
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        isServiceBind = true
        notificationListener = (service as NotificationListenerBinder).getService()
        notificationListener!!.setOnGotNotificationListener(this)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        isServiceBind = false
        notificationListener = null
    }

    override fun onGotNotification(array: Array<StatusBarNotification?>) {
        notificationsArray = array
        if (isSettingFragmentBind) settingFragmentListener!!.onGotNotification(array)
        if (isShowingNotificationFragmentBind) showingNotificationFragmentListener!!.onGotNotification(array)
        /*var string = ""
        array.forEach {
            string += it!!.notification.extras.get(NotificationCompat.EXTRA_TITLE).toString() + "\n"
        }
        Log.d("锁定通知", string)*/
    }
}