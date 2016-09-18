package com.qou.h7se.sbr.restore;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Telephony;
import android.util.Pair;

import com.qou.h7se.sbr.ContentProviderHandlerEx;
import com.qou.h7se.sbr.DataFilterCallback;
import com.qou.h7se.sbr.DataFilterCompleteCallback;
import com.qou.h7se.sbr.GenericCallback4;
import com.qou.h7se.sbr.Uris;
import com.qou.h7se.sbr.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by k0de9x on 10/17/2015.
 */

// android.provider.

public class Sms {

    ContentResolver resolver;

    public Sms(Context context) {
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
                    Pair<String, String[]> selection =
                            HelperEx.buildSelection(item
                                    , Telephony.Sms.DATE
                                    , Telephony.Sms.ADDRESS
                                    , Telephony.Sms.TYPE);

                    if (!(HelperEx.exists(resolver, Uris.SMS, selection.first, selection.second))) {
                        builder = ContentProviderOperation
                                .newInsert(Uris.SMS);
                    } else {
                        builder = ContentProviderOperation
                                .newUpdate(Uris.SMS)
                                .withSelection(selection.first, selection.second);
                    }

                    List<String> whiteList = new ArrayList<>(
                            Arrays.asList(new String[]{Telephony.TextBasedSmsColumns.ADDRESS,
                                    Telephony.TextBasedSmsColumns.BODY,
                                    Telephony.TextBasedSmsColumns.TYPE,
                                    Telephony.TextBasedSmsColumns.SUBJECT,
                                    Telephony.TextBasedSmsColumns.DATE,
                                    Telephony.TextBasedSmsColumns.DATE_SENT,
                                    Telephony.TextBasedSmsColumns.READ,
                                    Telephony.TextBasedSmsColumns.SEEN,
                                    Telephony.TextBasedSmsColumns.SERVICE_CENTER,
                                    Telephony.TextBasedSmsColumns.STATUS,
                                    Telephony.TextBasedSmsColumns.ERROR_CODE,
                                    Telephony.TextBasedSmsColumns.LOCKED,
                                    Telephony.TextBasedSmsColumns.PERSON,
                                    Telephony.TextBasedSmsColumns.PROTOCOL}));

                    for (ContentProviderHandlerEx.XDImport.XDAttr a : item.attrs) {
                        if(whiteList.contains(a.getName())) {
                            //if(!(a.key.equals(BaseColumns._ID))) {
                            HelperEx.insertValue(builder, a);
                            //}
                        }
                    }

                    ops.add(builder.build());
                }

                try {
                    ContentProviderResult[] results = resolver.applyBatch(Uris.SMS.getAuthority(), ops);
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
