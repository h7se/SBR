package com.qou.h7se.sbr;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;


/**
 * Created by k0de9x on 9/22/2015.
 */
public class HomeFragment extends BaseFragmentEx {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */

    HomeLastRestoreAdapter lastRestoreAdapter;
    HomeItemsLastBackupAdapter itemsLastBackupAdapter;

    ImageView imgFtp, imgDrive, imgDBox;

    // todo: register connection listener to update rv items
    public static HomeFragment newInstance(int sectionNumber) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public HomeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        imgFtp = (ImageView) rootView.findViewById(R.id.imageView7);
        imgDrive = (ImageView) rootView.findViewById(R.id.imageView9);
        imgDBox = (ImageView) rootView.findViewById(R.id.imageView10);

        ProgressBar prg2 = (ProgressBar) rootView.findViewById(R.id.progressBar1);

        if (Environment.getExternalStorageDirectory().exists()) {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            int max = (int) (stat.getTotalBytes() / (1024 * 1024));
            int free = (int) (stat.getFreeBytes() / (1024 * 1024));
            prg2.setMax(max);
            prg2.setSecondaryProgress(max);

            AnimatorSets.getProgressAnimatorSet(prg2, 0, max - free, 3500).start();
        }


        ArrayList<HomeLastRestoreAdapter.Card> cards2 = new ArrayList<>();
        cards2.add(new HomeLastRestoreAdapter.Card("last_restore_1"));
        cards2.add(new HomeLastRestoreAdapter.Card("last_restore_2"));
        lastRestoreAdapter = new HomeLastRestoreAdapter(getActivity(), cards2, null);
        RecyclerView rv2 = (RecyclerView) rootView.findViewById(R.id.rv2);
        rv2.setHasFixedSize(true);
        rv2.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rv2.setAdapter(lastRestoreAdapter);


        ArrayList<HomeItemsLastBackupAdapter.Card> cards3 = new ArrayList<>();
        cards3.add(new HomeItemsLastBackupAdapter.Card("Contacts", "contacts_last_backup"));
        cards3.add(new HomeItemsLastBackupAdapter.Card("Logs", "logs_last_backup"));
        cards3.add(new HomeItemsLastBackupAdapter.Card("Sms", "sms_last_backup"));
        cards3.add(new HomeItemsLastBackupAdapter.Card("mms", "mms_last_backup"));
        cards3.add(new HomeItemsLastBackupAdapter.Card("Images", "images_last_backup"));
        cards3.add(new HomeItemsLastBackupAdapter.Card("Audio", "audio_last_backup"));
        cards3.add(new HomeItemsLastBackupAdapter.Card("Video", "video_last_backup"));
        cards3.add(new HomeItemsLastBackupAdapter.Card("Events", "events_last_backup"));
        cards3.add(new HomeItemsLastBackupAdapter.Card("Bookmarks", "bookmarks_last_backup"));
        cards3.add(new HomeItemsLastBackupAdapter.Card("Searches", "searches_last_backup"));
        cards3.add(new HomeItemsLastBackupAdapter.Card("Alarms", "alarms_last_backup"));
        itemsLastBackupAdapter = new HomeItemsLastBackupAdapter(getActivity(), cards3, null);
        RecyclerView rv3 = (RecyclerView) rootView.findViewById(R.id.rv3);
        rv3.setHasFixedSize(true);
        rv3.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rv3.setAdapter(itemsLastBackupAdapter);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    void animateMemoryLayout() {
        View v =  getView();
        if(v != null) {
            v = v.findViewById(R.id.relativeLayout_sdcard);
            if(v != null) {
                AnimatorSets.getBounceInDownAnimatorSet(v,
                        (AppEx.self.random.nextInt(600) + 1200)).start();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        animateMemoryLayout();

        AnimatorSets.attachEndCallback(AnimatorSets.getFadeInAnimatorSet(imgFtp, 1200), new GenericCallback2() {
            @Override
            public void event() {
                AnimatorSets.attachEndCallback(AnimatorSets.getFadeInAnimatorSet(imgDrive, 800), new GenericCallback2() {
                    @Override
                    public void event() {
                        AnimatorSets.attachEndCallback(AnimatorSets.getFadeInAnimatorSet(imgDBox, 800), new GenericCallback2() {
                            @Override
                            public void event() {

                            }
                        }).start();
                    }
                }).start();
            }
        }).start();

        AppEx.self.gclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
            @Override
            public void connection(boolean status) {
                if (status) {
                    imgDrive.setBackgroundResource(R.drawable.circle_green);
                } else {
                    imgDrive.setBackgroundResource(R.drawable.circle_yellow);
                }
            }

            @Override
            public String id() {
                return "/drive_last_backup";
            }
        }, true);

        AppEx.self.dclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
            @Override
            public void connection(boolean status) {
                if (status) {
                    imgDBox.setBackgroundResource(R.drawable.circle_green);
                } else {
                    imgDBox.setBackgroundResource(R.drawable.circle_yellow);
                }
            }

            @Override
            public String id() {
                return "/dbox_last_backup";
            }
        }, true);

        AppEx.self.fclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
            @Override
            public void connection(boolean status) {
                if (status) {
                    imgFtp.setBackgroundResource(R.drawable.circle_green);
                } else {
                    imgFtp.setBackgroundResource(R.drawable.circle_yellow);
                }
            }

            @Override
            public String id() {
                return "/ftp_last_backup";
            }
        }, true);
    }

    @Override
    public void onPause() {
        AppEx.self.fclient.removeConnectionStatusListener("/ftp_last_backup");
        AppEx.self.gclient.removeConnectionStatusListener("/drive_last_backup");
        AppEx.self.dclient.removeConnectionStatusListener("/dbox_last_backup");

        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
