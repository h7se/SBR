package com.qou.h7se.sbr;

import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by k0de9x on 9/17/2015.
 */


public class StorageItem {
    enum ItemType {
        LOCAL_GROUP, DIR, ZIP_FILE, IN_ZIP_FILE_ITEM, ITEM, FTP_GROUP, GOOGLE_GROUP, DROPBOX_GROUP, SDCARD_GROUP, NONE
    }

    static class Constants {
        static class PropertyNames {
            public static final String FILE_SIZE = "get.file.size";
            public static final String ZIP_FILE_COMMENT = "zip.file.comment";
            public static final String ZIP_FILE_PROTECTED = "zip.file.protected";
            public static final String COUNT_ENTRIES = "count.dir.entries";

            public static final String IGNORE_ONITEM_CLICK = "ignore.onitem.click";
            public static final String NO_DELETE = "no.delete";
            public static final String NOT_CHECKABLE = "not.checkable";
            public static final String CONTAIN_FOLDERS = "contain.folders";

            public static final String CLOUD_FILE = "cloud.file";

            public static final String EMPTY_FOLDER = "empty.folder";
        }
    }

    public StorageItem(String title,java.sql.Timestamp date, Uri[] uris, String path, ItemType type, StorageGroup.Types groupType) {
        this.title = title; // title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();
        this.date = date;
        this.uris = uris;
        this.path = path;
        this.type = type;
        this.groupType = groupType;

        this.properties = new HashMap<>();

        if(this.groupType == StorageGroup.Types.SDCARD && (!(type.equals(ItemType.IN_ZIP_FILE_ITEM)))) {
            setProperty(Constants.PropertyNames.NO_DELETE, (this.groupType.equals(StorageGroup.Types.SDCARD)));
            setProperty(Constants.PropertyNames.NOT_CHECKABLE, (this.groupType.equals(StorageGroup.Types.SDCARD)));
        }
    }

    public Uri[] uris;
    public String path;
    public String title;
    public java.sql.Timestamp date;
    public ItemType type;
    public StorageGroup.Types groupType;

    private Map<String, Object> properties;

    public boolean containsProperty(String name) {
        return this.properties.containsKey(name.toUpperCase());
    }

    public <T> T getProperty(String name) {
        if (containsProperty(name)) {
            return (T)(this.properties.get(name.toUpperCase()));
        }
        return null;
    }

    public <T> StorageItem setProperty(String name, T value) {
        this.properties.put(name.toUpperCase(), value);
        return this;
    }

    public boolean isCloudFile() {
        return !((this.groupType.equals(StorageGroup.Types.LOCAL)) || (this.groupType.equals(StorageGroup.Types.SDCARD)));
    }
}
