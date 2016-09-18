package com.qou.h7se.sbr;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by k0de9x on 10/20/2015.
 */
public class HomeItemsLastBackupAdapter extends RecyclerViewAdapterBase<HomeItemsLastBackupAdapter.Card, HomeItemsLastBackupAdapter.ViewHolder> {
    Context context;
    AdapterView.OnItemClickListener onItemClickListener;
    private int lastPosition = -1;

    public HomeItemsLastBackupAdapter(Context context, ArrayList<Card> data, AdapterView.OnItemClickListener onItemClickListener) {
        super(data);

        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_last_backup_entry, parent, false);
        ViewHolder pvh = new ViewHolder(v);
        return pvh;
    }

    public int refreshItem(final RecyclerView.ViewHolder holder, boolean recheckPrefs) {
        if(recheckPrefs) {
            getItem(holder.getPosition()).refresh();
        }

        return holder.getPosition();
    }

    public Card findItemWithPrefKey(String text) {
        for (Card entry : this.getItems()) {
            if (entry.prefKey.equalsIgnoreCase(text)) {
                return entry;
            }
        }
        return null;
    }


    public int findItemPositionWithPrefKey(String text) {
        List<Card> cards = this.getItems();
        for (int i=0, size=cards.size(); i < size; i++) {
            if (cards.get(i).prefKey.equalsIgnoreCase(text)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Card item = getItem(position);

        holder.txtTitle.setText(item.title);
        holder.txtDate.setText(item.date);
        holder.txtLocation.setText(item.location);

        if (position > lastPosition) {
            lastPosition = refreshItem(holder, false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null) {
                    onItemClickListener.onItemClick(null, holder.itemView, position, -1);
                }
            }
        });
    }

    public static class Card {
        String title;
        String date;
        String location;
        String prefKey;

        public Card(String title, String prefKey) {
            this.title = title;
            this.prefKey = prefKey;

            refresh();
        }

        public void refresh() {
            String s = PrefsActivity.getString(prefKey, "");
            if(!(TextUtils.isEmpty(s))) {
                String[] data = s.split("\\|");
                if(data.length == 2) {
                    this.date = (new java.sql.Date(Long.parseLong(data[0])).toLocaleString()) ;
                    this.location = Utils.StringEx.title(data[1]);
                }
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtDate;
        TextView txtLocation;

        CoordinatorLayout coordinatorLayout;

        ViewHolder(View itemView) {
            super(itemView);

            coordinatorLayout =  (CoordinatorLayout)  itemView.findViewById(R.id.coordinatorLayout);

            txtTitle = (TextView) itemView.findViewById(R.id.textView);
            txtDate = (TextView) itemView.findViewById(R.id.textView1);
            txtLocation = (TextView) itemView.findViewById(R.id.textView3);
        }
    }
}
