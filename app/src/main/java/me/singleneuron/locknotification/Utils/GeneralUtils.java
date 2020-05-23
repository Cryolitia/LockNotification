package me.singleneuron.locknotification.Utils;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import de.robv.android.xposed.XposedBridge;
import me.singleneuron.locknotification.BuildConfig;

final public class GeneralUtils {

    public static Context getContext() {
        return AndroidAppHelper.currentApplication().getApplicationContext();
    }

    public static SharedPreferences getSharedPreferenceOnUI(Context context) {
        return context.getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", Context.MODE_PRIVATE);
    }

}
