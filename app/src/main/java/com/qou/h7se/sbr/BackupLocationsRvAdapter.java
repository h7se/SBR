package com.qou.h7se.sbr;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by k0de9x on 9/22/2015.
 */

public class BackupLocationsRvAdapter extends RecyclerViewAdapterBase<StorageGroupListViewItemWrapper, BackupLocationsRvAdapter.BackupLocationsViewHolder> {
    Context context;
    AdapterView.OnItemClickListener onItemClickListener;

    public BackupLocationsRvAdapter(ArrayList<StorageGroupListViewItemWrapper> data, AdapterView.OnItemClickListener onItemClickListener) {
        super(data);
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public BackupLocationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.backup_location_rv_entry, parent, false);
        BackupLocationsViewHolder pvh = new BackupLocationsViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(final BackupLocationsViewHolder holder, final int position) {

        StorageGroupListViewItemWrapper item = getItem(position);

        holder.txt1.setText(item.title);
        holder.img1.setImageResource(RestoreListViewItemsAdapter.MapGroupToDrawable(item.groupType));

        if(item.connectionStatusApply) {
            if(item.connected) {
                if (item.checked) {
                    holder.itemView.setBackgroundResource(R.drawable.a12_row_selector);
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.a12_row_orange_selector);
                }
            } else {
                if (item.checked) {
                    holder.itemView.setBackgroundResource(R.drawable.a12_row_red_selector);
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.a10_row_selector);
                }
            }
        } else {
            if (item.checked) {
                holder.itemView.setBackgroundResource(R.drawable.a12_row_selector);
            } else {
                holder.itemView.setBackgroundResource(R.drawable.a10_row_selector);
            }
        }

//        if (item.checked) {
//            if(!(item.connected) && item.connectionStatusApply) {
//                holder.itemView.setBackgroundResource(R.drawable.a12_row_red_selector);
//            } else {
//                holder.itemView.setBackgroundResource(R.drawable.a12_row_selector);
//            }
//        } else {
//            if(!(item.connected) || (!item.connectionStatusApply)) {
//                holder.itemView.setBackgroundResource(R.drawable.a10_row_selector);
//            } else {
//                holder.itemView.setBackgroundResource(R.drawable.a12_row_orange_selector);
//            }
//        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onItemClickListener.onItemClick(null, holder.itemView, position, -1);
           }
       });
    }

    public void toggleChecked(int i) {
        getItem(i).checked = !(getItem(i).checked);
    }

    public int getCheckedItemsCount() {
       return this.getItemsCountMatchingPredicate(new DataFilterCallback<StorageGroupListViewItemWrapper>() {
            @Override
            public boolean include(StorageGroupListViewItemWrapper item) {
                return item.checked;
            }
        });
    }

    public StorageGroupListViewItemWrapper findItemByText(String text) {
        for (StorageGroupListViewItemWrapper entry : this.getItems()) {
            if (entry.title.equalsIgnoreCase(text)) {
                return entry;
            }
        }
        return null;
    }

    public int findItemPositionByText(String text) {
        for (int i=0, size = this.getItems().size(); i < size; i++) {
            if (getItem(i).title.equalsIgnoreCase(text)) {
                return i;
            }
        }
        return -1;
    }

    public StorageGroupListViewItemWrapper findItemByStorageGroup(StorageGroup.Types storageGroup) {
        for (StorageGroupListViewItemWrapper entry : this.getItems()) {
            if (entry.groupType == storageGroup) {
                return entry;
            }
        }
        return null;
    }

    public static class BackupLocationsViewHolder extends RecyclerView.ViewHolder {
        TextView txt1;
        ImageView img1;

        StorageItem.ItemType stype;

        BackupLocationsViewHolder(View itemView) {
            super(itemView);

            txt1 = (TextView) itemView.findViewById(R.id.textView);
            img1 = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }
}
