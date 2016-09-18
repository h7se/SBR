package com.qou.h7se.sbr;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by k0de9x on 9/17/2015.
 */

public class RestoreListViewItem extends BaseEntry {
    StorageItem si = null;

    RestoreListViewItem(StorageItem src) {
        this.si = src;
        //if (this.name.type.equals(BackupRestoreSources.RestoreBackupSourceType.LOCAL_ITEM)) {
        //checkVisible = true;
        //}
//        if (this.si.type.equals(StorageItem.ItemType.IN_ZIP_FILE_ITEM)) {
//            // checkVisible = true;
//        }
    }

    private boolean checked;
    private boolean busy;
    private boolean checkBoxVisible;

    public void doWork(final Activity context, final OnRestoreCallBackListener callback) {
        final MutableVar<GenericFs.Base> clle = new
                MutableVar<>(GenericFs.Helper.getNewClientOfType(si.groupType));

        switch (si.type) {
            case LOCAL_GROUP:
            case FTP_GROUP:
            case GOOGLE_GROUP:
            case DROPBOX_GROUP:
            case SDCARD_GROUP: {
                clle.value.list(si.path, false, new GenericFs.DataCallback<GenericFs.Base.FileInfo>() {
                    @Override
                    public void data(List<GenericFs.Base.FileInfo> items, boolean status) {
                        if (status && items != null) {
                            final MutableVar<Integer> counter = new MutableVar<Integer>(0);
                            if(items.size() == 0) {
                            } else {
                                for (int i = 0, size = items.size(); i < size; i++) {
                                    try {

                                        if (!(items.get(i).title.toLowerCase().endsWith(".ignore"))) {
                                            if (items.get(i).isFolder == 1) {
                                                final String fileName = items.get(i).title;
                                                final String fullPath = PathUtils.combine(si.path, fileName);

                                                counter.value +=1;

                                                clle.value.getChildesInfo(fullPath, new GenericFs.DataCallback2<GenericFs.Base.ChildesInfo>() {
                                                    @Override
                                                    public void data(GenericFs.Base.ChildesInfo data) {
                                                        counter.value -=1;

                                                        boolean ignoreOnItemClicks =
                                                                ((data.zipFileCount == 0) && (!(data.hasChildFolders)));

                                                        if (fileName.toLowerCase().endsWith("keep")) {
                                                            ignoreOnItemClicks = true;
                                                        }

                                                        if ((data.hasChildFolders) || (data.zipFileCount > 0)) { // empty folders filter
                                                            Uri[] uris = Utils.MapZipFileNameToSingleUri(fileName);
                                                            //if (uris != null) {
                                                            callback.data(new StorageItem(fileName
                                                                            , null
                                                                            , uris, fullPath
                                                                            , StorageItem.ItemType.DIR, si.groupType)
                                                                            .setProperty(StorageItem.Constants.PropertyNames
                                                                                    .COUNT_ENTRIES, data.zipFileCount)
                                                                            .setProperty(StorageItem.Constants.PropertyNames
                                                                                    .CONTAIN_FOLDERS, data.hasChildFolders)
                                                                            .setProperty(StorageItem.Constants.PropertyNames
                                                                                    .IGNORE_ONITEM_CLICK, ignoreOnItemClicks)
                                                            );
                                                            //}
                                                        }

                                                        if(counter.value == 0) {
                                                            callback.finish("");
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(AppEx.PACKAGE_NAME, e.getMessage());
                                    }
                                }
                            }


                        } else {
                            callback.finish("Error reading file list");
                        }
                    }
                });
            }
            break;

            case DIR: {
                clle.value.list(si.path, true, new GenericFs.DataCallback<GenericFs.Base.FileInfo>() {
                    @Override
                    public void data(List<GenericFs.Base.FileInfo> items, boolean status) {
                        if (status && items != null) {
                            final MutableVar<Integer> counter = new MutableVar<Integer>(0);
                            for (int i = 0, size = items.size(); i < size; i++) {
                                GenericFs.Base.FileInfo fileInfo = items.get(i);
                                final File f = new File(si.path, fileInfo.title);
                                if (fileInfo.isFolder != 1) {
                                    java.sql.Timestamp ts = new Timestamp(0), tstmp;
                                    if (f.getName().toLowerCase().endsWith(".zip")) {
                                        String[] parts = f.getName().replace(".zip", "").split("_");
                                        tstmp = new java.sql.Timestamp(Long.valueOf(parts[1]));
                                        //if(tstmp.after(ts)) {
                                        //ts = tstmp;
                                        //}

                                        try {
                                            Uri[] uris = Utils.MapZipFileNameToSingleUri(f.getName());
                                            if (uris != null) {
                                                final StorageItem storageItem = new StorageItem(parts[0]
                                                        , (tstmp)
                                                        , uris
                                                        , f.getCanonicalPath()
                                                        , StorageItem.ItemType.ZIP_FILE, si.groupType);
                                                storageItem.setProperty(StorageItem.Constants.PropertyNames.FILE_SIZE, fileInfo.size);

                                                if (!(si.isCloudFile())) {
                                                    storageItem.setProperty(StorageItem.Constants.PropertyNames.ZIP_FILE_PROTECTED,
                                                            EncryptAndCompress.isPasswordProtected(f.getCanonicalPath()));

                                                    EncryptAndCompress.getZipFileComment(f, new OnDataCallback<String>() {
                                                        @Override
                                                        public void data(String data) {
                                                            storageItem.setProperty(StorageItem.Constants.PropertyNames.ZIP_FILE_COMMENT,
                                                                    data);

                                                            context.sendBroadcast(new
                                                                    Intent(Constants.ACTION_STORAGE_ITEM_PROPERTY_CHANGED));
                                                        }
                                                    });
                                                }

                                                callback.data(storageItem);
                                            }
                                        } catch (IOException e) {
                                            Utils.LogException(e);
                                        }
                                    }
                                } else {
                                    if (!(fileInfo.title.toLowerCase().endsWith(".ignore"))) {
                                        try {
                                            final String fileName = fileInfo.title;
                                            final String fullPath = PathUtils.combine(si.path, fileInfo.title);

                                            counter.value += 1;
                                            clle.value.getChildesInfo(fullPath, new GenericFs.DataCallback2<GenericFs.Base.ChildesInfo>() {
                                                @Override
                                                public void data(GenericFs.Base.ChildesInfo data) {
                                                    counter.value -= 1;

                                                    boolean ignoreOnItemClicks =
                                                            ((data.zipFileCount == 0) && (!(data.hasChildFolders)));
                                                    Uri[] uris = Utils.MapZipFileNameToSingleUri(fileName);
                                                    //if (uris != null) {
                                                    callback.data(new StorageItem(fileName
                                                                    , null
                                                                    , uris
                                                                    , fullPath
                                                                    , StorageItem.ItemType.DIR, si.groupType)
                                                                    .setProperty(StorageItem.Constants.PropertyNames
                                                                            .COUNT_ENTRIES, data.zipFileCount)
                                                                    .setProperty(StorageItem.Constants.PropertyNames
                                                                            .CONTAIN_FOLDERS, data.hasChildFolders)
                                                                    .setProperty(StorageItem.Constants.PropertyNames
                                                                            .IGNORE_ONITEM_CLICK, ignoreOnItemClicks)
                                                    );
                                                    //}

                                                    if(counter.value == 0/* && (i == size - 1*/) {
                                                        callback.finish("");
                                                    }
                                                }
                                            });
                                        } catch (Exception ex) {
                                            Utils.LogException(ex);
                                        }
                                    }
                                }
                            }

                            if(counter.value == 0) {
                                callback.finish("");
                            }
                        } else {
                            callback.finish("Error reading file list");
                        }
                    }
                });
            }
            break;
            case ZIP_FILE: {
                try {
                    final String tempFile = new File(
                            AppEx.self.getTemporaryStorageDir(), new File(si.path).getName()).getCanonicalPath();

                    if (si.groupType.equals(StorageGroup.Types.LOCAL) ||
                            si.groupType.equals(StorageGroup.Types.SDCARD)
                            ) {
                        if (new File(si.path).length() > 3145728) {  // 3 mb
                            LoadingPanel.instance.setVisibility(true, "downloading / extracting files...");
                        }
                    } else {
                        LoadingPanel.instance.setVisibility(true, "downloading / extracting files...");
                    }

                    clle.value.download(PathUtils.getCanonicalPath(si.path, true), tempFile, new GenericFs.ReadyCallback() {
                        @Override
                        public void status(boolean value) {
                            if (value) {
                                EncryptAndCompress.getAllFilesListInZipFile(tempFile, new OnDataCallback2<List<String>>() {
                                    @Override
                                    public void data(List<String> files) {
                                        if (files != null && files.size() > 0) {
                                            for (int i = 0; i < files.size(); i++) {
                                                if (files.get(i).toLowerCase().endsWith(".xml")) {
                                                    callback.data(new StorageItem(files.get(i), null,
                                                                    si.uris, si.path,
                                                                    StorageItem.ItemType.IN_ZIP_FILE_ITEM, si.groupType)
                                                    );
                                                }
                                            }
                                        }

                                        (new File(tempFile)
                                        ).delete();

                                        callback.finish("");
                                        LoadingPanel.instance.setVisibility(false);
                                    }
                                });
                            } else {
                                LoadingPanel.instance.setVisibility(false);
                            }
                        }
                    });

                } catch (IOException e) {
                    callback.finish("Error reading file content");
                    Utils.LogException(e);
                }
            }
            break;

            case ITEM: {
                callback.data(new StorageItem(null, null, null, null,
                        StorageItem.ItemType.NONE, null));
            }
            break;
        }
    }


    public boolean getChecked() {
        return this.checked;
    }

    public void setChecked(boolean value) {
        if (si.containsProperty(StorageItem.Constants.PropertyNames.NOT_CHECKABLE)) {
            if (si.<Boolean>getProperty(StorageItem.Constants.PropertyNames.NOT_CHECKABLE)) {
                return;
            }
        }
        this.checked = value;
    }

    public void toggleChecked() {
        this.setChecked(!(this.getChecked()));
    }

    public boolean getBusy() {
        return this.busy;
    }

    public void setBusy(boolean value) {
        this.busy = value;
    }

    public boolean getCheckBoxVisible() {
        return this.checkBoxVisible;
    }

    public void setCheckBoxVisible(boolean value) {
        this.checkBoxVisible = value;
    }
}
