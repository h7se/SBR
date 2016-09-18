package com.qou.h7se.sbr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ZoomControls;

import java.util.List;

/**
 * Created by k0de9x on 9/23/2015.
 */
public class LogsFragment  extends BaseFragmentEx {

    ListView lvLogs;
    ZoomControls zoomControls;
    int secondsSinceLastClick = 0;

    Handler timer;

    boolean paused = true;

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static LogsFragment newInstance(int sectionNumber) {
        LogsFragment fragment = new LogsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public LogsFragment() {
        if(timer == null) {
            timer = new Handler();
            timer.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!paused) {
                        if (secondsSinceLastClick == 0) {
                            if(zoomControls.getVisibility() != View.VISIBLE) {
                                AnimatorSets.getBounceInRightAnimatorSet(zoomControls, 800).start();
                            }
                            zoomControls.setVisibility(View.VISIBLE);
                        }

                        secondsSinceLastClick += 1;

                        if (secondsSinceLastClick > Constants.LOG_FRAGMENT_HIDE_ZOOM_CONTROLS_AFTER_X_SECONDS) {
                            zoomControls.setVisibility(View.GONE);
                        }
                    }
                    timer.postDelayed(this, 1000);
                }
            }, 1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_logs, container, false);

        lvLogs = (ListView) rootView.findViewById(R.id.listView);
        lvLogs.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                secondsSinceLastClick = 0;
                return false;
            }
        });
        lvLogs.setAdapter( AppLog.instance.getAdapter());


        zoomControls = (ZoomControls) rootView.findViewById(R.id.zoomControls);
        zoomControls.setOnZoomInClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogsAdapter.text_size += 0.5;
                AppLog.instance.
                        getAdapter().notifyDataSetChanged();
                secondsSinceLastClick = 0;
            }
        });
        zoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogsAdapter.text_size -= 0.5;
                AppLog.instance.
                        getAdapter().notifyDataSetChanged();
                secondsSinceLastClick = 0;
            }
        });

        zoomControls.setVisibility(View.GONE);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }


    @Override
    public void onResume() {
        super.onResume();

        if(lvLogs.getAdapter().getCount() > 0) {
           lvLogs.setSelection(lvLogs.getAdapter().getCount() - 1);
        }

        paused = false;
        secondsSinceLastClick = 0;
    }

    @Override
    public void onPause() {
        paused = true;

        AppLog.instance.writeLogsToDisk();

        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.logs_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear_logs) {
            if( AppLog.instance.getAdapter().getLogsFilterCallback() != null) {
                AppLog.instance.getAdapter().getItemsMatchingPredicate( AppLog.instance.getAdapter().getLogsFilterCallback(), new DataFilterActionCallback<LogsEntry>() {
                    @Override
                    public void run(LogsEntry item, int position) {
                        AppLog.instance.getAdapter().removeItem(item, false);
                    }
                }, null, new DataFilterCompleteCallback<LogsEntry>() {
                    @Override
                    public void run(List<Integer> positions, List<LogsEntry> result) {
                        AppLog.instance.getAdapter().notifyDataSetChanged();
                    }
                });
            } else {
                AppLog.instance.getAdapter().clearData(true);
            }

            getActivity().invalidateOptionsMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // TODO: complete

       int count = 0;

        count = AppLog.
                instance.getAdapter().getCount();
        menu.findItem(R.id.action_clear_logs).setVisible(count > 0);

        super.onPrepareOptionsMenu(menu);
    }

}
