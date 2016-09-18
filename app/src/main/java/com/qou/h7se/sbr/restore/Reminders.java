package com.qou.h7se.sbr.restore;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.util.Pair;

import com.qou.h7se.sbr.ContentProviderHandlerEx;
import com.qou.h7se.sbr.DataFilterCallback;
import com.qou.h7se.sbr.DataFilterCompleteCallback;
import com.qou.h7se.sbr.GenericCallback4;
import com.qou.h7se.sbr.Uris;

import java.util.List;

/**
 * Created by k0de9x on 10/22/2015.
 */

public class Reminders {
    public final static Uri URI = Uris.CALENDARS_REMINDERS;

    private ContentResolver resolver;

    public Reminders(ContentResolver resolver) {
        this.resolver = resolver;
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
                for (final ContentProviderHandlerEx.XDImport.XDItem item : result) {

                    ContentValues values = new ContentValues();

                    Pair<String, String[]> selection =
                            HelperEx.buildSelection(item
                                    , CalendarContract.Reminders.EVENT_ID
                                    , CalendarContract.Reminders.MINUTES
                                    , CalendarContract.Reminders.METHOD
                            );


                    for (ContentProviderHandlerEx.XDImport.XDAttr a : item.attrs) {
                        HelperEx.insertValue(values, a, BaseColumns._ID);
                    }

                    if (!(HelperEx.exists(resolver, URI, selection.first, selection.second))) {
                        resolver.insert(URI, values);
                    } else {
                        resolver.update(URI, values, selection.first, selection.second);
                    }
                }
            }
        }, false);

        complete.event(true);
    }
}
