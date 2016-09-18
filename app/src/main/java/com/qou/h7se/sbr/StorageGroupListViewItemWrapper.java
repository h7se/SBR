package com.qou.h7se.sbr;

/**
 * Created by k0de9x on 9/22/2015.
 */

public class StorageGroupListViewItemWrapper extends BaseEntry {
    StorageGroupListViewItemWrapper(String title, StorageGroup.Types storage) {
        this(title, storage, false);
    }

    StorageGroupListViewItemWrapper(String title, StorageGroup.Types groupType, boolean checked) {
        this.title = title;
        this.groupType = groupType;
        this.checked = checked;
        this.connected = false;
        this.connectionStatusApply = true;
    }

    public String title;
    public StorageGroup.Types groupType;
    public boolean checked;
    public boolean connected;
    public boolean connectionStatusApply;
}


