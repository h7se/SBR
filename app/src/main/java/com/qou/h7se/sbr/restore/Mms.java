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

public class Mms {

    ContentResolver resolver;

    public Mms(Context context) {
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

                List<String> whiteList = new ArrayList<>(
                        Arrays.asList(new String[]{
                                Telephony.BaseMmsColumns.SEEN,
                                Telephony.BaseMmsColumns.STATUS,
                                Telephony.BaseMmsColumns.SUBJECT,
                                Telephony.BaseMmsColumns.SUBJECT_CHARSET,
                                Telephony.BaseMmsColumns.MESSAGE_TYPE,
                                Telephony.BaseMmsColumns.MMS_VERSION,
                                Telephony.BaseMmsColumns.DATE,
                                Telephony.BaseMmsColumns.DATE_SENT,
                                Telephony.BaseMmsColumns.READ,
                                Telephony.BaseMmsColumns.MESSAGE_ID,
                                Telephony.BaseMmsColumns.CONTENT_TYPE,
                                Telephony.BaseMmsColumns.DELIVERY_REPORT,
                                Telephony.BaseMmsColumns.DELIVERY_TIME,
                                Telephony.BaseMmsColumns.CONTENT_LOCATION,
                                //Telephony.BaseMmsColumns.THREAD_ID,
                                Telephony.BaseMmsColumns.PRIORITY,
                                Telephony.BaseMmsColumns.MESSAGE_SIZE,
                                Telephony.BaseMmsColumns.EXPIRY,
                                Telephony.BaseMmsColumns.LOCKED,
                                Telephony.BaseMmsColumns.READ_REPORT,
                                Telephony.BaseMmsColumns.READ_STATUS,
                                Telephony.BaseMmsColumns.REPORT_ALLOWED,
                                Telephony.BaseMmsColumns.RESPONSE_STATUS,
                                Telephony.BaseMmsColumns.RESPONSE_TEXT,
                                Telephony.BaseMmsColumns.RETRIEVE_STATUS,
                                Telephony.BaseMmsColumns.RETRIEVE_TEXT,
                                Telephony.BaseMmsColumns.RETRIEVE_TEXT_CHARSET,
                                Telephony.BaseMmsColumns.TEXT_ONLY,
                                Telephony.BaseMmsColumns.TRANSACTION_ID,
                                Telephony.BaseMmsColumns.CONTENT_CLASS,
                                Telephony.BaseMmsColumns.MESSAGE_CLASS,
                                Telephony.BaseMmsColumns.MESSAGE_BOX
                        }));

                for (ContentProviderHandlerEx.XDImport.XDItem item : result) {
                    Pair<String, String[]> selection =
                            HelperEx.buildSelection(item
                                    , Telephony.Mms.DATE
                                    , Telephony.Mms.MESSAGE_SIZE
                                    , Telephony.Mms.MESSAGE_TYPE);

                    if (!(HelperEx.exists(resolver, Uris.MMS, selection.first, selection.second))) {
                        builder = ContentProviderOperation
                                .newInsert(Uris.MMS);
                    } else {
                        builder = ContentProviderOperation
                                .newUpdate(Uris.MMS)
                                .withSelection(selection.first, selection.second);
                    }

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
                    ContentProviderResult[] results = resolver.applyBatch(Uris.MMS.getAuthority(), ops);
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
