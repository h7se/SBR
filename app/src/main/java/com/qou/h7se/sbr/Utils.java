package com.qou.h7se.sbr;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by k0de7x on 11/9/2014.
 */
public class Utils {

    Context context;

    Utils(Context context) {
        this.context = context;
    }

    public static void LogException(String msg) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement e = stackTraceElements[3];

        Log.e(AppEx.PACKAGE_NAME,
                String.format("[ERROR]: %s ( %d ) :: %s", e.getMethodName(), e.getLineNumber(), msg));

        TunePlayer.instance.play();

        AppLog.instance.add(msg, LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.WARNING);
    }

    public static void LogException(Throwable ex) {
        try {
            ex.printStackTrace();

            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            StackTraceElement e = stackTraceElements[3];

            String msg = String.format("EXCEPTION ( %s ): %s ( %d ) :: %s",
                    e.getFileName(), e.getMethodName(), e.getLineNumber(),
                    (TextUtils.isEmpty(ex.getMessage())) ? ex.toString() : ex.getMessage());

            Log.e(AppEx.PACKAGE_NAME, msg);

            TunePlayer.instance.play();

            AppLog.instance.add(msg, LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.ERROR);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void loge(Exception e) {
        e.printStackTrace();

        if(e.getMessage() != null) {
            Log.e(AppEx.PACKAGE_NAME, e.getMessage());
        }
    }

    public static String generateRandomFilePath(String prefix, String suffix, String dir) {
        long n = AppEx.self.random.nextLong();
        if (n == Long.MIN_VALUE) {
            n = 0;
        } else {
            n = Math.abs(n);
        }
        return dir.concat("/" + prefix + Long.toString(n) + suffix);
    }

    static boolean containsCaseInsensitive(String[] list, String value) {
        for (String s : list) {
            if (s.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    static <T> boolean contains(T[] list, T value) {
        for (T v : list) {
            if (v.equals(value)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    // AsyncTask<Params, Progress, Result>
    static class DoAsyncEx<T> extends AsyncTask<Callable<T>, Void, T> {
        Runnable preExecuteCallback;
        OnDataCallback<T> postExecuteCallback;

        public DoAsyncEx(OnDataCallback<T> postExecuteCallback) {
            this(null, postExecuteCallback);
        }

        public DoAsyncEx(Runnable preExecuteCallback, OnDataCallback<T> postExecuteCallback) {
            this.preExecuteCallback = preExecuteCallback;
            this.postExecuteCallback = postExecuteCallback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(preExecuteCallback != null) {
                preExecuteCallback.run();
            }
        }

        @Override
        protected T doInBackground(Callable<T>... callables) {
            T list = null;
            try {
                list = callables[0].call();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        @Override
        protected void onPostExecute(T result) {
            super.onPostExecute(result);

            if(postExecuteCallback != null) {
                postExecuteCallback.data(result);
            }
        }

        public final AsyncTask<Callable<T>, Void, T> run(Callable<T> callableEx) {
            return execute(callableEx);
        }
    }

    static class DoAsyncEx2<T> extends AsyncTask<Callable<T>, T, Void> {
        Runnable preExecuteCallback;
        Runnable postExecuteCallback;
        OnDataCallback<T> onDataCallback;

        public DoAsyncEx2(OnDataCallback<T> onDataCallback) {
            this(null, onDataCallback, null);
        }

        public DoAsyncEx2(Runnable preExecuteCallback, OnDataCallback<T> onDataCallback, Runnable postExecuteCallback) {
            this.preExecuteCallback = preExecuteCallback;
            this.postExecuteCallback = postExecuteCallback;
            this.onDataCallback = onDataCallback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(preExecuteCallback != null) {
                preExecuteCallback.run();
            }
        }

        @Override
        protected void onProgressUpdate(T... values) {
            super.onProgressUpdate(values);

            if(onDataCallback != null) {
                if (values != null) {
                    onDataCallback.data(values[0]);
                } else {
                    onDataCallback.data((T) null);
                }
            }
        }

        @Override
        protected Void doInBackground(Callable<T>... callables) {
            try {
                publishProgress(callables[0].call());
            } catch (Exception e) {
                if(e.getMessage() != null) {
                    e.printStackTrace();
                    Log.e(AppEx.PACKAGE_NAME, e.getMessage());
                } else if(e.getCause() != null) {
                    Log.e(AppEx.PACKAGE_NAME, "Unknown error, " + e.getCause().getMessage());
                } else {
                    Log.e(AppEx.PACKAGE_NAME, "Unknown error, ");
                }
                publishProgress(null);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(postExecuteCallback != null) {
                postExecuteCallback.run();
            }
        }

        public final AsyncTask<Callable<T>, T, Void> run(Callable<T> callableEx) {
            return execute(callableEx);
        }
    }

    public static String MapUriToTitle(Uri u) {
        String f = null;
        if (u.equals(Uris.CONTACTS)) {
            f = "contacts";
        } else if (u.equals(Uris.SMS)) {
            f = "sms"; //
        } else if (u.equals(Uris.MMS)) {
            f = "mms"; //
        } else if (u.equals(Uris.LOGS)) {
            f = "logs";
        } else if (u.equals(Uris.IMAGES)) {
            f = "images";
        } else if (u.equals(Uris.AUDIO)) {
            f = "audio";
        } else if (u.equals(Uris.VIDEO)) {
            f = "video";
        } else if (u.equals(Uris.CALENDARS_EVENTS)) {
            f = "events";
        } else if (u.equals(Uris.CALENDARS_REMINDERS)) {
            f = "reminders";
        } else if (u.equals(Uris.BROWSER_BOOKMARKS)) {
            f = "bookmarks";
        } else if (u.equals(Uris.BROWSER_SEARCHES)) {
            f = "searches";
        } else if (u.equals(Uris.SAMSUNG_ALARMS)) {
            f = "alarms";
        }
        return f;
    }

    public static Uri MapTitleToUri(String title) {
        Uri u = null;
        if (title.equalsIgnoreCase("contacts")) {
            u = Uris.CONTACTS;
        } else if (title.equalsIgnoreCase("sms")) {
            u = Uris.SMS;
        } else if (title.equalsIgnoreCase("mms")) {
            u = Uris.MMS;
        } else if (title.equalsIgnoreCase("logs")) {
            u = Uris.LOGS;
        } else if (title.equalsIgnoreCase("images")) {
            u = Uris.IMAGES;
        } else if (title.equalsIgnoreCase("audio")) {
            u = Uris.AUDIO;
        } else if (title.equalsIgnoreCase("video")) {
            u = Uris.VIDEO;
        } else if (title.equalsIgnoreCase("events")) {
            u = Uris.CALENDARS_EVENTS;
        } else if (title.equalsIgnoreCase("reminders")) {
            u = Uris.CALENDARS_REMINDERS;
        } else if (title.equalsIgnoreCase("bookmarks")) {
            u = Uris.BROWSER_BOOKMARKS;
        } else if (title.equalsIgnoreCase("searches")) {
            u = Uris.BROWSER_SEARCHES;
        } else if (title.equalsIgnoreCase("alarms")) {
            u = Uris.SAMSUNG_ALARMS;
        }
        return u;
    }

    public static Uri[] MapZipFileNameToSingleUri(String zipFileName) {
        assert zipFileName.endsWith(".zip");
        String title = (new File(zipFileName).getName())
                .toLowerCase().split(Constants.ZIP_FILE_NAME_DATE_SEP)[0];
        Uri u = MapTitleToUri(title);
        if(u == null) {
            return null;
        }
        return new Uri[] {u};
    }

    public static String MapUriToXmlFile(Uri u) {
        String title = MapUriToTitle(u);
        if(title != null) {
            return title.concat(".xml");
        }
        return null;
    }

    public static DataSource MapUriToLocation(Uri u) {
        DataSource v = null;
        if (u.equals(Uris.CONTACTS)) {
            v = DataSource.CONTACTS;
        } else if ((u.equals(Uris.SMS)) || (u.equals(Uris.MMS))) {
            v = DataSource.MESSAGES;
        } else if (u.equals(Uris.LOGS)) {
            v = DataSource.LOGS;
        } else if ((u.equals(Uris.IMAGES)) || (u.equals(Uris.AUDIO)) || (u.equals(Uris.VIDEO))) {
            v = DataSource.GALLERY;
        } else if ((u.equals(Uris.CALENDARS_EVENTS))) {
            v = DataSource.EVENTS;
        } else if ((u.equals(Uris.BROWSER_BOOKMARKS)) || (u.equals(Uris.BROWSER_SEARCHES))) {
            v = DataSource.BROWSER;
        } else if ((u.equals(Uris.SAMSUNG_ALARMS))) {
            v = DataSource.ALARMS;
        }
        return v;
    }

    public static Uri[] MapZipFileToUris(String file) {
        Utils.LogException(file);
        assert file.endsWith(".zip");
        Uri uris[] = null;
        if (file.contains(DataSource.CONTACTS.name(DataSource.Case.Lower))) {
            uris = MapToUris(DataSource.CONTACTS);
        } else if (file.contains(DataSource.MESSAGES.name(DataSource.Case.Lower))) {
            uris = MapToUris(DataSource.MESSAGES);
        } else if (file.contains(DataSource.LOGS.name(DataSource.Case.Lower))) {
            uris = MapToUris(DataSource.LOGS);
        } else if (file.contains(DataSource.GALLERY.name(DataSource.Case.Lower))) {
            uris = MapToUris(DataSource.GALLERY);
        } else if (file.contains(DataSource.EVENTS.name(DataSource.Case.Lower))) {
            uris = MapToUris(DataSource.EVENTS);
        } else if (file.contains(DataSource.BROWSER.name(DataSource.Case.Lower))) {
            uris = MapToUris(DataSource.BROWSER);
        } else if (file.contains(DataSource.ALARMS.name(DataSource.Case.Lower))) {
            uris = MapToUris(DataSource.ALARMS);
        }
        Utils.LogException(uris.toString());
        Utils.LogException("-----------");

        return uris;
    }

    public static Uri[] MapToUris(DataSource location) {
        Uri[] uris = null;
        if (location.equals(DataSource.CONTACTS)) {
            uris = new Uri[] {Uris.CONTACTS};
        } else if (location.equals(DataSource.MESSAGES)) {
            uris = new Uri[] {Uris.SMS, Uris.MMS};
        } else if (location.equals(DataSource.LOGS)) {
            uris = new Uri[] {Uris.LOGS};
        } else if (location.equals(DataSource.GALLERY)) {
            uris = new Uri[] {Uris.IMAGES, Uris.AUDIO, Uris.VIDEO};
        } else if (location.equals(DataSource.EVENTS)) {
            uris = new Uri[] {Uris.CALENDARS_EVENTS};
        } else if (location.equals(DataSource.BROWSER)) {
            uris = new Uri[] {Uris.BROWSER_BOOKMARKS, Uris.BROWSER_SEARCHES};
        } else if (location.equals(DataSource.ALARMS)) {
            uris = new Uri[] {Uris.SAMSUNG_ALARMS};
        }
        return uris;
    }

    public static Uri MapZipFileEntryToUri(String zipFileName, String xmlFileName) {
        assert zipFileName.endsWith(".zip");
        Uri uri = null;
        String zfile = (new File(zipFileName).getName()).toLowerCase().split(Constants.ZIP_FILE_NAME_DATE_SEP)[0];
        String xfile = xmlFileName.toLowerCase();

        if (zfile.equals(DataSource.CONTACTS.name(DataSource.Case.Lower))) {
            uri = Uris.CONTACTS;
        } else if (zfile.equals(DataSource.MESSAGES.name(DataSource.Case.Lower))) {
            if(xfile.startsWith("sms")) {
                uri = Uris.SMS;
            } else if(xfile.startsWith("mms")) {
                uri = Uris.MMS;
            }
        } else if (zfile.equals(DataSource.LOGS.name(DataSource.Case.Lower))) {
            if(xfile.startsWith("logs")) {
                uri = Uris.LOGS;
            }
        }  else if (zfile.equals(DataSource.GALLERY.name(DataSource.Case.Lower))) {
            if(xfile.startsWith("images")) {
                uri = Uris.IMAGES;
            } else if(xfile.startsWith("audio")) {
                uri = Uris.AUDIO;
            } else if(xfile.startsWith("video")) {
                uri = Uris.VIDEO;
            }
        } else if (zfile.equals(DataSource.EVENTS.name(DataSource.Case.Lower))) {
            if(xfile.startsWith("events")) {
                uri = Uris.CALENDARS_EVENTS;
            }
        }else if (zfile.equals(DataSource.BROWSER.name(DataSource.Case.Lower))) {
            if(xfile.startsWith("bookmarks")) {
                uri = Uris.BROWSER_BOOKMARKS;
            } else if(xfile.startsWith("searches")) {
                uri = Uris.BROWSER_SEARCHES;
            }
        } else if (zfile.equals(DataSource.ALARMS.name(DataSource.Case.Lower))) {
            if(xfile.startsWith("logs")) {
                uri = Uris.SAMSUNG_ALARMS;
            }
        }

        if(uri == (null)) {
            Utils.LogException(" ?? " + zipFileName + " : " + xmlFileName);
        }
        return uri;
    }


    public static void copyFile(final File source, final File dest, final boolean overwrite, final int delay, final GenericCallback2 callback) {
       // AppEx.DisableStrictMode();

        new Utils.DoAsyncEx2<Void>(new OnDataCallback<Void>() {
            @Override
            public void data(Void data) {
                if(callback != null) {
                    callback.event();
                }
            }
        }).run(new Callable<Void>() {
            @Override
            public Void call() {
                if ((dest.exists() && !(overwrite))) {
                    return null;
                }

                if(source.exists()) {
                    try {
                        FileUtils.copyFile(source, dest);
                    } catch (IOException e) {
                        Utils.LogException(e);
                    }

                    if (delay > 0) {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Log.e(AppEx.PACKAGE_NAME, "CopyFile: Source not exists");
                }
                return null;
            }
        });

       // AppEx.EnableStrictMode();
    }

        public static void readFileToByteArray(final File file, OnDataCallback<byte[]> callback) {
            new DoAsyncEx2<byte[]>(callback).run(new Callable<byte[]>() {
                @Override
                public byte[] call() throws Exception {
                    try {
                        return FileUtils.readFileToByteArray(file);
                    } catch (IOException e) {
                        Utils.LogException(e);
                    }
                    return null;
                }
            });

        }

    public static void toast(int i) {
        toast(String.valueOf(i));
    }

    public static void toast(String text) {
        final Toast toast = Toast.makeText(
                AppEx.self.getApplicationContext(), text, Toast.LENGTH_SHORT);
        toast.show();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                toast.cancel();
//            }
//        }, duration);
    }

    public static void toastAndLog(String text) {
        LogException(text);
        toast(text);
    }

    public static Snackbar snackbar(View view, String text, final boolean playTune, final boolean vibrate) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);

                if(playTune) {
                    TunePlayer.instance.play();
                }
                if(vibrate) {
                    vibrationNotifyError2(AppEx.self.getApplicationContext());
                }
            }
        });
        return snackbar;
    }

    public static boolean IsNetworkAvailable() {
        return IsNetworkAvailable(true);
    }

    public static boolean IsNetworkAvailable(boolean wifiOnly) {
        return IsNetworkAvailable(AppEx.self.getApplicationContext(), wifiOnly);
    }

    public static boolean IsNetworkAvailable(Context context, boolean wifiOnly) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        boolean connected = false;
        // TODO: read pref and decide
        if (wifi != null && wifi.isAvailable() && wifi.isConnected()){
            connected = true;
        } else {
            if(!(wifiOnly)) {
                NetworkInfo mobile =
                        connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if ((mobile != null) && mobile.isAvailable() && mobile.isConnected()) {
                    connected = true;
                }
            }
        }

        return connected;
    }

    public static String escapeRE(String str) {
        if(str != null) {
            return str.replaceAll("([^a-zA-Z0-9])", "\\\\$1");
        }
        return null;
    }

    public static void vibrationNotify(Context context, long[] vibrate) {
        NotificationManager nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = new Notification();
        n.vibrate = vibrate;
        nManager.notify(0, n);
    }

    public static void vibrationNotify3(Context context) {
        vibrationNotify(context, new long[]{7, 15});
    }

    public static void vibrationNotifyError(Context context) {
        vibrationNotify(context, new long[]{3, 5, 7, 20, 7, 5, 3});
    }

    public static void vibrationNotifyError2(Context context) {
        vibrationNotify(context, new long[]{9, 11, 15, 21, 15, 11, 9});
    }

    public static void hideSoftKeyboard(Activity activity, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showSoftKeyboard(Activity activity, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public static Dialog makeMsgBox(final Context context, String title, String text, boolean checkBoxVisibility, final View.OnClickListener yesHandler, final View.OnClickListener noHandler, final View.OnClickListener cancelHandler) {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.Animations_Dialog;
        dialog.setContentView(R.layout.prompt);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final TextView txtTitle = ((TextView) dialog.findViewById(R.id.textViewTitle));
        final TextView txtContent = ((TextView) dialog.findViewById(R.id.textViewContent));
        CheckBox checkBox = (CheckBox) dialog.findViewById(R.id.checkBox);

        Button btnYes = (Button) dialog.findViewById(R.id.buttonYes);
        Button btnNo = (Button) dialog.findViewById(R.id.buttonNo);
        Button btnCancel = (Button) dialog.findViewById(R.id.buttonCancel);

        txtTitle.setText(title);
        txtContent.setText(text);

        checkBox.setVisibility(checkBoxVisibility ? View.VISIBLE : View.GONE);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
//                Animation anim = new AlphaAnimation(0.0f, 1.0f);
//                anim.setDuration(5); //You can manage the time of the blink with this parameter
//                anim.setStartOffset(20);
//                anim.setRepeatMode(Animation.REVERSE);
//                anim.setRepeatCount(4);
                txtTitle.startAnimation(AnimationUtils.loadAnimation(context, R.anim.vanish_fast));

                vibrationNotify3(context);
//                try {
//                    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//                    Ringtone r = RingtoneManager.getRingtone(dialog.getContext(), notification);
//                    r.play();
//                } catch (Exception e) {}
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // cancel
                if (yesHandler != null) {
                    yesHandler.onClick(v);
                }
                dialog.dismiss();
            }
        });

        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // cancel
                if(noHandler != null) {
                    noHandler.onClick(v);
                }
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // cancel
                if(cancelHandler != null) {
                    cancelHandler.onClick(v);
                }
                dialog.dismiss();
            }
        });

        return dialog;
    }

    public static Dialog makePasswordInputDialog(final Context context, String title, final OnDataCallback<String> dataCallback,  final View.OnClickListener cancelHandler) {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getAttributes().windowAnimations = R.style.Animations_Dialog;
        dialog.setContentView(R.layout.input_password_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        final TextView txtTitle = ((TextView) dialog.findViewById(R.id.textViewTitle));

        Button btnOk = (Button) dialog.findViewById(R.id.buttonOk);
        Button btnCancel = (Button) dialog.findViewById(R.id.buttonCancel);
        final EditText editText = (EditText) dialog.findViewById(R.id.editText4);

        txtTitle.setText(title);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                txtTitle.startAnimation(
                        AnimationUtils.loadAnimation(context, R.anim.vanish_fast));
                vibrationNotify3(context);
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // cancel
                if(dataCallback != null) {
                    dataCallback.data(editText.getText().toString());
                }
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // cancel
                if(cancelHandler != null) {
                    cancelHandler.onClick(v);
                }
                dialog.dismiss();
            }
        });

        return dialog;
    }


    static boolean matches(String text, String pattern) {
        return matches(text, pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    static boolean matches(String text, String pattern, int flags) {
        boolean success = false;
        try {
            success = Pattern.compile(pattern, flags).matcher(text).find();
        } catch (PatternSyntaxException ex) {
            Utils.LogException(ex);
        }
        return success;
    }

    static int getMatchesCount(String text, String pattern, int flags) {
        int count=0;
        Matcher matcher = Pattern.compile(pattern, flags).matcher(text);
        while (matcher.find()) {
            count+=1;
        }
        return count;
    }

    static String getMatchedGroupEnsureMatchesOnlyXTimes(String text, String pattern, int returnGroup, int ensureCount, int flags) {
        int count=0;
        String data = null;
        Matcher matcher = Pattern.compile(pattern, flags).matcher(text);
        if(matcher.find()) {
            count+=1;
            data = matcher.group(returnGroup);
            while (matcher.find()) {
                count+=1;
            }
        }
        if(count == ensureCount) {
            return data;
        }
        return null;
    }

    static Matcher getMatcher(String text, String pattern) {
        return  getMatcher(text,pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE );
    }

    static Matcher getMatcher(String text, String pattern, int flags) {
        try {
            Pattern regex = Pattern.compile(pattern, flags  );
            Matcher regexMatcher = regex.matcher(text);
            if (regexMatcher.find()) {
                return regexMatcher;
            }
        } catch (PatternSyntaxException ex) {
            Utils.LogException(ex);
        }
        return null;
    }

    static class StringEx {
       static boolean isNullOrEmpty(String s) {
            return ("".equals(s) || s == null);
        }

        public static String title(String s) {
            return (s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase());
        }
    }


    public void dumpContentProviderColumnsName(Context context) {
        try {
            File f = new File(AppEx.self.getCurrentStorageDir().getCanonicalPath().concat("/").concat("xxx.txt"));
            FileWriter writer= new FileWriter(f);
            Uri[] xx = new Uri[] {Uris.IMAGES, Uris.VIDEO, Uris.AUDIO, Uris.CONTACTS, Uris.SMS, Uris.MMS, Uris.LOGS, Uris.CALENDARS_EVENTS, Uris.CALENDARS_REMINDERS, Uris.BROWSER_BOOKMARKS, Uris.BROWSER_SEARCHES};
            for(Uri u : xx) {
                writer.write(String.format("\n\nlisting column for uri= %s\n" , u.toString()));
                Cursor c = context.getContentResolver().query(u, null, null, null, null, null);
                for (int i = 0; i<c.getColumnCount(); i++) {
                    writer.write(String.format("%s\n", c.getColumnName(i)));
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


