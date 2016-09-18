package com.qou.h7se.sbr;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.google.api.client.repackaged.com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by k0de9x on 10/6/2015.
 */


public class DatePickerPreference extends DialogPreference implements DialogInterface.OnClickListener {

    TimePicker timePicker;
    //DatePicker datePicker;

    RadioButton radioButton, radioButton2, radioButton3;
    ToggleButton toggleButton, toggleButton2, toggleButton3, toggleButton4, toggleButton5, toggleButton6, toggleButton7;

    ListView listview, listview2;

    public DatePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        View view = makeDialog(getContext(), "Schedule backup", null, null).getWindow().getDecorView();

        timePicker = ((TimePicker) view.findViewById(R.id.timePicker));

        radioButton = ((RadioButton) view.findViewById(R.id.radioButton));
        radioButton2 = ((RadioButton) view.findViewById(R.id.radioButton2));
        radioButton3 = ((RadioButton) view.findViewById(R.id.radioButton3));

        toggleButton = ((ToggleButton) view.findViewById(R.id.toggleButton));
        toggleButton2 = ((ToggleButton) view.findViewById(R.id.toggleButton2));
        toggleButton3 = ((ToggleButton) view.findViewById(R.id.toggleButton3));
        toggleButton4 = ((ToggleButton) view.findViewById(R.id.toggleButton4));
        toggleButton5 = ((ToggleButton) view.findViewById(R.id.toggleButton5));
        toggleButton6 = ((ToggleButton) view.findViewById(R.id.toggleButton6));
        toggleButton7 = ((ToggleButton) view.findViewById(R.id.toggleButton7));

        radioButton.setChecked(PrefsActivity.prefs.getBoolean("schedule_backup_disabled", false));
        radioButton2.setChecked(PrefsActivity.prefs.getBoolean("schedule_backup_daily", false));
        radioButton3.setChecked(PrefsActivity.prefs.getBoolean("schedule_backup_weekly", false));

        toggleButton.setChecked(PrefsActivity.prefs.getBoolean("schedule_backup_day_1", false));
        toggleButton2.setChecked(PrefsActivity.prefs.getBoolean("schedule_backup_day_2", false));
        toggleButton3.setChecked(PrefsActivity.prefs.getBoolean("schedule_backup_day_3", false));
        toggleButton4.setChecked(PrefsActivity.prefs.getBoolean("schedule_backup_day_4", false));
        toggleButton5.setChecked(PrefsActivity.prefs.getBoolean("schedule_backup_day_5", false));
        toggleButton6.setChecked(PrefsActivity.prefs.getBoolean("schedule_backup_day_6", false));
        toggleButton7.setChecked(PrefsActivity.prefs.getBoolean("schedule_backup_day_7", false));

        List<String> tmp2 = new ArrayList<>();
        String tmp = PrefsActivity.
                prefs.getString("schedule_backup_sources", null);
        if(tmp != null) {
            tmp2 = Arrays.asList(tmp.split(","));
        }

        listview= (ListView) view.findViewById(R.id.list);
        listview.setChoiceMode(
                ListView.CHOICE_MODE_MULTIPLE);
        List<String> data = new ArrayList<>();
        for (StorageItem item : AppEx.self.backupRestoreSources) {
            if (item.groupType != StorageGroup.Types.SDCARD) {
                data.add(item.title);
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.simple_list_item_multiple_choice, data.toArray(new String[data.size()]));
        listview.setAdapter(adapter);
        for (String item : data) {
            if (tmp2.contains(item)) {
                listview.setItemChecked(adapter.getPosition(item), true);
            }
        }
        adapter.notifyDataSetChanged();

        tmp2 = new ArrayList<>();
        tmp = PrefsActivity.
                prefs.getString("schedule_backup_items", null);
        if(tmp != null) {
            tmp2 = Arrays.asList(tmp.split(","));
        }
        listview2= (ListView) view.findViewById(R.id.list2);
        listview2.setChoiceMode(
                ListView.CHOICE_MODE_MULTIPLE);
        data = new ArrayList<>();
        for (DataSource e : DataSource.values()) {
            Uri[] uris = Utils.MapToUris(e);
            for (Uri u : uris) {
                data.add(Utils.StringEx.title(Utils.MapUriToTitle(u)));
            }
        }
        adapter = new ArrayAdapter<>(getContext(),
                R.layout.simple_list_item_multiple_choice, data.toArray(new String[data.size()]));
        listview2.setAdapter(adapter);
        for (String item : data) {
            if (tmp2.contains(item)) {
                listview2.setItemChecked(adapter.getPosition(item), true);
            }
        }
        adapter.notifyDataSetChanged();

        AnimatorSets.getFadeInAnimatorSet(
                view.findViewById(R.id.radioGroup), 800).start();

        return view;
    }

    public static Dialog makeDialog(final Context context, String title, final OnDataCallback<String> dataCallback, final View.OnClickListener cancelHandler) {
        final Dialog dialog = new Dialog(context);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getDecorView().setBackgroundResource(R.drawable.fragment_bg3);
        dialog.setContentView(R.layout.date_time_picker);
       // dialog.setTitle(title);

        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);

        if (which == DialogInterface.BUTTON_POSITIVE) {

            SharedPreferences prefs = PrefsActivity.prefs;

            ListAdapter adapter = listview.getAdapter();
            SparseBooleanArray checkedItemPositions = listview.getCheckedItemPositions();
            List<String> s = new ArrayList<>();
            for(int i=0, size=adapter.getCount(); i<size; i++) {
                if(checkedItemPositions.get(i)) {
                   s.add(adapter.getItem(i).toString());
                }
            }
            prefs.edit().putString("schedule_backup_sources", Joiner.on(",").join(s)).apply();


            adapter = listview2.getAdapter();
            checkedItemPositions = listview2.getCheckedItemPositions();
            s = new ArrayList<>();
            for(int i=0, size=adapter.getCount(); i<size; i++) {
                if(checkedItemPositions.get(i)){
                    s.add(adapter.getItem(i).toString());
                }
            }
            prefs.edit().putString("schedule_backup_items", Joiner.on(",").join(s)).apply();

            int hour = timePicker.getCurrentHour();
            int minute = timePicker.getCurrentMinute();

            boolean disabled = radioButton.isChecked();
            boolean daily = radioButton2.isChecked();
            boolean weekly = radioButton3.isChecked();

            boolean[] days = {
                    toggleButton.isChecked()
                    , toggleButton2.isChecked()
                    , toggleButton3.isChecked()
                    , toggleButton4.isChecked()
                    , toggleButton5.isChecked()
                    , toggleButton6.isChecked()
                    , toggleButton7.isChecked()
            };

            prefs.edit()
                    .putInt("schedule_backup_hour", hour)
                    .putInt("schedule_backup_minute", minute)
                    .putBoolean("schedule_backup_disabled", disabled)
                    .putBoolean("schedule_backup_daily", daily)
                    .putBoolean("schedule_backup_weekly", weekly)
                    .apply();

            for(int i=0, size=days.length; i<size; i++) {
                prefs.edit().putBoolean(String.format("schedule_backup_day_%d", i + 1), days[i]).apply();
            }

            AlarmBroadcastReceiver.Helper.instance.start(days, hour, minute, disabled, daily, weekly);
//            AlarmBroadcastReceiver.enable(getContext(),
//                    AlarmBroadcastReceiver.class);

        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            dialog.dismiss();
        }
    }

}
