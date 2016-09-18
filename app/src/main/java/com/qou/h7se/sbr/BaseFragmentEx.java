package com.qou.h7se.sbr;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;

public class BaseFragmentEx extends BaseFragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    public BaseFragmentEx() {
    }

    @Override
    public void onResume() {
        super.onResume();

        View v = getView();
        if(v != null) {
            if(Helper.Background.drawable != null) {
                v.setBackground(Helper.Background.drawable);
            } else {
                v.setBackgroundResource(0);
            }
        }
    }

    public void setBackgroundFromResource(View rootView, int resid) {
        rootView.findViewById(R.id.layoutFragmentRoot).setBackgroundResource(resid);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Helper.Background.update(getContext());
    }

    public void toggleMenuItem(MenuItem menuItem, int checkedIcon, int unCheckedIcon) {
        menuItem.setChecked(!menuItem.isChecked());
        if(menuItem.isChecked()) {
            menuItem.setIcon(checkedIcon);
        } else {
            menuItem.setIcon(unCheckedIcon);
        }
    }

    @Override
    boolean onBackPressed() {
        return false;
    }

    static class Helper {
        static class Background {
            public static Drawable drawable = null;

            public static void update(Context context) {
                if(PrefsActivity.getBoolean(PrefsActivity.ENABLE_BACKGROUND_IMAGE, false)) {
                    String file = PrefsActivity.getString(PrefsActivity.BACKGROUND_IMAGE, null);
                    if(file != null) {
                        try {
                            if(Utils.containsCaseInsensitive(context.getAssets().list("backgrounds"), file)) {
                                drawable = Drawable.createFromStream(context.getAssets().open(String.format("backgrounds/%s", file)), null);
                            }
                        } catch (IOException e) {
                            Utils.LogException(e);
                        }
                    } else {
                        drawable = null;
                    }
                } else {
                    drawable = null;
                }
            }
        }
    }
}
