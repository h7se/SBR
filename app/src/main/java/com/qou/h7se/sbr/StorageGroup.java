package com.qou.h7se.sbr;

/**
 * Created by k0de9x on 9/29/2015.
 */
public class StorageGroup {
    enum Types {
        LOCAL, FTP, GOOGLE_DRIVE, DROP_BOX, SDCARD
    }

    public static abstract class ConnectionStatus {
        private boolean status;

        abstract String id();
        abstract void connection(boolean success);

        boolean removeIf() {
            return false;
        }

        boolean notifyIf() {
            return true;
        }

        boolean getStatus() {
            return this.status;
        }

        ConnectionStatus setStatus(boolean success) {
            this.status = success;
            if(notifyIf()) {
                connection(success);
            }
            return this;
        }
    }
}
