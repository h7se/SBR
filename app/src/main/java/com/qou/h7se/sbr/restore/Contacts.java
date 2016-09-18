package com.qou.h7se.sbr.restore;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.qou.h7se.sbr.ContentProviderHandlerEx;
import com.qou.h7se.sbr.DataFilterActionCallback;
import com.qou.h7se.sbr.DataFilterCallback;
import com.qou.h7se.sbr.DataFilterCompleteCallback;
import com.qou.h7se.sbr.GenericCallback4;
import com.qou.h7se.sbr.MimeTypeTuple;
import com.qou.h7se.sbr.NameValueTuple;
import com.qou.h7se.sbr.Uris;
import com.qou.h7se.sbr.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by k0de9x on 10/17/2015.
 */
public class Contacts {

    ContentResolver resolver;

    public Contacts(Context context) {
        this.resolver = context.getContentResolver();
    }

    public void run(ContentProviderHandlerEx.XDImport.XDItem meta, final GenericCallback4<Boolean> complete) {
        DataFilterCallback<ContentProviderHandlerEx.XDImport.XDItem> predicate = new DataFilterCallback<ContentProviderHandlerEx.XDImport.XDItem>() {
            @Override
            public boolean include(ContentProviderHandlerEx.XDImport.XDItem item) {
                return (item.tagName.equalsIgnoreCase(ContentProviderHandlerEx.TagNames.DATA_TAG_NAME)
                        && item.parent.tagName.equalsIgnoreCase(ContentProviderHandlerEx.TagNames.ITEM_TAG_NAME)
                        && item.parent.hasAttribute("uri")
                        && item.parent.getAttribute("uri").equalsIgnoreCase(Uris.CONTACTS.toString())
                );
            }
        };

        //  int count = XDItem.getItemsMatchingPredicate(meta, predicate, null, null, null, false).size();

        ContentProviderHandlerEx.XDImport.XDItem.getItemsMatchingPredicate(meta, predicate, null, null, new DataFilterCompleteCallback<ContentProviderHandlerEx.XDImport.XDItem>() {
            @Override
            public void run(List<Integer> positions, List<ContentProviderHandlerEx.XDImport.XDItem> result) {
                for (ContentProviderHandlerEx.XDImport.XDItem item : result) {
                    parseXDItem(resolver, item);
                }
            }
        }, false);

        complete.event(true);
    }

    static void filter(final ContentProviderHandlerEx.XDImport.XDItem item, final ArrayList<ContentProviderOperation> ops,
                       final int id, final MimeTypeTuple mimeTypeTuple, final String[] columns) {

        ContentProviderHandlerEx.XDImport.XDItem.getItemsMatchingPredicate(item, new DataFilterCallback<ContentProviderHandlerEx.XDImport.XDItem>() {
            @Override
            public boolean include(ContentProviderHandlerEx.XDImport.XDItem item) {
                return item.hasAttribute(ContactsContract.Data.MIMETYPE) &&
                        (item.getAttribute(ContactsContract.Data.MIMETYPE).equals(mimeTypeTuple.mime));
            }
        }, new DataFilterActionCallback<ContentProviderHandlerEx.XDImport.XDItem>() {
            @Override
            public void run(ContentProviderHandlerEx.XDImport.XDItem item, int position) {
                ops.add(getBuilder(item, id, mimeTypeTuple, columns));
            }
        }, null, null, false);
    }

    static ContentProviderOperation getBuilder(ContentProviderHandlerEx.XDImport.XDItem meta, final int id, final MimeTypeTuple mimeTypeTuple, final String[] columns) {
        ContentProviderOperation.Builder builder;
        final List<NameValueTuple> entries = new ArrayList<>();


        for (String column : columns) {
            String value = meta.getAttribute(column);
            if (value != null) {
                entries.add(new NameValueTuple(column, value));
            }
        }

        if (id != -1) {
            String typeValue = null;
            if (mimeTypeTuple.type != null && meta.hasAttribute(mimeTypeTuple.type)) {
                typeValue = meta.getAttribute(mimeTypeTuple.type);
            }
            if (typeValue != null) {
                builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(ContactsContract.Data.RAW_CONTACT_ID +
                                        " = ? " + " AND " + ContactsContract.Data.MIMETYPE + " = ?" + " AND " + mimeTypeTuple.type + " = ?",
                                new String[]{String.valueOf(id), mimeTypeTuple.mime, typeValue});
            } else {
                builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(ContactsContract.Data.RAW_CONTACT_ID +
                                        " = ? " + " AND " + ContactsContract.Data.MIMETYPE + " = ?",
                                new String[]{String.valueOf(id), mimeTypeTuple.mime});
            }
        } else {
            builder = (ContentProviderOperation
                    .newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0 /*rawContactId*/)
                    .withValue(ContactsContract.Data.MIMETYPE, mimeTypeTuple.mime));
        }

        for (NameValueTuple t : entries) {
            builder.withValue(t.name, t.value);
        }

        return builder.build();
    }

    static int contactExists(ContentResolver resolver, String name) {
        Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.Data.RAW_CONTACT_ID},
                ContactsContract.Contacts.DISPLAY_NAME + " = ? ", new String[]{name}, null);

        int id = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                id = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID)));
            }
            cursor.close();
        }
        return id;
    }

    static void parseXDItem(ContentResolver resolver, ContentProviderHandlerEx.XDImport.XDItem item) {
        final ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        String oldName = item.
                getAttribute(ContactsContract.Contacts.DISPLAY_NAME);
        int id = contactExists(resolver, oldName);

        // withValueBackReference ??
        if (id != -1) { // update

        } else { // insert
            ops.add(ContentProviderOperation
                    .newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                    .withValue(ContactsContract.RawContacts.AGGREGATION_MODE,
                            ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
                    .build());
        }

        Map<MimeTypeTuple, String[]> map = new HashMap<>();

        map.put(new MimeTypeTuple(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                        null),
                new String[]{
                        ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                        ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
                        ContactsContract.CommonDataKinds.StructuredName.PREFIX,
                        ContactsContract.CommonDataKinds.StructuredName.SUFFIX
                });

        map.put(new MimeTypeTuple(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE),
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                });

        map.put(new MimeTypeTuple(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Email.TYPE),
                new String[]{
                        ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.DATA
                });

        map.put(new MimeTypeTuple(ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Website.TYPE),
                new String[]{
                        ContactsContract.CommonDataKinds.Website.TYPE,
                        ContactsContract.CommonDataKinds.Website.URL
                });

        map.put(new MimeTypeTuple(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Organization.TYPE),
                new String[]{
                        ContactsContract.CommonDataKinds.Organization.TYPE,
                        ContactsContract.CommonDataKinds.Organization.COMPANY,
                        ContactsContract.CommonDataKinds.Organization.DEPARTMENT,
                        ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION,
                        ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION,
                        ContactsContract.CommonDataKinds.Organization.PHONETIC_NAME,
                        ContactsContract.CommonDataKinds.Organization.SYMBOL,
                        ContactsContract.CommonDataKinds.Organization.TITLE
                });

        map.put(new MimeTypeTuple(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
                        null),
                new String[]{
                        ContactsContract.CommonDataKinds.Note.NOTE
                });


        map.put(new MimeTypeTuple(ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Event.TYPE),
                new String[]{
                        ContactsContract.CommonDataKinds.Event.TYPE,
                        ContactsContract.CommonDataKinds.Event.START_DATE,
                        ContactsContract.CommonDataKinds.Event.DATA
                });

        map.put(new MimeTypeTuple(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Nickname.TYPE),
                new String[]{
                        ContactsContract.CommonDataKinds.Nickname.TYPE,
                        ContactsContract.CommonDataKinds.Nickname.NAME
                });

        map.put(new MimeTypeTuple(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.TYPE),
                new String[]{
                        ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                        ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                        ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
                        ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD,
                        ContactsContract.CommonDataKinds.StructuredPostal.POBOX,
                        ContactsContract.CommonDataKinds.StructuredPostal.REGION,
                        ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                        ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE
                });

        for (MimeTypeTuple k : map.keySet()) {
            filter(item, ops, id, k, map.get(k));
        }

        try {
            ContentProviderResult[] results = resolver.applyBatch(ContactsContract.AUTHORITY, ops);
//            for (ContentProviderResult r : results) {
//                AppLog.instance.add(r.toString(), LogsEntry.LOG_SOURCE.APP, LogsEntry.TYPE.INFO);
//            }
        } catch (Exception e) {
            Utils.LogException(e);
        }
    }
}
