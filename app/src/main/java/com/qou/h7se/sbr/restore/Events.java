package com.qou.h7se.sbr.restore;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Pair;

import com.qou.h7se.sbr.ContentProviderHandlerEx;
import com.qou.h7se.sbr.DataFilterCallback;
import com.qou.h7se.sbr.DataFilterCompleteCallback;
import com.qou.h7se.sbr.GenericCallback4;
import com.qou.h7se.sbr.Uris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by k0de9x on 10/20/2015.
 */

// http://developer.android.com/reference/android/provider/CalendarContract.Events.html
public class Events {
    public final static Uri URI = Uris.CALENDARS_EVENTS;

    private ContentResolver resolver;

    public Events(Context context) {
        this.resolver = context.getContentResolver();
    }

    public void run(ContentProviderHandlerEx.XDImport.XDItem meta, final GenericCallback4<Boolean> complete) {
        ContentProviderHandlerEx.XDImport.XDItem.getItemsMatchingPredicate(meta, new DataFilterCallback<ContentProviderHandlerEx.XDImport.XDItem>() {
            @Override
            public boolean include(ContentProviderHandlerEx.XDImport.XDItem item) {
                return (item.tagName.equalsIgnoreCase(ContentProviderHandlerEx.TagNames.DATA_TAG_NAME)
                        && item.parent.tagName.equalsIgnoreCase(ContentProviderHandlerEx.TagNames.ITEM_TAG_NAME)
                        && item.parent.hasAttribute("uri")
                        && item.parent.getAttribute("uri").equalsIgnoreCase(URI.toString()));
            }
        }, null, null, new DataFilterCompleteCallback<ContentProviderHandlerEx.XDImport.XDItem>() {
            @Override
            public void run(List<Integer> positions, List<ContentProviderHandlerEx.XDImport.XDItem> result) {
                ArrayList<String> whiteList = new ArrayList<>(Arrays.asList(new String[]{
                        CalendarContract.Events.CALENDAR_ID
                        , CalendarContract.Events.TITLE
                        , CalendarContract.Events.DESCRIPTION
                        , CalendarContract.Events.DTSTART
                        , CalendarContract.Events.HAS_ALARM
                        , CalendarContract.Events.EVENT_LOCATION
                        , CalendarContract.Events.EVENT_TIMEZONE

                        , CalendarContract.Events.ORGANIZER
                        , CalendarContract.Events.EVENT_COLOR

                        , CalendarContract.Events.DTEND
                        , CalendarContract.Events.LAST_DATE

                        , CalendarContract.Events.EVENT_END_TIMEZONE
                        , CalendarContract.Events.DURATION
                        , CalendarContract.Events.ALL_DAY
                        , CalendarContract.Events.RRULE

                        , CalendarContract.Events.RDATE
                        , CalendarContract.Events.EXRULE
                        , CalendarContract.Events.EXDATE
                        , CalendarContract.Events.STATUS
                        , CalendarContract.Events.ORIGINAL_ID
                        , CalendarContract.Events.ORIGINAL_SYNC_ID
                        , CalendarContract.Events.ORIGINAL_INSTANCE_TIME
                        , CalendarContract.Events.ORIGINAL_ALL_DAY

                       , CalendarContract.Events.AVAILABILITY
                        , CalendarContract.Events.HAS_ATTENDEE_DATA
                        , CalendarContract.Events.IS_ORGANIZER

//                        , CalendarContract.Events.ACCESS_LEVEL
//                        , CalendarContract.Events.GUESTS_CAN_MODIFY
//                        , CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS
//                        , CalendarContract.Events.GUESTS_CAN_SEE_GUESTS
//                        , CalendarContract.Events.CUSTOM_APP_PACKAGE
//                        , CalendarContract.Events.CUSTOM_APP_URI
//                        , CalendarContract.Events.UID_2445
//                        , "visibility"
//                        , "transparency"
                }));

                for (final ContentProviderHandlerEx.XDImport.XDItem item : result) {

                    ContentValues values = new ContentValues();

                    Pair<String, String[]> selection =
                            HelperEx.buildSelection(item
                                    , CalendarContract.Events.CALENDAR_ID
                                    , CalendarContract.Events.DTSTART
                                    , CalendarContract.Events.DTEND
                            );

                    for (ContentProviderHandlerEx.XDImport.XDAttr a : item.attrs) {
                        if(whiteList.contains(a.getName())) {
                            //if(!(a.key.equals(BaseColumns._ID))) {
                            HelperEx.insertValue(values, a);
                            //}
                        }
                    }

                   // java.util.Date d = new java.util.Date(item.getAttribute(CalendarContract.Events.DTEND));

                    if (!(HelperEx.exists(resolver, URI, selection.first, selection.second))) {
                        resolver.insert(URI, values);
                    } else {
                        resolver.update(URI, values, selection.first, selection.second);
                    }
                }
            }
        }, false);

        new Reminders(resolver).run(meta, complete);
    }
}
