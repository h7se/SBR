package com.qou.h7se.sbr;

import android.app.Activity;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by k0de9x on 10/23/2015.
 */
public class DBoxClient {
    private Activity context;
    private DropboxAPI dbApi;

    public final static String DBOX_TOKEN_PREFS_KEY = "dbox_token";
    private final static String ACCESS_KEY = "9oxtmz5gld3gj72";
    private final static String ACCESS_SECRET = "hu274xn5ywohyef";
    private final static Session.AccessType ACCESS_TYPE = Session.AccessType.APP_FOLDER;

    Map<String, StorageGroup.ConnectionStatus> connectionStatusListeners;
    public void addConnectionStatusListener(StorageGroup.ConnectionStatus listener, boolean notifyOnAddition) {
        if (!connectionStatusListeners.containsKey(listener.id())) {
            connectionStatusListeners.put(listener.id(), listener);
        }

        if(getApi() != null) {
            if(notifyOnAddition) {
                listener.setStatus(getApi().getSession().isLinked());
            }
        }
    }

    public void removeConnectionStatusListener(String id) {
        if (connectionStatusListeners.containsKey(id)) {
            connectionStatusListeners.remove(id);
        }
    }

    void publishConnectionStatus(final boolean value) {
        List<StorageGroup.ConnectionStatus> tmp = new ArrayList<>(connectionStatusListeners.values());
        for(StorageGroup.ConnectionStatus listener : tmp) {
            listener.setStatus(value);

            if (listener.removeIf()) {
                removeConnectionStatusListener(listener.id());
            }
        }
    }

    public DBoxClient(Activity context) {
        this.context = context;
        this.connectionStatusListeners = new HashMap<>();

        init();
    }

    public DropboxAPI getApi() {
        return dbApi;
    }

    public void init() { // on create
        AndroidAuthSession session =
                new AndroidAuthSession(new AppKeyPair(ACCESS_KEY, ACCESS_SECRET), ACCESS_TYPE);

        dbApi = new DropboxAPI<>(session);
    }

    void finishAuthentication() { // on resume
        if(isAuthenticationStarted()) {
            setAuthenticationStarted(false);

            AndroidAuthSession session = getSession();
            if (session.authenticationSuccessful()) {
                try {
                    session.finishAuthentication();

                    String accessToken = session.getOAuth2AccessToken();
                    if(accessToken != null) {
                        PrefsActivity.prefs.edit()
                                .putString(DBOX_TOKEN_PREFS_KEY, session.getOAuth2AccessToken())
                                .apply();
//                        context.getPreferences(Context.MODE_PRIVATE).edit()
//                                .putString(DBOX_TOKEN_PREFS_KEY, session.getOAuth2AccessToken()).apply();
                    }

                    // loggedIn(true);
                    publishConnectionStatus(true);
                } catch (IllegalStateException e) {
                    Utils.loge(e);
                }
            }

            publishConnectionStatus(isLinked());
        }
    }

    public boolean isAuthenticationStarted() {
        return authenticationStarted;
    }

    public void setAuthenticationStarted(boolean authenticationStarted) {
        this.authenticationStarted = authenticationStarted;
    }

    private boolean authenticationStarted;

    public void login() {
        if(!(Utils.IsNetworkAvailable())) {
            Log.e(AppEx.PACKAGE_NAME,
                    "Not connected to the internet");
            publishConnectionStatus(false);
            return;
        }

        if(isLinked()) {
            Log.e(AppEx.PACKAGE_NAME,
                    "Already linked to dropbox account");
            publishConnectionStatus(true);
            return;
        }

        String accessToken = PrefsActivity.prefs.getString(DBOX_TOKEN_PREFS_KEY, null);
        if (accessToken == null) {
            setAuthenticationStarted(true);
            getSession().startOAuth2Authentication(context);
        } else {
            getSession().setOAuth2AccessToken(accessToken);
            publishConnectionStatus(true);
        }
    }

    AndroidAuthSession getSession() {
        return ((AndroidAuthSession) getApi().getSession());
    }

    public void unlink() {
        if(getApi() != null) {
            getSession().unlink();
            publishConnectionStatus(getSession().isLinked());
        }
    }

    public boolean isLinked() {
        if(getApi() != null) {
            return getSession().isLinked();
        }
        return false;
    }

    public DropboxAPI.Entry putFileOverwrite(String remote, File local) throws IOException, DropboxException {
        InputStream inputStream = new FileInputStream(local);
        DropboxAPI.Entry entry = getApi().putFileOverwrite(remote, inputStream, local.length(), null);
        inputStream.close();
        return entry;
    }

    public DropboxAPI.Entry putFile(String remote, File local) throws Exception {
        InputStream inputStream = new FileInputStream(local);
        DropboxAPI.Entry entry = getApi().putFile(remote, inputStream, local.length(), null, null);
        inputStream.close();
        return entry;
    }

    public DropboxAPI.Entry putFile(String remote, byte[] data, boolean overwrite) throws Exception {
        if(overwrite) {
            return getApi().putFileOverwrite(remote, new ByteArrayInputStream(data), data.length, null);
        } else {
            return getApi().putFile(remote, new ByteArrayInputStream(data), data.length, null, null);
        }
    }


    public void deleteFile(String path) throws DropboxException {
        getApi().delete(path);
    }

    public File getFile(String remote, String local) throws DropboxException, IOException {
        File localFile = new File(local);
        if(!(localFile.exists())) {
            localFile.getParentFile().mkdirs();
            localFile.createNewFile();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(localFile);
        DropboxAPI.DropboxFileInfo fileInfo = getApi().getFile(remote, null, fileOutputStream,
                null);
        fileOutputStream.close();
        return localFile;
    }

    public List<DropboxAPI.Entry> search(String remote, String query) throws Exception {
        return getApi().search(remote, query, 0, false);
    }

    public InputStream getFileAsStream(String remote) throws Exception {
        return getApi().getFileStream(remote, null);
    }

    boolean exists(String remote) throws DropboxException {
        boolean exists = false;
        try {
            DropboxAPI.Entry entry = getApi().metadata(remote, 1, null, false, null);
            if (!(entry.isDeleted)) {
                exists = true;
            }
        } catch (DropboxServerException se) {
            if (se.error == DropboxServerException._404_NOT_FOUND) {
                exists = false;
            }
        }
        return exists;
    }

    List<DropboxAPI.Entry> getDirImmediateChildren(String remote) throws DropboxException {
        DropboxAPI.Entry entry = getApi().metadata(remote, 0, null, true, null);
        if (!(entry.isDeleted) && entry.isDir) {
            return entry.contents;
        }
        return null;
    }

    List<String> getDirImmediateChildrenNames(String remote) throws DropboxException {
        DropboxAPI.Entry entry = getApi().metadata(remote, 0, null, true, null);
        if (!(entry.isDeleted) && entry.isDir) {
            List<String> names = new ArrayList<>();
            List<DropboxAPI.Entry> contents = entry.contents;
            for (int i = 0, size = entry.contents.size(); i < size; i++) {
                DropboxAPI.Entry e = contents.get(i);
                if (!(e.isDeleted)) {
                    names.add(contents.get(i).fileName());
                }
            }
            return names;
        }
        return null;
    }

    boolean isFile(String remote) throws DropboxException {
        if(exists(remote)) {
            DropboxAPI.Entry entry = getEntry(remote, false);
            if (!(entry == null)) {
                return !(entry.isDir);
            }
        }
        return false;
    }

    boolean isFolder(String remote) throws DropboxException {
        if(exists(remote)) {
            DropboxAPI.Entry entry = getEntry(remote, true);
            if (!(entry == null)) {
                return entry.isDir;
            }
        }
        return false;
    }

    DropboxAPI.Entry getEntry(String remote, boolean list) throws DropboxException {
        DropboxAPI.Entry entry = null;
        entry = getApi().metadata(remote, 0, null, list, null);
        if (!(entry.isDeleted)) {
            return entry;
        }
        return null;
    }

    DropboxAPI.Entry createFolder(String remote) throws DropboxException {
        return getApi().createFolder(remote);
    }

}
