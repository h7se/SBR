package com.qou.h7se.sbr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by k0de9x on 9/16/2015.
 */
public class PrefsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    //  SharedPreferences SP;
//        public static Drawable background = null;
    public static SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        /// AppEx.self.prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        addPreferencesFromResource(R.xml.preferences);

        PreferenceManager.getDefaultSharedPreferences(
                this).registerOnSharedPreferenceChangeListener(this);

        ListPreference listPreference = (ListPreference)findPreference(BACKGROUND_IMAGE);
        try {
            String[] assets = getAssets().list("backgrounds");
            List<String> titles = new ArrayList<>();
            for(String a : assets) {
                titles.add(a.replaceAll("_", " ").replaceAll("[.]\\w+$", ""));
            }
            listPreference.setEntries(titles.toArray(new String[titles.size()]));
            listPreference.setEntryValues(assets);
        } catch (IOException e) {
            Utils.LogException(e);
        }


        findPreference("delete_google_account_info").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Utils.toast("deleting google drive account info");

                if(AppEx.self.gclient.getApiClient().isConnected()) {
                    AppEx.self.gclient.getApiClient().clearDefaultAccountAndReconnect();
                }

                PrefsActivity.prefs.edit()
                        .remove(GoogleDriveClientExt.Constants.PREF_ACCOUNT_NAME)
                        .apply();
                return false;
            }
        });


        findPreference("delete_dbox_account_info").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Utils.toast("deleting drop box account info");

                if( AppEx.self.dclient.isLinked()) {
                    AppEx.self.dclient.unlink();
                }

                PrefsActivity.prefs.edit()
                        .remove(DBoxClient.DBOX_TOKEN_PREFS_KEY)
                        .apply();
                return false;
            }
        });

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        switch (s) {
            case LOGS_FONT_SIZE:
                LogsAdapter.text_size =
                        Float.parseFloat(prefs.getString(LOGS_FONT_SIZE, "8.5"));
                break;

            case LOGs_MAX_FILE_SIZE:
                int value = Integer.parseInt(prefs.getString(LOGs_MAX_FILE_SIZE, "16"));
                if(value >= 1 && value <= 128) {
                    Constants.MAX_LOG_FILE_SIZE = value;
                }
                break;

            case MESSAGE_BAR_TEXT_DISPLAY_DURATION:
                QuickMessage.instance.msg_display_duration =
                        Integer.parseInt(prefs.getString(MESSAGE_BAR_TEXT_DISPLAY_DURATION, "2600"));
                break;

            case MESSAGE_BAR_VISIBLE:
                QuickMessage.instance.setVisibility(prefs.getBoolean(MESSAGE_BAR_VISIBLE, true));
                break;

            case ENABLE_BACKGROUND_IMAGE:
            case BACKGROUND_IMAGE:
                BaseFragmentEx.Helper.Background.update(this);
            break;

            case BACKUP_LOCATION_LOCAL:
                AppEx.self.updateCurrentStorageDirPath(prefs.getString(BACKUP_LOCATION_LOCAL, ""), false);
                AppEx.self.setupBackupRestoreSources();
                break;

            case PrefsActivity.BACKUP_LOCATION_DBOX:
//                if(TextUtils.isEmpty(prefs.getString(BACKUP_LOCATION_LOCAL, ""))) {
//                    prefs.edit().putString(BACKUP_LOCATION_LOCAL, "/").apply();
//                }
            case PrefsActivity.BACKUP_LOCATION_FTP:
            case PrefsActivity.BACKUP_LOCATION_GDRIVE:

                AppEx.self.setupBackupRestoreSources();
               break;

            case PROGRESS_WINDOW_ANIMATION:
                LoadingPanel.instance.setAnimationType(
                        Integer.parseInt(prefs.getString(PROGRESS_WINDOW_ANIMATION, "-1")));
                break;
        }
    }

    public static void loadPrefs(Context context) {
        PreferenceManager.setDefaultValues
                (context, R.xml.preferences, false);

        LogsAdapter.text_size = Float.parseFloat(prefs.getString(LOGS_FONT_SIZE, "8.5"));
        QuickMessage.instance.msg_display_duration = Integer.parseInt(prefs.getString(MESSAGE_BAR_TEXT_DISPLAY_DURATION, "2600"));
        QuickMessage.instance.setVisibility(prefs.getBoolean(MESSAGE_BAR_VISIBLE, true));
        Constants.MAX_LOG_FILE_SIZE = Integer.parseInt(prefs.getString(LOGs_MAX_FILE_SIZE, "16"));
        BaseFragmentEx.Helper.Background.update(context);

        LoadingPanel.instance.setAnimationType(Integer.parseInt(prefs.getString(PROGRESS_WINDOW_ANIMATION, "-1")));
    }

    @Override
    protected void onResume() {
        super.onResume();

        overridePendingTransition(R.anim.prefs_activity_enter, R.anim.vanish_fast);
    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(R.anim.prefs_activity_enter, R.anim.vanish_fast);
    }

    public static final  String ENABLE_BACKGROUND_IMAGE = "enable_background_image";
    public static final  String BACKGROUND_IMAGE = "background_image_list";
    public static final  String MESSAGE_BAR_VISIBLE = "message_bar_visible";
    public static final  String MESSAGE_BAR_TEXT_DISPLAY_DURATION = "msg_display_duration";

    public static final  String PROGRESS_WINDOW_ANIMATION = "progress_window_animation";


    public static final  String LOGS_FONT_SIZE = "logs_font_size";
    public static final  String LOGs_MAX_FILE_SIZE = "max_log_file_size";

    public static final  String BACKUP_LOCATION_LOCAL = "local_backup_location_value";

    public static final  String BACKUP_LOCATION_FTP = "ftp_backup_location_value";
    public static final  String BACKUP_LOCATION_DBOX = "dropbox_backup_location_value";
    public static final  String BACKUP_LOCATION_GDRIVE = "drive_backup_location_value";




    public static final  String SDCARD_ROOT_DIRECTORY = "sdcard_root_directory";

    public static final  String COMPRESSION_LEVEL = "compression_level";
    public static final  String COMPRESSION_METHOD = "compression_method";
    public static final  String COMPRESSION_ENCRYPTION_ENABLE_FLAG = "compression_encryption_enable_flag";
    public static final  String COMPRESSION_PASSWORD = "compression_password";
    public static final  String COMPRESSION_ENCRYPTION_METHOD = "compression_encryption_method";
    public static final  String COMPRESSION_ENCRYPTION_AES_STRENGTH = "compression_encryption_aes_strength";

    public static final  String FTP_SERVER = "ftp_server_value";
    public static final  String FTP_USER  = "ftp_user_value";
    public static final  String FTP_PASS = "ftp_pass_value";

    public static final  String FTP_CONNECT_TIMEOUT = "ftp_connect_timeout";
    public static final  String FTP_SOKCET_TIMEOUT = "ftp_socket_timeout";
    public static final  String FTP_CONNECTION_PORT = "ftp_connection_port";
    public static final  String FTP_SHOW_HIDDEN_FILES = "ftp_show_hidden_files";

    public static long getLong(String key, long defaultValue) {
        return prefs.getLong(key, defaultValue);
    }

    public static String getString(String key) {
        return prefs.getString(key, null);
    }

    public static String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public static boolean getBoolean(String key) {
        return prefs.getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public static void  setPrefString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public static void  setPrefLong(String key, Long value) {
        prefs.edit().putLong(key, value).apply();
    }

    public static SharedPreferences getPrefMgr() {
        return prefs;
    }


}
