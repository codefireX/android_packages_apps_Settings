
package com.android.settings.codefire;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.Spannable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsFragment;

import com.android.settings.colorpicker.ColorPickerPreference;

import java.io.IOException;

public class ThemePrefs extends SettingsFragment
    implements Preference.OnPreferenceChangeListener {

    private final static String TAG = ThemePrefs.class.getSimpleName();

    private static final String KEY_DUAL_PANE = "dual_pane";
    private static final String KEY_NAVIGATION_BAR = "navigation_bar";
    private static final String KEY_LCD_DENSITY = "lcd_density";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String PREF_MODE_TABLET_UI = "mode_tabletui";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;
    private PreferenceScreen mNavigationBar;
    private CheckBoxPreference mDualPane;
    private CheckBoxPreference mTabletui;

    private Preference mCustomLabel;
    String mCustomLabelText = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.codefire_theme);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();
        mNavigationBar = (PreferenceScreen) findPreference(KEY_NAVIGATION_BAR);

        /* Custom Carrier Label */
        mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        updateCustomLabelTextSummary();

        /* Dual pane toggle */
        mDualPane = (CheckBoxPreference) mPrefSet.findPreference(
                KEY_DUAL_PANE);
        mDualPane.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.DUAL_PANE_SETTINGS, 0) == 1);
        mDualPane.setOnPreferenceChangeListener(this);

        /* Force Tablet UI */
        mTabletui = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_MODE_TABLET_UI);
        mDualPane.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.MODE_TABLET_UI, 0) == 1);
        mDualPane.setOnPreferenceChangeListener(this);
    }

    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null || mCustomLabelText.length() == 0) {
            mCustomLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mCustomLabel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(mCustomLabelText != null ? mCustomLabelText : "");
            alert.setView(input);

            alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString();
                    Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                    Intent i = new Intent();
                    i.setAction("com.android.settings.LABEL_CHANGED");
                    getActivity().getApplicationContext().sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        if (KEY_DUAL_PANE.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.DUAL_PANE_SETTINGS, (Boolean) newValue ? 1 : 0);
        } else if (PREF_MODE_TABLET_UI.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.MODE_TABLET_UI, (Boolean) newValue ? 1 : 0);
        }
        return true;
    }

}
