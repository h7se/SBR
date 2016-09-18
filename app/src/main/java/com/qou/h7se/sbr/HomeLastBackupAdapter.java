package com.qou.h7se.sbr;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by k0de9x on 10/20/2015.
 */
public class HomeLastBackupAdapter extends RecyclerViewAdapterBase<HomeLastBackupAdapter.Card, HomeLastBackupAdapter.ViewHolder> {
    Context context;
    AdapterView.OnItemClickListener onItemClickListener;
    private int lastPosition = -1;

    public HomeLastBackupAdapter(Context context, ArrayList<Card> data, AdapterView.OnItemClickListener onItemClickListener) {
        super(data);

        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.last_backup_entry, parent, false);
        ViewHolder pvh = new ViewHolder(v);
        return pvh;
    }

    public int refreshItem(final RecyclerView.ViewHolder holder, boolean recheckPrefs) {
        if(holder != null) {
            holder.itemView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    AnimatorSets.getBounceInRightAnimatorSet(holder.itemView, (holder.getPosition() + 1) *
                            (AppEx.self.random.nextInt(160) + 400)).start();
                }
            }, 1);

            if(recheckPrefs) {
                getItem(holder.getPosition()).refresh();
            }

            return holder.getPosition();
        }
return 0;
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

        holder.txtText.setText(item.text);

        holder.img1.setImageResource(item.image);

        if (position > lastPosition) {
            lastPosition = refreshItem(holder, false);
        }

        if (item.isConnected && item.connectionStatusApply) {
            holder.img1.setBackgroundResource(R.drawable.fragment_bg3_green);
        } else {
            holder.img1.setBackgroundResource(R.drawable.fragment_bg3_yellow);
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
        int image;
        String text;
        String prefKey;

        boolean isConnected;
        boolean connectionStatusApply;

        public Card(String prefKey, int image) {
            this.prefKey = prefKey;
            this.image = image;
            this.isConnected = false;
            this.connectionStatusApply = true;

            refresh();
        }

        public void refresh() {
            Long mils = PrefsActivity.getLong(prefKey, 0);
            if(mils != 0) {
                this.text = (new java.sql.Date(mils).toLocaleString());
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtText;
        ImageView img1;
        CoordinatorLayout coordinatorLayout;

        ViewHolder(View itemView) {
            super(itemView);

            coordinatorLayout =  (CoordinatorLayout)  itemView.findViewById(R.id.coordinatorLayout);

            txtText = (TextView) itemView.findViewById(R.id.textView1);
            img1 = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }
}
