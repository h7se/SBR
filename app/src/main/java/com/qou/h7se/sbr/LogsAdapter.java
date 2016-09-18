package com.qou.h7se.sbr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by k0de9x on 9/20/2015.
 */

public class LogsAdapter extends CustomBaseAdapter<LogsEntry> {
    Context context;
    private ViewHolder holder = null;
    private LayoutInflater inflater = null;
    public static float text_size = 8.5f;

    public static final String DATA_FILTER_KEY = "logs";

    private DataFilterCallback<LogsEntry> logsFilterCallback;

    private final class ViewHolder extends CustomBaseAdapter.ViewHolder {
        TextView txt1;
        TextView txt2;
        TextView txt3;
    }

    public LogsAdapter(Context context, ArrayList<LogsEntry> data) {
        super(data);

        this.context = context;
        this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public DataFilterCallback<LogsEntry> getLogsFilterCallback() {
        return this.logsFilterCallback;
    }

    public void setLogsFilterCallback(DataFilterCallback<LogsEntry> logsFilterCallback, boolean notify) {
        this.logsFilterCallback = logsFilterCallback;

        if(notify) {
            notifyDataSetChanged();
        }
    }

    @Override
    public CustomBaseAdapter<LogsEntry>.ViewHolder getHolder() {
        return holder;
    }

    @Override
    public LogsEntry getItem(int position) {
        return getData().get(position);
    }

    @Override
    public int getCount() {
        return getData().size();
    }

    public List<LogsEntry> getData(boolean filterApply) {
        if(this.logsFilterCallback != null && filterApply) {
            ArrayList<LogsEntry> tmp = new ArrayList<>();
            List<LogsEntry> entries = super.getData();
            for(LogsEntry item : entries) {
                // if(!tmp.contains(item)) {
                if(this.logsFilterCallback.include(item)) {
                    tmp.add(item);
                }
                // }
            }

            return tmp;
        }

        return super.getData();
    }

    @Override
    public List<LogsEntry> getData() {
        return getData(true);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();

            convertView = inflater.inflate(R.layout.log_entry, null);
            if(convertView != null) {

                holder.itemView = convertView;

                holder.txt1 = (TextView) convertView.findViewById(R.id.textView);
                holder.txt2 = (TextView) convertView.findViewById(R.id.textView2);
                holder.txt3 = (TextView) convertView.findViewById(R.id.textView3);

                convertView.setTag(holder);
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        LogsEntry item = getItem(position);
        if (item != null) {
            holder.txt1.setText(item.toString() );
           // holder.txt2.setText(item.text);
         //   holder.txt3.setText(item.time);

            if(holder.txt1.getTextSize() != LogsAdapter.text_size) {
                holder.txt1.setTextSize(LogsAdapter.text_size);
            }
         //   holder.txt2.setTextSize(LogsAdapter.textSize);
           // holder.txt3.setTextSize(LogsAdapter.textSize);

            int color;
            switch (item.type) {
                case ERROR:
                    color = context.getResources().getColor(R.color.red_dark);
                    break;
                case WARNING:
                    color = context.getResources().getColor(R.color.orange);
                    break;
                case ARCHIVED:
                    color = context.getResources().getColor(R.color.gray);
                    break;
                default:
                    color = context.getResources().getColor(R.color.yellow);
            }
            holder.txt1.setTextColor(color);
        }
        return convertView;
    }
}
