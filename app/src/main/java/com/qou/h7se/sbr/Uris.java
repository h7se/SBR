package com.qou.h7se.sbr;

import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by k0de9x on 9/21/2015.
 */



public class Uris {
    private static final String[] globalBlackListedAttrs = new String[]{};
    private static final String[] globalWhiteListedAttrs = new String[]{};
    private static final Map<Uri, String[]> blackListedAttrsDict = new HashMap<>();
    private static final Map<Uri, String[]> whiteListedAttrsDict = new HashMap<>();

    public static final Uri IMAGES = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static final Uri VIDEO = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    public static final Uri AUDIO = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public static final Uri CONTACTS = ContactsContract.Contacts.CONTENT_URI; // ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    public static final Uri SMS = Telephony.Sms.CONTENT_URI; // Uri.parse("content://sms/inbox"); // Telephony.Mms.Inbox
    public static final Uri MMS = Telephony.Mms.CONTENT_URI; // Uri.parse("content://mms/inbox");
    public static final Uri CONVERSATIONS = Uri.parse("content://mms-sms/conversations/");

    public static final Uri LOGS = CallLog.Calls.CONTENT_URI;

    // public static final Uri CALENDARS = CalendarContract.Calendars.CONTENT_URI;
    public static final Uri CALENDARS_EVENTS = CalendarContract.Events.CONTENT_URI;
    public static final Uri CALENDARS_REMINDERS = CalendarContract.Reminders.CONTENT_URI;

    public static Uri BROWSER_BOOKMARKS = Uri.parse("content://browser/bookmarks");
    public static Uri BROWSER_SEARCHES = Uri.parse("content://browser/searches");

    public static Uri SAMSUNG_ALARMS = Uri.parse("content://com.samsung.sec.android.clockpackage/alarm");

    public static Uri FILES_EXTERNAL = MediaStore.Files.getContentUri("external");

    static {
        if(BROWSER_BOOKMARKS == null) {
            BROWSER_BOOKMARKS = Uri.parse("content://com.android.chrome.browser/bookmarks");
//            if(BROWSER_BOOKMARKS == null) { // samsung
//                BROWSER_BOOKMARKS = Uri.parse("content://com.sec.android.app.sbrowser.browser/bookmarks");
//            }
        }

        if(BROWSER_SEARCHES == null) {
            BROWSER_SEARCHES = Uri.parse("content://com.android.chrome.browser/searches");
        }
    }
}
