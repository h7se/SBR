package com.qou.h7se.sbr;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by k0de9x on 10/2/2015.
 */
public class AboutFragment  extends BaseFragmentEx {

    public static AboutFragment newInstance(int sectionNumber) {
        AboutFragment fragment = new AboutFragment();
        Bundle args = new Bundle();
        args.putInt(Constants.ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public AboutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(Constants.ARG_SECTION_NUMBER));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
