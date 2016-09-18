package com.qou.h7se.sbr;

import android.support.annotation.Nullable;

import java.util.ArrayList;

/**
 * Created by k0de9x on 10/2/2015.
 */

interface OnCallBackListener {
        void finish(String text);
        void message(String text);
    }

interface OnRestoreCallBackListener extends OnCallBackListener {
        void data(StorageItem item);
    }

interface OnDataChange {
    ArrayList<TreeNode> refresh();
}

interface OnDataCallback<T> {
    void data(@Nullable final T data);
}

abstract class OnDataCallback2<T> implements OnDataCallback<T>, MessageCallback, OnErrorCallback {
    @Override
    public void message(String text) {
    }

    @Override
    public void error(Exception e) {
    }
}

interface OnErrorCallback {
    void error(final Exception e);
}

interface MessageCallback {
    void message(String text);
}

interface PredicateIsEnabledEx {
    boolean enabled();
}



