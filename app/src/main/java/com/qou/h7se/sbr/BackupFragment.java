package com.qou.h7se.sbr;

import android.app.Activity;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

/**
 * Created by k0de9x on 9/22/2015.
 */
public class BackupFragment extends BaseFragmentEx {

    BackupLocationsRvAdapter backupLocationsRvAdapter = null;
    RecyclerView.AdapterDataObserver backupLocationsRvAdapterDataObserver;

    BackupLocationsRvAdapter3 backupDataSourcesAdapter  = null;
    RecyclerView.AdapterDataObserver backupDataSourcesAdapterDataObserver;

    boolean buttonBackupVisibility = false;

    public BackupFragment() {
    }

    public static BackupFragment newInstance(int sectionNumber) {
        BackupFragment fragment = new BackupFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("schedule.backup")) {
            ScheduleBackup.backup(new GenericCallback4<Boolean>() {
                @Override
                public void event(Boolean value) {
                    ScheduleBackup.showNotification(value);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_backup, container, false);

        createRecyclerView(rootView);
        create(rootView);

        setHasOptionsMenu(true);

        return rootView;
    }

    void create(View rootView) {
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv2);
        recyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        ArrayList<BackupRvEntry> entries = new ArrayList<>();

        for (DataSource e : DataSource.values()) {
            Uri[] uris = Utils.MapToUris(e);

            for (Uri u : uris) {
                String title =Utils.MapUriToTitle(u);
                entries.add(new BackupRvEntry(u, Utils.StringEx.title(title),
                        BackupLocationsRvAdapter3.MapTitleToDrawable(title)));
            }
        }

        backupDataSourcesAdapter = new BackupLocationsRvAdapter3(getContext(), entries,
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        backupDataSourcesAdapter.toggleChecked(i);
                        backupDataSourcesAdapter.notifyDataSetChanged();
                    }
                });
        recyclerView.setAdapter(backupDataSourcesAdapter);
    }

    void createRecyclerView(View rootView) {
        RecyclerView rv = (RecyclerView) rootView.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

        ArrayList<StorageGroupListViewItemWrapper> data = new ArrayList<>();
        for (StorageItem item : AppEx.self.backupRestoreSources) {
            if (item.groupType != StorageGroup.Types.SDCARD) {
                data.add(new StorageGroupListViewItemWrapper(item.title, item.groupType));
            }
        }

        backupLocationsRvAdapter = new BackupLocationsRvAdapter(data, new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                backupLocationsRvAdapter.toggleChecked(i);
                backupLocationsRvAdapter.notifyDataSetChanged();

                StorageGroupListViewItemWrapper item = backupLocationsRvAdapter.getItem(i);
                if (item.groupType == StorageGroup.Types.GOOGLE_DRIVE) {
                    if (item.checked && (!AppEx.self.gclient.isConnected())) {
                        AppEx.self.gclient.connect(true);
                    } else if (!(item.checked) && (AppEx.self.gclient.isConnected())) {
                        AppEx.self.gclient.disconnect();
                    }
                } else if (item.groupType == StorageGroup.Types.DROP_BOX) {
                    if (item.checked && (!AppEx.self.dclient.isLinked())) {
                        // dbxclient.link();
                        // AppEx.self.dclient.init(); // getActivity(), Constants.REQUEST_CODE_DROP_BOX_LINK_ACTIVITY
                        AppEx.self.dclient.login();
                    } else if (!(item.checked) && (AppEx.self.dclient.isLinked())) {
                        // dbxclient.unlink(); // TODO: undo comment
                    }
                } else if (item.groupType == StorageGroup.Types.FTP) {
                    if (item.checked && (!AppEx.self.fclient.isConnected())) {
                        AppEx.self.fclient.connect(null);
                    } else if (!(item.checked) && (AppEx.self.fclient.isConnected())) {
                        AppEx.self.fclient.disconnect(null);
                    }
                }
            }
        });

        backupLocationsRvAdapter.getItemsMatchingPredicate(new DataFilterCallback<StorageGroupListViewItemWrapper>() {
            @Override
            public boolean include(StorageGroupListViewItemWrapper item) {
                return (item.groupType == StorageGroup.Types.LOCAL || item.groupType == StorageGroup.Types.SDCARD /* */);
            }
        }, new DataFilterActionCallback<StorageGroupListViewItemWrapper>() {
            @Override
            public void run(StorageGroupListViewItemWrapper item, int position) {
                item.checked = true;
                item.connectionStatusApply = false;
            }
        }, null, null);

        rv.setAdapter(backupLocationsRvAdapter);
    }

    void backup() {
        backup(null);
    }

    void backup(final GenericCallback4<Boolean> callback) {
        boolean isOnline = Utils.IsNetworkAvailable();

        if (!AppEx.self.getCurrentStorageDir().exists()) {
            if (!(AppEx.self.getCurrentStorageDir().mkdirs())) {
                AppLog.instance.add("Could not make required directory structure, aborting.",
                        LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.ERROR, true);

                if (callback != null) {
                    callback.event(false);
                }
                return;
            }
        }

        List<StorageGroupListViewItemWrapper> data = backupLocationsRvAdapter.getItems();
        for (final StorageGroupListViewItemWrapper item : data) {
            if (item.checked) {
                if (item.groupType == StorageGroup.Types.FTP) {
                    if (isOnline) {
                        if (!(AppEx.self.fclient.isConnected())) {
//                            Intent i = new Intent(getActivity(), LoginActivity.class);
//                            i.putExtra("type", "ftp");
//                            i.putExtra("msg", "Login to ftp server and try again.");
//                            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                            startActivityForResult(i, Constants.REQUEST_CODE_LOGIN_ACTIVITY);
//                            getActivity().overridePendingTransition(R.anim.bottom_up, R.anim.bottom_down);
//                            // Utils.toast("Login to ftp server and try again.");
                            AppLog.instance.add("Can't login to ftp server, aborting.",
                                    LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.ERROR);

                            if (callback != null) {
                                callback.event(false);
                            }
                            return;
                        }
                    } else {
                        AppLog.instance.add("Can't connect to the internet, aborting.",
                                LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.ERROR, true);

                        if (callback != null) {
                            callback.event(false);
                        }
                        return;
                    }
                } else if (item.groupType == StorageGroup.Types.GOOGLE_DRIVE) {
                    if (isOnline) {
                        if (!AppEx.self.gclient.isConnected()) {
                            AppLog.instance.add("Not connected to google drive, aborting.",
                                    LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.ERROR, true);

                            if (callback != null) {
                                callback.event(false);
                            }
                            return;
                        }
                    } else {
                        AppLog.instance.add("Can't connect to the internet, aborting.",
                                LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.ERROR, true);

                        if (callback != null) {
                            callback.event(false);
                        }
                        return;
                    }
                } else if (item.groupType == StorageGroup.Types.DROP_BOX) {
                    if (isOnline) {
                        if (!AppEx.self.dclient.isLinked()) {
                            AppLog.instance.add("Not connected to dropbox, aborting.",
                                    LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.ERROR, true);

                            if (callback != null) {
                                callback.event(false);
                            }
                            return;
                        }
                    } else {
                        AppLog.instance.add("Can't connect to the internet, aborting.",
                                LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.ERROR, true);

                        if (callback != null) {
                            callback.event(false);
                        }
                        return;
                    }
                }
            }
        }

        setBackupButtonVisibility(false);

        final List<GenericFs.Base> clients = new ArrayList<>();
        for (final StorageGroupListViewItemWrapper item : data) {
            if (item.checked) {
                clients.add(GenericFs.
                        Helper.getNewClientOfType(item.groupType));
                item.checked = false;
            }
        }

        backupDataSourcesAdapter.getItemsMatchingPredicate(new DataFilterCallback<BackupRvEntry>() {
            @Override
            public boolean include(BackupRvEntry item) {
                return item.isChecked();
            }
        }, null, null, new DataFilterCompleteCallback<BackupRvEntry>() {
            @Override
            public void run(List<Integer> positions, final List<BackupRvEntry> result) {
                final Queue<BackupRvEntry> nodes = new LinkedList<>(result);

                BackupHelper.canceled = false;
                LoadingPanel.instance.reset();
                LoadingPanel.instance.setVisibility(true);
                LoadingPanel.instance.setProgress2Max(result.size());

                BackupHelper.instance = new BackupHelper(getActivity(), clients, new GenericCallback2() {
                    @Override
                    public void event() {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BackupHelper.canceled = (!(LoadingPanel.instance.getVisibility()));

                                if (BackupHelper.canceled) {
                                    nodes.clear();
                                }

                                if (nodes.size() > 0) {
                                    BackupHelper.instance.process(nodes.remove());
                                } else {
                                    if (!(BackupHelper.canceled)) {
                                        PrefsActivity.getPrefMgr().edit().putLong(
                                                "local.last.backup", System.currentTimeMillis()).apply();

                                        AppLog.instance.add("all tasks completed.",
                                                LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.INFO, true);

                                        LoadingPanel.instance.setVisibility(false);
                                    } else {
                                        AppLog.instance.add("backup canceled, aborting.", LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.ERROR, true);
                                    }

                                    backupDataSourcesAdapter.notifyDataSetChanged();
                                    backupLocationsRvAdapter.notifyDataSetChanged();

                                    new Utils.DoAsyncEx2<Void>(new OnDataCallback2<Void>() {
                                        @Override
                                        public void data(@Nullable Void data) {
                                            if (callback != null) {
                                                callback.event(true);
                                            }
                                        }
                                    }).run(new Callable<Void>() {
                                        @Override
                                        public Void call() throws Exception {
                                            FileUtils.cleanDirectory(AppEx.self.getTemporaryStorageDir());
                                            return null;
                                        }
                                    });
                                }
                            }
                        });
                    }
                });

                BackupHelper.instance.process(nodes.remove());
            }
        });
    }

    static class BackupHelper {
        static boolean canceled = false;
        static BackupHelper instance = null;

        Activity activity;
        List<GenericFs.Base> clients;
        GenericCallback2 callback;

        public BackupHelper(Activity activity, List<GenericFs.Base> clients, GenericCallback2 callback) {
            this.activity = activity;
            this.clients = clients;
            this.callback = callback;
        }

        public void process(final BackupRvEntry node) {
            LoadingPanel.instance.resetProgress1();

            final Uri u = node.getUri();

            String fileName = Utils.MapUriToXmlFile(u);
            if (fileName == null) {
                Utils.LogException("Not recognized, " + u);
                return;
            }

            final File file = new java.io.File(
                    AppEx.self.getTemporaryStorageDir(), fileName);

            final String comment = ((MainActivity) activity).
                    getSidePanelDrawerFragment().getEditText().getText().toString();

            final List<String> files = new ArrayList<>();
            files.add(file.getAbsolutePath());

            ContentProviderHandlerEx.XDExport.Helpers.Export(activity, u, new ContentProviderHandlerEx.XDExport.OnXDDataCallback() {
                @Override
                public void enter(Uri uri) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LoadingPanel.instance.showMsg(String.format("processing %s...", node.getTitle()));
                        }
                    });
                }

                @Override
                public void data(Uri uri, int type, final String key, final Object value) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (u.equals(Uris.IMAGES) || u.equals(Uris.AUDIO) || u.equals(Uris.VIDEO)) {
                                if (key.equals(MediaStore.MediaColumns.DATA)) {
                                    files.add(String.valueOf(value));
                                }
                            }
                        }
                    });
                }

                @Override
                public void exit(Uri uri) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LoadingPanel.instance.showMsg(String.format("finished processing %s...", node.getTitle()));
                        }
                    });
                }
            }, new ContentProviderHandlerEx.XDExport.OnXDProgressCallback() {
                @Override
                public void reportTotalChange(final int total) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LoadingPanel.instance.setProgress1Max(total);
                            // LoadingPanel.instance.showMsg(String.format("processing %s...", node.title));
                        }
                    });
                }

                @Override
                public void reportValueChange(final int value) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LoadingPanel.instance.setProgress1Value(value);
                        }
                    });
                }
            }, new ContentProviderHandlerEx.XDExport.OnXDDataReadyCallback() {
                @Override
                public void data(final String data) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final MutableVar<Integer> completionCounter
                                    = new MutableVar<>(clients.size());

                            ContentProviderHandlerEx.XDExport.write(file, data, new GenericCallback2() {
                                @Override
                                public void event() {

                                    LoadingPanel.instance.showMsg("compressing files...");

                                    new Utils.DoAsyncEx2<>(null, new OnDataCallback<String>() {
                                        @Override
                                        public void data(final String file) {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    for (int i = 0, size = clients.size(); i < size; i++) {
                                                        final GenericFs.Base cli = clients.get(i);
                                                        LoadingPanel.instance.showMsg(String.format("uploading %s to: %s...", node.getTitle(), cli.getClass().getSimpleName()));

                                                        cli.connect(new GenericFs.ReadyCallback() {
                                                            @Override
                                                            public void status(boolean success) {
                                                                if (BackupHelper.canceled)
                                                                    return;

                                                                if (success) {
                                                                    cli.upload(file, file.replace(AppEx.self.getTemporaryStorageDir().getPath() + "/", ""), new GenericFs.ReadyCallback() {
                                                                        @Override
                                                                        public void status(final boolean success) { // TODO:
                                                                            activity.runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    if (success) {
                                                                                        String name = cli.getClass().getSimpleName().toLowerCase();
                                                                                        PrefsActivity.setPrefLong(String.format("%s_last_backup", name), System.currentTimeMillis());

                                                                                        PrefsActivity.getPrefMgr().edit().putString(
                                                                                                String.format("%s_last_backup", node.getTitle().toLowerCase()),
                                                                                                String.format("%d|%s", System.currentTimeMillis(), name)).apply();

                                                                                        AppLog.instance.add(String.format("finished backing up %s to: %s",
                                                                                                node.getTitle(), name), LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.INFO, true);
                                                                                    }

                                                                                    LoadingPanel.instance.incrementProgress2By(1);

                                                                                    if ((--completionCounter.value) == 0) {
                                                                                        callback.event();
                                                                                    }
                                                                                }
                                                                            });
                                                                        }
                                                                    });
                                                                } else {
                                                                    AppLog.instance.add(String.format("could not connect to %s", cli.getClass().getSimpleName()), LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.INFO, true);
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }, null).run(new Callable<String>() {
                                        @Override
                                        public String call() throws Exception {
                                            return EncryptAndCompress.Helper.
                                                    CompressFiles(u, files, AppEx.self.getTemporaryStorageDir(), comment, null);
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    @Override
    boolean onBackPressed() {
        if (LoadingPanel.instance.getVisibility()) {
            LoadingPanel.instance.setVisibility(false);
            return true;
        }

        if (backupDataSourcesAdapter.getItemCount() > 0) {
            int checkItemsCount = backupDataSourcesAdapter.getItemsCountMatchingPredicate(new DataFilterCallback<BackupRvEntry>() {
                @Override
                public boolean include(BackupRvEntry item) {
                    return item.isChecked();
                }
            }, new DataFilterActionCallback<BackupRvEntry>() {
                @Override
                public void run(BackupRvEntry item, int position) {
                    item.setChecked(false);
                }
            }, null, new DataFilterCompleteCallback<BackupRvEntry>() {
                @Override
                public void run(List<Integer> positions, List<BackupRvEntry> result) {
                    backupDataSourcesAdapter.notifyDataSetChanged();
                }
            });

            if ((checkItemsCount > 0)) {
                return true;
            }
        }

        if (backupLocationsRvAdapter.getItemCount() > 0) {
            int checkItemsCount = backupLocationsRvAdapter.getItemsCountMatchingPredicate(new DataFilterCallback<StorageGroupListViewItemWrapper>() {
                @Override
                public boolean include(StorageGroupListViewItemWrapper item) {
                    return item.checked;
                }
            }, new DataFilterActionCallback<StorageGroupListViewItemWrapper>() {
                @Override
                public void run(StorageGroupListViewItemWrapper item, int position) {
                    item.checked = false;
                }
            }, null, new DataFilterCompleteCallback<StorageGroupListViewItemWrapper>() {
                @Override
                public void run(List<Integer> positions, List<StorageGroupListViewItemWrapper> result) {
                    backupLocationsRvAdapter.notifyDataSetChanged();
                }
            });

            if ((checkItemsCount > 0)) {
                return true;
            }
        }

        return super.onBackPressed();
    }

    @Override
    public void onPause() {
        backupLocationsRvAdapter.unregisterAdapterDataObserver(backupLocationsRvAdapterDataObserver);
        backupDataSourcesAdapter.unregisterAdapterDataObserver(backupDataSourcesAdapterDataObserver);

        AppEx.self.fclient.removeConnectionStatusListener("/resume/fclient");
        AppEx.self.gclient.removeConnectionStatusListener("/resume/gclient");
        AppEx.self.dclient.removeConnectionStatusListener("/resume/dclient");

        ((MainActivity) getActivity()).getSidePanelDrawerFragment()
                .setCallback(null);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        this.backupLocationsRvAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateBackupButtonVisibility();
            }
        };
        backupLocationsRvAdapter.registerAdapterDataObserver(backupLocationsRvAdapterDataObserver);

        this.backupDataSourcesAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateBackupButtonVisibility();
            }
        };
        backupDataSourcesAdapter.registerAdapterDataObserver(backupDataSourcesAdapterDataObserver);


        AppEx.self.gclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
            @Override
            public void connection(boolean status) {
                StorageGroupListViewItemWrapper item = backupLocationsRvAdapter.findItemByStorageGroup(
                        StorageGroup.Types.GOOGLE_DRIVE);

                if (item != null) {
                    item.connected = status && Utils.IsNetworkAvailable();
                    backupLocationsRvAdapter.notifyDataSetChanged(); // to update item drawable
                }
            }

            @Override
            public boolean notifyIf() {
                return backupLocationsRvAdapter != null;
            }

            @Override
            public String id() {
                return "/resume/gclient";
            }
        }, true);

        AppEx.self.fclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
            @Override
            public void connection(boolean status) {
                StorageGroupListViewItemWrapper item = backupLocationsRvAdapter.findItemByStorageGroup(
                        StorageGroup.Types.FTP);

                if (item != null) {
                    item.connected = status && Utils.IsNetworkAvailable();
                    backupLocationsRvAdapter.notifyDataSetChanged(); // to update item drawable
                }
            }

            @Override
            public boolean notifyIf() {
                return backupLocationsRvAdapter != null;
            }

            @Override
            public String id() {
                return "/resume/fclient";
            }
        }, true);

        AppEx.self.dclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
            @Override
            public void connection(boolean status) {
                StorageGroupListViewItemWrapper item = backupLocationsRvAdapter.findItemByStorageGroup(
                        StorageGroup.Types.DROP_BOX);

                if (item != null) {
                    item.connected = status && Utils.IsNetworkAvailable();
                    backupLocationsRvAdapter.notifyDataSetChanged(); // to update item drawable
                }
            }

            @Override
            public boolean notifyIf() {
                return backupLocationsRvAdapter != null;
            }

            @Override
            public String id() {
                return "/resume/dclient";
            }
        }, true);


        //  AppEx.self.requestConnectionNotify();

        // backupLocationsRvAdapter.notifyDataSetChanged();

        ((MainActivity) getActivity()).getSidePanelDrawerFragment()
                .setCallback(new SidePanelDrawerFragment.ContentCallback() {
                    @Override
                    public boolean readOnly() {
                        return false;
                    }

                    @Override
                    public boolean visibility() {
                        return true;
                    }

                    @Override
                    public String title() {
                        return "optional comment";
                    }
                });
        // gclient.connect();


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getArguments() != null) {
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(Constants.ARG_SECTION_NUMBER));
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();

        inflater.inflate(R.menu.backup_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_backup:
                backup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO: complete
        MenuItem item = menu.findItem(R.id.action_backup);
        if (item != null) {
            item.setVisible(buttonBackupVisibility);
        }
        super.onPrepareOptionsMenu(menu);
    }

    void updateBackupButtonVisibility() {
        boolean checkedItemsCount = backupLocationsRvAdapter.getCheckedItemsCount() > 0;
        boolean checkedNodesCount = backupDataSourcesAdapter.getCheckedItemsCount() > 0;
        setBackupButtonVisibility(checkedItemsCount && checkedNodesCount);
    }

    void setBackupButtonVisibility(boolean value) {
        buttonBackupVisibility = (value);
        getActivity().invalidateOptionsMenu();
    }
}
