package com.qou.h7se.sbr;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by k0de9x on 9/22/2015.
 */

public class BackupLocationsRvAdapter3 extends RecyclerViewAdapterBase<BackupRvEntry, BackupLocationsRvAdapter3.BackupLocationsViewHolder> {
    Context context;
    AdapterView.OnItemClickListener onItemClickListener;

    public BackupLocationsRvAdapter3(Context context, ArrayList<BackupRvEntry> data, AdapterView.OnItemClickListener onItemClickListener) {
        super(data);
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public BackupLocationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.backup_rv_entry, parent, false);
        BackupLocationsViewHolder pvh = new BackupLocationsViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final BackupLocationsViewHolder holder, final int position) {

        BackupRvEntry item = getItem(position);

        holder.txt1.setText(item.getTitle());
        holder.img1.setImageResource(item.getImage());

        if (item.isChecked()) {
            holder.itemView.setBackgroundResource(R.drawable.a12_row_red_selector3);
            holder.img2.setVisibility(View.VISIBLE);
        } else {
            holder.itemView.setBackgroundResource(R.drawable.a10_row_selector);
            holder.img2.setVisibility(View.GONE);
        }

        Uri u = item.getUri();
        if (u != null) {
            String tmp = null, tmp2 = null;
            int count = com.qou.h7se.sbr.restore.HelperEx.
                    queryCount(context.getContentResolver(), u);
            tmp = String.format("( %d )", count);

            long size = 0;
            if(u.equals(Uris.IMAGES) || u.equals(Uris.AUDIO) || u.equals(Uris.VIDEO)) {
                Cursor c = context.getContentResolver().query(u,
                        new String[]{"_size"}, null, null, null);
                if (c != null) {
                    while (c.moveToNext()) {
                        size += c.getLong(0);
                    }
                    c.close();
                }
                tmp2 = String.format("%d M.B.", size / 1024 / 1024);
                holder.txt3.setText(tmp2);
                holder.rlyotextView3.setVisibility(View.VISIBLE);
            } else {
                holder.rlyotextView3.setVisibility(View.GONE);
            }
            holder.txt2.setText(tmp);

        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onItemClickListener.onItemClick(null, holder.itemView, position, -1);
           }
       });
    }

    public void toggleChecked(int i) {
        getItem(i).setChecked(!(getItem(i).isChecked()));
    }

    public int getCheckedItemsCount() {
       return this.getItemsCountMatchingPredicate(new DataFilterCallback<BackupRvEntry>() {
            @Override
            public boolean include(BackupRvEntry item) {
                return item.isChecked();
            }
        });
    }

    public BackupRvEntry findItemByText(String text) {
        for (BackupRvEntry entry : this.getItems()) {
            if (entry.getTitle().equalsIgnoreCase(text)) {
                return entry;
            }
        }
        return null;
    }

    public int findItemPositionByText(String text) {
        for (int i=0, size = this.getItems().size(); i < size; i++) {
            if (getItem(i).getTitle().equalsIgnoreCase(text)) {
                return i;
            }
        }
        return -1;
    }

    public static class BackupLocationsViewHolder extends RecyclerView.ViewHolder {
        TextView txt1;
        ImageView img1;
        ImageView img2;
        TextView txt2;
        TextView txt3;
        RelativeLayout  rlyotextView3;


        BackupLocationsViewHolder(View itemView) {
            super(itemView);

            txt1 = (TextView) itemView.findViewById(R.id.textView);
            img1 = (ImageView) itemView.findViewById(R.id.imageView);
            img2 = (ImageView) itemView.findViewById(R.id.imageView2);

            txt2 = (TextView) itemView.findViewById(R.id.textView2);
            txt3 = (TextView) itemView.findViewById(R.id.textView3);
            rlyotextView3 = (RelativeLayout) itemView.findViewById(R.id.rlyotextView3);
        }
    }

    public static int MapTitleToDrawable(String text) {
        int id = R.drawable.unknown_doc_96;
        if (text.startsWith("sms")) {
            id = R.drawable.bk_messages;
        } else if (text.startsWith("mms")) {
            id = R.drawable.bk_mms;
        } else if (text.startsWith("logs")) {
            id = R.drawable.bk_logs;
        } else if (text.startsWith("contacts")) {
            id = R.drawable.bk_contacts;
        } else if (text.startsWith("events")) {
            id = R.drawable.bk_cal;
        } else if (text.startsWith("searches")) {
            id = R.drawable.bk_search;
        } else if (text.startsWith("bookmarks")) {
            id = R.drawable.bk_chrome;
        } else if (text.startsWith("audio")) {
            id = R.drawable.bk_music;
        } else if (text.startsWith("video")) {
            id = R.drawable.bk_movie;
        } else if (text.startsWith("images")) {
            id = R.drawable.bk_images;
       } else if (text.startsWith("alarms")) {
            id = R.drawable.bk_alarms;
        }
        return id;
    }
}
