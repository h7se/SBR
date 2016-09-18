package com.qou.h7se.sbr;


import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Charsets;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by k0de9x on 9/27/2015.
 */

public class GoogleDriveClientExt implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static class Constants {
        final static int REQUEST_CODE_RESOLUTION = 1701;
        final static int REQUEST_ACCOUNT_PICKER = 1711;
        final static int REQUEST_AUTHORIZATION = 1712;
        final static int REQUEST_GOOGLE_PLAY_SERVICES = 1713;

        public final static String PREF_ACCOUNT_NAME = "google.drive.selected.account.name";
        static String[] SCOPES = {
                    DriveScopes.DRIVE_FILE
                    , DriveScopes.DRIVE_APPDATA
                    , DriveScopes.DRIVE_METADATA
        };


        /*DriveScopes.DRIVE_METADATA,
        DriveScopes.DRIVE, DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE*/
    }

    private GoogleApiClient gClient;
    private com.google.api.services.drive.Drive gService;
    private GoogleAccountCredential gCredential;

    private Activity activity;
    private MessageCallback callback;
    Map<String, StorageGroup.ConnectionStatus> connectionStatusListeners;

    public void addConnectionStatusListener(StorageGroup.ConnectionStatus listener, boolean notifyOnAddition) {
        if (!connectionStatusListeners.containsKey(listener.id())) {
            connectionStatusListeners.put(listener.id(), listener);
        }

        if (notifyOnAddition) {
            listener.setStatus(isConnected());
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

    public GoogleDriveClientExt(final Activity activity, MessageCallback callback) {
        this.activity = activity;
        this.connectionStatusListeners = new HashMap<>();

        if(callback == null) {
            this.callback = (new MessageCallback() {
                @Override
                public void message(final String text) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AppLog.instance.add(text, LogsEntry.LOG_SOURCE.GDRIVE, LogsEntry.TYPE.INFO);
                        }
                    });
                }
            });
        } else {
            this.callback = callback;
        }

        // SharedPreferences settings = activity.getPreferences(Context.MODE_PRIVATE);
        // SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        String accountName = PrefsActivity.prefs.getString(Constants.PREF_ACCOUNT_NAME, null);
        this.gCredential = GoogleAccountCredential.usingOAuth2(
                this.activity, Arrays.asList(Constants.SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(accountName);

        this.gService = new com.google.api.services.drive.Drive.Builder(
                AndroidHttp.newCompatibleTransport(), JacksonFactory.getDefaultInstance(), gCredential)
                .setApplicationName("Drive API Android Quickstart" ) // TODO: change
                .build();

        this.gClient = new GoogleApiClient.Builder(activity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public GoogleApiClient getApiClient() {
        return this.gClient;
    }

    public com.google.api.services.drive.Drive getService() {
        return this.gService;
    }

    public GoogleAccountCredential getCredential() {
        return this.gCredential;
    }

    public Activity getActivity() {
        return this.activity;
    }

    public MessageCallback getCallback() {
        return this.callback;
    }

    public void log(final String text) {
        this.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getCallback().message(text);
            }
        });
        Log.e(AppEx.PACKAGE_NAME, text);
    }

    public boolean isConnected() {
        return getApiClient().isConnected();
    }

    public void connect(boolean displayMsg) {
        if(!(Utils.IsNetworkAvailable())) {
            Log.e(AppEx.PACKAGE_NAME, "Not connected to the internet");
            publishConnectionStatus(false);
            return;
        }

        if(isConnected()) {
            Log.e(AppEx.PACKAGE_NAME, "Already connected to google drive");
            publishConnectionStatus(true);
            return;
        }

        if(displayMsg) {
            callback.message("connecting...");
        }

        getApiClient().connect();
    }

    public void disconnect() {
        if (getApiClient() != null) {
            callback.message("disconnecting...");
            getApiClient().disconnect();
            publishConnectionStatus(isConnected());
        }
    }

    public void activityResultCallbacks(int requestCode, int resultCode, Intent data) {
        if (data != null &&
                data.getExtras() != null) {
            String accountName =
                    data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            if (accountName != null) {
                gCredential.setSelectedAccountName(accountName);
//                SharedPreferences settings =
//                        activity.getPreferences(Context.MODE_PRIVATE);
                PrefsActivity.prefs.edit()
                        .putString(Constants.PREF_ACCOUNT_NAME, accountName)
                        .apply();

                gClient.connect();
            } else {
                // screen canceled: todo
                 AppLog.instance.add("you must link the app inorder to backup & restore data to google drive.", LogsEntry.LOG_SOURCE.GDRIVE, LogsEntry.TYPE.WARNING, true);
            }
        } else {
            gClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        publishConnectionStatus( isConnected() );
    }

    @Override
    public void onConnectionSuspended(int i) {
        callback.message("connection suspended");

        publishConnectionStatus(false);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        callback.message("connection failed: " + connectionResult.toString());


            if (!(connectionResult.hasResolution())) {
                Dialog dialog = GoogleApiAvailability.getInstance().
                        getErrorDialog(activity, connectionResult.getErrorCode(), 0);
                dialog.setCancelable(true);

                dialog.show();

                return;
            }

            try {
                connectionResult.startResolutionForResult(activity, Constants.REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                callback.message("Exception while starting resolution activity");
            }

        publishConnectionStatus(false);
    }

    public <T> void AsyncCallable(Callable<T> callable, OnDataCallback<T> rOnDataCallback, OnErrorCallback onErrorCallback) {
        if(onErrorCallback != null) {
            new TaskAsyncEx<>(this, rOnDataCallback, onErrorCallback, true /*publishResult*/).call(callable);
        } else {
            new TaskAsyncEx<>(this, rOnDataCallback, new OnErrorCallback() {
                @Override
                public void error(Exception e) {
                    if(e != null) {
                        log(e.getMessage());
                    }
                }
            }, true /*publishResult*/).call(callable);
        }
    }

}


class TaskAsyncEx<T> extends AsyncTask<Callable<T>, T, Void> {
    private OnDataCallback<T> rOnDataCallback;
    private OnErrorCallback onErrorCallback;
    // private ProgressDialog progress;
    private GoogleDriveClientExt clientExt;
    private boolean publishResult;

    public TaskAsyncEx(GoogleDriveClientExt clientExt, OnDataCallback<T> rOnDataCallback, OnErrorCallback onErrorCallback, final boolean publishResult) {
        this.clientExt = clientExt;
        this.publishResult = publishResult;
        this.rOnDataCallback = rOnDataCallback;
        this.onErrorCallback = onErrorCallback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // this.clientExt.log("Calling Drive API ...");

//        progress = new ProgressDialog(this.clientExt.getActivity());
//        progress.setMessage("Calling Drive API ...");
//        progress.show();
    }

    @Override
    protected Void doInBackground(Callable<T>... callables) {
        for (Callable<T> callable : callables) {
            try {
                T result = callable.call();
                if (publishResult) {
                    publishProgress(result);
                }
            } catch (final Exception e) {
                if (onErrorCallback != null) {
//                    clientExt.getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
                    if(e != null) {
                        //          onErrorCallback.error(e);
                        //    TODO: raises unknow error // null
                    }
//                        }
//                    });
                } else {
                    Log.e(AppEx.PACKAGE_NAME, e.getMessage());
                    e.printStackTrace();
                }
            }

            if (isCancelled()) break;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(T... values) {
        super.onProgressUpdate(values);

        if(this.rOnDataCallback != null) {
            for (T v : values) {
                this.rOnDataCallback.data(v);
            }
        }
    }

    @Override
    protected void onPostExecute(Void aResult) {
        super.onPostExecute(aResult);

//        if (progress.isShowing()) {
//            progress.dismiss();
//        }
    }

    private void _execute_(Callable<T> callable) throws GooglePlayServicesAvailabilityIOException, UserRecoverableAuthIOException {
        this.execute(callable);
    }

    public TaskAsyncEx<T> call(Callable<T> callable) {
        if (Utils.IsNetworkAvailable()) {
            if (Helpers.Utils.isGooglePlayServicesAvailable(clientExt.getActivity(), GoogleDriveClientExt.Constants.REQUEST_GOOGLE_PLAY_SERVICES)) {
//                if (this.clientExt.getCredential().getSelectedAccountName() == null) {
//                    Helpers.Utils.chooseAccount(this.clientExt.getActivity(), this.clientExt.getCredential(), GoogleDriveClientExt.Constants.REQUEST_ACCOUNT_PICKER);
//                } else {
                    try {
                        _execute_(callable);
                    } catch (final GooglePlayServicesAvailabilityIOException availabilityException) {
                        Helpers.Utils.showGooglePlayServicesAvailabilityErrorDialog(this.clientExt.getActivity(),
                                availabilityException.getConnectionStatusCode(), GoogleDriveClientExt.Constants.REQUEST_GOOGLE_PLAY_SERVICES);

                    } catch (UserRecoverableAuthIOException userRecoverableException) {
                        this.clientExt.getActivity().startActivityForResult(
                                userRecoverableException.getIntent(),
                                GoogleDriveClientExt.Constants.REQUEST_AUTHORIZATION);
                    }
               // }
            } else {
                this.clientExt.log("Google Play Services required: " +
                        "after installing, close and relaunch this app.");
            }
        } else {
            this.clientExt.log("No network connection available.");
        }
        return this;
    }
}


class Helpers {
    public static class RequestSync {
        public static void runAsync(final GoogleDriveClientExt client, final OnDataCallback<Status> onDataCallback) {
            client.AsyncCallable(new Callable<Status>() {
                @Override
                public Status call() throws Exception {
                    return run(client);
                }
            }, onDataCallback, null);
        }

        public static Status run(final GoogleDriveClientExt client) throws IOException {
            return Utils.requestSync(client);
        }
    }

    public static class CreateFolder {
        public static void runAsync(final GoogleDriveClientExt client, final String path, final OnDataCallback<DriveFolder> onDataCallback) {
            client.AsyncCallable(new Callable<DriveFolder>() {
                @Override
                public DriveFolder call() throws Exception {
                    return run(client, path);
                }
            }, onDataCallback, null);
        }

        public static DriveFolder run(final GoogleDriveClientExt client, final String path) {
            return Utils.getDirOrMakeNew(client, path);
        }
    }


    public static class WriteFile {
        public static void runAsync(final GoogleDriveClientExt client, final String path, final byte[] data, final String mime, final OnDataCallback<Boolean> onDataCallback) {
            client.AsyncCallable(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return run(client, path, data, mime);
                }
            }, onDataCallback, null);
        }

        public static Boolean run(final GoogleDriveClientExt client, final String path, final byte[] data, final String mime) throws IOException {
            if(data != null) {
                String dir = path.substring(0, path.lastIndexOf("/"));
                String name = path.substring(path.lastIndexOf("/") + 1);

                DriveFolder folder = Utils.getDirOrMakeNew(client, dir);
                if(Utils.getChildesCount(client, folder, name) == 0) {
                    if(folder != null) {
                        DriveFolder.DriveFileResult result = Utils.writeContent(client, folder, name, mime, data);
                        if(result != null) {
                            return result.getStatus().isSuccess();
                        }
                    } else {
                        client.log("Error while trying to create new file, " + path);
                    }
                } else {
                    Log.e(AppEx.PACKAGE_NAME + "/DRIVE", "File already exists, " + path);
                    return true;
                }
            } else {
                client.log("data is not valid");
            }
            return false;
        }
    }

    public static class ListFolder {
        public static void runAsync(final GoogleDriveClientExt client, final String path, final OnDataCallback<List<GenericFs.Base.FileInfo>> onDataCallback) {
            client.AsyncCallable(new Callable<List<GenericFs.Base.FileInfo>>() {
                @Override
                public List<GenericFs.Base.FileInfo> call() throws Exception {
                    return run(client, path);
                }
            }, onDataCallback, null);
        }

        public static List<GenericFs.Base.FileInfo> run(final GoogleDriveClientExt client, final String path) throws IOException {
            DriveResource resource;

            if((path == null) || path.equals("") || path.equals("/")) {
                resource = Drive.DriveApi.getRootFolder(client.getApiClient());
            } else {
                resource = getResourceOrNull.run(client, path, false /* isFile */);
            }

            List<GenericFs.Base.FileInfo> tmp = null;
            if(resource != null) {
                DriveFolder folder = (DriveFolder) resource;

                Query qb = new Query.Builder().
                        addFilter(Filters.in(SearchableField.PARENTS, folder.getDriveId())).
                        addFilter(Filters.eq(SearchableField.TRASHED, false)).build();

                DriveApi.MetadataBufferResult mbr =
                        folder.queryChildren(client.getApiClient(), qb).await();

                if (mbr.getStatus().isSuccess()) {
                    tmp = new ArrayList<>();
                    MetadataBuffer mb = mbr.getMetadataBuffer();
                    for(Metadata d : mb) {
                        tmp.add(new GenericFs.Base.FileInfo(d.getTitle(), (d.getFileSize()), d.isFolder() ? 1 : 0));
                    }
                    mb.release();
                } else {
                    client.log("Error while trying to locate resource");
                }
                mbr.release();
            }
            return tmp;
        }
    }

    public static class ReadFile {
        public static void runAsync(final GoogleDriveClientExt client, final String path, final OnDataCallback<byte[]> onDataCallback) {
            client.AsyncCallable(new Callable<byte[]>() {
                @Override
                public byte[] call() throws Exception {
                    return run(client, path);
                }
            }, onDataCallback, null);
        }

        public static byte[] run(final GoogleDriveClientExt client, final String path) throws IOException {
            byte[] data = null;
            DriveResource resource = getResourceOrNull.run(client, path, true /* isFile */);
            if(resource != null) {
                DriveFile file = Drive.DriveApi.getFile(client.getApiClient(), resource.getDriveId());
                if (file != null) {
                    DriveApi.DriveContentsResult driveContentsResult =
                            file.open(client.getApiClient(), DriveFile.MODE_READ_ONLY, null).await();

                    if (driveContentsResult.getStatus().isSuccess()) {
                        DriveContents driveContents = driveContentsResult.getDriveContents();
                        data = IOUtils.toByteArray(driveContents.getInputStream());
                        driveContents.discard(client.getApiClient());
                    }
                }
            }
            // client.log("Error while trying to read file");
            return data;
        }

        public static String bytesToString(byte[] data) {
            return bytesToString(data, Charsets.UTF_8);
        }

        public static String bytesToString(byte[] data, Charset charset) {
            return (new String(data, charset));
        }
    }

    public static class DeleteFile {
        public static void runAsync(final GoogleDriveClientExt client, final String path, final boolean permanent, final OnDataCallback<Boolean> onDataCallback) {
            DeleteFsEntry.runAsync(client, path, permanent, true, onDataCallback);
        }
    }

    public static class DeleteFolder {
        public static void runAsync(final GoogleDriveClientExt client, final String path, final boolean permanent, final OnDataCallback<Boolean> onDataCallback) {
            DeleteFsEntry.runAsync(client, path, permanent, false, onDataCallback);
        }
    }



    private static class DeleteFsEntry {
        public static void runAsync(final GoogleDriveClientExt client, final String path, final boolean permanent,final boolean isFile, final OnDataCallback<Boolean> onDataCallback) {
            client.AsyncCallable(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    Boolean f = run(client, path, permanent, isFile);
                    onDataCallback.data(f);
                    return f;
                }
            }, onDataCallback, null);
        }

        public static boolean run(final GoogleDriveClientExt client, final String path, final boolean permanent, final boolean isFile) throws IOException {
            DriveResource f = getResourceOrNull.run(client, path, isFile);
            if(f != null) {
                if(permanent) {
                    f.delete(client.getApiClient());
                } else {
                    f.trash(client.getApiClient());
                }
                return true; // TODO: recheck
            }
            return false;
        }
    }

    public static class isFolder {
        public static void runAsync(final GoogleDriveClientExt client, final String path, final OnDataCallback<Boolean> onDataCallback) {
            client.AsyncCallable(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    Boolean f = run(client, path);
                    onDataCallback.data(f);
                    return f;
                }
            }, onDataCallback, null);
        }

        public static boolean run(final GoogleDriveClientExt client, final String path) throws IOException {
            DriveResource f = getResourceOrNull.run(client, path, false);
            if(f != null) {
                return Utils.isFolder(f.getDriveId());
            }
            return false;
        }
    }


    public static class getResourceOrNull {
        public static void runAsync(final GoogleDriveClientExt client, final String path, final boolean isFile, final OnDataCallback<DriveResource> onDataCallback) {
            client.AsyncCallable(new Callable<DriveResource>() {
                @Override
                public DriveResource call() throws Exception {
                    return run(client, path, isFile);
                }
            }, onDataCallback, null);
        }

        public static DriveResource run(final GoogleDriveClientExt client, final String path, boolean isFile) {
            ArrayDeque<String> q = new ArrayDeque<>
                    (Arrays.asList(PathUtils.removeLeadingSlash(path).split("/")));

            DriveFolder folder = Drive.DriveApi.getRootFolder(
                    client.getApiClient());
            String fileName = null;
            if(isFile) {
                fileName = q.removeLast();
            }

            while (!q.isEmpty()) {
                String title = q.removeFirst();
                Query query = Utils.buildQueryForFolderEntry(title, folder);

                DriveApi.MetadataBufferResult mbr =
                        folder.queryChildren(client.getApiClient(), query).await();
                MetadataBuffer mb = null;

                if (mbr.getStatus().isSuccess()) {
                    mb = mbr.getMetadataBuffer();
                    if(mb.getCount() == 1) {
                        if (mb.get(0).isFolder()) {
                            folder = Drive.DriveApi.getFolder(client.getApiClient(),
                                    mb.get(0).getDriveId());
                        } else {
                            break;
                        }
                    }
                } else {
                    break;
                }

                mb.release();
                mbr.release();
            }

            DriveResource result = folder;
            if(isFile && q.isEmpty()) {
                Query query = new Query.Builder().
                        addFilter(Filters.eq(SearchableField.TITLE, fileName)).
                        addFilter(Filters.in(SearchableField.PARENTS, folder.getDriveId())).
                        addFilter(Filters.eq(SearchableField.TRASHED, false)).
                        build();

                DriveApi.MetadataBufferResult mbr  = folder.queryChildren(client.getApiClient(), query).await();
                if (mbr.getStatus().isSuccess()) {
                    MetadataBuffer mb = mbr.getMetadataBuffer();
                    if(mb.getCount() == 1) {
                        if (!(mb.get(0).isFolder())) {
                            result = Drive.DriveApi.getFolder(client.getApiClient(),
                                    mb.get(0).getDriveId());
                        }
                    }
                    mb.release();
                }
                mbr.release();
            }

            return q.isEmpty() ? result : null;
        }
    }





    public static class Utils {
        public static class MimeType {
            public final static String XML = "text/xml";
            public final static String TXT = "text/plain";
            public final static String ZIP = "application/zip";
            public final static String FILE = "application/vnd.google-apps.file";
            public final static String FOLDER = "application/vnd.google-apps.folder";
            public final static String DEFAULT = "application/octet-stream";
            public final static String UNKNOWN = "application/vnd.google-apps.unknown";
        }

        static boolean isFolder(DriveId driveId) {
            return driveId != null && driveId.getResourceType() == DriveId.RESOURCE_TYPE_FOLDER;
        }

        static boolean isFile(DriveId driveId) {
            return driveId != null && driveId.getResourceType() == DriveId.RESOURCE_TYPE_FILE;
        }

        public static DriveFolder.DriveFileResult writeContent(final GoogleDriveClientExt client, DriveFolder parent, String fileName, String mimeType, final byte[] data) throws IOException {
            DriveApi.DriveContentsResult driveContentsResult =  Drive.DriveApi.newDriveContents(
                    client.getApiClient()).await();

            if (driveContentsResult.getStatus().isSuccess()) {
                final DriveContents driveContents = driveContentsResult.getDriveContents();
                ByteArrayOutputStream outPut = new ByteArrayOutputStream();
                outPut.write(data);
                outPut.writeTo(driveContents.getOutputStream());
                outPut.flush();
                outPut.close();

                String mi = (mimeType == null ? MimeType.DEFAULT : mimeType);
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(fileName)
                        .setMimeType(mi)
                        .build();

                DriveFolder.DriveFileResult driveFileResult = parent.createFile(
                        client.getApiClient(), changeSet, driveContents).await();

                return driveFileResult;
            } else {
                client.log("Error while trying to create new file contents");
            }
            return null;
        }

        public static Status requestSync(final GoogleDriveClientExt client) {
            return Drive.DriveApi.requestSync(client.getApiClient()).await();
        }

        public static DriveFolder getDirOrMakeNew(final GoogleDriveClientExt client, String path) {
            ArrayDeque<String> q = new ArrayDeque<>
                    (Arrays.asList(PathUtils.removeLeadingSlash(path).split("/")));

            DriveFolder folder = Drive.DriveApi.getRootFolder(
                    client.getApiClient());

            DriveApi.MetadataBufferResult mbr = null;
            while (!q.isEmpty()) {
                String title = q.removeFirst();

                Query query = buildQueryForFolderEntry(title, folder);

                mbr = folder.queryChildren(client.getApiClient(), query).await();
                MetadataBuffer mb = mbr.getMetadataBuffer();
                if (mbr.getStatus().isSuccess() && mb.getCount() == 1) {
                    if (mb.get(0).isFolder()) {
                        folder = Drive.DriveApi.getFolder(client.getApiClient(),
                                mb.get(0).getDriveId());
                    } else {
                        break;
                    }
                } else {
                    DriveFolder.DriveFolderResult result = createFolder(client, folder, title);
                    if (result.getStatus().isSuccess()) {
                        folder = result.getDriveFolder();
                    } else {
                        break;
                    }
                }
                mb.release();
            }

            if(mbr != null) {
                mbr.release();
            }

            return q.isEmpty() ? folder : null;
        }


        public static DriveFolder.DriveFolderResult createFolder(final GoogleDriveClientExt client, DriveFolder parent, String title) {
            return parent.createFolder(client.getApiClient(),
                    new MetadataChangeSet.Builder().
                            setTitle(title).
                            setMimeType(MimeType.FOLDER).
                            build()).await();
        }


        public static Query buildQueryForFolderEntry(String title, DriveFolder parent) {
            Query.Builder qb = new Query.Builder().
                    addFilter(Filters.eq(SearchableField.TITLE, title)).
                    addFilter(Filters.eq(SearchableField.MIME_TYPE, MimeType.FOLDER)).
                    addFilter(Filters.eq(SearchableField.TRASHED, false));
            if(parent != null) {
                qb.addFilter(Filters.in(SearchableField.PARENTS, parent.getDriveId()));
            }
            return qb.build();
        }

        public static Metadata getChild(final GoogleDriveClientExt client, DriveFolder parent, String title) {
            MetadataBuffer mb = getChildes(client, parent, title);
            if(mb != null) {
                if(mb.getCount() == 1) {
                    return mb.get(0);
                }
            }
            return null;
        }

        public static MetadataBuffer getChildes(final GoogleDriveClientExt client, DriveFolder parent, String title) {
            Query query = new Query.Builder().
                    addFilter(Filters.eq(SearchableField.TITLE, title)).
                    addFilter(Filters.in(SearchableField.PARENTS, parent.getDriveId())).
                    addFilter(Filters.eq(SearchableField.TRASHED, false)).
                    build();

            DriveApi.MetadataBufferResult mbr  = parent.queryChildren(client.getApiClient(), query).await();
            if (mbr.getStatus().isSuccess()) {
                return mbr.getMetadataBuffer();
            }
            return null;
        }

        public static int getChildesCount(final GoogleDriveClientExt client, DriveFolder parent, String title) {
            int count = 0;
            MetadataBuffer mb = getChildes(client, parent, title);
            if(mb != null) {
                count = mb.getCount(); mb.release();
            }
            return count;
        }

        public static void chooseAccount(Activity activity, GoogleAccountCredential credential, int requestCode) {
            activity.startActivityForResult(credential.newChooseAccountIntent(), requestCode);
        }


        /**
         * Check that Google Play services APK is installed and up to date. Will
         * launch an error dialog for the user to update Google Play Services if
         * possible.
         * @return true if Google Play Services is available and up to
         *     date on this device; false otherwise.
         */
        public static boolean isGooglePlayServicesAvailable(Activity activity, int requestCode) {
            final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
            if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
                showGooglePlayServicesAvailabilityErrorDialog(activity, connectionStatusCode, requestCode);
                return false;
            } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
                return false;
            }
            return true;
        }

        /**
         * Display an error dialog showing that Google Play Services is missing
         * or out of date.
         * @param connectionStatusCode code describing the presence (or lack of)
         *     Google Play Services on this device.
         */
        public static void showGooglePlayServicesAvailabilityErrorDialog(final Activity activity, final int connectionStatusCode,final int requestCode) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                            connectionStatusCode,
                            activity,
                            requestCode);
                    dialog.setCancelable(true);
                    dialog.show();
                }
            });
        }
    }
}

