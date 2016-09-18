package com.qou.h7se.sbr;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;

/**
 * Created by k0de9x on 9/22/2015.
 */

public class AppLog {
    public static AppLog instance;


    private LogsAdapter logsAdapter;
    private OnDataCallback<String> onDataCallback = null;
    private Handler handler;

    public AppLog() {
        logsAdapter = new LogsAdapter(AppEx.self.getBaseContext(),
                (new ArrayList<LogsEntry>()));

        handler = new Handler();
    }

    void loadOldLogs() {
        new Utils.DoAsyncEx2<List<LogsEntry>>(new OnDataCallback2<List<LogsEntry>>() {
            @Override
            public void data(@Nullable List<LogsEntry> data) {
                if(data != null) {
                    addAll(data);
                    logsAdapter.notifyDataSetChanged();
                }
            }
        }).run(new Callable<List<LogsEntry>>() {
            @Override
            public List<LogsEntry> call() throws Exception {
                List<LogsEntry> values = new ArrayList<>();
                try {
                    File f = new File(AppEx.self.getCurrentStorageDir().getCanonicalPath().concat("/").concat("logs.txt"));
                    if(f.exists()) {
                        if(f.canRead()) {
                            BufferedReader br = new BufferedReader(new FileReader(f));
                            String line;
                            while ((line = br.readLine()) != null) {
                                values.add(new LogsEntry(System.currentTimeMillis(), line.replaceAll("\\[.+?\\]:\\s*", ""), LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.ARCHIVED));
                            }
                            br.close();
                        }
                    }
                } catch (IOException e) {
                    Utils.LogException(e);
                }
                return values;
            }
        });

    }

    public  void add(String text, LogsEntry.LOG_SOURCE src, LogsEntry.TYPE type) {
        add(text, src, type, false);
    }

    public  void add(String text, LogsEntry.LOG_SOURCE src, LogsEntry.TYPE type, boolean sendBroadcast) {
        add(new LogsEntry(System.currentTimeMillis(), text, src, type), sendBroadcast);
    }

    public void add(final LogsEntry e, final boolean sendBroadcast) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                logsAdapter.addItem(e);

                if (sendBroadcast) {
                    Intent intent = new Intent(Constants.ACTION_IMPORTANT_MSG);
                    intent.putExtra("msg", e.text);
                    AppEx.self.getBaseContext().sendBroadcast(intent);
                }

                if (onDataCallback != null) {
                    onDataCallback.data(e.text);
                }
            }
        });
    }

    public  void addAll(List<LogsEntry> entries) {
        logsAdapter.addAll(entries);

        if(AppLog.instance.onDataCallback != null) {
            for(LogsEntry e : entries) {
                AppLog.instance.onDataCallback.data(e.text);
            }
        }
    }

    public void writeLogsToDisk() {
        try {
            File f = new File(AppEx.self.getCurrentStorageDir().getCanonicalPath().concat("/").concat("logs.txt"));
            if(f.exists()) {
                if(!f.delete()) {
                    Log.e(AppEx.PACKAGE_NAME, "could not delete log file");
                };
            } else {
                if(f.getParentFile().mkdirs()) {
                    if(f.createNewFile()) {
                        Log.e(AppEx.PACKAGE_NAME, "could not create log file");
                    }
                }
            }

            StringBuilder tmp = new StringBuilder();
            Stack<LogsEntry> stk = new Stack<>();
            stk.addAll(logsAdapter.getData(false));
            int max = (Constants.MAX_LOG_FILE_SIZE) * 1024;
            while(tmp.length() < max && !(stk.isEmpty())) {
                tmp.append(stk.pop().toString()).append("\n");
            }

            FileWriter fileWriter = new FileWriter(f, false);
            fileWriter.append(tmp);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            Utils.LogException(e);
        }
    }

    public LogsAdapter getAdapter() {
        return logsAdapter;
    }

    public OnDataCallback<String> getOnDataCallback() {
        return onDataCallback;
    }

    public void setOnDataCallback(OnDataCallback<String> onDataCallback) {
        this.onDataCallback = onDataCallback;
    }
}
