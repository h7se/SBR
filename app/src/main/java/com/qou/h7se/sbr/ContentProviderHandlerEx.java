package com.qou.h7se.sbr;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.Xml;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by k0de9x on 10/7/2015.
 */
public class ContentProviderHandlerEx {
    // http://developer.android.com/reference/android/content/ContentResolver.html
    // http://developer.android.com/reference/android/provider/CalendarContract.Events.html

    public static class TagNames {
        public  static String ROOT_TAG_NAME = "items";
        public  static String ITEM_TAG_NAME = "meta";
        public  static String DATA_TAG_NAME = "item";
        public  static String KEY_TYPE_SEP = "_";
        public  static String KEY_ATTRIBUTE_NAME = "key";
        public  static String URI_ATTRIBUTE_NAME = "uri";
    }

    public static class XDImport {
        public static XDItem run(File file) {
            XDItem root = new XDItem();

            try {
                if ((file.exists())) {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document document = db.parse(file);
                    NodeList nodes = document.getElementsByTagName(TagNames.ROOT_TAG_NAME);

                    for (int x = 0; x < nodes.getLength(); x++) {
                        Node items = nodes.item(x);
                        if (items.getNodeType() == Node.ELEMENT_NODE) {
                            if(items.hasAttributes() && items.getAttributes().getNamedItem(AppEx.PACKAGE_NAME) != null) {
                                getChildes(root, items);
                            } else {
                                Log.e(AppEx.PACKAGE_NAME, "File is not a valid backup file.");
                                AppEx.self.getApplicationContext().sendBroadcast(
                                        new Intent(Constants.NOTIFICATION_NOT_RECOGNIZED_BACKUP_FILE));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Utils.LogException(e);
            }

            return root;
        }

        static List<XDAttr> getAttributes(Node node) throws UnsupportedEncodingException {
            List<XDAttr> attrs = new ArrayList<>();
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.hasAttributes()) {
                    boolean isDataNode = node.
                            getNodeName().equals(TagNames.DATA_TAG_NAME);
                    NamedNodeMap attributes = node.getAttributes();
                    for (int i = 0, size = attributes.getLength(); i < size; i++) {
                        Node attr = attributes.item(i);
                        String name = attr.getNodeName();
                        String value = URLDecoder.decode(attr.getNodeValue(), StandardCharsets.UTF_8.name());
                        if (isDataNode) {
                            int c = name.lastIndexOf(TagNames.KEY_TYPE_SEP);
                            String key = name.substring(0, c);
                            String type = name.substring(c + 1);
                            attrs.add(new XDAttr(key, value, Integer.valueOf(type)));
                        } else {
                            attrs.add(new XDAttr(name, value, Cursor.FIELD_TYPE_NULL));
                        }
                    }
                }
            }
            return attrs;
        }

        static void getChildes(XDItem item, Node child) throws UnsupportedEncodingException {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                item.tagName = child.getNodeName();
                item.attrs = getAttributes(child);
                NodeList nodes = child.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        XDItem item2 = new XDItem();
                        item2.parent = item;
                        item.subItems.add(item2);
                        getChildes(item2, nodes.item(i));
                    }
                }
            }
        }

        public static class XDAttr {
            private String name;
            private String value; // TODO: declare as object
            private int type;

            public XDAttr(String name, String value, int type) {
                assert type >= 0 && type <= 4;

                this.name = name;
                this.value = value;
                this.type = type;
            }

            public String getName() {
                return this.name;
            }

            public int getType() {
                return this.type;
            }

            public Long getValueAsLong() {
                return Long.parseLong(this.value);
            }

            public Double getValueAsDouble() {
                return Double.parseDouble(this.value);
            }

            public byte[] getValueAsByteArray() {
                try {
                    return this.value.getBytes(StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                    Utils.LogException(e);
                }
                return this.value.getBytes();
            }

            public String getValueAsString() {
                return this.value;
            }
        }

        public static class XDItem {
            public  Uri uri;
            public  String tagName;
            public  List<XDAttr> attrs;
            public  List<XDItem> subItems;
            public  XDItem parent;

            public XDItem(/*Item parent*/) {
                this.parent = null;
                this.attrs = new ArrayList<>();
                this.subItems = new ArrayList<>();
            }

            public boolean hasAttribute(String key) {
                for (XDAttr s : this.attrs) {
                    if (s.getName().equalsIgnoreCase(key)) {
                        return true;
                    }
                }
                return false;
            }

            public String getAttribute(String key) {
                for (XDAttr s : this.attrs) {
                    if (s.getName().equalsIgnoreCase(key)) {
                        return s.value;
                    }
                }
                return null;
            }

            public Long getAttributeAsLong(String key) {
                for (XDAttr s : this.attrs) {
                    if (s.getName().equalsIgnoreCase(key)) {
                        return Long.parseLong(s.value);
                    }
                }
                return null;
            }

            public static List<XDItem> getItemsMatchingPredicate(XDItem start, DataFilterCallback<XDItem> predicate) {
                return getItemsMatchingPredicate(start, predicate, null, null, null, false);
            }

            public static List<XDItem> getItemsMatchingPredicate(XDItem start, DataFilterCallback<XDItem> predicate, DataFilterActionCallback<XDItem> onSuccess, DataFilterActionCallback<XDItem> onFail, DataFilterCompleteCallback<XDItem> onComplete, boolean oneMatchMode) {
                class ItemsMatchingPredicate {
                    private void run(List<Integer> positions, List<XDItem> tmp, XDItem item, DataFilterCallback<XDItem> predicate, DataFilterActionCallback<XDItem> onSuccess, DataFilterActionCallback<XDItem> onFail, boolean oneMatchMode) {
                        for(int i =0, size = item.subItems.size(); i < size; i++) {
                            XDItem entry = item.subItems.get(i);
                            if (predicate.include(entry)) {
                                tmp.add(entry);
                                positions.add(i);

                                if (onSuccess != null) {
                                    onSuccess.run(entry, i);
                                }

                                if (oneMatchMode) {
                                    break;
                                }
                            } else {
                                if (onFail != null) {
                                    onFail.run(entry, i);
                                }
                            }

                            if(entry.subItems.size() > 0) {
                                run(positions, tmp, entry, predicate, onSuccess, onFail, oneMatchMode);
                            }
                        }
                    }
                }

                List<XDItem> tmp = new ArrayList<>();
                List<Integer> positions = new ArrayList<>();
                ItemsMatchingPredicate itemsMatchingPredicate = new ItemsMatchingPredicate();
                itemsMatchingPredicate.run(positions, tmp, start, predicate, onSuccess, onFail, oneMatchMode);
                if (onComplete != null) {
                    onComplete.run(positions, tmp);
                }
                return tmp;
            }
        }
    }


    public static class XDExport {
        Context context;
        XmlSerializer serializer = null;
        StringWriter stringWriter = null;
        List<String> bannedAttrs;

        public XDExport(Context context) {
            this.context = context;
            this.serializer = Xml.newSerializer();
            this.stringWriter = new StringWriter();
            this.bannedAttrs = new ArrayList<>();

            bannedAttrs.add("[(]");
        }

        private void startXml() {
            try {
                serializer.setOutput(stringWriter);
                serializer.startDocument("utf-8", null);
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                serializer.startTag(null, TagNames.ROOT_TAG_NAME);
                serializer.attribute(null, AppEx.PACKAGE_NAME, AppEx.PACKAGE_TAG);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void startTag(String name) {
            try {
                serializer.startTag(null, name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void addAttribute(String key, String value) {
            //if (value != null) {
                try {
                    for (String pattern : bannedAttrs) {
                        if (Utils.matches(key, pattern)) {
                            return;
                        }
                    }

                    serializer.attribute(null, key, (value == null) ? "" : URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            //}
        }

        private void endTag(String name) {
            try {
                serializer.endTag(null, name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void endXml() {
            try {
                serializer.endTag(null, TagNames.ROOT_TAG_NAME);
                serializer.endDocument();
                serializer.flush();
                stringWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void query(Provider provider, final OnXDDataCallback dataCallback, final OnXDProgressCallback progressCallback, OnXDDataReadyCallback callback) {
            startXml();
            dataCallback.enter(provider.uri);
            query(provider, dataCallback, progressCallback);
            dataCallback.exit(provider.uri);
            endXml();

            callback.data(stringWriter.toString());
        }

        public void query(final Provider provider, final OnXDDataCallback dataCallback, final OnXDProgressCallback progressCallback) {
            final MutableVar<Integer> total = new MutableVar<>(0);
            final MutableVar<Integer> current = new MutableVar<>(0);

            class Re {
                void run(Provider subProvider, String id) {

//                    CursorLoader cursorLoader;
//                    if (id == null || subProvider.linkKey == null) {
//                        cursorLoader = new CursorLoader(context, subProvider.uri, null, null, null, null);
//                    } else {
//                        cursorLoader = new CursorLoader(context, subProvider.uri, null, subProvider.linkKey + "= ?", new String[]{"" + id}, null);
//                    }
//                    Cursor cursor = cursorLoader.loadInBackground();
                    Cursor cursor;
                    if (id == null || subProvider.linkKey == null) {
                        cursor = context.getContentResolver().query(subProvider.uri, null, null, null, null);
                    } else {
                        cursor = context.getContentResolver().query(subProvider.uri, null, subProvider.linkKey + "= ?", new String[]{"" + id}, null);
                    }

                    if (cursor != null && cursor.getCount() > 0) {
                        if (null != subProvider.mustHaveColumn) {
                            int index = cursor.getColumnIndex(subProvider.mustHaveColumn);
                            if (index == -1) {
                                return;
                            }
                        }

                        total.value += cursor.getCount();
                        if(progressCallback != null) {
                            progressCallback.reportTotalChange(total.value);
                        }

                        startTag(TagNames.ITEM_TAG_NAME);
                        addAttribute(TagNames.URI_ATTRIBUTE_NAME, subProvider.uri.toString());
                        if (subProvider.linkKey != null) {
                            addAttribute(TagNames.KEY_ATTRIBUTE_NAME, subProvider.linkKey);
//                            int index = subCursor.getColumnIndex(subProvider.linkKey);
//                            if(index != -1) {
//                                try {
//
//                                  //  addAttribute("type", String.valueOf(subCursor.getType(index)));
//                                } catch(Exception e) {
//                                    Utils.LogException(e);
//                                }
//
//                            }
                        }

                        while (cursor.moveToNext()) {
                            // startTag(TagNames.ITEM_TAG_NAME);
                            startTag(TagNames.DATA_TAG_NAME);

                            for (int i = 0, size = cursor.getColumnCount(); i < size; i++) {
                                String key = cursor.getColumnName(i);
                                Object value = null;
                                if (cursor.getType(i) == Cursor.FIELD_TYPE_STRING) {
                                    value = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(i)));
                                } else if (cursor.getType(i) == Cursor.FIELD_TYPE_INTEGER) {
                                    value = cursor.getLong(cursor.getColumnIndex(cursor.getColumnName(i)));
                                } else if (cursor.getType(i) == Cursor.FIELD_TYPE_FLOAT) {
                                    value = cursor.getDouble(cursor.getColumnIndex(cursor.getColumnName(i)));
                                } else if (cursor.getType(i) == Cursor.FIELD_TYPE_BLOB) {
                                    value = cursor.getBlob(cursor.getColumnIndex(cursor.getColumnName(i)));
                                } else if (cursor.getType(i) == Cursor.FIELD_TYPE_NULL) {
                                    value = null;
                                }

                                try {
                                    int type = cursor.getType(i);

                                    if(dataCallback != null) {
                                        dataCallback.data(subProvider.uri, type , key, value);
                                    }

                                    addAttribute(key + TagNames.KEY_TYPE_SEP + String.valueOf(type), (value == null) ? null : value.toString());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e(AppEx.PACKAGE_NAME, e.getMessage());
                                }
                            }

                            String _id = cursor.getString(
                                    cursor.getColumnIndex(BaseColumns._ID));




                            if (subProvider.getLinked().size() > 0) {
                                for (Provider innerProvider : subProvider.getLinked()) {
                                    new Re().run(innerProvider, _id);
                                }
                            }

                            endTag(TagNames.DATA_TAG_NAME);

                            if(progressCallback != null) {
                                progressCallback.reportValueChange(current.value++);
                            }
                            // endTag(TagNames.ITEM_TAG_NAME);
                        }

                        endTag(TagNames.ITEM_TAG_NAME);
                    }

                    if(cursor != null && !(cursor.isClosed())) {
                        cursor.close();
                    }
                }
            }



            new Re().run(provider, null);
        }

        public static void write(final File f, final String data, final GenericCallback2 callback) {
            new Utils.DoAsyncEx2<Void>(null, null, new Runnable() {
                @Override
                public void run() {
                    callback.event();
                }
            }).run(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (f.exists()) {
                        f.delete();
                    } else {
                        f.getParentFile().mkdir();
                    }

//                    BufferedWriter writer = new BufferedWriter( new FileWriter(f));
//                    writer.write(data);
//                    writer.close();

                    FileUtils.writeStringToFile(f, data, StandardCharsets.UTF_8.name());
                    return null;
                }
            });

        }

        public interface OnXDDataCallback {
            void enter(Uri uri);
            void data(Uri uri, int type, String key, Object value);
            void exit(Uri uri);
        }

        public interface OnXDDataReadyCallback {
            void data(String data);
        }

        public interface OnXDProgressCallback {
            // void report(int total, int value);
            void reportTotalChange(int total);
            void reportValueChange(int value);
        }

        public static class Provider {
            Uri uri;
            String mustHaveColumn;
            String columnExpectedValue;
            String linkKey;
            private List<Provider> linked;

            public Provider(Uri uri, String mustHaveColumn, String columnExpectedValue /*type: String*/, String linkKey) {
                this.uri = uri;
                this.mustHaveColumn = mustHaveColumn;
                this.columnExpectedValue = columnExpectedValue;
                this.linkKey = linkKey;
                this.setLinked(new ArrayList<Provider>());
            }

            Provider add(Provider provider) {
                this.getLinked().add(provider);
                return this;
            }

            public List<Provider> getLinked() {
                return linked;
            }

            public void setLinked(List<Provider> linked) {
                this.linked = linked;
            }
        }

        public static class Helpers {
            public static void ExportGeneric(Context context, Uri uri, final OnXDDataCallback dataCallback, final OnXDProgressCallback progressCallback, final OnXDDataReadyCallback dataReadyCallback) {
                final Provider provider = new Provider(uri, null, null, null);

                new ContentProviderHandlerEx.XDExport(context).query(provider, dataCallback, progressCallback, dataReadyCallback);
            }

            public static void Export(final Context context, final Uri u, final OnXDDataCallback dataCallback, final OnXDProgressCallback progressCallback, final OnXDDataReadyCallback dataReadyCallback) {
                new Utils.DoAsyncEx2<Void>(null, null, null).run(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        if (u.equals(Uris.CONTACTS)) {
                            com.qou.h7se.sbr.backup.Contacts.run(context, dataCallback, progressCallback, dataReadyCallback);
                        } else if (u.equals(Uris.LOGS)) {
                            com.qou.h7se.sbr.backup.Logs.run(context, dataCallback, progressCallback, dataReadyCallback);
                        } else if (u.equals(Uris.SAMSUNG_ALARMS)) {
                            com.qou.h7se.sbr.backup.Alarms.run(context, dataCallback, progressCallback, dataReadyCallback);
                        } else if (u.equals(Uris.SMS)) {
                            com.qou.h7se.sbr.backup.Sms.run(context, dataCallback, progressCallback, dataReadyCallback);
                        } else if (u.equals(Uris.MMS)) {
                            com.qou.h7se.sbr.backup.Mms.run(context, dataCallback, progressCallback, dataReadyCallback);
                        } else if (u.equals(Uris.BROWSER_SEARCHES)) {
                            com.qou.h7se.sbr.backup.Searches.run(context, dataCallback, progressCallback, dataReadyCallback);
                        } else if (u.equals(Uris.BROWSER_BOOKMARKS)) {
                            com.qou.h7se.sbr.backup.Bookmarks.run(context, dataCallback, progressCallback, dataReadyCallback);
                        } else if (u.equals(Uris.IMAGES)) {
                            com.qou.h7se.sbr.backup.Images.run(context, dataCallback, progressCallback, dataReadyCallback);
                        } else if (u.equals(Uris.AUDIO)) {
                            com.qou.h7se.sbr.backup.Audio.run(context, dataCallback, progressCallback, dataReadyCallback);
                        } else if (u.equals(Uris.VIDEO)) {
                            com.qou.h7se.sbr.backup.Video.run(context, dataCallback, progressCallback, dataReadyCallback);
                        } else if ((u.equals(Uris.CALENDARS_EVENTS))) {
                            com.qou.h7se.sbr.backup.Events.run(context, dataCallback, progressCallback, dataReadyCallback);
                        } else {
                            Utils.LogException("not implemented");
                        }
                        return null;
                    }
                });
            }
        }
    }

}