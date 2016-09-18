package com.qou.h7se.sbr;

/**
 * Created by k0de9x on 9/11/2015.
 */


import android.app.Application;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Created by k0de7x on 1/23/14.
 */
public class AppEx extends Application {

    public static AppEx self;

    public static String PACKAGE_NAME;
    public static String PACKAGE_TAG = String.valueOf(BuildConfig.VERSION_CODE);

    private File storageDir = null;
    private File storageTempDir = null;

    public SecureRandom random = null;

    public ArrayList<StorageItem> backupRestoreSources = null;

    private MainActivity activity;

    GoogleDriveClientExt gclient;
    NewFtpClientEx fclient;
    DBoxClient dclient;

    // Settings.Global.putInt(getContentResolver(),Settings.Global.ADB_ENABLED, 0); // 0 to disable, 1 to enable

    void requestConnectionNotify(boolean connectedToInternet) {
        if(gclient != null) {
            gclient.publishConnectionStatus(connectedToInternet && gclient.isConnected());
        }
        if(fclient != null) {
            fclient.publishConnectionStatus(connectedToInternet && fclient.isConnected());
        }
        if(dclient != null) {
            dclient.publishConnectionStatus(connectedToInternet && dclient.isLinked());
        }
    }

    public static void EnableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyFlashScreen()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    public static void DisableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .build());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        AppEx.PACKAGE_NAME = getApplicationContext().getPackageName();

        random = new SecureRandom();

        // EnableStrictMode();

        PrefsActivity.prefs =
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        updateCurrentStorageDirPath(
                PrefsActivity.getString(PrefsActivity.BACKUP_LOCATION_LOCAL), true);

        setupBackupRestoreSources();

        NetworkStateReceiver.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
            @Override
            String id() {
                return "appex/create/wifi";
            }

            @Override
            void connection(boolean success) {
                requestConnectionNotify(success);
            }
        }, false /* keep false */);

        self = this;
    }


    void setupBackupRestoreSources() {
        backupRestoreSources = new ArrayList<>();
        try {
            backupRestoreSources.add(new StorageItem("Local", null, null, PathUtils.getCanonicalPath(getCurrentStorageDir(), false),
                    StorageItem.ItemType.LOCAL_GROUP, StorageGroup.Types.LOCAL));

            backupRestoreSources.add(new StorageItem("Ftp", null, null, GenericFs.Ftp.getCurrentRootDirectory(),
                    StorageItem.ItemType.FTP_GROUP, StorageGroup.Types.FTP));

            backupRestoreSources.add(new StorageItem("GoogleDrive", null, null, GenericFs.Drive.getCurrentRootDirectory(),
                    StorageItem.ItemType.GOOGLE_GROUP, StorageGroup.Types.GOOGLE_DRIVE));

            backupRestoreSources.add(new StorageItem("DropBox", null, null, GenericFs.DBox.getCurrentRootDirectory(),
                    StorageItem.ItemType.DROPBOX_GROUP, StorageGroup.Types.DROP_BOX));

            backupRestoreSources.add(new StorageItem("Browse", null, null, GenericFs.SDCard.getCurrentRootDirectory(),
                    StorageItem.ItemType.SDCARD_GROUP, StorageGroup.Types.SDCARD));

        } catch (Exception e) {
            Utils.LogException(e);
        }
    }


    public File getDefaultStorageDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public void updateCurrentStorageDirPath(String newPath, boolean saveToPreferences) {
        if(TextUtils.isEmpty(newPath)) {
            storageDir =  getDefaultStorageDir();
        } else {
            File file = new File(newPath);
            file.mkdirs();
            if(file.exists() && file.isDirectory() && file.canRead() && file.canWrite()) {
                storageDir = file;
            } else {
                storageDir =  getDefaultStorageDir();
            }
        }

        if(saveToPreferences) {
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            prefs.edit().putString(PrefsActivity.BACKUP_LOCATION_LOCAL, storageDir.getAbsolutePath()).apply();
        }

        storageTempDir = new File(storageDir, "temp.ignore");
    }

    public File getCurrentStorageDir() {
        return storageDir;
    }

    public File getTemporaryStorageDir() {
        return storageTempDir;
    }


    public MainActivity getActivity() {
        return this.activity;
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }
}

