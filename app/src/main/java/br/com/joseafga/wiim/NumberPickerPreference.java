package br.com.joseafga.wiim;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

/**
 * A {@link android.preference.Preference} that displays a number picker as a dialog.
 */
public class NumberPickerPreference extends DialogPreference {

    // allowed range
    private int minValue = 0;
    private int maxValue = 0;
    private int stepValue = 1;
    private String[] valueSet = null;
    // enable or disable the 'circular behavior'
    private static final boolean WRAP_SELECTOR_WHEEL = true;

    private NumberPicker picker;
    private int value;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        processXmlAttributes(context, attrs, 0);
    }

    public NumberPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        processXmlAttributes(context, attrs, defStyleAttr);
    }

    /**
     * This method reads the parameters given in the xml file and sets the properties according to it
     */
    private void processXmlAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.NumberPickerPreferences, defStyleAttr, 0);

        try {
            this.minValue = attributes.getInt(R.styleable.NumberPickerPreferences_minValue, minValue);
            this.maxValue = attributes.getInt(R.styleable.NumberPickerPreferences_maxValue, maxValue);
            this.stepValue = attributes.getInt(R.styleable.NumberPickerPreferences_stepValue, stepValue);
        } finally {
            attributes.recycle();
        }
    }

    @Override
    protected View onCreateDialogView() {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;

        picker = new NumberPicker(getContext());
        picker.setLayoutParams(layoutParams);

        FrameLayout dialogView = new FrameLayout(getContext());
        dialogView.addView(picker);

        return dialogView;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        // array length
        int len = ((maxValue - minValue) / stepValue) + 1;

        // create array with right size
        valueSet = new String[len];
        // fill array with values
        for (int i = 0; i < len; i++) {
            valueSet[i] = String.valueOf(i * stepValue + minValue);
        }

        // set number picker settings
        picker.setDisplayedValues(valueSet);
        picker.setMinValue(minValue / stepValue);
        picker.setMaxValue(maxValue / stepValue);
        picker.setWrapSelectorWheel(WRAP_SELECTOR_WHEEL);
        picker.setValue(value);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            picker.clearFocus();
            int newValue = picker.getValue();
            if (callChangeListener(newValue)) {
                setValue(newValue);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, minValue);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedInt(minValue) : (Integer) defaultValue);
    }

    public String getDisplayedValue() {
        return this.valueSet[this.value];
    }

    public void setValue(int value) {
        this.value = value;
        persistInt(this.value);
    }

    public int getValue() {
        return this.value;
    }
}