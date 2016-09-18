package com.qou.h7se.sbr;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends BaseFragment {


    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
   // private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;


    TreeView tree;
    TreeNodesAdapter treeNodesAdapter;

    public NavigationDrawerFragment() {
        tree = new TreeView();
    }

    public View getFragmentContainerView() {
        return mFragmentContainerView;
    }
    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
        setCurrentFragment(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    boolean onBackPressed() {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);

        for(int i =0; i < 5; i++) {
            tree.add(MainActivity.Helper.MapPositionToTitle(getActivity(), i))
                    .setProperty("fragment_id", i)
                    .setNoCollapseOnClick(TreeNode.TRUE);
        }

        tree.findNodeByPath("/Restore")
                .add("Local").setNoCollapseOnClick(TreeNode.TRUE)
                .add("Stats").setNoCollapseOnClick(TreeNode.SMART).setActionImageClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                populateRestoreStatsNode(true);
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AnimatorSets.getZoomInAnimatorSet(view, 1600).start();
                    }
                }, 1);
            }
        });

        TreeNode nodeLogs = tree.findNodeByPath("/Logs");
        nodeLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppLog.instance.getAdapter().getLogsFilterCallback() != null) {
                    AppLog.instance.getAdapter().setLogsFilterCallback(null, true);
                }
            }
        });

        for(final LogsEntry.TYPE e : LogsEntry.TYPE.values()) {
            final TreeNode nodeLogsSubNode = nodeLogs.add(Utils.StringEx.title(e.name()));
            nodeLogsSubNode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppLog.instance.getAdapter().setLogsFilterCallback(new DataFilterCallback<LogsEntry>() {
                        @Override
                        public boolean include(LogsEntry item) {
                            return item.type.equals(e); // TODO: // equals ?
                        }
                    }, true);

                    AppLog.instance.getAdapter().notifyDataSetChanged();

                    MainActivity.Helper.self.selectFragment(
                            getActivity(), nodeLogsSubNode.parent.<Integer>getProperty("fragment_id"));

                    if (mDrawerLayout != null) {
                        mDrawerLayout.closeDrawer(mFragmentContainerView);
                    }
                }
            });
        }

        nodeLogs.setExpanded(false);

        treeNodesAdapter = new TreeNodesAdapter(getActivity(), new OnDataChange() {
           @Override
           public ArrayList<TreeNode> refresh() {
               return tree.build();
           }
       });
        treeNodesAdapter.setRightImageMarginVisibility(true);

        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

//                if ((node.id != -1)) {
//                    adapter.getItemsMatchingPredicate(new DataFilterCallback<TreeNode>() {
//                        @Override
//                        public boolean include(TreeNode item) {
//                            return item.id == node.id;
//                        }
//                    }, new DataFilterActionCallback<TreeNode>() {
//                        @Override
//                        public void run(TreeNode item, int position) {
//                            selectItem(position);
//                        }
//                    }, null, null);
//                }
                TreeNode node = treeNodesAdapter.getItem(i);
                while(node.getLevel() != 1) {
                    node = node.parent;
                }
                int fragment_id = node.<Integer>getProperty("fragment_id");
                if(fragment_id != mCurrentSelectedPosition) {
                    setCurrentFragment(fragment_id);
                }

                selectItem(i);

                treeNodesAdapter.invokeOnItemClick(view, i);
            }
        });

        mDrawerListView.setAdapter(treeNodesAdapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        populateRestoreStatsNode(false);

        treeNodesAdapter.setData(tree.build(), true);

        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);

            if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
            }
        }

    }

    private void selectItem(int nodeIndex) {
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(nodeIndex, true);
        }

        boolean keepDrawerOpened = false;
        if(treeNodesAdapter != null) {
            TreeNode node = treeNodesAdapter.getItem(nodeIndex);
           keepDrawerOpened = ((node.hasChildes())
                    && ((node.getLevel() != 1) || !(node.getExpanded())));
        }

        if (mDrawerLayout != null && !keepDrawerOpened) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    private void setCurrentFragment(int fragmentId) {
        mCurrentSelectedPosition = fragmentId;
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(fragmentId);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (mDrawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }

//        if (item.getItemId() == R.id.action_example) {
//            Toast.makeText(getActivity(), "Example action.", Toast.LENGTH_SHORT).show();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

    public void populateRestoreStatsNode(boolean expand) {
        TreeNode nodeStats = tree.findNodeByPath("/Restore/Local/Stats");
        if(nodeStats != null) {
            nodeStats.childes.clear();
            // nodeStats.add(String.format("%s", AppEx.self.getPrefString("local.last.backup")));

            File dir = AppEx.self.getCurrentStorageDir();
            try {
                String[] dirs = dir.list();
                for (final String d : dirs) {
                        final File f = new File(dir.getCanonicalPath().concat("/").concat(d));
                        if((f).isDirectory()) {
                            if(!(d.toLowerCase().endsWith(".ignore"))) {
                                int zipFileCount =0;
                                String[] zipFiles = f.list();
                                for (String z : zipFiles) {
                                    File f2 = new File(f.getCanonicalPath().concat("/").concat(z));
                                    if(f2.isFile() && f2.getName().toLowerCase().endsWith(".zip")) {
                                        zipFileCount +=1;
                                    }
                                }
                                nodeStats.add(String.format("%s ( %d )",
                                        Utils.StringEx.title(d), zipFileCount)).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(Constants.ACTION_RESTORE_LIST_NAVIGATE);
                                        intent.putExtra("path" /*DIR*/, PathUtils.getCanonicalPath(f, true));
                                        getActivity().sendBroadcast(intent);
                                    }
                                });
                            }
                        }
                }

                nodeStats.setExpanded(expand);

                treeNodesAdapter.setData(tree.build(), true);

            } catch (Exception ex) {
                Utils.LogException(ex);
            }
        }
    }
}
