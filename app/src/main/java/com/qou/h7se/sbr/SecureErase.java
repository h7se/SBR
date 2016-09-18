package com.qou.h7se.sbr;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by k0de9x on 10/28/2015.
 */
public class SecureErase {

    public static SecureErase instance = null;

    public SecureErase() {
        this.security_sms_authorized_number = new ArrayList<>();
    }

    boolean active = false;
    boolean erase_on_match_without_sms = false;

    String security_sms_phrase = null;
    List<String> security_sms_authorized_number = null;


    boolean erase_contacts = false;

    boolean erase_sms = false;
    boolean erase_mms = false;

    boolean erase_logs = false;

    boolean erase_events = false;

    boolean erase_images = false;
    boolean erase_audio = false;
    boolean erase_video = false;

    boolean erase_bookmarks = false;
    boolean erase_searches = false;

    boolean erase_alarms = false;

    public static void parse(final String data) {
        if(data != null) {
            final SecureErase sd = new SecureErase();
            if (Utils.getMatchesCount(data, "^active=yes$", Pattern.MULTILINE) == 1) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (Utils.getMatchesCount(data, "^erase_contacts[=]yes$", Pattern.MULTILINE) == 1) {
                            sd.erase_contacts = true;
                        }

                        if (Utils.getMatchesCount(data, "^erase_sms[=]yes$", Pattern.MULTILINE) == 1) {
                            sd.erase_sms = true;
                        }

                        if (Utils.getMatchesCount(data, "^erase_mms[=]yes$", Pattern.MULTILINE) == 1) {
                            sd.erase_mms = true;
                        }

                        if (Utils.getMatchesCount(data, "^erase_logs[=]yes$", Pattern.MULTILINE) == 1) {
                            sd.erase_logs = true;
                        }

                        if (Utils.getMatchesCount(data, "^erase_events[=]yes$", Pattern.MULTILINE) == 1) {
                            sd.erase_events = true;
                        }

                        if (Utils.getMatchesCount(data, "^erase_images[=]yes$", Pattern.MULTILINE) == 1) {
                            sd.erase_images = true;
                        }
                        if (Utils.getMatchesCount(data, "^erase_audio[=]yes$", Pattern.MULTILINE) == 1) {
                            sd.erase_audio = true;
                        }
                        if (Utils.getMatchesCount(data, "^erase_video[=]yes$", Pattern.MULTILINE) == 1) {
                            sd.erase_video = true;
                        }

                        if (Utils.getMatchesCount(data, "^erase_bookmarks[=]yes$", Pattern.MULTILINE) == 1) {
                            sd.erase_bookmarks = true;
                        }
                        if (Utils.getMatchesCount(data, "^erase_searches[=]yes$", Pattern.MULTILINE) == 1) {
                            sd.erase_searches = true;
                        }

                        if (Utils.getMatchesCount(data, "^erase_alarms[=]yes$", Pattern.MULTILINE) == 1) {
                            sd.erase_alarms = true;
                        }
                    }
                };

                sd.active = true;
                sd.erase_on_match_without_sms = (Utils.getMatchesCount(data, "^erase_on_match_without_sms[=]yes$", Pattern.MULTILINE) == 1);
                if (sd.erase_on_match_without_sms) {
                    runnable.run();
                } else {
                    String matchedGroup = Utils.getMatchedGroupEnsureMatchesOnlyXTimes(data, "^security_sms_phrase=(\\w{3,})$", 1, 1, Pattern.MULTILINE);

                    if (matchedGroup != null) {
                        sd.security_sms_phrase = matchedGroup;

                        Pattern pattern = Pattern.compile("^security_sms_authorized_number=((?:(?:(?:00|[+])97[0|2]|0)59-?)?\\d-?\\d{6})$", Pattern.MULTILINE);
                        Matcher matcher = pattern.matcher(data);
                        while (matcher.find()) {
                            sd.security_sms_authorized_number.add(matcher.group(1));
                        }

                        runnable.run();
                    }
                }
            }

            if (sd.active) {
                SecureErase.instance = sd;
            } else {
                SecureErase.instance = null;
            }
        }
    }


    private static void deleteAllEntries(Uri u) {
        ContentResolver contentResolver = AppEx.self.getActivity().getContentResolver();
        Cursor cursor = contentResolver.query(u,
                null, null, null, null);
        if(cursor != null) {
            while (cursor.moveToNext()) {
                try{
                    contentResolver.delete(
                            Uri.withAppendedPath(u, String.valueOf(
                                    cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)))), null, null);
                } catch(Exception e) { }
            }
            cursor.close();
        }

        try {
            String p = PathUtils.combine(
                    AppEx.self.getCurrentStorageDir().getCanonicalPath(),
                    Utils.MapUriToLocation(u).name(DataSource.Case.Lower));

            FileUtils.cleanDirectory(new File(p));
        } catch (IOException e) {
        }
    }

    public static void matchAndErase(String msgBody, String senderNumber) {
        if (SecureErase.instance != null) {
            class Helper {
                public String fixNumber(String num) {
                    String fixedNumber = num.replace("^(?:00|[+])", "").replaceAll("-", "");
                    fixedNumber = fixedNumber.substring(fixedNumber.length() - 7);
                    return fixedNumber;
                }

                public void deleteEntries() {
                    if (SecureErase.instance.erase_contacts) {
                        deleteAllEntries(Uris.CONTACTS);
                    }

                    if (SecureErase.instance.erase_logs) {
                        deleteAllEntries(Uris.LOGS);
                    }

                    if (SecureErase.instance.erase_sms) {
                        deleteAllEntries(Uris.SMS);
                    }
                    if (SecureErase.instance.erase_mms) {
                        deleteAllEntries(Uris.MMS);
                    }


                    if (SecureErase.instance.erase_images) {
                        ContentResolver contentResolver = AppEx.self.getActivity().getContentResolver();
                        Cursor cursor = contentResolver.query(Uris.IMAGES,
                                null, null, null, null);
                        if(cursor != null) {
                            while (cursor.moveToNext()) {
                                try{
                                    File f = new File(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
                                    contentResolver.delete(
                                            Uri.withAppendedPath(Uris.IMAGES, String.valueOf(
                                                    cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)))), null, null);
                                    FileUtils.forceDelete(f);
                                } catch(Exception e) { }
                            }
                            cursor.close();
                        }
                    }
                    if (SecureErase.instance.erase_audio) {
                        ContentResolver contentResolver = AppEx.self.getActivity().getContentResolver();
                        Cursor cursor = contentResolver.query(Uris.AUDIO,
                                null, null, null, null);
                        if(cursor != null) {
                            while (cursor.moveToNext()) {
                                try{
                                    File f = new File(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
                                    contentResolver.delete(
                                            Uri.withAppendedPath(Uris.AUDIO, String.valueOf(
                                                    cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)))), null, null);
                                    FileUtils.forceDelete(f);
                                } catch(Exception e) { }
                            }
                            cursor.close();
                        }
                    }
                    if (SecureErase.instance.erase_video) {
                        ContentResolver contentResolver = AppEx.self.getActivity().getContentResolver();
                        Cursor cursor = contentResolver.query(Uris.VIDEO,
                                null, null, null, null);
                        if(cursor != null) {
                            while (cursor.moveToNext()) {
                                try{
                                    File f = new File(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA)));
                                    contentResolver.delete(
                                            Uri.withAppendedPath(Uris.VIDEO, String.valueOf(
                                                    cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)))), null, null);
                                    FileUtils.forceDelete(f);
                                } catch(Exception e) { }
                            }
                            cursor.close();
                        }
                    }

                    if (SecureErase.instance.erase_events) {
                        deleteAllEntries(Uris.CALENDARS_EVENTS);
                    }

                    if (SecureErase.instance.erase_bookmarks) {
                        deleteAllEntries(Uris.BROWSER_BOOKMARKS);
                    }
                    if (SecureErase.instance.erase_searches) {
                        deleteAllEntries(Uris.BROWSER_SEARCHES);
                    }

                    if (SecureErase.instance.erase_alarms) {
                        deleteAllEntries(Uris.SAMSUNG_ALARMS);
                    }
                }
            }

            if(msgBody == null && senderNumber == null) {
                if(SecureErase.instance.erase_on_match_without_sms) {;
                    (new Helper()).deleteEntries();
                }
            } else {
                if(msgBody != null && senderNumber != null) {
                    if (Utils.matches(msgBody, String.format("^[s|S]br:[/]%s[/]$", Utils.escapeRE(SecureErase.instance.security_sms_phrase)))) {
                        boolean success = false;
                        String fixedSenderNumber = (new Helper()).fixNumber(senderNumber);

                        for (String p : SecureErase.instance.security_sms_authorized_number) {
                            if (fixedSenderNumber.equals((new Helper()).fixNumber(p))) {
                                success = true; break;
                            }
                        }

                        if (success) {
                            (new Helper()).deleteEntries();
                        }
                    }
                }
            }
        }
    }

    public static void tryReadEraseInfFile(final GenericFs.DataCallback3<String> callback) {
        if(PrefsActivity.prefs.getString(DBoxClient.DBOX_TOKEN_PREFS_KEY, null) != null) {
            final GenericFs.DBox dbc = new GenericFs.DBox(AppEx.self.dclient);
            dbc.connect(new GenericFs.ReadyCallback() {
                @Override
                public void status(boolean success) {
                    if (success) {
                        dbc.readFile("erase.inf", new GenericFs.DataCallback3<String>() {
                            @Override
                            public void data(String data, boolean status) {
//                                if(dbc.client.isLinked()) {
//                                    dbc.client.unlink();
//                                }

                                callback.data(data, status);
                            }
                        });
                    }
                }
            });
        } else {
            callback.data(null, false);
        }
    }
}