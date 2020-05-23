package me.singleneuron.locknotification.Fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.GnuLesserGeneralPublicLicense3;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import me.singleneuron.locknotification.BuildConfig;
import me.singleneuron.locknotification.R;
import me.singleneuron.locknotification.Utils.HookStatue;

import static me.singleneuron.locknotification.Utils.GeneralUtils.getSharedPreferenceOnUI;

@Keep
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootkey) {
        addPreferencesFromResource(R.xml.settings);

        HookStatue.Statue statue = HookStatue.getStatue(requireContext());
        if (getSharedPreferenceOnUI(requireActivity()).getBoolean("debugMode", false) || statue.name().contains("taichi")) {
            Preference taichiProblemPreference = findPreference("taichiProblem");
            taichiProblemPreference.setVisible(true);
            taichiProblemPreference.setOnPreferenceClickListener(preference1 -> {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                ImageView imageView = new ImageView(requireContext());
                imageView.setImageResource(R.drawable.taichi);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setAdjustViewBounds(true);
                builder.setView(imageView).setPositiveButton(R.string.OK, null).create().show();
                return true;
            });
        }
        if (getSharedPreferenceOnUI(requireActivity()).getBoolean("debugMode", false) || statue.name().contains("Edxp")) {
            Preference edxpProblemPreference = findPreference("edxpProblem");
            edxpProblemPreference.setVisible(true);
            edxpProblemPreference.setOnPreferenceClickListener(preference1 -> {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                ImageView imageView = new ImageView(requireContext());
                imageView.setImageResource(R.drawable.edxp);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setAdjustViewBounds(true);
                builder.setView(imageView).setPositiveButton(R.string.OK, null).create().show();
                return true;
            });
        }

        if (getSharedPreferenceOnUI(requireActivity()).getBoolean("debugMode", false) || isMIUI()) {
            Preference miuiProblemPreference = findPreference("miuiProblem");
            miuiProblemPreference.setVisible(true);
            miuiProblemPreference.setOnPreferenceClickListener(preference1 -> {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
                ImageView imageView = new ImageView(requireContext());
                imageView.setImageResource(R.drawable.miui);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setAdjustViewBounds(true);
                builder.setView(imageView).setPositiveButton(R.string.OK, null).create().show();
                return true;
            });
        }

        findPreference("version").setSummary(BuildConfig.VERSION_NAME);
        findPreference("qqqun").setOnPreferenceClickListener(preference1 -> {
            /*
             *
             * 发起添加群流程。群号：某些不靠谱插件交流群(951343825) 的 key 为： AjOW9zYQyaV9LQhyqIQrjo21bXnu3JRC
             * 调用 joinQQGroup(AjOW9zYQyaV9LQhyqIQrjo21bXnu3JRC) 即可发起手Q客户端申请加群 某些不靠谱插件交流群(951343825)
             *
             * @param key 由官网生成的key
             * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
             ******************/
            final String key = "AjOW9zYQyaV9LQhyqIQrjo21bXnu3JRC";
            Intent intent = new Intent();
            intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
            // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                startActivity(intent);
            } catch (Exception e) {
                ClipboardManager cmb = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData = ClipData.newPlainText(getString(R.string.qq_group), "951343825");
                assert cmb != null;
                cmb.setPrimaryClip(mClipData);
                Toast.makeText(requireContext(), R.string.already_copy_to_clipbroad, Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        findPreference("connect").setOnPreferenceClickListener(preference1 -> {
            startActivity(Intent.createChooser(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:liziyuan0720@gmail.com")), getString(R.string.send_email)));
            return true;
        });

        findPreference("openSource").setOnPreferenceClickListener(preference1 -> {
            final Notices notices = new Notices();
            notices.addNotice(new Notice(getString(R.string.xposedmusicnotify), "https://github.com/singleNeuron/XposedMusicNotify", "Copyright 2019 神经元", new GnuLesserGeneralPublicLicense3()));
            notices.addNotice(new Notice("Android", "https://source.android.com/license", "The Android Open Source Project", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("XposedBridge", "https://github.com/rovo89/XposedBridge", "Copyright 2013 rovo89, Tungstwenty", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("Kotlin", "https://github.com/JetBrains/kotlin", "Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("libsu", "https://github.com/topjohnwu/libsu", "topjohnwu", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("suspension-fab", "https://github.com/userwangjf/MindLock/tree/master/suspension-fab", "Copyright [2016-09-21] [阿钟]", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("gson", "https://github.com/google/gson", "Copyright 2008 Google Inc.", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("audiohq_md2","https://github.com/Alcatraz323/audiohq_md2","Alcatraz323", new MITLicense()));
            notices.addNotice(new Notice("Nevolution sdk","https://github.com/Nevolution/sdk","Copyright (C) 2015 The Nevolution Project", new ApacheSoftwareLicense20()));
            notices.addNotice(new Notice("WeChat Modernized (Nevolution Decorator)","https://github.com/Nevolution/decorator-wechat", "oasisfeng", new ApacheSoftwareLicense20()));
            new LicensesDialog.Builder(requireContext())
                    .setNotices(notices)
                    .setIncludeOwnLicense(true)
                    .build()
                    .show();
            return true;
        });

        findPreference("forceNight").setOnPreferenceChangeListener((preference1, newValue) -> {
            requireActivity().recreate();
            return true;
        });
        findPreference("autoStart").setOnPreferenceClickListener(preference -> {
            try {
                Intent intent = getAutostartSettingIntent(requireContext());
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        });
        findPreference("config_setting").setOnPreferenceClickListener(preference ->{
            requireActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out, R.anim.slide_left_in, R.anim.slide_right_out).replace(R.id.content_frame, new ConfigFragment(), "configFragment").addToBackStack(ConfigFragment.class.getSimpleName()).commit();
            return true;
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        getListView().setOverScrollMode(ListView.OVER_SCROLL_NEVER);
    }

    private static Intent getAutostartSettingIntent(Context context) {
        ComponentName componentName = null;
        String brand = Build.MANUFACTURER;
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (brand.toLowerCase()) {
            case "samsung"://三星
                componentName = new ComponentName("com.samsung.android.sm", "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity");
                break;
            case "huawei"://华为
                //荣耀V8，EMUI 8.0.0，Android 8.0上，以下两者效果一样
                componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity");
//            componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");//目前看是通用的
                break;
            case "xiaomi"://小米
                componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                break;
            case "vivo"://VIVO
//            componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.safaguard.PurviewTabActivity");
                componentName = new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity");
                break;
            case "oppo"://OPPO
//            componentName = new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity");
                componentName = new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity");
                break;
            case "yulong":
            case "360"://360
                componentName = new ComponentName("com.yulong.android.coolsafe", "com.yulong.android.coolsafe.ui.activity.autorun.AutoRunListActivity");
                break;
            case "meizu"://魅族
                componentName = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity");
                break;
            case "oneplus"://一加
                componentName = new ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity");
                break;
            case "letv"://乐视
                intent.setAction("com.letv.android.permissionautoboot");
            default://其他
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                break;
        }
        intent.setComponent(componentName);
        return intent;
    }

    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    public static boolean isMIUI() {
        Properties prop = new Properties();
        boolean isMIUI;
        try {
            prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        isMIUI = prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        return isMIUI;
    }

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        if (true) {//condition
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(getView(), "alpha", 1, 1);
            objectAnimator.setDuration(requireContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));//time same with parent fragment's animation
            return objectAnimator;
        }
        return super.onCreateAnimator(transit, enter, nextAnim);
    }

}
