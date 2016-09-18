package com.qou.h7se.sbr;

import java.text.SimpleDateFormat;

/**
 * Created by k0de9x on 10/4/2015.
 */
public class LogsEntry {
    public enum TYPE {
        INFO, WARNING, ERROR, ARCHIVED
    }

    public enum LEVEL {
        USER, DEBUG
    }

    public enum LOG_SOURCE {
        APP , GDRIVE, DBOX , FTP, MISC
    }

    public Long mils;
    public String text;
    public LOG_SOURCE src;
    public TYPE type;
    public String time;

    public LogsEntry(Long mils, String text, LOG_SOURCE src, TYPE type) {
        this.mils = mils;
        this.text = text;
        this.src = src;
        this.type = type;

        this.time = (new SimpleDateFormat("HH:mm:ss").
                format(new java.sql.Date(this.mils)));

        // DateUtils.getRelativeTimeSpanString(lastUpdated.getTime(), now, DateUtils.DAY_IN_MILLIS);
    }

    @Override
    public String toString() {
        long diff = (/*System.currentTimeMillis() - */this.mils);
        long ts = (diff / 1000) % 60 ;
        long tm = (ts / 60) % 60;
        long th = (tm / 60) % 24;
        return String.format("[%s][%s]: %s", (new SimpleDateFormat("HH:mm:ss").
                format(new java.sql.Date(this.mils))), src.name().toLowerCase(), this.text);
    }
}


