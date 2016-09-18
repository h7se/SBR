package com.qou.h7se.sbr;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import org.apache.commons.io.FileUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

/**
 * Created by k0de9x on 10/30/2015.
 */
public class ScheduleBackup {

    static void backup(final GenericCallback4<Boolean> callback) {
        boolean isOnline = Utils.IsNetworkAvailable();

        if (!AppEx.self.getCurrentStorageDir().exists()) {
            if(!(AppEx.self.getCurrentStorageDir().mkdirs())) {
                if(callback != null) {
                    callback.event(false);
                }
                return;
            }
        }

//        if (!Utils.IsNetworkAvailable()) {
//            if(callback != null) {
//                callback.event(false);
//            }
//            return;
//        }

        final SharedPreferences prefs = PrefsActivity.prefs;
        final MutableVar<String> tmp = new MutableVar<>(prefs.getString("schedule_backup_sources", ""));
        if(!(TextUtils.isEmpty(tmp.value))) {
            final MutableVar<String[]> sources = new MutableVar<>(tmp.value.split(","));

            if (sources.value.length > 0) {
                NetworkStateReceiver.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
                    @Override
                    String id() {
                        return "schedule.backup.network.listener";
                    }

                    @Override
                    void connection(boolean success) {
                        if (success) {
                            final List<GenericFs.Base> clients = new ArrayList<>();

                            final Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    tmp.value = prefs.getString("schedule_backup_items", "");
                                    if(!(TextUtils.isEmpty(tmp.value))) {
                                        final MutableVar<String[]> items = new MutableVar<>(tmp.value.split(","));
                                        final Queue<BackupRvEntry> nodes = new LinkedList<>();
                                        for(final String s : items.value) {
                                            nodes.add(new BackupRvEntry(Utils.MapTitleToUri(s), s, 0));
                                        }

                                        BackupFragment.BackupHelper.canceled = false;
                                        LoadingPanel.instance.setVisibility(true);
                                        LoadingPanel.instance.setProgress2Max(nodes.size());

                                        BackupFragment.BackupHelper.instance = new BackupFragment.BackupHelper(getActivity(), clients, new GenericCallback2() {
                                            @Override
                                            public void event() {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        BackupFragment.BackupHelper.canceled = (!(LoadingPanel.instance.getVisibility()));

                                                        if (BackupFragment.BackupHelper.canceled) {
                                                            nodes.clear();
                                                        }

                                                        if (nodes.size() > 0) {
                                                            BackupFragment.BackupHelper.instance.process(nodes.remove());
                                                        } else {
                                                            if (!(BackupFragment.BackupHelper.canceled)) {
                                                                PrefsActivity.getPrefMgr().edit().putLong(
                                                                        "local.last.backup", System.currentTimeMillis()).apply();

                                                                AppLog.instance.add("All tasks completed.",
                                                                        LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.INFO, true);

                                                                LoadingPanel.instance.setVisibility(false);
                                                            } else {
                                                                AppLog.instance.add("Backup canceled, aborting.", LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.ERROR, true);
                                                            }

                                                            new Utils.DoAsyncEx2<Void>(new OnDataCallback2<Void>() {
                                                                @Override
                                                                public void data(@Nullable Void data) {
                                                                    if(callback != null) {
                                                                        callback.event(true);
                                                                    }
                                                                }
                                                            }).run(new Callable<Void>() {
                                                                @Override
                                                                public Void call() throws Exception {
                                                                    FileUtils.cleanDirectory(
                                                                            AppEx.self.getTemporaryStorageDir());
                                                                    return null;
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        });

                                        BackupFragment.BackupHelper.instance.process(nodes.remove());
                                    }
                                }
                            };

                            for (final String s : sources.value) {
                                if (s.equalsIgnoreCase("Ftp")) {
                                    AppEx.self.fclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
                                        @Override
                                        String id() {
                                            return "111";
                                        }

                                        @Override
                                        boolean removeIf() {
                                            return getStatus();
                                        }

                                        @Override
                                        void connection(boolean success) {
                                            if(success) {
                                                clients.add(new GenericFs.Ftp(AppEx.self.fclient));
                                            } else {
                                                if(callback != null) {
                                                    callback.event(false);
                                                }
                                                return;
                                            }
                                            if(clients.size() == sources.value.length) {
                                                runnable.run();
                                            }
                                        }
                                    }, false);
                                    AppEx.self.fclient.connect(null);

                                } else if (s.equalsIgnoreCase("GoogleDrive")) {
                                    AppEx.self.gclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
                                        @Override
                                        String id() {
                                            return "111";
                                        }

                                        @Override
                                        boolean removeIf() {
                                            return getStatus();
                                        }

                                        @Override
                                        void connection(boolean success) {
                                            if(success) {
                                                clients.add(new GenericFs.Drive(AppEx.self.gclient));
                                            } else {
                                                if(callback != null) {
                                                    callback.event(false);
                                                }
                                                return;
                                            }
                                            if(clients.size() == sources.value.length) {
                                                runnable.run();
                                            }
                                        }
                                    }, false);
                                    AppEx.self.gclient.connect(false);

                                } else if (s.equalsIgnoreCase("DropBox")) {
                                    AppEx.self.dclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
                                        @Override
                                        String id() {
                                            return "111";
                                        }

                                        @Override
                                        boolean removeIf() {
                                            return getStatus();
                                        }

                                        @Override
                                        void connection(boolean success) {
                                            if(success) {
                                                clients.add(new GenericFs.DBox(AppEx.self.dclient));
                                            } else {
                                                if(callback != null) {
                                                    callback.event(false);
                                                }
                                                return;
                                            }
                                            if(clients.size() == sources.value.length) {
                                                runnable.run();
                                            }
                                        }
                                    }, false);
                                    AppEx.self.dclient.login();

                                } else if (s.equalsIgnoreCase("Local")) {
                                    clients.add(new GenericFs.Local());
                                }
                            }
                        } else {
//                            if(callback != null) {
//                                callback.event(false);
//                            }
//                            return;
                        }
                    }

                    @Override
                    boolean removeIf() {
                        return getStatus(); //  == true
                    }
                }, false);

                if(!(sources.value.length == 1 && Utils.containsCaseInsensitive(sources.value, "local"))) {
                    WifiManager wifi;
                    wifi=(WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                    if(!(wifi.isWifiEnabled())) {
                        wifi.setWifiEnabled(true);
                    } else {
                        NetworkStateReceiver.publishConnectionStatus(Utils.IsNetworkAvailable());
                    }
                } else {
                    NetworkStateReceiver.publishConnectionStatus(true);
                }
            }
        }
    }

    static Activity getActivity() {
        return AppEx.self.getActivity();
    }

    static void showNotification(boolean success) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("SBR")
                        .setContentText((success ? "[success]: " : "[failure]: ") + "schedule backup has been completed")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(getActivity(), MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getActivity());
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(101, builder.build());

        getActivity().finish();
    }
}
