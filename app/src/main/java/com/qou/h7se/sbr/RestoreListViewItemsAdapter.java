package com.qou.h7se.sbr;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Comparator;
import java.util.List;

/**
 * Created by k0de9x on 9/20/2015.
 */

public class RestoreListViewItemsAdapter extends CustomBaseAdapter<RestoreListViewItem> {
    Context activity;
    private ViewHolder holder = null;
    private LayoutInflater inflater = null;

    @Override
    public CustomBaseAdapter<RestoreListViewItem>.ViewHolder getHolder() {
        return holder;
    }

    private final class ViewHolder extends CustomBaseAdapter.ViewHolder {
        TextView txt1;
        TextView txtDate;
        TextView txtSize;

        ImageView img1;
        ImageView img2;
        ImageView img3;
        ImageView img4;
        ImageView img5;

        ProgressBar pgr1;
    }

    public RestoreListViewItemsAdapter(Context activity, List<RestoreListViewItem> data) {
        super(data);

        this.activity = activity;
        this.inflater = (LayoutInflater) this.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        setComparator(new Comparator<RestoreListViewItem>() {
            @Override
            public int compare(RestoreListViewItem o1, RestoreListViewItem o2) {
                if (o1.si.equals(null) || o1.si.date == (null) || o2.si.equals(null) || o2.si.date == (null)) {
                    return 0;
                }

                return o1.si.date.after(o1.si.date) ? 1 : -1;
            }
        });
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.restore_checked_listview_entry_2, null);
            assert convertView != null;

            holder.itemView = convertView;

            holder.txt1 = (TextView) convertView.findViewById(R.id.textView1);
            holder.txtDate = (TextView) convertView.findViewById(R.id.textViewSepHor);
            holder.txtSize = (TextView) convertView.findViewById(R.id.textView4);

            holder.img1 = (ImageView) convertView.findViewById(R.id.imageView);
            holder.img2 = (ImageView) convertView.findViewById(R.id.imageView2);
            holder.img3 = (ImageView) convertView.findViewById(R.id.imageView3);
            holder.img4 = (ImageView) convertView.findViewById(R.id.imageView4);
            holder.img5 = (ImageView) convertView.findViewById(R.id.imageView5);

            holder.pgr1 = (ProgressBar) convertView.findViewById(R.id.progressBar2);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final RestoreListViewItem item = getItem(position);

        if (item != null) {
            holder.img2.setVisibility(View.VISIBLE);

            if (item.si.type == StorageItem.ItemType.LOCAL_GROUP ||
                    item.si.type == StorageItem.ItemType.FTP_GROUP ||
                    item.si.type == StorageItem.ItemType.GOOGLE_GROUP ||
                    item.si.type == StorageItem.ItemType.DROPBOX_GROUP ||
                    item.si.type == StorageItem.ItemType.SDCARD_GROUP ) {

                holder.img2.setImageResource(MapGroupToDrawable(item.si.groupType));
            } else if (item.si.type == StorageItem.ItemType.DIR)  {
                holder.img2.setImageResource(MapTitleToDrawable_DIR(item.si.title.toLowerCase()));
            } else if (item.si.type == StorageItem.ItemType.ZIP_FILE) {
                holder.img2.setImageResource(R.drawable.zip_96);
            } else if (item.si.type == StorageItem.ItemType.IN_ZIP_FILE_ITEM) {
                holder.img2.setImageResource(MapTitleToDrawable(item.si.title.toLowerCase()));
            } else {
                holder.img2.setVisibility(View.GONE);
            }

            if (item.si.type == StorageItem.ItemType.LOCAL_GROUP ||
                    item.si.type == StorageItem.ItemType.FTP_GROUP ||
                    item.si.type == StorageItem.ItemType.GOOGLE_GROUP ||
                    item.si.type == StorageItem.ItemType.DROPBOX_GROUP ||
                    item.si.type == StorageItem.ItemType.SDCARD_GROUP ) {

                holder.img3.setVisibility(View.GONE);
            } else {
                holder.img3.setImageResource(MapGroupToDrawable(item.si.groupType));
                holder.img3.setVisibility(View.VISIBLE);
            }

            holder.txt1.setText(item.si.title);

            if (item.si.date == null) {
                (holder.txtDate).setVisibility(View.GONE);
            } else {
                holder.txtDate.setText(String.format("( %s )", item.si.date.toString()));
                holder.txtDate.setVisibility(View.VISIBLE);
            }

            if (item.getChecked()) {
                convertView.setBackgroundResource(R.drawable.a12_row_selector);

                holder.img1.setImageResource(R.drawable.check1);
                // holder.img1.setVisibility(View.VISIBLE);
            } else {
                convertView.setBackgroundResource(R.drawable.a10_row_selector);

                // holder.img1.setImageResource(R.drawable.check0);
                // holder.img1.setVisibility(View.GONE);
            }

            if (item.getCheckBoxVisible()) {
                holder.img1.setVisibility(View.VISIBLE);
            } else {
                holder.img1.setVisibility(View.GONE);
            }

            holder.img4.setVisibility(View.GONE);
            holder.img5.setVisibility(View.GONE);

            convertView.setAlpha(1.0f);
            boolean containsAttrFileSize =  item.si.<Boolean>containsProperty(StorageItem.Constants.PropertyNames.FILE_SIZE);
            boolean containsAttrCountEntries =  item.si.<Integer>containsProperty(StorageItem.Constants.PropertyNames.COUNT_ENTRIES);
            if(containsAttrFileSize || containsAttrCountEntries) {
                holder.txtSize.setVisibility(View.VISIBLE);
                double size = -1;
                if(containsAttrFileSize) {
                    size = ((float)item.si.<Long>getProperty(StorageItem.Constants.PropertyNames.FILE_SIZE)) / 1024.0 / 1024.0;
                    holder.txtSize.setText(String.format(size > 1 ? "%.1f mb" : "%.3f mb", size));
                }

                if(containsAttrCountEntries && (size == -1)) {
                    int count = item.si.<Integer>getProperty(StorageItem.Constants.PropertyNames.COUNT_ENTRIES);
                    holder.txtSize.setText(String.format("( %d )", count));
                    if(count == 0) {
                        boolean containsAttrContainFolders = item.si.<Boolean>containsProperty(StorageItem.Constants.PropertyNames.CONTAIN_FOLDERS);
                        if(containsAttrContainFolders) {
                            if(!(item.si.<Boolean>getProperty(StorageItem.Constants.PropertyNames.CONTAIN_FOLDERS))) {
//                                holder.txtSize.setText("( empty )");
                                convertView.setAlpha(0.35f);
                            } else {
                                holder.img4.setVisibility(View.VISIBLE);
                                holder.img4.setImageResource(R.drawable.file175);
                            }
                        }
                    }
                }
            } else {
                holder.txtSize.setVisibility(View.GONE);
            }


            boolean containsAttrZipFileComment = item.si.<Boolean>containsProperty(StorageItem.Constants.PropertyNames.ZIP_FILE_COMMENT);
            if(containsAttrZipFileComment) {
                String comment = item.si.<String>getProperty(StorageItem.Constants.PropertyNames.ZIP_FILE_COMMENT);
                if(!TextUtils.isEmpty(comment)) {
                    holder.img4.setVisibility(View.VISIBLE);
                    holder.img4.setImageResource(R.drawable.chat78);
                }
            }

            boolean containsAttrZipFileProtected = item.si.<Boolean>containsProperty(StorageItem.Constants.PropertyNames.ZIP_FILE_PROTECTED);
            if(containsAttrZipFileProtected) {
                boolean encrypted = item.si.<Boolean>getProperty(StorageItem.Constants.PropertyNames.ZIP_FILE_PROTECTED);
                if(encrypted) {
                    holder.img5.setVisibility(View.VISIBLE);
                    holder.img5.setImageResource(R.drawable.treasure2);

                    if (!item.getChecked()) {
                        convertView.setBackgroundResource(R.drawable.a10_row_selector_copy);
                    }
                }
            }

            if (item.getBusy()) {
                holder.pgr1.setVisibility(View.VISIBLE);
                convertView.setEnabled(false);
            } else {
                holder.pgr1.setVisibility(View.GONE);
                convertView.setEnabled(true);
            }

            //if(holder.stype == BackupRestoreSource.RestoreBackupSourceType.LOCAL_ZIP_FILE) {
            //Animation animation = AnimationUtils.loadAnimation(activity, R.anim.popup_show);
            //convertView.startAnimation(animation);
            // }
        }

        if(getTrash().contains(item)) {
            AnimatorSets.attachEndCallback(AnimatorSets.getFadeOutAnimatorSet(convertView, 500), new GenericCallback2() {
                @Override
                public void event() {
                    removeItem(item, true);
                }
            }).start();
        }
        return convertView;
    }

    private static int MapTitleToDrawable(String text) {
        int id = R.drawable.unknown_doc_96;
        if (text.startsWith("message")) {
            id = R.drawable.messages_96;
        } else if (text.startsWith("logs")) {
            id = R.drawable.logs_96;
        } else if (text.startsWith("contacts")) {
            id = R.drawable.contacts_96;
        } else if (text.startsWith("calendar")) {
            id = R.drawable.calendar_96;
        } else if (text.startsWith("gallery")) {
            id = R.drawable.gallery_96;
        } else if (text.startsWith("browser")) {
            id = R.drawable.chrome_96;

        } else if (text.startsWith("audio")) {
            id = R.drawable.music_96;
        } else if (text.startsWith("video")) {
            id = R.drawable.movies_large;
        } else if (text.startsWith("images")) {
            id = R.drawable.images;
        }
        return id;
    }

    private static int MapTitleToDrawable_DIR(String text) {
        int id = R.drawable.folder_unknown_doc_96;
        if (text.startsWith("message")) {
            id = R.drawable.folder_messages_96;
        } else if (text.startsWith("logs")) {
            id = R.drawable.folder_logs_96;
        } else if (text.startsWith("contacts")) {
            id = R.drawable.folder_contacts_96;
        } else if (text.startsWith("calendar")) {
            id = R.drawable.folder_calendar_96;
        } else if (text.startsWith("gallery")) {
            id = R.drawable.folder_gallery_96;
        } else if (text.startsWith("browser")) {
            id = R.drawable.folder_chrome_96;
        }
        return id;
    }

    public static int MapGroupToDrawable(StorageGroup.Types type) {
        int id = R.drawable.unknown_96;
        if (type == StorageGroup.Types.LOCAL) {
            id = R.drawable.local_96;
        } else if (type == StorageGroup.Types.FTP) {
            id = R.drawable.ftp_96;
        } else if (type == StorageGroup.Types.GOOGLE_DRIVE) {
            id = R.drawable.google_drive_96;
        } else if (type == StorageGroup.Types.DROP_BOX) {
            id = R.drawable.dropbox_96;
        } else if (type == StorageGroup.Types.SDCARD) {
            id = R.drawable.search52;
        }
        return id;
    }

    public static int MapTitleOfGroupToDrawable(String text) {
        int id = R.drawable.unknown_96;
        if (text.contains("local")) {
            id = R.drawable.local_96;
        } else if (text.contains("local")) {
            id = R.drawable.ftp_96;
        } else if (text.contains("drive")) {
            id = R.drawable.google_drive_96;
        } else if (text.contains("drop")) {
            id = R.drawable.dropbox_96;
        } else if (text.contains("sdcard")) {
            id = R.drawable.search52;
        }
        return id;
    }
}
