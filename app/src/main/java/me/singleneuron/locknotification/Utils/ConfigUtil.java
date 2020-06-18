package me.singleneuron.locknotification.Utils;

import android.app.Notification;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.regex.Pattern;

@Keep
public class ConfigUtil {

    public String configName = null;
    public String configKey = null;
    public boolean enable = true;

    public boolean useRegex = false;
    public String usePackage = null;
    public String useTitle = null;
    public String useMessage = null;
    public String useChannel = null;
    public Set<String> useStyle = null;

    public boolean lockNotification = false;
    public boolean throwInAnotherChannel = false;
    public String channelTitle = null;
    public String channelKey = null;
    public String channelImportance = null;
    public String visibility = null;

    public boolean replace = false;
    public String toReplace = "";
    public String replaceTo = "";

    @NonNull
    public static ConfigUtil fromJson(@NonNull String json) {
        return new Gson().fromJson(json, ConfigUtil.class);
    }

    @NotNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    @SuppressWarnings("RedundantIfStatement")
    public boolean configHit(@NonNull Bundle extras, @NonNull String packageName, @Nullable String channelID) {
        if (!enable) return false;
        if (!stringNullOrEmpty(usePackage)) {
            if (regexNotEquals(usePackage, packageName)) return false;
        }
        String string;
        if (!stringNullOrEmpty(useTitle)) {
            string = getBundleObjectString(extras,NotificationCompat.EXTRA_TITLE);
            if (stringNullOrEmpty(string)) return false;
            else if (regexNotEquals(useTitle, string)) return false;
        }
        if (!stringNullOrEmpty(useMessage)) {
            string = getBundleObjectString(extras,NotificationCompat.EXTRA_TEXT);
            if (stringNullOrEmpty(string)) return false;
            else if (regexNotEquals(useMessage, string)) return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if (!stringNullOrEmpty(useChannel)) {
                if (regexNotEquals(useChannel, channelID)) return false;
            }
        }
        if (!styleHit(extras)) return false;
        return true;
    }

    private String getBundleObjectString(Bundle extras, String key) {
        Object object = null;
        String string = null;
        try {
            object = extras.get(key);
        } catch (Exception e) {
            //ignored
        }
        if (object==null) return null;
        try {
            string = object.toString();
        } catch (Exception e) {
            //ignored
        }
        return string;
    }

    private boolean regexNotEquals(@NonNull String regex, String string) {
        if (useRegex) {
            if (string == null) return !Pattern.matches(regex, "");
            return !Pattern.matches(regex, string);
        } else return !regex.equals(string);
    }

    private boolean styleHit(@NonNull Bundle extras) {
        if (useStyle==null||useStyle.isEmpty()) return true;
        boolean isHit = false;
        if (!extras.containsKey(NotificationCompat.EXTRA_TEMPLATE)) return false;
        String extra_template = extras.getString(Notification.EXTRA_TEMPLATE);
        if (stringNullOrEmpty(extra_template)) return false;

        if (useStyle.contains("BigPicture")) {
            if (extras.containsKey(NotificationCompat.EXTRA_PICTURE)) isHit = true;
            else isHit |= Notification.BigPictureStyle.class.getName().equals(extra_template);
        }
        if (useStyle.contains("BigText")) {
            if (extras.containsKey(NotificationCompat.EXTRA_BIG_TEXT)) isHit = true;
            else isHit |= Notification.BigTextStyle.class.getName().equals(extra_template);
        }
        if (useStyle.contains("Inbox")) {
            if (extras.containsKey(NotificationCompat.EXTRA_TEXT_LINES)) isHit = true;
            else isHit |= Notification.InboxStyle.class.getName().equals(extra_template);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N&&useStyle.contains("Messaging")) {
            isHit |= Notification.MessagingStyle.class.getName().equals(extra_template);
        }
        if (useStyle.contains("Media")) {
            if (extras.containsKey(NotificationCompat.EXTRA_MEDIA_SESSION)) isHit = true;
            else isHit |= Notification.MediaStyle.class.getName().equals(extra_template);
        }
        return isHit;
    }

    public int getProperty() {
        if (channelImportance==null||channelImportance.isEmpty()) return NotificationCompat.PRIORITY_DEFAULT;
        switch (channelImportance) {
            case "PRIORITY_MIN" : {
                return NotificationCompat.PRIORITY_MIN;
            }
            case "PRIORITY_LOW" : {
                return NotificationCompat.PRIORITY_LOW;
            }
            case  "PRIORITY_HIGH" : {
                return NotificationCompat.PRIORITY_HIGH;
            }
            default: {
                return NotificationCompat.PRIORITY_DEFAULT;
            }
        }
    }

    public int getImportance() {
        if (channelImportance==null||channelImportance.isEmpty()) return NotificationManagerCompat.IMPORTANCE_DEFAULT;
        switch (channelImportance) {
            case "PRIORITY_MIN" : {
                return NotificationManagerCompat.IMPORTANCE_MIN;
            }
            case "PRIORITY_LOW" : {
                return NotificationManagerCompat.IMPORTANCE_LOW;
            }
            case  "PRIORITY_HIGH" : {
                return NotificationManagerCompat.IMPORTANCE_HIGH;
            }
            default: {
                return NotificationManagerCompat.IMPORTANCE_DEFAULT;
            }
        }
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    public int getVisibility() {
        if (stringNullOrEmpty(visibility)) return NotificationCompat.VISIBILITY_PUBLIC;
        switch (visibility) {
            case "VISIBILITY_PUBLIC" : {
                return NotificationCompat.VISIBILITY_PUBLIC;
            }
            case "VISIBILITY_PRIVATE" : {
                return NotificationCompat.VISIBILITY_PRIVATE;
            }
            case "VISIBILITY_SECRET" : {
                return NotificationCompat.VISIBILITY_SECRET;
            }
            default: {
                return NotificationCompat.VISIBILITY_PUBLIC;
            }
        }
    }

    private boolean stringNullOrEmpty(String string) {
        if(string==null) return true;
        return string.isEmpty();
    }

    public Notification replace(Notification notification) {
        Notification newNotification = notification.clone();
        String string = getBundleObjectString(notification.extras,NotificationCompat.EXTRA_TEXT);
        if (string==null) return notification;
        string = string.replaceAll(toReplace,replaceTo);
        newNotification.extras.putString(NotificationCompat.EXTRA_TEXT,string);
        newNotification.visibility = NotificationCompat.VISIBILITY_PUBLIC;
        notification.publicVersion = newNotification;
        //Log.d("LockNotification","already replaced");
        return notification;
    }

}
