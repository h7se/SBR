package com.qou.h7se.sbr.restore;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.BaseColumns;
import android.util.Pair;

import com.qou.h7se.sbr.ContentProviderHandlerEx;
import com.qou.h7se.sbr.DataFilterCallback;
import com.qou.h7se.sbr.DataFilterCompleteCallback;
import com.qou.h7se.sbr.GenericCallback4;
import com.qou.h7se.sbr.Uris;
import com.qou.h7se.sbr.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by k0de9x on 10/17/2015.
 */

// android.provider.

public class Searches {

    ContentResolver resolver;

    public Searches(Context context) {
        this.resolver = context.getContentResolver();
    }

    public void run(ContentProviderHandlerEx.XDImport.XDItem meta, final GenericCallback4<Boolean> complete) {
        ContentProviderHandlerEx.XDImport.XDItem.getItemsMatchingPredicate(meta, new DataFilterCallback<ContentProviderHandlerEx.XDImport.XDItem>() {
            @Override
            public boolean include(ContentProviderHandlerEx.XDImport.XDItem item) {
                return (item.tagName.equalsIgnoreCase(ContentProviderHandlerEx.TagNames.DATA_TAG_NAME));
            }
        }, null, null, new DataFilterCompleteCallback<ContentProviderHandlerEx.XDImport.XDItem>() {
            @Override
            public void run(List<Integer> positions, List<ContentProviderHandlerEx.XDImport.XDItem> result) {
                ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                ContentProviderOperation.Builder builder = null;

                for (ContentProviderHandlerEx.XDImport.XDItem item : result) {
                    Pair<String, String[]> selection = HelperEx.buildSelection(item, "date", "search");

                    if (!(HelperEx.exists(resolver, Uris.BROWSER_SEARCHES, selection.first, selection.second))) {
                        builder = ContentProviderOperation
                                .newInsert(Uris.BROWSER_SEARCHES);
                    } else {
                        builder = ContentProviderOperation
                                .newUpdate(Uris.BROWSER_SEARCHES)
                                .withSelection(selection.first, selection.second);
                    }

                    for (ContentProviderHandlerEx.XDImport.XDAttr a : item.attrs) {
                        HelperEx.insertValue(builder, a, BaseColumns._ID);
                    }

                    ops.add(builder.build());
                }

                try {
                    ContentProviderResult[] results = resolver.applyBatch(Uris.BROWSER_SEARCHES.getAuthority(), ops);
//                    for (ContentProviderResult r : results) {
//                        AppLog.instance.add(r.toString(), LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.INFO);
//                    }
                } catch (Exception e) {
                    Utils.LogException(e);
                }

                complete.event(true);
            }
        }, false);
    }
}
