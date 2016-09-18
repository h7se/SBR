package com.qou.h7se.sbr;

import android.net.Uri;

public class BackupRvEntry {
    private Uri uri;
    private String title;
    private int image;
    private boolean checked;

    public BackupRvEntry(Uri u, String title, int image) {
        this.uri = u;
        this.title = title;

        this.image = image;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
