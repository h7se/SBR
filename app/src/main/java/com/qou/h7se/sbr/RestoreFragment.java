package com.qou.h7se.sbr;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;

/**
 * Created by k0de9x on 9/22/2015.
 */
public class RestoreFragment extends BaseFragmentEx {

    ListView lvRestore;
    RestoreListViewItemsAdapter adapter;
    DataSetObserver adapterDataSetObserver;

    TextView textView;

    RelativeLayout relativeLayoutLoading, relativeLayoutData;

    LayoutInflater layoutInflater;
    boolean buttonRestoreVisibility = false;

    // LinkedList<Tuple<Integer, List<RestoreListViewItem>>> backLinkedList;
    BroadcastReceiver receiver;

    public static RestoreFragment newInstance(int sectionNumber) {
        RestoreFragment fragment = new RestoreFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public RestoreFragment() {
    }

    private int click_flag = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_restore, container, false);

        textView = (TextView) rootView.findViewById(R.id.textView);

        relativeLayoutLoading = (RelativeLayout) rootView.findViewById(R.id.relativeLayoutLoading);
        relativeLayoutData = (RelativeLayout) rootView.findViewById(R.id.relativeLayoutData);

        layoutInflater = (LayoutInflater) AppEx.self.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // int ht= ViewGroup.LayoutParams.WRAP_CONTENT;
        // int wt=ViewGroup.LayoutParams.FILL_PARENT;

        ViewGroup parent = (ViewGroup) rootView.findViewById(R.id.relativeLayoutData);


        layoutInflater.inflate(R.layout.activity_backup_elements_listview, parent);
        lvRestore = (ListView) rootView.findViewById(R.id.listView);

        final ArrayList<RestoreListViewItem> entries = new ArrayList<>();
        for (StorageItem item : AppEx.self.backupRestoreSources) {
            entries.add(new RestoreListViewItem(item));
        }

        adapter = new RestoreListViewItemsAdapter(getActivity(), entries);

        lvRestore = (ListView) rootView.findViewById(R.id.listView);
        lvRestore.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View itemClicked, final int index, long id) {
                {
                    click_flag = -1;
                    final RestoreListViewItem entry = adapter.getItem(index);

                    //if (entry.si.groupType.equals(StorageGroup.Types.SDCARD) || entry.si.groupType.equals(StorageGroup.Types.LOCAL)) {
                        if (entry.si.containsProperty(StorageItem.Constants.PropertyNames.IGNORE_ONITEM_CLICK)) {
                            if (entry.si.<Boolean>getProperty(StorageItem.Constants.PropertyNames.IGNORE_ONITEM_CLICK)) {
                                return;
                            }
                        }
                    //}

                    int count = adapter.getItemsCountMatchingPredicate(new DataFilterCallback<RestoreListViewItem>() {
                        @Override
                        public boolean include(RestoreListViewItem item) {
                            return (item.getChecked()) && (item.si.type.equals(StorageItem.ItemType.ZIP_FILE) || item.si.type.equals(StorageItem.ItemType.DIR));
                        }
                    });
                    if (count > 0) { // update action bar/check status with single click
                        entry.toggleChecked();
                        adapter.notifyDataSetChanged();
                        //  getActivity().invalidateOptionsMenu();

                        return;
                    }

                    if (entry.si.type.equals(StorageItem.ItemType.FTP_GROUP)) {
                        if (!AppEx.self.fclient.isConnected()) {
                            click_flag = 1;
                            AppEx.self.fclient.connect(new GenericCallback4<Boolean>() {
                                @Override
                                public void event(Boolean status) {
                                    if (status) {
                                        if(click_flag == 1) {
                                            lvRestore.performItemClick(lvRestore, index,
                                                    lvRestore.getItemIdAtPosition(index));
                                        }
                                    }
//
//                                    if (status) {
//                                        AppLog.instance.add("successfully connected", LogsEntry.LOG_SOURCE.FTP, LogsEntry.TYPE.ERROR, true);
//                                    } else {
//                                        AppLog.instance.add("connection failure", LogsEntry.LOG_SOURCE.FTP, LogsEntry.TYPE.INFO, true);
//                                    }
                                }
                            });
                            AppLog.instance.add("connecting...", LogsEntry.LOG_SOURCE.FTP, LogsEntry.TYPE.INFO, true);
                            return;
                        }
                    } else if (entry.si.type.equals(StorageItem.ItemType.GOOGLE_GROUP)) {
                        if (!AppEx.self.gclient.isConnected()) {
                            click_flag = 2;
                            AppEx.self.gclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
                                @Override
                                public void connection(boolean status) {
                                    if (status) {
                                        if(click_flag == 2) {
                                            lvRestore.performItemClick(lvRestore, index,
                                                    lvRestore.getItemIdAtPosition(index));
                                        }
                                    }
                                    AppEx.self.gclient.removeConnectionStatusListener(this.id());
                                }

                                @Override
                                public String id() {
                                    return "temp/restore/gclient";
                                }
                            }, false);

                            AppEx.self.gclient.connect(false);
                            AppLog.instance.add("connecting...", LogsEntry.LOG_SOURCE.GDRIVE, LogsEntry.TYPE.INFO, true);
                            return;
                        }
                    } else if (entry.si.type.equals(StorageItem.ItemType.DROPBOX_GROUP)) {
                        if (!AppEx.self.dclient.isLinked()) {
                            click_flag = 3;
                            AppEx.self.dclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
                                @Override
                                public void connection(boolean status) {
                                    if (status) {
                                        if(click_flag == 3) {
                                            lvRestore.performItemClick(lvRestore, index,
                                                    lvRestore.getItemIdAtPosition(index));
                                        }
                                    }
                                    AppEx.self.dclient.removeConnectionStatusListener(this.id());
                                }

                                @Override
                                public String id() {
                                    return "temp/restore/dclient";
                                }
                            }, false);

                            //AppEx.self.dclient.init();
                            AppEx.self.dclient.login();
                            AppLog.instance.add("connecting...",
                                    LogsEntry.LOG_SOURCE.DBOX, LogsEntry.TYPE.INFO, true);
                            return;
                        }
                    } else {
                        if (entry.si.containsProperty(StorageItem.Constants.PropertyNames.ZIP_FILE_COMMENT)) {
                            final String comment = entry.si.<String>getProperty(
                                    StorageItem.Constants.PropertyNames.ZIP_FILE_COMMENT);
                            if (comment != null) {
                                final SidePanelDrawerFragment sidePanelDrawerFragment =
                                        ((MainActivity) getActivity()).getSidePanelDrawerFragment();
                                sidePanelDrawerFragment.setScheduledToOpen(true);
                                lvRestore.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(sidePanelDrawerFragment.getCallback().visibility()) {
                                            sidePanelDrawerFragment.setText(comment);
                                        } else {
                                            lvRestore.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if(sidePanelDrawerFragment.getCallback().visibility()) {
                                                        sidePanelDrawerFragment.setText(comment);
                                                    }
                                                }
                                            }, 150);
                                        }
                                    }
                                }, 150);
                            }
                        }
                    }

                    if ((entry.si.type.equals(StorageItem.ItemType.LOCAL_GROUP)) ||
                            (entry.si.type.equals(StorageItem.ItemType.FTP_GROUP)) ||
                            (entry.si.type.equals(StorageItem.ItemType.GOOGLE_GROUP)) ||
                            (entry.si.type.equals(StorageItem.ItemType.DROPBOX_GROUP)) ||
                            (entry.si.type.equals(StorageItem.ItemType.SDCARD_GROUP)) ||
                            (entry.si.type.equals(StorageItem.ItemType.DIR)) ||
                            (entry.si.type.equals(StorageItem.ItemType.ZIP_FILE))) {

                        //showLoadingPanel();

                        final ArrayList<RestoreListViewItem> tmp = new ArrayList<>();

                        entry.doWork(getActivity(), new OnRestoreCallBackListener() {
                            @Override
                            public void data(final StorageItem item) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tmp.add(new RestoreListViewItem(item));
                                    }
                                });
                            }

                            @Override
                            public void finish(String text) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ListViewRollBack.instance.push(adapter.getOriginalData());
                                        ListViewRollBack.instance.push(lvRestore.getLastVisiblePosition());

                                        adapter.clearData(true);
                                        adapter.setData(tmp, true);
                                        //showDataPanel();
                                    }
                                });
                            }

                            @Override
                            public void message(String text) {
                                AppLog.instance.add(text, LogsEntry.LOG_SOURCE.MISC, LogsEntry.TYPE.INFO);
                            }
                        });

                    } else if ((entry.si.type.equals(StorageItem.ItemType.IN_ZIP_FILE_ITEM))) {
                        adapter.getItem(index).toggleChecked();
                        adapter.notifyDataSetChanged();

                    } else if ((entry.si.type.equals(StorageItem.ItemType.ITEM))) {
                        adapter.getItem(index).toggleChecked();
                        adapter.notifyDataSetChanged();

                    }
                }

                try {
                    // TODO: filter based on item type ?, * imgbtn visible only in last page
                    adapter.getItemsCountMatchingPredicate(new DataFilterCallback<RestoreListViewItem>() {
                        @Override
                        public boolean include(RestoreListViewItem item) {
                            return item.getChecked();
                        }
                    }, null, null, new DataFilterCompleteCallback<RestoreListViewItem>() {
                        @Override
                        public void run(List<Integer> positions, List<RestoreListViewItem> result) {
                            setRestoreButtonVisibility(result.size() > 0);
                        }
                    });
                } catch(Exception e) { //TODO: throws null exception
                }
            }
        });

        lvRestore.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // inflater2.inflate(R.menu.mod_listview, menu2);
                RestoreListViewItem entry = adapter.getItem(i);
                if (entry.si.type.equals(StorageItem.ItemType.ZIP_FILE)) {
                    // TODO:  || entry.si.type.equals(StorageItem.ItemType.DIR
                    entry.toggleChecked();

                    adapter.notifyDataSetChanged();
                    // getActivity().invalidateOptionsMenu();
                    return true;
                }
                return false;
            }
        });


        lvRestore.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        setRestoreButtonVisibility(false);

        View emptyView = inflater.inflate(R.layout.empty_view, null);
        ((ViewGroup) lvRestore.getParent()).addView(emptyView);
        lvRestore.setEmptyView(emptyView);

        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        if (bundle.containsKey("navigate.to.file")) {
            String path = bundle.getString("navigate.to.file");
            if (!(TextUtils.isEmpty(path))) {
                navigateToSdCardPath(new File(path.replace("file://", "")));
            }
        }

        return rootView;
    }

    public void onButtonRestoreClick() {
        List<String> filter = new ArrayList<>(); //TODO: <<<<
        List<RestoreListViewItem> entries = adapter.getData();
        final String tmpDir = Utils.generateRandomFilePath("tmp", "/",
                AppEx.self.getTemporaryStorageDir().getAbsolutePath());

        adapter.getItemsMatchingPredicate(new DataFilterCallback<RestoreListViewItem>() {
            @Override
            public boolean include(RestoreListViewItem item) {
                return item.getChecked() && item.si.type.equals(StorageItem.ItemType.IN_ZIP_FILE_ITEM);
            }
        }, new DataFilterActionCallback<RestoreListViewItem>() {
            @Override
            public void run(final RestoreListViewItem item, int position) {
                item.setChecked(false);

                final com.qou.h7se.sbr.restore.HelperEx.ProgressCallback2 progressCallback =
                        new com.qou.h7se.sbr.restore.HelperEx.ProgressCallback2() {
                            @Override
                            public void begin() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LoadingPanel.instance.setVisibility(true);
                                    }
                                });
                            }

                            @Override
                            public void end() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LoadingPanel.instance.setVisibility(false);
                                    }
                                });
                            }

                            @Override
                            public void reportTotalChange(final int total) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LoadingPanel.instance.setProgress1Max(total);
                                    }
                                });
                            }

                            @Override
                            public void reportValueChange(final int value) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LoadingPanel.instance.incrementProgress1By(value);
                                    }
                                });
                            }

                            @Override
                            public void message(final String text) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        LoadingPanel.instance.showMsg(text);
                                    }
                                });
                            }
                        };

                GenericFs.Base client = GenericFs.Helper.
                        getNewClientOfType(item.si.groupType);

                final GenericCallback4<Boolean> onComplete = new GenericCallback4<Boolean>() {
                    @Override
                    public void event(Boolean value) {
                        if(value) {
                            if(PrefsActivity.prefs.contains("last_restore_1")) {
                                if(PrefsActivity.prefs.contains("last_restore_2")) {
                                    PrefsActivity.prefs.edit().putString("last_restore_1",String.format("%d|%s", System.currentTimeMillis(), item.si.groupType.name())).apply();
                                } else {
                                    PrefsActivity.prefs.edit().putString("last_restore_2",String.format("%d|%s", System.currentTimeMillis(), item.si.groupType.name())).apply();
                                }
                            } else {
                                PrefsActivity.prefs.edit().putString("last_restore_1",String.format("%d|%s", System.currentTimeMillis(), item.si.groupType.name())).apply();
                            }

                            AppLog.instance.add("Task completed",
                                    LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.INFO, true);
                        }

                        progressCallback.message("cleaning temp. files...");
                        try {
                            FileUtils.cleanDirectory(AppEx.self.getTemporaryStorageDir());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        progressCallback.end();
                    }
                };

                // TODO: check if it's ok to download without calling replaceFirst
               // final File localFile = new File(AppEx.self.getTemporaryStorageDir(), item.si.path);
                final File localFile = new File(
                        AppEx.self.getTemporaryStorageDir(), new File(item.si.path).getName());

                client.download(item.si.path.replaceFirst("^[/]", ""), localFile.getAbsolutePath(), new GenericFs.ReadyCallback() {
                    @Override
                    public void status(boolean success) {
                        if (success) {
                            new Utils.DoAsyncEx2<Void>(null, null, null).run(new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    progressCallback.begin();
                                    progressCallback.message("extracting file...");
                                    try {
                                        EncryptAndCompress.ExtractAllFiles(localFile.getCanonicalPath(), tmpDir, true);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        progressCallback.message("error extracting file...");
                                        onComplete.event(false);
                                        return null;
                                    }

                                    for (Uri u : item.si.uris) {
                                        String fileName = Utils.MapUriToXmlFile(u);
                                        if (fileName == null) {
                                            progressCallback.message("Not recognized, " + u);
                                            continue;
                                        }

                                        ContentProviderHandlerEx.XDImport.XDItem xdItem = ContentProviderHandlerEx.XDImport.run(
                                                new java.io.File(tmpDir, fileName));

                                        if (xdItem != null) {
                                            if (u.equals(Uris.CONTACTS)) {
                                                new com.qou.h7se.sbr.restore.Contacts(getActivity()).run(xdItem, onComplete);
                                            } else if (u.equals(Uris.IMAGES)) {
                                                new com.qou.h7se.sbr.restore.
                                                        Media(getActivity()).run(xdItem, tmpDir, progressCallback, onComplete);
                                            } else if (u.equals(Uris.AUDIO)) {
                                                new com.qou.h7se.sbr.restore.
                                                        Media(getActivity()).run(xdItem, tmpDir, progressCallback, onComplete);
                                            } else if (u.equals(Uris.VIDEO)) {
                                                new com.qou.h7se.sbr.restore.
                                                        Media(getActivity()).run(xdItem, tmpDir, progressCallback, onComplete);
                                            } else if (u.equals(Uris.LOGS)) {
                                                new com.qou.h7se.sbr.restore.Logs(getActivity()).run(xdItem, onComplete);
                                            } else if (u.equals(Uris.BROWSER_SEARCHES)) {
                                                new com.qou.h7se.sbr.restore.Searches(getActivity()).run(xdItem, onComplete);
                                            } else if (u.equals(Uris.BROWSER_BOOKMARKS)) {
                                                new com.qou.h7se.sbr.restore.Bookmarks(getActivity()).run(xdItem, onComplete);
                                            } else if (u.equals(Uris.SMS)) {
                                                new com.qou.h7se.sbr.restore.Sms(getActivity()).run(xdItem, onComplete);
                                            } else if (u.equals(Uris.MMS)) {
                                                new com.qou.h7se.sbr.restore.Mms(getActivity()).run(xdItem, onComplete);
                                            } else if (u.equals(Uris.SAMSUNG_ALARMS)) {
                                                new com.qou.h7se.sbr.restore.Alarms(getActivity()).run(xdItem, onComplete);
                                            } else if (u.equals(Uris.CALENDARS_EVENTS)) {
                                                new com.qou.h7se.sbr.restore.Events(getActivity()).run(xdItem, onComplete);
                                            } else if (u.equals(Uris.CALENDARS_REMINDERS)) {
                                                // new com.qou.h7se.sbr.restore.Reminders(getActivity()).run(xdItem);
                                            } else {
                                                throw new Exception(u.toString());
                                            }
                                        }
                                    }

                                    return null;
                                }
                            });
                        }
                    }
                });
            }
        }, null, new DataFilterCompleteCallback<RestoreListViewItem>() {
            @Override
            public void run(List<Integer> positions, List<RestoreListViewItem> result) {
                adapter.notifyDataSetChanged();
                setRestoreButtonVisibility(false);

//                AppLog.instance.add("all tasks completed.",
//                        LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.INFO, true);
            }
        }, false);
    }

    @Override
    boolean onBackPressed() {
        RelativeLayout rlyo = (RelativeLayout)
                getActivity().findViewById(R.id.relativeLayoutPanel);

        if (rlyo != null) {
            if (rlyo.getVisibility() == View.VISIBLE) {
                rlyo.setVisibility(View.GONE);
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.vanish);

                if (animation != null) {
                    rlyo.startAnimation(animation);
                }

                return true;
            }
        }

        if (LoadingPanel.instance.getVisibility()) {
            LoadingPanel.instance.setVisibility(false);
            return true;
        }

        int checkedItemsCount = adapter.getItemsCountMatchingPredicate(new DataFilterCallback<RestoreListViewItem>() {
            @Override
            public boolean include(RestoreListViewItem item) {
                return (item.getChecked());
            }
        }, new DataFilterActionCallback<RestoreListViewItem>() {
            @Override
            public void run(RestoreListViewItem item, int position) {
                item.setChecked(false);
            }
        }, null, new DataFilterCompleteCallback<RestoreListViewItem>() {
            @Override
            public void run(List<Integer> positions, List<RestoreListViewItem> result) {
                adapter.notifyDataSetChanged();
                // getActivity().invalidateOptionsMenu();
            }
        });

        if (!(ListViewRollBack.instance.isEmpty())) {
            if (checkedItemsCount == 0) {
                adapter.setData(ListViewRollBack.instance.popData(), false);
                int position = ListViewRollBack.instance.popPosition();
                lvRestore.smoothScrollToPosition(position > 0 ? position - 1 : 0);
                adapter.notifyDataSetChanged();
                // getActivity().invalidateOptionsMenu();
                // showDataPanel();
            }
            return true;
        }

        return super.onBackPressed();
    }

    @Override
    public void onPause() {
        adapter.unregisterDataSetObserver(adapterDataSetObserver);

        getActivity().unregisterReceiver(receiver);

        ((MainActivity) getActivity()).
                getSidePanelDrawerFragment().setCallback(null);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        adapterDataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateRestoreButtonVisibility();
            }
        };
        adapter.registerDataSetObserver(adapterDataSetObserver);

        ((MainActivity) getActivity()).
                getSidePanelDrawerFragment().setCallback(new SidePanelDrawerFragment.ContentCallback() {
            @Override
            public boolean readOnly() {
                return true;
            }

            @Override
            public boolean visibility() {
                return (adapter.getCount() > 0) &&
                        (adapter.getItem(0).si.type.equals(StorageItem.ItemType.IN_ZIP_FILE_ITEM));
            }

            @Override
            public String title() {
                return "file comment";
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_RESTORE_LIST_NAVIGATE);
        filter.addAction(Constants.NOTIFICATION_NOT_RECOGNIZED_BACKUP_FILE);
        filter.addAction(Constants.ACTION_STORAGE_ITEM_PROPERTY_CHANGED);

        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Constants.ACTION_RESTORE_LIST_NAVIGATE)) {
                    if (intent.hasExtra("path")) {
                        navigateToLocalPath(new File(intent.getStringExtra("path")));
                    }
                } else if (action.equals(Constants.NOTIFICATION_NOT_RECOGNIZED_BACKUP_FILE)) {
                    Utils.snackbar(getView(), "File is not a valid backup file.", true, true).show();
                } else if (action.equals(Constants.ACTION_STORAGE_ITEM_PROPERTY_CHANGED)) {
                    adapter.notifyDataSetChanged();
                }
            }
        };

        getActivity().registerReceiver(receiver, filter);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(Constants.ARG_SECTION_NUMBER));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.restore_fragment_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_restore:
                onButtonRestoreClick();
                return true;

            case R.id.action_delete_item: {
                final AsyncTask<List<RestoreListViewItem>, RestoreListViewItem, Boolean> task =
                        (new AsyncTask<List<RestoreListViewItem>, RestoreListViewItem, Boolean>() {
                            @Override
                            protected void onProgressUpdate(final RestoreListViewItem... values) {
                                adapter.removeItemWithAnimation(values[0]); // values from publishProgress
                            }

                            @Override
                            protected void onPostExecute(Boolean aBoolean) {
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            protected Boolean doInBackground(List<RestoreListViewItem>... lists) {
                                List<RestoreListViewItem> tmp = lists[0];
                                for (final RestoreListViewItem item : tmp) {
                                    if (!(item.si.containsProperty(StorageItem.Constants.PropertyNames.NO_DELETE))) {
                                        if (item.si.type.equals(StorageItem.ItemType.DIR)) {
                                            // Utils.LogException("todo: delete dir");
                                            // DELETE DIR
                                        } else if (item.si.type.equals(StorageItem.ItemType.ZIP_FILE)) {
                                            // Utils.LogException("todo: delete file");
                                            final GenericFs.Base client =  GenericFs.
                                                    Helper.getNewClientOfType(item.si.groupType);
                                            if(client != null) {
                                                client.connect(new GenericFs.ReadyCallback() {
                                                    @Override
                                                    public void status(boolean success) {
                                                        if (success) {
                                                            client.delete(item.si.path, new GenericFs.ReadyCallback() {
                                                                @Override
                                                                public void status(boolean success) {
                                                                    if(success) {
                                                                        getActivity().runOnUiThread(new Runnable() {
                                                                            @Override
                                                                            public void run() {
                                                                                publishProgress(item);
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                                return null;
                            }
                        });

                adapter.getItemsMatchingPredicate(new DataFilterCallback<RestoreListViewItem>() {
                    @Override
                    public boolean include(RestoreListViewItem item) {
                        return (item.getChecked()) && (item.si.type.equals(StorageItem.ItemType.ZIP_FILE) || item.si.type.equals(StorageItem.ItemType.DIR));
                    }
                }, null, null, new DataFilterCompleteCallback<RestoreListViewItem>() {
                    @Override
                    public void run(final List<Integer> positions, final List<RestoreListViewItem> result) {
                        if (result.get(0).si.type.equals(StorageItem.ItemType.DIR)) {
                            Utils.makeMsgBox(getActivity(), "Warning",
                                    "You are about to delete stored backup directory and all it's contents, continue ?",
                                    false, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            task.execute(result);
                                        }
                                    }, null, null).show();
                        } else if (result.get(0).si.type.equals(StorageItem.ItemType.ZIP_FILE)) {
                            Utils.makeMsgBox(getActivity(), "Warning",
                                    "You are about to delete stored backup file, continue ?",
                                    false, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
//                                            for(final int i : positions) {
//                                                RestoreListViewItemsAdapter.zoomOutDown(lvRestore.getChildAt(i), new GenericCallback2() {
//                                                    @Override
//                                                    public void event() {
//                                                        adapter.removeItem(adapter.getItem(i), true);
//                                                    }
//                                                }).start();
//                                            }
                                            task.execute(result);
                                        }
                                    }, null, null).show();
                        }
                    }
                });
                return true;
            }

            case R.id.action_share: {
                adapter.getItemsMatchingPredicate(new DataFilterCallback<RestoreListViewItem>() {
                    @Override
                    public boolean include(RestoreListViewItem item) {
                        return (item.getChecked()) && (item.si.type.equals(StorageItem.ItemType.ZIP_FILE));
                    }
                }, null, null, new DataFilterCompleteCallback<RestoreListViewItem>() {
                    @Override
                    public void run(List<Integer> positions, List<RestoreListViewItem> result) {
                        {
                            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                            intent.setType("application/zip");
                            ArrayList<Uri> uris = new ArrayList<>();
                            for (RestoreListViewItem item : result) {
                                uris.add(Uri.fromFile(new File(item.si.path)));
                            }
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            startActivity(Intent.createChooser(intent, String.format("Backup files (count: %d)", result.size())));
                        }
                        updateRestoreButtonVisibility();
                    }
                });
                return true;
            }

            case R.id.action_check_all:
            case R.id.action_check_none: {
                adapter.getItemsMatchingPredicate(new DataFilterCallback<RestoreListViewItem>() {
                    @Override
                    public boolean include(RestoreListViewItem item) {
                        return (item.si.type.equals(StorageItem.ItemType.ZIP_FILE));
                    }
                }, new DataFilterActionCallback<RestoreListViewItem>() {
                    @Override
                    public void run(RestoreListViewItem item, int position) {
                        item.setChecked(menuItem.getItemId() == R.id.action_check_all);
                    }
                }, null, new DataFilterCompleteCallback<RestoreListViewItem>() {
                    @Override
                    public void run(List<Integer> positions, List<RestoreListViewItem> result) {
                        adapter.notifyDataSetChanged();
                        // getActivity().invalidateOptionsMenu();
                    }
                });
                return true;
            }
//            case R.id.action_hide_empty_folders: {
//                toggleMenuItem(menuItem,
//                        R.drawable.ic_view_day_white_24dp, R.drawable.ic_view_day_black_24dp);
//
//                RestoreListViewItemsAdapter.allowEmptyFolders = menuItem.isChecked();
////
//
//
////                if(!(adapter.dataFilters.containsKey("hideempty"))) {
////                    adapter.dataFilters.put("hideempty", new CustomBaseAdapter.DataFilterWithState<RestoreListViewItem>() {
////                        @Override
////                        boolean include(RestoreListViewItem item) {
////                            return !(item.hidden);
////                        }
////                    });
////
////                    adapter.dataFilters.get("hideempty").bindTo = new PredicateIsEnabledEx<RestoreListViewItem>() {
////                        @Override
////                        public boolean enabled() {
////                            final boolean insideSdCardSection = adapter.hasItemsMatchPredicate(new DataFilterCallback<RestoreListViewItem>() {
////                                @Override
////                                public boolean include(RestoreListViewItem item) {
////                                    return (item.si.type.equals(StorageItem.ItemType.DIR) && item.si.groupType.equals(StorageGroup.Types.SDCARD));
////                                }
////                            });
////
////                            return (insideSdCardSection && menuItem.isVisible() && menuItem.isChecked());
////                        }
////                    };
////                }
//
//                //  adapter.notifyDataSetChanged();
//
//                //  Utils.toast("todo");
//                return true;
//            }
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        int count = adapter.getItemsCountMatchingPredicate(new DataFilterCallback<RestoreListViewItem>() {
            @Override
            public boolean include(RestoreListViewItem item) {
                return (item.getChecked()) && (item.si.type.equals(StorageItem.ItemType.ZIP_FILE) || item.si.type.equals(StorageItem.ItemType.DIR));
            }
        });

        final boolean insideSdCardSection = adapter.hasItemsMatchPredicate(new DataFilterCallback<RestoreListViewItem>() {
            @Override
            public boolean include(RestoreListViewItem item) {
                return (item.si.type.equals(StorageItem.ItemType.DIR) && item.si.groupType.equals(StorageGroup.Types.SDCARD));
            }
        });

        // TODO: complete
        menu.findItem(R.id.action_delete_item).setVisible((count > 0));
        ;
        menu.findItem(R.id.action_share).setVisible((count > 0));
        ;
        menu.findItem(R.id.action_menu_check).setVisible((count > 0));
        ;

        menu.findItem(R.id.action_restore).setVisible(buttonRestoreVisibility);

        super.onPrepareOptionsMenu(menu);
    }


    void updateRestoreButtonVisibility() {
        setRestoreButtonVisibility(false);
    }

    void setRestoreButtonVisibility(boolean value) {
        buttonRestoreVisibility = (value);
        getActivity().invalidateOptionsMenu();
    }

    public void navigateToLocalPath(final File f) {
        navigateTo(f, StorageGroup.Types.LOCAL, StorageItem.ItemType.LOCAL_GROUP, false);
    }

    public void navigateToSdCardPath(final File f) {
        navigateTo(f, StorageGroup.Types.SDCARD, StorageItem.ItemType.SDCARD_GROUP, true);
    }

    public void navigateTo(final File f, final StorageGroup.Types itemGroup, final StorageItem.ItemType itemType, final boolean fromSystem) {

        class ItemClicker {
            private int getAnimationDuration() {
                return fromSystem ? 160 : 100;
            }

            private void animate() {
                AnimatorSet set = new AnimatorSet();
                set.playTogether(
                        ObjectAnimator.ofFloat(lvRestore, "alpha", 0.6f, 0, 0.4f, 1)
                );
                set.setDuration(getAnimationDuration());
                set.start();
            }

            public void execute(final int position, final GenericCallback2 onFinish) {
                lvRestore.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // lvRestore.getAdapter().getView(position, null, null).performClick();
                        // lvRestore.performItemClick(null, position, lvRestore.getItemIdAtPosition(position));
                        lvRestore.performItemClick(
                                lvRestore.getChildAt(position), position,
                                lvRestore.getAdapter().getItemId(position));
                        adapter.notifyDataSetChanged();

                        // animate();

                        lvRestore.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onFinish.event();
                            }
                        }, getAnimationDuration());

                    }
                }, 1);
            }
        }

        class ItemMatcher {
            public void execute(final Queue<String> q) {
                final MutableVar<String> p = new MutableVar<>(q.remove());
                int c = adapter.getItemsCountMatchingPredicate(new DataFilterCallback<RestoreListViewItem>() {
                    @Override
                    public boolean include(RestoreListViewItem item) {
                        return (item.si.groupType.equals(itemGroup)) && FilenameUtils.getName(item.si.path).equalsIgnoreCase(p.value);
                    }
                });
                adapter.getItemsMatchingPredicate(new DataFilterCallback<RestoreListViewItem>() {
                    @Override
                    public boolean include(RestoreListViewItem item) {
                        return (item.si.groupType.equals(itemGroup)) && FilenameUtils.getName(item.si.path).equalsIgnoreCase(p.value);
                    }
                }, new DataFilterActionCallback<RestoreListViewItem>() {
                    @Override
                    public void run(RestoreListViewItem item, int position) {
                        new ItemClicker().execute(position, new GenericCallback2() {
                            @Override
                            public void event() {
                                if (!(q.isEmpty())) {
                                    execute(q);
                                } else {
                                    LoadingPanel.instance.setDisabled(false, 1);

                                    lvRestore.setSoundEffectsEnabled(true);

                                    if (fromSystem) {
                                        ListViewRollBack.instance.clear();
                                    }
                                }
                            }
                        });
                    }
                }, null, null, true);
            }
        }

        if (f.exists()) {
            if (f.isDirectory()) {
                if (fromSystem) {
                    return;
                }
            } else {
                if (!(Utils.matches(f.getName(), "^[^_]+_\\d{13}[.]zip$"))) {
                    return;
                }
            }
        } else {
            return;
        }

        LoadingPanel.instance.setDisabled(true, 15000);

        String p;
        if(itemGroup.equals(StorageGroup.Types.LOCAL))  {
            String rootDir = PathUtils.getCanonicalPath(AppEx.self.getCurrentStorageDir(), false);
            p =  PathUtils.getCanonicalPath(f, false).replaceFirst(rootDir + "/", ""); // "^[/]"
        } else if(itemGroup.equals(StorageGroup.Types.SDCARD)) {
            String rootDir = PrefsActivity.getString(PrefsActivity.SDCARD_ROOT_DIRECTORY,
                    PathUtils.getCanonicalPath(GenericFs.SDCard.root, false));
            p =  PathUtils.getCanonicalPath(f, false).replaceFirst(rootDir + "/", ""); // "^[/]"
        } else {
            return;
        }

        lvRestore.setSoundEffectsEnabled(false);

        final Queue<String> q = new ArrayDeque<>(Arrays.asList(p.split("/")));

        while (!ListViewRollBack.instance.isEmpty()) {
            adapter.setData(ListViewRollBack.instance.popData(), false);
        }
        adapter.notifyDataSetChanged();

        adapter.getItemsMatchingPredicate(new DataFilterCallback<RestoreListViewItem>() {
            @Override
            public boolean include(RestoreListViewItem item) {
                return item.si.type.equals(itemType);
            }
        }, new DataFilterActionCallback<RestoreListViewItem>() {
            @Override
            public void run(RestoreListViewItem item, int position) {
                new ItemClicker().execute(position, new GenericCallback2() {
                    @Override
                    public void event() {
                        new ItemMatcher().execute(q);
                    }
                });
            }
        }, null, null, true);
    }
}
