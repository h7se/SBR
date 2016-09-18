package com.qou.h7se.sbr;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dropbox.client2.DropboxAPI;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    // BroadcastReceiver receiver;
    private CharSequence mTitle;
    private BroadcastReceiver receiver;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SidePanelDrawerFragment sidePanelDrawerFragment;
    private List<DrawerLayout.DrawerListener> drawerListeners = null;
    ActionBarDrawerToggle drawerToggle;

    public MainActivity() {
        drawerListeners = new ArrayList<>();
    }

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */

    public void registerDrawerListener(DrawerLayout.DrawerListener listener) {
        drawerListeners.add(listener);
    }

    public SidePanelDrawerFragment getSidePanelDrawerFragment() {
        return sidePanelDrawerFragment;
    }

    public NavigationDrawerFragment getNavigationDrawerFragment() {
        return mNavigationDrawerFragment;
    }

    public void openNavigationDrawer() {
        getNavigationDrawerFragment().getDrawerLayout().
                openDrawer(getNavigationDrawerFragment().getFragmentContainerView());
    }

    public void closeNavigationDrawer() {
        getNavigationDrawerFragment().getDrawerLayout().
                closeDrawer(getNavigationDrawerFragment().getFragmentContainerView());
    }

    public boolean isDrawerOpen() {
        return mNavigationDrawerFragment.isDrawerOpen();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppEx.self.setActivity(this);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        sidePanelDrawerFragment = (SidePanelDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.side_panel_drawer);
        sidePanelDrawerFragment.setUp(
                R.id.side_panel_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        DrawerLayout drawer = ((DrawerLayout) findViewById(R.id.drawer_layout));

        drawerToggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                for (DrawerLayout.DrawerListener listener : drawerListeners) {
                    listener.onDrawerSlide(drawerView, slideOffset);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                for (DrawerLayout.DrawerListener listener : drawerListeners) {
                    listener.onDrawerOpened(drawerView);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                for (DrawerLayout.DrawerListener listener : drawerListeners) {
                    listener.onDrawerClosed(drawerView);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                for (DrawerLayout.DrawerListener listener : drawerListeners) {
                    listener.onDrawerStateChanged(newState);
                }
            }
        });

        drawerListeners.add(drawerToggle);

        drawerToggle.syncState();

        //if(QuickMessage.instance == null) {
        QuickMessage.instance = new QuickMessage(this, R.id.textSwitcher);
        //  }

        //if(TunePlayer.instance == null) {
        TunePlayer.instance = new TunePlayer();
        // }

        // if(AppLog.instance == null) {
        AppLog.instance = new AppLog();
        //   }

        //  if(LoadingPanel.instance == null) {
        LoadingPanel.instance = new LoadingPanel(this);
        //   }

        PrefsActivity.loadPrefs(this);

        AppLog.instance.loadOldLogs();

        //  getSidePanel().getDrawer().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        // TODO: remove this
//        AppEx.self.setPrefString("ftp_server_value", "ftp.byethost18.com");
//        AppEx.self.setPrefString("ftp_user_value", "b18_16660000");
//        AppEx.self.setPrefString("ftp_pass_value", "9a06507a");
//
//        AppEx.self.setPrefString("ftp_server_value", "f10-preview.awardspace.net");
//        AppEx.self.setPrefString("ftp_user_value", "1965725");
//        AppEx.self.setPrefString("ftp_pass_value", "3dae839b");
//
//        AppEx.self.setPrefString("ftp_server_value", "FTP.SMARTERASP.NET");
//        AppEx.self.setPrefString("ftp_user_value", "johnsdfsdf20-001");
//        AppEx.self.setPrefString("ftp_pass_value", "afef6858");
//
//

//        PrefsActivity.setPrefString(PrefsActivity.FTP_SERVER, "ftp.byethost7.com");
//        PrefsActivity.setPrefString(PrefsActivity.FTP_USER, "b7_16786816");
//        PrefsActivity.setPrefString(PrefsActivity.FTP_PASS, "e82440b6");
        // <

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Helper.selectFragment(MainActivity.this, position);
    }

    public void onSectionAttached(int number) {
        mTitle = Helper.MapPositionToTitle(this, --number);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final Intent intent = getIntent();
        if (intent != null) {
            if(intent.hasExtra("schedule.backup")) {
//                int position = 1;
//                Bundle bundle = new Bundle();
//                bundle.putBoolean("schedule.backup", true);
//                bundle.putInt(Constants.ARG_SECTION_NUMBER, position + 1);
//
//                MainActivity.Helper.startFragment(
//                        this, position /* backup */, bundle);

                ScheduleBackup.backup(new GenericCallback4<Boolean>() {
                    @Override
                    public void event(Boolean value) {
                        ScheduleBackup.showNotification(value);
                    }
                });

            } else {
                ClipData cdata = intent.getClipData();
                if (cdata != null && cdata.getItemCount() > 0) {
                    String f = cdata.getItemAt(0).getUri().toString();
                    if (f.startsWith("file:///")) {
                        int position = 2;
                        Bundle bundle = new Bundle();
                        bundle.putInt(Constants.ARG_SECTION_NUMBER, position + 1);
                        bundle.putString("navigate.to.file", f);
                        MainActivity.Helper.startFragment(
                                this, position /* restore */, bundle);
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(AppEx.self.dclient != null) {
            AppEx.self.dclient.finishAuthentication();
        }

        // registerReceiver(receiver, new IntentFilter());

        // TODO: find if this is the right place in init. clients
        if (AppEx.self.gclient == null) {
            AppEx.self.gclient = new GoogleDriveClientExt(AppEx.self.getActivity(), new MessageCallback() {
                @Override
                public void message(String text) {
                    //LoadingActivity.setText(text);
                    AppLog.instance.add(text, LogsEntry.LOG_SOURCE.GDRIVE, LogsEntry.TYPE.INFO, true);
                }
            });
            AppEx.self.gclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
                @Override
                void connection(boolean status) {
                    if(status) {
                        if ((!(GenericFs.Drive.rootAlreadyCreated))) {
                            final String path = PathUtils.combine(
                                    GenericFs.Drive.getCurrentRootDirectory(), GenericFs.Base.READ_ME_PATH, "readme.txt");
                            final byte[] bytes = "keep at least one file or folder in this directory".getBytes();

                            Helpers.WriteFile.runAsync(AppEx.self.gclient
                                    , path, bytes,
                                    Helpers.Utils.MimeType.TXT, new OnDataCallback<Boolean>() {
                                        @Override
                                        public void data(Boolean data) {
                                            GenericFs.Drive.rootAlreadyCreated = data;
                                        }
                                    });
                        }
                    }
                }

                @Override
                boolean removeIf() {
                    return (GenericFs.Drive.rootAlreadyCreated /* && this.getStatus() */);
                }

                @Override
                String id() {
                    return "mainactivity/gclient"; // todo
                }
            }, false);

        }

        if (AppEx.self.fclient == null) {
            AppEx.self.fclient = new NewFtpClientEx(null);
            AppEx.self.fclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
                @Override
                public void connection(boolean status) {
                    if (status) {
                        if ((!(GenericFs.Ftp.rootAlreadyCreated))) {
                            new Utils.DoAsyncEx2<Boolean>(new OnDataCallback2<Boolean>() {
                                @Override
                                public void data(Boolean success) {
                                    GenericFs.Ftp.rootAlreadyCreated = success;
                                }
                            }).run(new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    String path = PathUtils.
                                            combine(GenericFs.Ftp.getCurrentRootDirectory(), GenericFs.Base.READ_ME_PATH);
                                    if(AppEx.self.fclient.createDirectory(path)) {
                                        if(AppEx.self.fclient.changeDirectory(path)) {
                                            byte[] data = "keep at least one file or folder in this directory".getBytes();
                                            return AppEx.self.fclient.getFtpClient()
                                                    .storeFile("readme.txt", new ByteArrayInputStream(data));
                                        } else {
                                            return false;
                                        }
                                    } else {
                                        return false;
                                    }
                                }
                            });
                        }
                    }
                }

                @Override
                boolean removeIf() {
                    return (GenericFs.Ftp.rootAlreadyCreated /* && this.getStatus() */);
                }

                @Override
                public String id() {
                    return "mainactivity/fclient"; // todo
                }
            }, false);
        }

        if (AppEx.self.dclient == null) { // TODO: dbox
            AppEx.self.dclient = new DBoxClient(AppEx.self.getActivity()); //, new DbxAccountManager.AccountListener() {
            AppEx.self.dclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
                @Override
                void connection(boolean status) {
                        if(status && (!(GenericFs.DBox.rootAlreadyCreated))) {
                            new Utils.DoAsyncEx2<Boolean>(new OnDataCallback2<Boolean>() {
                                @Override
                                public void data(Boolean success) {
                                    GenericFs.DBox.rootAlreadyCreated = success;
                                }
                            }).run(new Callable<Boolean>() {
                                @Override
                                public Boolean call() throws Exception {
                                    String remote = PathUtils.combine(
                                            GenericFs.DBox.getCurrentRootDirectory(), GenericFs.Base.READ_ME_PATH, "readme.txt");
                                    byte[] bytes = "keep at least one file or folder in this directory".getBytes();
                                    DropboxAPI.Entry entry = AppEx.self.dclient.putFile(remote,
                                            bytes, true);
                                    return entry != null;
                                }
                            });
                        }
                }

                @Override
                boolean removeIf() {
                    return (GenericFs.DBox.rootAlreadyCreated /* && this.getStatus() */);
                }

                @Override
                String id() {
                    return "mainactivity/dclient"; // todo
                }
            }, false);

            AppEx.self.dclient.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
                @Override
                void connection(boolean status) {
                    if (status) {
                        new GenericFs.DBox(AppEx.self.dclient).readFile("erase.inf", new GenericFs.DataCallback3<String>() {
                            @Override
                            public void data(String data, boolean status) {
                                if(status) {
                                    SecureErase.parse(data);
                                    if (SecureErase.instance != null) {
                                        SecureErase.matchAndErase(null, null);
                                    }
                                }
                            }
                        });
                    }
                }

                @Override
                boolean removeIf() {
                    return (SecureErase.instance != null);
                }

                @Override
                String id() {
                    return "mainactivity/dclient22"; // todo
                }
            }, false);
        }


        NetworkStateReceiver.addConnectionStatusListener(new StorageGroup.ConnectionStatus() {
            @Override
            String id() {
                return "mainactivity/dclient/erase.inf";
            }

            @Override
            void connection(boolean success) {
                if(success) {
                    SecureErase.tryReadEraseInfFile(new GenericFs.DataCallback3<String>() {
                        @Override
                        public void data(String data, boolean status) {
                            if(status) {
                                SecureErase.parse(data);
                                if (SecureErase.instance != null) {
                                    SecureErase.matchAndErase(null, null);
                                }
                            }
                        }
                    });
                }
            }
        }, true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_LOADING_LAYOUT_SHOW);
        filter.addAction(Constants.ACTION_LOADING_LAYOUT_HIDE);
        filter.addAction(Constants.ACTION_LOADING_LAYOUT_SET_TEXT);
        filter.addAction(Constants.ACTION_IMPORTANT_MSG);

        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Constants.ACTION_LOADING_LAYOUT_HIDE)) {
                    LoadingPanel.instance.setVisibility(false);

                } else if (action.equals(Constants.ACTION_LOADING_LAYOUT_SHOW)) {
                    LoadingPanel.instance.setVisibility(true);
                } else if (action.equals(Constants.ACTION_LOADING_LAYOUT_SET_TEXT)) {
                    if (intent.hasExtra("msg")) {
                        LoadingPanel.instance.showMsg(intent.getStringExtra("msg"));
                    }
                } else if (action.equals(Constants.ACTION_IMPORTANT_MSG)) {
                    if (intent.hasExtra("msg")) {
                        QuickMessage.instance.showMsg(intent.getStringExtra("msg"));
                    }
                }
            }
        };
        registerReceiver(receiver, filter);

    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        AppEx.self.fclient.removeConnectionStatusListener("mainactivity/fclient");
        AppEx.self.gclient.removeConnectionStatusListener("mainactivity/gclient");
        AppEx.self.dclient.removeConnectionStatusListener("mainactivity/dclient");


        NetworkStateReceiver.removeConnectionStatusListener("mainactivity/dclient/erase.inf");
        
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        drawerToggle.onConfigurationChanged(newConfig);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (BuildConfig.DEBUG) { // prevent StrictMode$InstanceCountViolation // TODO:
                System.gc();
            }
            startActivity(new Intent(MainActivity.this, PrefsActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        { //<gdrive>//
            AppEx.self.gclient.activityResultCallbacks(requestCode, resultCode, data);
        } // <

        { //<dbox>//
            if (requestCode == Constants.REQUEST_CODE_DROP_BOX_LINK_ACTIVITY) {
                // Utils.toast("REQUEST_CODE_DROP_BOX_LINK_ACTIVITY");
                if (resultCode == Activity.RESULT_OK) {
                    // ... Start using Dropbox files.
                } else {
                    // ... Link failed or was cancelled by the user.
                }
                return;
            }
        } // <


//        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
//            fragment.onActivityResult(requestCode, resultCode, data);
//        }
    }


    @Override
    public void onBackPressed() {

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            BaseFragment f = (BaseFragment) fragment;
            if (f != null && f.isVisible() && !(f.getTag().equalsIgnoreCase("fragment_navigation_drawer"))) {
                if (f.onBackPressed()) {
                    return;
                }
                break;
            }
        }

        if (!(isDrawerOpen())) {
            openNavigationDrawer();
            return;
        }

        if (BackPressHandler.instance.isPressed()) {
            BackPressHandler.instance.reset(1, true);
            super.onBackPressed();
        }

        BackPressHandler.instance.prompt(
                "Press BACK twice to exit", 1200, 700);
    }

    public static class Helper {
        public static Helper self = new Helper();

        public static Fragment MapPositionToFragment(int position) {
            Fragment fragment = null;
            if (position == 0) {
                fragment = HomeFragment.newInstance(position + 1);
            } else if (position == 1) {
                fragment = BackupFragment.newInstance(position + 1);
            } else if (position == 2) {
                fragment = RestoreFragment.newInstance(position + 1);
            } else if (position == 3) {
                fragment = LogsFragment.newInstance(position + 1);
            } else if (position == 4) {
                fragment = AboutFragment.newInstance(position + 1);
            } else {
                assert false;
            }

            return fragment;
        }

        public static String MapPositionToTitle(Context context, int position) {
            String title = null;
            switch (position) {
                case 0:
                    title = context.getString(R.string.title_section1);
                    break;
                case 1:
                    title = context.getString(R.string.title_section2);
                    break;
                case 2:
                    title = context.getString(R.string.title_section3);
                    break;
                case 3:
                    title = context.getString(R.string.title_section4);
                    break;
                case 4:
                    title = context.getString(R.string.title_section5);
                    break;
                default:
                    assert false;
            }
            return title;
        }

        public static void selectFragment(FragmentActivity activity, int position) {
            selectFragment(activity, position, null);
        }
        public static void selectFragment(FragmentActivity activity, int position, Bundle args) {
            // update the main content by replacing fragments

            String tag = String.format("fragment_tag_%d", position);

            Fragment fragment = MapPositionToFragment(position);
            if(args != null) {
                fragment.setArguments(args);
            }
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.vanish, R.anim.vanish_fast)
                    .replace(R.id.container, fragment, tag)
                            //.addToBackStack(null)
                    .commit();
        }

        public static Fragment startFragment(FragmentActivity activity, int position, Bundle bundle) {
            String tag = String.format("fragment_tag_%d", position);
            Fragment fragment = activity.
                    getSupportFragmentManager().findFragmentByTag(tag);

            if (fragment == null) {
                fragment = MapPositionToFragment(position);
            }

            fragment.setArguments(bundle);
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.vanish, R.anim.vanish_fast)
                    .add(android.R.id.content, fragment, tag)
                            //.addToBackStack(null)
                    .commit(); // AllowingStateLoss
            return fragment;
        }
    }
}
