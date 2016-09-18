package com.qou.h7se.sbr;

import android.os.Handler;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


/**
 * Created by k0de9x on 10/24/2015.
 */
public class NewFtpClientEx {
    String svr;
    String user;
    String pass;
    TransferCallback callback;
    Map<String, StorageGroup.ConnectionStatus> connectionStatusListeners; // TODO: implement
    private FTPClient ftp = null;

    public static abstract class TransferCallback {
        abstract void message(String text);

        abstract void started();

        abstract void transferred();

        abstract void completed(int status);
    }

    public NewFtpClientEx(StorageGroup.ConnectionStatus connectionCallback) {
        this.svr = PrefsActivity.getString(PrefsActivity.FTP_SERVER);
        this.user = PrefsActivity.getString(PrefsActivity.FTP_USER);
        this.pass = PrefsActivity.getString(PrefsActivity.FTP_PASS);

        this.connectionStatusListeners = new HashMap<>();

        ftp = new FTPClient();
        ftp.setListHiddenFiles(PrefsActivity.getBoolean(PrefsActivity.FTP_SHOW_HIDDEN_FILES));

        ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out), true));

        if (connectionCallback != null) {
            if (!connectionStatusListeners.containsKey(connectionCallback.id())) {
                addConnectionStatusListener(connectionCallback, false);
            }
        }

        ftp.setCopyStreamListener(new CopyStreamListener() {
            @Override
            public void bytesTransferred(CopyStreamEvent event) {
            }

            @Override
            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
            }
        });

        TransferCallback callback = new TransferCallback() {
            @Override
            void message(String text) {
                Log.e(AppEx.PACKAGE_NAME, text);
            }

            @Override
            void started() {

            }

            @Override
            void transferred() {

            }

            @Override
            void completed(int status) {

            }
        };
    }

    public void addConnectionStatusListener(StorageGroup.ConnectionStatus listener, boolean notifyOnAddition) {
        if (!connectionStatusListeners.containsKey(listener.id())) {
            connectionStatusListeners.put(listener.id(), listener);
        }

        if (notifyOnAddition) {
            listener.setStatus(ftp.isConnected());
        }
    }

    public void removeConnectionStatusListener(String id) {
        if (connectionStatusListeners.containsKey(id)) {
            connectionStatusListeners.remove(id);
        }
    }

    void publishConnectionStatus(final boolean value) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                List<StorageGroup.ConnectionStatus> tmp = new ArrayList<>(connectionStatusListeners.values());
                for (StorageGroup.ConnectionStatus listener : tmp) {
                    listener.setStatus(value);

                    if (listener.removeIf()) {
                        removeConnectionStatusListener(listener.id());
                    }
                }
            }
        });
    }

    private static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }

    boolean connect() {
        if (!(Utils.IsNetworkAvailable())) {
            Log.e(AppEx.PACKAGE_NAME, "Not connected to the internet");
            return false;
        }

        if (ftp.isConnected()) {
            try {
                if(ftp.sendNoOp()) {
                    Log.e(AppEx.PACKAGE_NAME, "Already connected to ftp server");
                    return true;
                }
            } catch (IOException e) {
                Utils.LogException(e);
            }
        }

        try {
            int port = Integer.parseInt(PrefsActivity.getString(PrefsActivity.FTP_CONNECTION_PORT, "21"));
            if (port > 0) {
                ftp.connect(svr, port);
            } else {
                ftp.connect(svr);
            }

           ftp.setConnectTimeout(
                   Integer.parseInt(PrefsActivity.getString(PrefsActivity.FTP_CONNECT_TIMEOUT, "7000")));
            ftp.setSoTimeout(Integer.parseInt(PrefsActivity.getString(PrefsActivity.FTP_CONNECT_TIMEOUT, "30000")));

            int reply = ftp.getReplyCode();
            if (!(FTPReply.isPositiveCompletion(reply))) {
                ftp.disconnect();
                callback.message("FTP server refused connection.");
            } else {
                if (login()) {
                    ftp.setFileType(FTP.BINARY_FILE_TYPE);
                    ftp.enterLocalPassiveMode();
                }
            }
        } catch (IOException e) {
            try {
                ftp.disconnect();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            callback.message("Could not connect to FTP server.");
        }

        return isConnected();
    }

    public void connect(final GenericCallback4<Boolean> callback2) {
        new Utils.DoAsyncEx2<Boolean>(new OnDataCallback<Boolean>() {
            @Override
            public void data(Boolean success) {
                if (callback2 != null) {
                    callback2.event(success);
                }
                publishConnectionStatus(success);
            }
        }).run(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return connect();
            }
        });
    }

    private boolean login() {
        boolean success = false;
        try {
            success = (ftp.login(user, pass));
            if (!(success)) {
                logout();
            }
        } catch (IOException e) {
            callback.message(e.getMessage());
        }
        return success;
    }

    public static void canLogin(final GenericCallback4<Boolean> callback) {
        final NewFtpClientEx tmp = new NewFtpClientEx(null);
        tmp.connect(new GenericCallback4<Boolean>() {
            @Override
            public void event(final Boolean value) {
                tmp.disconnect(new OnDataCallback2<Boolean>() {
                    @Override
                    public void data(Boolean ignore) {
                        callback.event(value);
                    }
                });
            }
        });
    }

    void disconnect(final OnDataCallback<Boolean> callback2) {
        new Utils.DoAsyncEx2<Boolean>(new OnDataCallback<Boolean>() {
            @Override
            public void data(Boolean success) {
                if(success != null) {
                    if(callback2 != null) {
                        callback2.data(success);
                    }
                    publishConnectionStatus((!(success)));
                } else {
                    publishConnectionStatus(false);
                }
            }
        }).run(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (ftp.isConnected()) {
                    try {
                        ftp.logout();
                        ftp.disconnect();
                    } catch (IOException e) {
                        callback.message(e.getMessage());
                    }
                }
                return !(isConnected());
            }
        });
    }

    FTPFile getFile(String remote) {
        if (ftp.isConnected()) {
            try {
                if (ftp.changeWorkingDirectory(remote)) {
                    FTPFile[] list = ftp.listFiles(FilenameUtils.getName(remote));
                    for (FTPFile f : list) {
                        if (f.getLink().equalsIgnoreCase(remote)) { // TODO: check both getLin as remotePtah for match
                            return f;
                        }
                    }
                    // ftp.changeWorkingDirectory("/", false);
                }
            } catch (IOException e) {
                callback.message(e.getMessage());
            }
        }
        return null;
    }

     boolean createDirectory(String remote) {
         String[] dirs = PathUtils.removeLeadingSlash(remote).split("/");
         try {
             ftp.changeWorkingDirectory("/");
             for (String name : dirs) {
                 if (!(ftp.changeWorkingDirectory(name))) {
                     if (ftp.makeDirectory(name)) {
                         if (!(ftp.changeWorkingDirectory(name))) {
                             break;
                         }
                     } else {
                         break;
                     }
                 }
             }
             return true;
         } catch (IOException e) {
             e.printStackTrace();
         }
        return false;
    }

    boolean changeDirectory(String remote) {
        String[] dirs = PathUtils.removeLeadingSlash(remote).split("/");
        try {
            ftp.changeWorkingDirectory("/");
            for (String name : dirs) {
                if (!(ftp.changeWorkingDirectory(name))) {
                   return false;
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    void logout() {
        try {
            ftp.logout();
        } catch (IOException e) {
            callback.message(e.getMessage());
        }
    }

    void checkConnection(OnDataCallback2<Boolean> callback2) {
        new Utils.DoAsyncEx2<Boolean>(callback2).run(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    return ftp.isConnected() && ftp.sendNoOp();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    boolean isConnected() {
       return ftp.isConnected();
    }

    public FTPClient getFtpClient() {
        return ftp;
    }
}


