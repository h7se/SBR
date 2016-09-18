package com.qou.h7se.sbr;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by k0de9x on 10/18/2015.
 */

public class SidePanelDrawerFragment extends BaseFragment {
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private TextView textViewTitle;
    private EditText editText;
    private TextView textView;
    private RelativeLayout textViewContainer;
    private ScrollView editTextContainer;
    private CheckBox chkKeepClosed;
    private boolean keepClosed;

    public SidePanelDrawerFragment() {
    }

    public View getFragmentContainerView() {
        return mFragmentContainerView;
    }
    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    @Override
    boolean onBackPressed() {
        if(isScheduledToOpen()) {
            setScheduledToOpen(false);
            return true;
        }

        if(isDrawerOpen()) {
            getDrawer().closeDrawer(mFragmentContainerView);
            return true;
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(
                R.layout.fragment_side_panel_drawer, container, false);

        textViewContainer = (RelativeLayout) v.findViewById(R.id.textViewContainer);
        editTextContainer = (ScrollView) v.findViewById(R.id.editTextContainer);

        textViewTitle = (TextView) v.findViewById(R.id.textViewTitle);
        editText = (EditText) v.findViewById(R.id.editText);
        textView = (TextView) v.findViewById(R.id.textView);

        v.findViewById(R.id.imageButton4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getCallback() != null && !(callback.readOnly())) {
                    callback.tag = getEditText().getText();
                }
            }
        });

        v.findViewById(R.id.imageButton3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.closeDrawer(mFragmentContainerView);
            }
        });

        chkKeepClosed = (CheckBox) v.findViewById(R.id.checkBox3);
        chkKeepClosed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                keepClosed = isChecked;
            }
        });
      //  getDrawer().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        return v;
    }

    private boolean scheduledToOpen = false;
    public boolean isScheduledToOpen() {
        return scheduledToOpen;
    }

    public  void setScheduledToOpen(boolean value) {
        scheduledToOpen = value;
    }

    public boolean isDrawerOpen() {
       return (mDrawerLayout != null && (mDrawerLayout.isDrawerOpen(mFragmentContainerView)));
       // return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.RIGHT);
    }

    public void setUp(final int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView =
                getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ((MainActivity)getActivity()).registerDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if ((drawerView == getFragmentContainerView())) {
                    if(slideOffset > 0.2) {
                            if (!(getCallback() == null)) {
                                if (!(callback.visibility())) {
                                    closeWithNotification();
                                } else {
                                    if(!(isDrawerOpen())) {
                                        textViewTitle.
                                                setText(callback.title());
                                        if (callback.readOnly()) {
                                            textViewContainer.setVisibility(View.VISIBLE);
                                            editTextContainer.setVisibility(View.GONE);
                                        } else {
                                            Utils.showSoftKeyboard(
                                                    getActivity(), getEditText());
                                            AnimatorSets.getFadeInAnimatorSet(getEditText(), 600).start();
                                            textViewContainer.setVisibility(View.GONE);
                                            editTextContainer.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            } else {
                                closeWithNotification();
                            }
                    }
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if ((drawerView == getFragmentContainerView())) {
                    setScheduledToOpen(false);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                if ((drawerView == getFragmentContainerView())) {
                    setScheduledToOpen(false);

                    if ((callback != null) && (!callback.readOnly())) {
                        Utils.hideSoftKeyboard(getActivity(), getEditText());
                    }
                }
//                mDrawerLayout.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(!(isDrawerOpen())) {
//                            clearText(false);
//                        }
//                    }
//                }, 5000);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
    }

    public void closeDrawer() {
        getDrawer().closeDrawer(mFragmentContainerView);
    }

    public void closeWithNotification() {
        Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
        mFragmentContainerView.startAnimation(anim);
        getDrawer().closeDrawer(mFragmentContainerView);
        Utils.vibrationNotifyError2(getActivity());
    }

    public DrawerLayout getDrawer() {
        return mDrawerLayout;
    }

    public EditText getEditText() {
        return editText;
    }

    public void setText(String text) {
        if(getCallback().readOnly()) {
            textView.setText(text);
        } else {
            editText.setText(text);
        }

        if(!(isDrawerOpen()) && !(keepClosed)) {
            getDrawer().openDrawer(mFragmentContainerView);
        }
    }

    public void reset(boolean close) {
        textViewTitle.setText(null);
        editText.setText(null);
        textView.setText(null);

        setScheduledToOpen(false);

        if(close && isDrawerOpen()) {
            getDrawer().closeDrawer(mFragmentContainerView);
        }
    }

    private ContentCallback callback;
    public ContentCallback getCallback() {
        return callback;
    }

    public void setCallback(ContentCallback callback) {
        this.callback = callback;

        if(callback == null) {
            reset(true);
        }
    }

    static abstract class ContentCallback {
        abstract boolean readOnly();
        abstract boolean visibility();
        abstract String title();
        Object tag = null;
    }
}
