package me.singleneuron.locknotification_debugtool;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.graphics.drawable.IconCompat;
import androidx.preference.DropDownPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragmentCompat;

public class MainActivity extends AppCompatActivity {

    private static int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        static String[] keyList = new String[]{"title","text","channelId","style","channelTitle","channelKey","channelImportance"};

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                requireContext().deleteSharedPreferences("me.singleneuron.locknotification_debugtool_preferences");
            }
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            for (String key : keyList) {
                Preference preference = findPreference(key);
                preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        preference.setSummary(newValue.toString());
                        return true;
                    }
                });
            }
            findPreference("send").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DropDownPreference dropDownPreference = findPreference("style");
                    String style = dropDownPreference.getEntry().toString();
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(),((EditTextPreference)findPreference("channelId")).getText())
                            .setSmallIcon(R.drawable.icon)
                            .setContentTitle(((EditTextPreference)findPreference("title")).getText())
                            .setContentText(((EditTextPreference)findPreference("text")).getText());
                    if (style.equals("BigPicture")) builder.setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(BitmapFactory.decodeResource(getResources(),R.drawable.bitmap))
                            .bigLargeIcon(null));
                    else if (style.equals("BigText")) builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("孝公既没，惠文、武、昭，蒙故业，因遗策，南取汉中，西举巴蜀，东割膏腴之地，北收要害之郡。诸侯恐惧，会盟而谋弱秦，不爱珍器重宝、肥饶之地，以致天下之士，合从缔交，相与为一。当此之时，齐有孟尝，赵有平原，楚有春申，魏有信陵；此四君者，皆明智而忠信，宽厚而爱人，尊贤重士，约从离横，兼韩、魏、燕、赵、宋、卫、中山之众，于是六国之士，有甯越、徐尚、苏秦、杜赫之属为之谋，齐明、周最、陈轸、昭滑、楼绥、翟景、苏厉、乐毅之徒通其意，吴起、孙膑、带佗、儿良、王廖、田忌、廉颇、赵奢之伦制其兵；尝以什倍之地，百万之众，叩关而攻秦。秦人开关延敌，九国之师逡巡遁逃而不敢进。秦无亡矢遗镞之费，而天下诸侯已困矣。于是从散约解，争割地而赂秦。秦有馀力而制其敝，追亡逐北，伏尸百万，流血漂橹；因利乘便，宰割天下，分裂河山，强国请服，弱国入朝。施及孝文王、庄襄王，享国之日浅，国家无事。"));
                    else if (style.equals("Inbox")) builder.setStyle(new NotificationCompat.InboxStyle()
                        .addLine("Line 1")
                        .addLine("Line 2")
                        .addLine("Line 3"));
                    else if (style.equals("Messaging")) {
                        Person person = new Person.Builder().setName("Person Name").setIcon(IconCompat.createWithResource(requireContext(),R.drawable.icon)).build();
                        builder.setStyle(new NotificationCompat.MessagingStyle(person)
                                .addMessage(new NotificationCompat.MessagingStyle.Message("Text 1",1, person))
                                .addMessage(new NotificationCompat.MessagingStyle.Message("Text 1",1, person))
                                .addMessage(new NotificationCompat.MessagingStyle.Message("Text 1",1, person)));
                    }
                    else if (style.equals("Media")) builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(new MediaSessionCompat(requireContext(),"MediaSessionTag").getSessionToken()));
                    NotificationManagerCompat.from(requireContext()).notify(i++,builder.build());
                    return true;
                }
            });
            findPreference("create").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int importance = NotificationManagerCompat.IMPORTANCE_DEFAULT;
                    switch (((DropDownPreference)findPreference("channelImportance")).getEntry().toString()) {
                        case "PRIORITY_MIN": {
                            importance = NotificationManagerCompat.IMPORTANCE_MIN;
                            break;
                        }
                        case "PRIORITY_LOW": {
                            importance = NotificationManagerCompat.IMPORTANCE_LOW;
                            break;
                        }
                        case "PRIORITY_HIGH": {
                            importance = NotificationManagerCompat.IMPORTANCE_HIGH;
                            break;
                        }
                    }
                    NotificationChannel channel = new NotificationChannel(((EditTextPreference)findPreference("channelKey")).getText(),((EditTextPreference)findPreference("channelTitle")).getText(),importance);
                    ((NotificationManager) requireContext().getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
                    return true;
                }
            });
            findPreference("remove").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ((NotificationManager) requireContext().getSystemService(NOTIFICATION_SERVICE)).deleteNotificationChannel(((EditTextPreference)findPreference("channelKey")).getText());
                    return true;
                }
            });
        }
    }
}