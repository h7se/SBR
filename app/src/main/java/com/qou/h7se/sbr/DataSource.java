package com.qou.h7se.sbr;

/**
 * Created by k0de9x on 9/21/2015.
 */

public enum DataSource {
    CONTACTS, LOGS, MESSAGES, GALLERY, EVENTS, BROWSER, ALARMS;

    enum Case {
        Lower, Upper, Title
    }

    public String name(Case c) {
        if(c == Case.Lower) {
            return this.name().toLowerCase();
        } else if(c == Case.Upper) {
            return this.name().toUpperCase();
        } else if(c == Case.Title) {
            return (this.name().substring(0, 1).toUpperCase() + this.name().substring(1).toLowerCase());
        }
        return this.name();
    }
}

