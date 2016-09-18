package com.qou.h7se.sbr;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by k0de9x on 9/11/2015.
 */

public class EncryptAndCompress {

    private static int BUFFER_SIZE = 512;
    private static Map<String, String> commentsCache = new HashMap<>();

    public static void getPassAndInvokeAction(boolean requirePassword, boolean invokedByUser, OnDataCallback<String> callback) {
        if (requirePassword) {
            String pass = PrefsActivity.getString(PrefsActivity.COMPRESSION_PASSWORD, null);
            if (Utils.StringEx.isNullOrEmpty(pass)) {
                if (invokedByUser) {
                    Utils.makePasswordInputDialog(
                            AppEx.self.getApplicationContext(), "File is password protected, Supply password to complete this action", callback, null);
                } else {
                    AppLog.instance.add("File is password protected, but saved password is empty.", LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.ERROR);
                }
            } else {
                callback.data(pass);
            }
        } else {
            callback.data(null);
        }
    }

    public static void AddFilesWithAESEncryption(String[] files, String dest, boolean append, String comment, com.qou.h7se.sbr.restore.HelperEx.ProgressCallback progressCallback) {
        try {
            File tmp = new File(dest);
            if (tmp.exists()) {
                if (!(append)) {
                    tmp.delete();
                }
            } else {
                if (!tmp.isDirectory()) {
                    tmp.getParentFile().mkdirs();
                }
            }

            ZipFile zipFile = new ZipFile(dest);

            ArrayList<File> filesToAdd = new ArrayList<>();

            for (String f : files) {
                filesToAdd.add(new File(f));
            }

            ZipParameters parameters = new ZipParameters();

            int compressionMethod = Integer.parseInt(PrefsActivity.getString(
                    PrefsActivity.COMPRESSION_METHOD, String.valueOf(Zip4jConstants.COMP_DEFLATE)));
            parameters.setCompressionMethod(compressionMethod);

            if (compressionMethod == Zip4jConstants.COMP_DEFLATE) {
                int compressionLevel = Integer.parseInt(PrefsActivity.getString(
                        PrefsActivity.COMPRESSION_LEVEL, String.valueOf(Zip4jConstants.DEFLATE_LEVEL_FASTEST)));
                parameters.setCompressionLevel(compressionLevel);
            }


            boolean encryptionEnabled = PrefsActivity.getBoolean(PrefsActivity.COMPRESSION_ENCRYPTION_ENABLE_FLAG);
            if (encryptionEnabled) {
                String pass = PrefsActivity.getString(PrefsActivity.COMPRESSION_PASSWORD);
                if (!(Utils.StringEx.isNullOrEmpty(pass))) {
                    parameters.setEncryptFiles(true);

                    int encryptionMethod = Integer.parseInt(PrefsActivity.getString(
                            PrefsActivity.COMPRESSION_ENCRYPTION_METHOD, String.valueOf(Zip4jConstants.ENC_METHOD_AES)));
                    parameters.setEncryptionMethod(encryptionMethod);

                    if (encryptionMethod == Zip4jConstants.ENC_METHOD_AES) {
                        int aesStrength = Integer.parseInt(PrefsActivity.getString(
                                PrefsActivity.COMPRESSION_ENCRYPTION_AES_STRENGTH, String.valueOf(Zip4jConstants.AES_STRENGTH_256)));
                        parameters.setAesKeyStrength(aesStrength);
                    }

                    parameters.setPassword(pass);
                }
            }

            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();

            // AppEx.DisableStrictMode();
            zipFile.addFiles(filesToAdd, parameters);
            // AppEx.EnableStrictMode();

            if (!(TextUtils.isEmpty(comment))) {
                zipFile.setComment(comment);
            }

            if (progressCallback != null) {
                progressCallback.reportTotalChange((int) progressMonitor.getTotalWork());
                while (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
                    progressCallback.reportValueChange(progressMonitor.getPercentDone());
                }
                progressCallback.reportValueChange(progressMonitor.getPercentDone());
            }
        } catch (ZipException e) {
            e.printStackTrace();
            Log.e(AppEx.PACKAGE_NAME, e.getMessage());
        }
    }

    public static void ExtractAllFiles(String file, final String dest, boolean invokedByUser) {
        try {
            final ZipFile zipFile = new ZipFile(file);
            OnDataCallback<String> callback = new OnDataCallback<String>() {
                @Override
                public void data(String data) {
                    try {
                        if (zipFile.isEncrypted() && !(Utils.StringEx.isNullOrEmpty(data))) {
                            zipFile.setPassword(data);
                        }
                        zipFile.extractAll(dest);
                    } catch (ZipException e) {
                        Utils.LogException(e);
                    }
                }
            };

            getPassAndInvokeAction(zipFile.isEncrypted(), invokedByUser, callback);
        } catch (ZipException e) {
            Utils.LogException(e);
        }
    }

    public static void getAllFilesListInZipFile(final String file, final OnDataCallback2<List<String>> callback) {
        new Utils.DoAsyncEx2<List<String>>(callback).run(new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                ArrayList<String> files = new ArrayList<>();
                try {
                    ZipFile zipFile = new ZipFile(file);
                    if (zipFile.isEncrypted()) {
                        String pass = PrefsActivity.getString(PrefsActivity.COMPRESSION_PASSWORD);
                        if (!(Utils.StringEx.isNullOrEmpty(pass))) {
                            zipFile.setPassword(pass);
                        } else {
                            callback.message("File %s is password protected, but saved password is empty.");
                        }
                    }

                    List fileHeaderList = zipFile.getFileHeaders();
                    for (int i = 0; i < fileHeaderList.size(); i++) {
                        files.add(((FileHeader) fileHeaderList.get(i)).getFileName());
                    }

                } catch (ZipException e) {
                    Utils.LogException(e);
                }

                return files;
            }
        });
    }

    public static void getZipFileComment(final File file, final OnDataCallback<String> callback) {
        final String key = file.getAbsolutePath();
        if (commentsCache.containsKey(key)) {
            callback.data(commentsCache.get(key));
        } else {
            new Utils.DoAsyncEx2<String>(new OnDataCallback<String>() {
                @Override
                public void data(String data) {
                    callback.data(data);
                    if (data != null) {
                        commentsCache.put(key, data);
                    }
                }
            }).run(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String comment = null;
                    if (file.exists()) {
                        try {
                            ZipFile zipFile = new ZipFile(file);
                            if (zipFile.isEncrypted()) {
                                String pass = PrefsActivity.getString(PrefsActivity.COMPRESSION_PASSWORD);
                                if (!(Utils.StringEx.isNullOrEmpty(pass))) {
                                    zipFile.setPassword(pass);
                                }
                            }
                            comment = zipFile.getComment();
                        } catch (ZipException e) {
                            e.printStackTrace();
                        }
                    }
                    return comment;
                }
            });
        }
    }

    public static boolean isPasswordProtected(String zfile) {
        if (new File(zfile).exists()) {
            try {
                return new ZipFile(zfile).isEncrypted();
            } catch (ZipException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    static class Helper {

        public static String CompressFiles(Uri u, List<String> files, File destinationDir, String comment, com.qou.h7se.sbr.restore.HelperEx.ProgressCallback progressCallback) {
            String f = null;
            try {
                f = destinationDir.getCanonicalPath();
                f = f.concat("/");
                f = f.concat(Utils.MapUriToLocation(u).name(DataSource.Case.Lower));
                f = f.concat("/");
                f = f.concat(Utils.MapUriToTitle(u));
                f = f.concat("_").concat(String.valueOf(System.currentTimeMillis()));
                f = f.concat(".zip");

                EncryptAndCompress.AddFilesWithAESEncryption(files.toArray(new String[files.size()]), f, false, comment, progressCallback);

                // if(deleteFiles) {
                // for (String s : files) {
                //   (new File(s)).delete();
                //  }
                // }

            } catch (Exception e) {
                Utils.LogException(e);
            }
            return f;
        }
    }
}
