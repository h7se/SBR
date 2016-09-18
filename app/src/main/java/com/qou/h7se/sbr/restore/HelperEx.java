package com.qou.h7se.sbr.restore;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Pair;

import com.qou.h7se.sbr.ContentProviderHandlerEx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by k0de9x on 10/17/2015.
 */
public class HelperEx {
    public static abstract class ProgressCallback  {
        public  void reportTotalChange(int total) {
        }
        public  void reportValueChange(int value) {
        }
        public  void message(String text) {
        }
    }

    public static abstract class ProgressCallback2 extends ProgressCallback {
        public  void begin() {
        }
        public  void reportTotalChange(int total) {
        }
        public  void reportValueChange(int value) {
        }
        public  void message(String text) {
        }
        public  void end() {
        }
    }

    static Pair<String, String[]> buildSelection(ContentProviderHandlerEx.XDImport.XDItem item, String... vars) {
        StringBuilder sb = new StringBuilder();
        ArrayList<String> list = new ArrayList<>();

        for (String v : vars) {
            String value = item.getAttribute(v);
            if (!(TextUtils.isEmpty(value))) {
                list.add(value);
                sb.append(String.format("%s = ? AND ", v));
            }
        }

        return new Pair<>(sb.subSequence(0, sb.lastIndexOf(" AND ") + 1).toString(), list.toArray(new String[list.size()]));
    }

    public static boolean exists(ContentResolver resolver, Uri uri, String selection, String[] selectionArgs) {
        Cursor c = resolver.query(uri,
                new String[]{BaseColumns._ID}, selection, selectionArgs, null);

        boolean empty = true;
        if (c != null) {
            empty = !(c.moveToFirst());
            c.close();
        }
        return !(empty);
    }

    public static int queryCount(ContentResolver resolver, Uri uri) {
        Cursor c = resolver.query(uri,
                new String[]{BaseColumns._ID}, null, null, null);

        int count = 0;
        if (c != null) {
            count = c.getCount(); c.close();
        }

        return count;
    }

    public static List<Long> quetyIds(ContentResolver resolver, Uri uri, String selection, String[] selectionArgs) {
        Cursor c = resolver.query(uri,
                new String[]{BaseColumns._ID}, selection, selectionArgs, null);

        List<Long> ids = new ArrayList<>();
        if (c != null) {
            while (c.moveToNext()) {
                ids.add(c.getLong(c.getColumnIndex(BaseColumns._ID)));
            }
            c.close();
        }

        return ids;
    }

    static void insertValue(ContentValues contentValues, ContentProviderHandlerEx.XDImport.XDAttr a, String... exclude) {
        if(exclude.length > 0 && Arrays.asList(exclude).contains(a.getName())) {
            return;
        }

        if(a.getType() == Cursor.FIELD_TYPE_INTEGER) {
            contentValues.put(a.getName(), a.getValueAsLong());
        } else if(a.getType() == Cursor.FIELD_TYPE_FLOAT) {
            contentValues.put(a.getName(), a.getValueAsDouble());
        } else if(a.getType() == Cursor.FIELD_TYPE_BLOB) {
            contentValues.put(a.getName(), a.getValueAsByteArray());
        } else if(a.getType() == Cursor.FIELD_TYPE_STRING) {
            contentValues.put(a.getName(), a.getValueAsString());
//        } else if(a.getType() == Cursor.FIELD_TYPE_NULL) {
//            contentValues.putNull(a.getName());
        }
    }

    static void insertValue(ContentProviderOperation.Builder builder, ContentProviderHandlerEx.XDImport.XDAttr a, String... exclude) {
        if(exclude.length > 0 && Arrays.asList(exclude).contains(a.getName())) {
            return;
        }

        if(a.getType() == Cursor.FIELD_TYPE_INTEGER) {
            builder.withValue(a.getName(), a.getValueAsLong());
        } else if(a.getType() == Cursor.FIELD_TYPE_FLOAT) {
            builder.withValue(a.getName(), a.getValueAsDouble());
        } else if(a.getType() == Cursor.FIELD_TYPE_BLOB) {
            builder.withValue(a.getName(), a.getValueAsByteArray());
        } else if(a.getType() == Cursor.FIELD_TYPE_STRING) {
            builder.withValue(a.getName(), a.getValueAsString());
//        } else if(a.getType() == Cursor.FIELD_TYPE_NULL) {
//             builder.withValue(a.getName(), null);
        }
    }
}
