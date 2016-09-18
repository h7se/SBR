package com.qou.h7se.sbr;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;

/**
 * Created by k0de9x on 10/6/2015.
 */
 public class EditTextIntegerPreference extends EditTextPreference {

    private Integer value;

    public EditTextIntegerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    public EditTextIntegerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    public EditTextIntegerPreference(Context context) {
        super(context);
        getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
    }

    @Override public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();
        value = parseInteger(text);
        persistString(value != null ? value.toString() : null);
        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) notifyDependencyChange(isBlocking);
    }

    @Override public String getText() {
        return value != null ? value.toString() : null;
    }

    private static Integer parseInteger(String text) {
        try { return Integer.parseInt(text);
        }
        catch (NumberFormatException e) { return null; }
    }
}
