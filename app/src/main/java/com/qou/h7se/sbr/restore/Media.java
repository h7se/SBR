package com.qou.h7se.sbr.restore;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.qou.h7se.sbr.ContentProviderHandlerEx;
import com.qou.h7se.sbr.DataFilterCallback;
import com.qou.h7se.sbr.DataFilterCompleteCallback;
import com.qou.h7se.sbr.GenericCallback2;
import com.qou.h7se.sbr.GenericCallback4;
import com.qou.h7se.sbr.MutableVar;
import com.qou.h7se.sbr.Uris;
import com.qou.h7se.sbr.Utils;

import java.io.File;
import java.util.List;

/**
 * Created by k0de9x on 10/17/2015.
 */

// android.provider.

public class Media {
    Context context;
    ContentResolver resolver;

    public Media(Context context) {
        this.context = context;
        this.resolver = context.getContentResolver();
    }

    public void run(ContentProviderHandlerEx.XDImport.XDItem meta, final String tmpDir, final HelperEx.ProgressCallback progressCallback, final GenericCallback4<Boolean> complete) {
        ContentProviderHandlerEx.XDImport.XDItem.getItemsMatchingPredicate(meta, new DataFilterCallback<ContentProviderHandlerEx.XDImport.XDItem>() {
            @Override
            public boolean include(ContentProviderHandlerEx.XDImport.XDItem item) {
                return (item.tagName.equalsIgnoreCase(ContentProviderHandlerEx.TagNames.DATA_TAG_NAME));
            }
        }, null, null, new DataFilterCompleteCallback<ContentProviderHandlerEx.XDImport.XDItem>() {
            @Override
            public void run(List<Integer> positions, final List<ContentProviderHandlerEx.XDImport.XDItem> result) {

                final MutableVar<Integer> progress =
                        new MutableVar<Integer>(0);
                progressCallback.reportTotalChange(result.size());

                for (final ContentProviderHandlerEx.XDImport.XDItem item : result) {
                    final String dest = item.getAttribute(MediaStore.MediaColumns.DATA);
                    String name = dest.substring(dest.lastIndexOf("/") + 1);
                    String src = tmpDir.concat(name);
                    progressCallback.message("copying files: "
                            + item.getAttribute(MediaStore.MediaColumns.TITLE));

                    Utils.copyFile(new File(src), new File(dest), false, 0, new GenericCallback2() {
                        @Override
                        public void event() {
                            context.sendBroadcast(new Intent(
                                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(dest))));

                            String mime = item.getAttribute(MediaStore.MediaColumns.MIME_TYPE);
                            String data = item.getAttribute(MediaStore.MediaColumns.DATA);
                            String size = item.getAttribute(MediaStore.MediaColumns.SIZE);

                            ContentValues values = new ContentValues();
                            for (ContentProviderHandlerEx.XDImport.XDAttr a : item.attrs) {
                                HelperEx.insertValue(values, a, BaseColumns._ID);
                            }

                            context.getContentResolver().update(
                                    Uris.IMAGES, values, String.format(
                                            "%s = ? AND %s = ? AND %s = ?"
                                            , MediaStore.MediaColumns.MIME_TYPE
                                            , MediaStore.MediaColumns.DATA
                                            , MediaStore.MediaColumns.SIZE)
                                    , new String[]{mime, data, size});

                            progressCallback.reportValueChange(++progress.value);

                            if(progress.value == result.size()) {
                                complete.event(true);
                            }
                        }
                    });
                }
            }
        }, false);
    }

    boolean exists(String displayName, String title, String data, String width, String height, String size) {
        Cursor c = resolver.query(Uris.IMAGES,
                new String[]{BaseColumns._ID}, String.format(
                        "%s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?"
                        , MediaStore.MediaColumns.DISPLAY_NAME
                        , MediaStore.MediaColumns.TITLE
                        , MediaStore.MediaColumns.DATA
                        , MediaStore.MediaColumns.WIDTH
                        , MediaStore.MediaColumns.HEIGHT
                        , MediaStore.MediaColumns.SIZE
                ),
                new String[]{displayName, title, data, width, height, size}, null);

        boolean empty = true;
        if (c != null) {
            empty = !(c.moveToFirst());
            c.close();
        }
        return !(empty);
    }
}
