package com.qou.h7se.sbr;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.dropbox.client2.DropboxAPI;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by k0de9x on 10/2/2015.
 */


public class GenericFs {
    interface DataCallback<T> {
        void data(List<T> items, boolean status);
    }

    interface DataCallback2<T> {
        void data(T data);
    }

    interface DataCallback3<T> {
        void data(T data, boolean status);
    }

    interface ReadyCallback {
        void status(boolean success);
    }

    public static class Helper {
        public static int getFilesCountMatchingPattern(List<Base.FileInfo> files, String pattern) {
            int count = 0;
            for (Base.FileInfo entry : files) {
                count += (Utils.matches(entry.title, pattern) ? 1 : 0);
            }
            return count;
        }

        public static boolean hasChildFolders(List<Base.FileInfo> files) {
            for (Base.FileInfo entry : files) {
                if(entry.isFolder == 1) {
                    return true;
                }
            }
            return false;
        }

        public static int getFilesCountMatchingPattern(String[] files, String pattern) {
            int count = 0;
            for (String entry : files) {
                count += (Utils.matches(entry, pattern) ? 1 : 0);
            }
            return count;
        }

        public static boolean hasChildFolders(File parent, String[] children) {
            for (String entry : children) {
                if (new File(parent, entry).isDirectory()) {
                    return true;
                }
            }
            return false;
        }

        public static GenericFs.Base getNewClientOfType(StorageGroup.Types groupType) {
            if (groupType == StorageGroup.Types.LOCAL) {
                return (new GenericFs.Local());
            } else if (groupType == StorageGroup.Types.SDCARD) {
                return (new GenericFs.SDCard());
            } else if (groupType == StorageGroup.Types.FTP) {
                return (new Ftp(AppEx.self.fclient));
            } else if (groupType == StorageGroup.Types.GOOGLE_DRIVE) {
                return (new GenericFs.Drive(AppEx.self.gclient));
            } else if (groupType == StorageGroup.Types.DROP_BOX) {
                return (new GenericFs.DBox(AppEx.self.dclient));
            }
            return null;
        }
    }

    static abstract class Base {
        public static String READ_ME_PATH = "keep/this";
       static class FileInfo {
            String title;
            long size;
            int isFolder;
            int type;

            public FileInfo(String title, long size, int isFolder) {
                this(title, size, isFolder, -1);
            }

           public FileInfo(String title, long size, int isFolder, int type) {
               this.title = title;
               this.size = size;
               this.isFolder = isFolder;
               this.type = type;
           }
        }

        class ChildesInfo {
            int zipFileCount;
            boolean hasChildFolders;

            public ChildesInfo(int zipFileCount, boolean hasChildFolders) {
                this.zipFileCount = zipFileCount;
                this.hasChildFolders = hasChildFolders;
            }
        }

        abstract void connect(ReadyCallback callback);
//        abstract void onReady(ReadyCallback callback);

        abstract void upload(String local, String remote, ReadyCallback callback);
        abstract void download(String remote, String local, ReadyCallback callback);

        abstract void delete(String remote, ReadyCallback callback);
        abstract void isFolder(String remote, ReadyCallback callback) ;
        abstract void exists(String remote, ReadyCallback callback) ;
        abstract void existsDir(String remote, ReadyCallback callback);
        abstract void list(String remote, boolean calcSize, DataCallback<FileInfo> callback);
        abstract void createDir(String remote, ReadyCallback callback);
        abstract void readFile(String remote, DataCallback3<String> callback);

        abstract String fixRemotePath(String remote);

//        static String getRootDirectory() {
//            return "/";
//        }
//
//        static String getCurrentRootDirectory() {
//            return "/";
//        }
        void getChildesInfo(String path, final DataCallback2<ChildesInfo> callback) {
            list(path, false, new DataCallback<FileInfo>() {
                @Override
                public void data(List<FileInfo> items, boolean status) {
                    if (status) {
                        callback.data(new ChildesInfo(
                                GenericFs.Helper.
                                        getFilesCountMatchingPattern(items, "^[^_]+_\\d{13}[.]zip$")
                                , GenericFs.Helper.hasChildFolders(items)));
                    } else {
                        callback.data(new ChildesInfo(-1, true));
                    }
                }
            });
        }
    }

    static class Local extends Base {

        @Override
        String fixRemotePath(String remote) {
            return remote;
        }

        static String getRootDirectory() {
            return PathUtils.getCanonicalPath(AppEx.self.getDefaultStorageDir(), false);
        }

        static String getCurrentRootDirectory() {
            return PathUtils.getCanonicalPath(AppEx.self.getCurrentStorageDir(), false);
        }

        @Override
        void connect(ReadyCallback callback) {
            callback.status(true);
        }

//        @Override
//        void onReady(ReadyCallback callback) {
//            callback.status(true);
//        }

        @Override
        void upload(final String local, final String remote, final ReadyCallback callback)  {
            if(!(local.equalsIgnoreCase(remote))) {
                final File file = new File(getCurrentRootDirectory(), remote);
                Utils.copyFile(new File(local), file, true, 500, new GenericCallback2() {
                    @Override
                    public void event() {
                        callback.status(file.exists());
                    }
                });
            } else {
                Utils.LogException("uploading the file to it self, " + local);
            }
        }

        @Override
        void download(final String remote, final String local, final ReadyCallback callback) {
            if(!(local.equalsIgnoreCase(remote))) {
                final File file = new File(local);
                Utils.copyFile(new File(remote), file, true, 0, new GenericCallback2() {
                    @Override
                    public void event() {
                        callback.status(file.exists());
                    }
                });
            } else {
                Utils.LogException("downloading the file to it self, " + local);
            }
        }

        @Override
        void delete(String src, ReadyCallback callback) {
            callback.status(new File(src).delete());
        }

        @Override
        void isFolder(String path, ReadyCallback callback) {
            callback.status(new File(path).isDirectory());
        }

        @Override
        void exists(String path, ReadyCallback callback) {
            callback.status(new File(path).exists());
        }

        @Override
        void existsDir(String path, ReadyCallback callback)  {
            callback.status(new File(path).exists());
        }

        @Override
        void list(final String path, final boolean calcSize, final DataCallback<FileInfo> callback) {
            new Utils.DoAsyncEx2<List<FileInfo>>(new OnDataCallback2<List<FileInfo>>() {
                @Override
                public void data(List<FileInfo> data) {
                    callback.data(data, true);
                }
            }).run(new Callable<List<FileInfo>>() {
                @Override
                public List<FileInfo> call() throws Exception {
                    File dir = new File(path);
                    ArrayList<FileInfo> tmp = new ArrayList<>();
                    if (dir.isDirectory()) {
                        String[] children = dir.list();
                        for (int i = 0; i < children.length; i++) {
                            File f = (new File(dir, children[i]));
                            tmp.add(new FileInfo(children[i],
                                    (calcSize ? f.length() : -1), f.isDirectory() ? 1 : 0));
                        }
                    }
                    return tmp;
                }
            });
        }

        @Override
        void createDir(String path, ReadyCallback callback) {
            callback.status(new File(path).mkdirs());
        }

        @Override
        void readFile(final String remote, final DataCallback3<String> callback) {
            new Utils.DoAsyncEx2<String>(new OnDataCallback2<String>() {
                @Override
                public void data(@Nullable String data) {
                    callback.data(data, data != null);
                }
            }).run(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    File file = new File(remote);
                    if(file.exists()) {
                        return FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
                    }
                    return null;
                }
            });
        }
    }


    static class SDCard extends Local {
        public static final File root =
                Environment.getExternalStorageDirectory();
        public static File current = root;

        static String getRootDirectory() {
            return PathUtils.getCanonicalPath(root, false);
        }

        static String getCurrentRootDirectory() {
            return PathUtils.getCanonicalPath(current, false);
        }
    }


    static class Ftp extends GenericFs.Base {
        NewFtpClientEx client = null;
        static boolean rootAlreadyCreated = false;

        public Ftp(NewFtpClientEx client) {
            this.client = client;
        }


        @Override
        String fixRemotePath(String remote) {
            String path = PathUtils.addLeadingSlash(remote).replaceFirst(
                    PathUtils.addLeadingSlash(getCurrentRootDirectory()), "");
            path = PathUtils.combine(getCurrentRootDirectory(), path);
            return path;
        }

        static String getRootDirectory() {
            return "/";
        }

        static String getCurrentRootDirectory() {
            return PathUtils.addLeadingSlash(PrefsActivity.prefs.getString(PrefsActivity.BACKUP_LOCATION_GDRIVE, "/htdocs/sbr"));
        }

        @Override
        void connect(final GenericFs.ReadyCallback callback) {
            client.connect(new GenericCallback4<Boolean>() {
                @Override
                public void event(Boolean value) {
                    callback.status(value);
                }
            });
        }

        @Override
        void upload(final String local, final String remote, final GenericFs.ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    String parent = fixRemotePath(FilenameUtils.getFullPath(remote));
                    String name = FilenameUtils.getName(remote);

                    boolean result = false;
                    InputStream input = null;
                    try {
                        if (client.callback != null) {
                            client.callback.message("uploading: " + local + " to " + fixRemotePath(remote));
                        }
                        input = new
                                FileInputStream(local);

                        client.getFtpClient().makeDirectory(parent);
                        client.getFtpClient().changeWorkingDirectory(parent); // changeWorkingDirectory();

                        result = client.getFtpClient().storeFile((name), input);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            input.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return result;
                }
            });
        }

        @Override
        void download(final String remote, final String local, final GenericFs.ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    boolean result = false;
                    FileOutputStream outputStream = null;
                    try {
                        if(client.callback != null) {
                            client.callback.message("downloading: " + local + " to " + fixRemotePath(remote));
                        }
                        outputStream = new
                                FileOutputStream(new java.io.File(local));
                        result = client.getFtpClient().retrieveFile(fixRemotePath(remote), outputStream);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return result;
                }
            });
        }

        @Override
        void delete(final String remote, final GenericFs.ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return (client.getFtpClient().deleteFile(fixRemotePath(remote)));
                }
            });
        }

        @Override
        void isFolder(final String remote, final GenericFs.ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    FTPFile resource = client.getFile(fixRemotePath(remote));
                    if (resource == null) {
                        client.callback.message(
                                "Could not locate, " + fixRemotePath(remote));
                        return false;
                    }
                    return (resource.isDirectory());
                }
            });
        }

        @Override
        void exists(final String remote, final GenericFs.ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    InputStream inputStream = client.getFtpClient().retrieveFileStream(fixRemotePath(remote));
                    if (inputStream == null) {
                        if(client.getFtpClient().getReplyCode() == 550) {
                            return false;
                        }
                    } else {
                        inputStream.close();
                    }
                    return true;
                }
            });
        }

        @Override
        void existsDir(final String remote, final GenericFs.ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    client.getFtpClient().changeWorkingDirectory(fixRemotePath(remote));
                    int replay = client.getFtpClient().getReplyCode();
                    if (replay == 550) {
                        return false;
                    }
                    return true;
                }
            });
        }

        @Override
        void list(final String remote, boolean calcSize, final GenericFs.DataCallback<FileInfo> callback) {
            new Utils.DoAsyncEx2<List<FileInfo>>(new OnDataCallback<List<FileInfo>>() {
                @Override
                public void data(List<FileInfo> data) {
                    callback.data(data, data != null);
                }
            }).run(new Callable<List<FileInfo>>() {
                @Override
                public List<FileInfo> call() throws Exception {
                    List<FileInfo> tmp = null;
                    try {
                        client.getFtpClient().changeWorkingDirectory(fixRemotePath(remote));
                        FTPFile[] files = client.getFtpClient().listFiles(null, new FTPFileFilter() {
                            @Override
                            public boolean accept(FTPFile file) {
                                return (!(file.getName().startsWith(".")) && (file.isFile() || file.isDirectory()));
                            }
                        });
                        tmp = new ArrayList<>();
                        for (FTPFile f : files) {
                            tmp.add(new FileInfo(f.getName(), f.getSize(), f.isDirectory() ? 1 : 0, f.getType()));
                        }
                        // client.changeWorkingDirectory("/");
                    } catch (IOException e) {
                        client.callback.message(e.getMessage());
                    }
                    return tmp;
                }
            });


        }

        @Override
        void createDir(final String remote, final GenericFs.ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return client.getFtpClient().makeDirectory(fixRemotePath(remote));
                }
            });
        }

        @Override
        void readFile(final String remote, final DataCallback3<String> callback) {
            new Utils.DoAsyncEx2<String>(new OnDataCallback2<String>() {
                @Override
                public void data(@Nullable String data) {
                    callback.data(data, data != null);
                }
            }).run(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String data = null;
                    InputStream inputStream = client.getFtpClient().retrieveFileStream(fixRemotePath(remote));
                    if (inputStream != null) {
                        data = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
                        inputStream.close();
                    }
                    return data;
                }
            });
        }
    }





    static class Drive extends Base {
        GoogleDriveClientExt client;
        static boolean rootAlreadyCreated = false;

        public Drive(GoogleDriveClientExt client) {
            this.client = client;
        }

        @Override
        String fixRemotePath(String remote) {
            String path = PathUtils.addLeadingSlash(remote).replaceFirst(
                    PathUtils.addLeadingSlash(getCurrentRootDirectory()), "");
            path = PathUtils.combine(getCurrentRootDirectory(), path);
            return path;
//            boolean root = PathUtils.addLeadingSlash(remote).equalsIgnoreCase(getCurrentRootDirectory());
//            String s;
//            if(root) {
//                s = PathUtils.addLeadingSlash(remote);
//            } else {
//                s = PathUtils.combine(getCurrentRootDirectory(), remote);
//            }
//            return s;
        }

        static String getRootDirectory() {
            return "";
        }

        static String getCurrentRootDirectory() {
            return PathUtils.addLeadingSlash(PrefsActivity.prefs.getString(PrefsActivity.BACKUP_LOCATION_GDRIVE, "/sbr"));
        }

        @Override
        void connect(final ReadyCallback callback) {
            client.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
                @Override
                String id() {
                    return "123344/drive";
                }

                @Override
                boolean removeIf() {
                    return true;
                }

                @Override
                void connection(boolean success) {
                    callback.status(success);
                }
            }, true);

            client.connect(false);
        }

//        @Override
//        void connect(final ReadyCallback callback) {
//
//            AppEx.self.gclient.addConnectionStatusListner(new StorageGroup.ConnectionStatus() {
//                @Override
//                public void connection(boolean status) {
//                    if(callback != null) {
//                        callback.status(status);
//                    }
//                    AppEx.self.gclient.removeConnectionStatusListner(this);
//                }
//            });
//            AppEx.self.gclient.connect(false);
//        }

//        @Override
//        void onReady(final ReadyCallback callback) {
//            Helpers.RequestSync.runAsync(client, new OnDataCallback<Status>() {
//                @Override
//                public void onData(final Status data) {
//                    boolean connected = (data != null) &&
//                            (data.getStatusCode() == CommonStatusCodes.SUCCESS);
//                    callback.status(connected);
//                }
//            });
//        }

        @Override
        void upload(String local, final String remote, final ReadyCallback callback) {
            Utils.readFileToByteArray(new File(local), new OnDataCallback<byte[]>() {
                @Override
                public void data(byte[] data) {
                    Helpers.WriteFile.runAsync(client, fixRemotePath(remote), data, null, new OnDataCallback<Boolean>() {
                        @Override
                        public void data(Boolean data) {
                            callback.status(data);
                        }
                    });
                }
            });
        }

        @Override
        void download(String remote, final String local, final ReadyCallback callback) {
            final MutableVar<Boolean> result = new MutableVar<>(true);
            Helpers.ReadFile.runAsync(client, fixRemotePath(remote), new OnDataCallback<byte[]>() {
                @Override
                public void data(byte[] data) {
                    if (data != null) {
                        try {
                            FileUtils.writeByteArrayToFile(new File(local), data);
                        } catch (IOException e) {
                            Utils.LogException(e);
                        }
                    }

                    callback.status(result.value);
                }
            });
        }

        @Override
        void delete(String remote, final ReadyCallback callback) {
            Helpers.DeleteFile.runAsync(client, fixRemotePath(remote), true, new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data)  {
                    callback.status(data);
                }
            });
        }

        @Override
        void isFolder(String remote, final ReadyCallback callback) {
            Helpers.isFolder.runAsync(client, fixRemotePath(remote), new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            });
        }

        @Override
        void exists(String remote, final ReadyCallback callback) {
            Helpers.getResourceOrNull.runAsync(client, fixRemotePath(remote), true, new OnDataCallback<DriveResource>() {
                @Override
                public void data(DriveResource data) {
                    callback.status(data != null);
                }
            });
        }

        @Override
        void existsDir(String remote, final ReadyCallback callback)  {
            Helpers.getResourceOrNull.runAsync(client, fixRemotePath(remote), false, new OnDataCallback<DriveResource>() {
                @Override
                public void data(DriveResource data) {
                    callback.status(data != null);
                }
            });
        }

        @Override
          void list(String remote, final boolean calcSize, final DataCallback<FileInfo> callback)  {
            Helpers.ListFolder.runAsync(client, fixRemotePath(remote), new OnDataCallback<List<FileInfo>>() {
                @Override
                public void data(List<FileInfo> data) {
//                    List<FileInfo> tmp = new ArrayList<>();
//                    for (Metadata d : data) {
//                        tmp.add(new FileInfo(d.getTitle(), (calcSize ? d.getFileSize() : -1), d.isFolder() ? 1 : 0));
//                    }
                    callback.data(data, data != null);
                    // client.getCallback().message(String.format("2 tmp.size(): %d", data.size()));
                }
            });
        }

        @Override
        void createDir(String remote, final ReadyCallback callback) {
            Helpers.CreateFolder.runAsync(client, fixRemotePath(remote), new OnDataCallback<DriveFolder>() {
                @Override
                public void data(DriveFolder data) {
                    callback.status(data != null);
                }
            });
        }

        @Override
        void readFile(final String remote, final DataCallback3<String> callback) {
            Helpers.ReadFile.runAsync(client, fixRemotePath(remote), new OnDataCallback2<byte[]>() {
                @Override
                public void data(@Nullable byte[] bytes) {
                    String data = null;
                    if(bytes != null) {
                        try {
                            data = new String(bytes, StandardCharsets.UTF_8.name());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    if (callback != null) {
                        callback.data(data, data != null);
                    }
                }
            });
        }
    }




    static class DBox extends Base {
        DBoxClient client;
        static boolean rootAlreadyCreated = false;

        public DBox(DBoxClient client) {
            this.client = client;
        }

        @Override
        String fixRemotePath(String remote) {
            String path = PathUtils.addLeadingSlash(remote).replaceFirst(
                    PathUtils.addLeadingSlash(getCurrentRootDirectory()), "");
            path = PathUtils.combine(getCurrentRootDirectory(), path);
            return path;
        }

        static String getRootDirectory() {
            return "/";
        }

        static String getCurrentRootDirectory() {
            return PathUtils.addLeadingSlash(PrefsActivity.prefs.getString(PrefsActivity.BACKUP_LOCATION_DBOX, "/"));
        }

        @Override
        void connect(ReadyCallback callback) {
            if(!(this.client.isLinked())) {
                // this.client.init();
                this.client.login();

                if(this.client.isLinked()) {
                    if(!(TextUtils.isEmpty(getCurrentRootDirectory()) || getCurrentRootDirectory().equals("/"))) {
                        createDir(getCurrentRootDirectory(), new ReadyCallback() {
                            @Override
                            public void status(boolean success) {
                                Utils.LogException("Creating root directory for, " + this.getClass().getSimpleName() + ": " + String.valueOf(success));
                            }
                        });
                    }
                }
            }
            callback.status(this.client.isLinked());
        }

        @Override
        void list(final String remote, final boolean calcSize, final DataCallback<FileInfo> callback) {
            new Utils.DoAsyncEx<List<FileInfo>>(new OnDataCallback<List<FileInfo>>() {
                @Override
                public void data(List<FileInfo> data) {
                    callback.data(data, data != null);
                }
            }).run(new Callable<List<FileInfo>>() {
                @Override
                public List<FileInfo> call() throws Exception {
                    ArrayList<FileInfo> tmp = new ArrayList<>();
                    try {
                        List<DropboxAPI.Entry> children = client.getDirImmediateChildren(fixRemotePath(remote));
                        for (int i = 0, size = children.size(); i < size; i++) {
                            DropboxAPI.Entry info = children.get(i);
                            tmp.add(new FileInfo(info.fileName(), (calcSize ? info.bytes : -1), children.get(i).isDir ? 1 : 0));
                        }
                        return tmp;
                    } catch (Exception e) {
                        Utils.LogException(e);
                    }
                    return null;
                }
            });
        }

        @Override
        void createDir(final String remote, final ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(null, new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }, null).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        return client.createFolder(fixRemotePath(remote)).isDir;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }

        @Override
        void upload(final String local, final String remote, final ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(null, new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }, null).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        File f = new File(local);
                        if (f.exists() && f.isFile()) {
                            client.putFileOverwrite(fixRemotePath(remote), f);
                        }
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }

        @Override
        void download(final String remote, final String local, final ReadyCallback callback) {
//            Utils.LogException(remote);
//            Utils.LogException(local);
            new Utils.DoAsyncEx2<Boolean>(null, new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }, null).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        return client.getFile(fixRemotePath(remote), local).exists();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }

        @Override
        void delete(final String remote, final ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(null, new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }, null).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        client.deleteFile(fixRemotePath(remote));
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }

        @Override
        void isFolder(final String remote, final ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(null, new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }, null).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        return client.isFolder(fixRemotePath(remote));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }

        @Override
        void exists(final String remote, final ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(null, new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }, null).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        return client.exists(fixRemotePath(remote));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }

        @Override
        void existsDir(final String remote, final ReadyCallback callback) {
            new Utils.DoAsyncEx2<Boolean>(null, new OnDataCallback<Boolean>() {
                @Override
                public void data(Boolean data) {
                    callback.status(data);
                }
            }, null).run(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        return client.exists(fixRemotePath(remote));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            });
        }

        @Override
        void readFile(final String remote, final DataCallback3<String> callback) {
            new Utils.DoAsyncEx2<String>(new OnDataCallback2<String>() {
                @Override
                public void data(@Nullable String data) {
                    callback.data(data, data != null);
                }
            }).run(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String data = null;
                    InputStream inputStream = client.getFileAsStream(fixRemotePath(remote));
                    if (inputStream != null) {
                        data = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
                        inputStream.close();
                    }
                    return data;
                }
            });
        }
    }
}
