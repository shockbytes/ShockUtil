package at.shockbytes.util.view;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import at.shockbytes.util.R;

/**
 * This is an own implementation of the SliderPreference, which allows the user
 * to choose between an interval from 0 - x seconds. This interval is the
 * location update interval in the TrackingService class.
 * 
 * @author Martin Macheiner
 *
 */
public class SliderPreference extends DialogPreference implements
		OnSeekBarChangeListener, OnClickListener {

	private static final String androidns = "http://schemas.android.com/apk/res/android";

	private SeekBar seekbar;
	private TextView valueText;

	private String unit;

	private int defaultVal;
	private int maxVal;
	private int val;

	public SliderPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(attrs);
	}

	public SliderPreference(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initialize(attrs);
	}

	private void initialize(AttributeSet attrs) {

		//Get string value for suffix
		int unitId = attrs.getAttributeResourceValue(androidns, "unit", 0);
		unit = (unitId == 0) ? attrs.getAttributeValue(androidns, "unit")
				: getContext().getString(unitId);

		defaultVal = attrs.getAttributeIntValue(androidns, "defaultValue", 0);
		maxVal = attrs.getAttributeIntValue(androidns, "max", 5);
	}

	@Override
	protected View onCreateDialogView() {

		View v = LayoutInflater.from(getContext()).inflate(
				R.layout.util_slider_pref, null);

		valueText = (TextView) v.findViewById(R.id.util_slider_pref_txt_value);
		seekbar = (SeekBar) v.findViewById(R.id.util_slider_pref_seekbar);
		seekbar.setOnSeekBarChangeListener(this);

		if (shouldPersist()) {
			val = getPersistedInt(defaultVal);
		}

		seekbar.setMax(maxVal);
		seekbar.setProgress(val);

		return v;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		seekbar.setMax(maxVal);
		seekbar.setProgress(val);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		super.onSetInitialValue(restorePersistedValue, defaultValue);

		if (restorePersistedValue) {
			val = (shouldPersist()) ? getPersistedInt(defaultVal) : 0;
		} else {
			val = (int) defaultValue;
		}
	}

	@Override
	protected void showDialog(Bundle state) {
		super.showDialog(state);

		Button positiveButton = ((AlertDialog) getDialog())
				.getButton(AlertDialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		if (shouldPersist()) {

			val = seekbar.getProgress();
			persistInt(val);
			callChangeListener(val);
			getDialog().dismiss();
		}

	}

	//--------------------------- OnSeekBarChangedListener ---------------------------
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {

		String t = String.valueOf(progress);
		valueText.setText((unit == null) ? t : t.concat(" " + unit));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	//--------------------------------------------------------------------------------

}
