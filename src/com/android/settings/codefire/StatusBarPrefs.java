
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

public class StatusBarPrefs extends SettingsFragment
    implements Preference.OnPreferenceChangeListener {

    private final static String TAG = StatusBarPrefs.class.getSimpleName();

    private static final String SHOW_BRIGHTNESS_TOGGLESLIDER = "pref_show_brightness_toggleslider";
    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock_style";
    private static final String ROTATIONLOCK_TOGGLE = "interface_rotationlock_toggle";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private CheckBoxPreference mShowBrightnessToggleslider;
    private ListPreference mStatusBarClockStyle;
    private ListPreference mRotationLockTogglePreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.codefire_statusbar);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();
        mStatusBarClockStyle = (ListPreference) mPrefSet.findPreference(STATUS_BAR_CLOCK_STYLE);

        /* Clock Style */
        int statusBarClockStyle = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_CLOCK_STYLE, 1);
        mStatusBarClockStyle.setValue(String.valueOf(statusBarClockStyle));
        mStatusBarClockStyle.setSummary(mStatusBarClockStyle.getEntry());
        mStatusBarClockStyle.setOnPreferenceChangeListener(this);

        /* RotationLock Toggle */
        mRotationLockTogglePreference = (ListPreference) findPreference("interface_rotationlock_toggle");
        mRotationLockTogglePreference.setOnPreferenceChangeListener(this);

        /* Notification Area Brightness Toggleslider pref */
        mShowBrightnessToggleslider = (CheckBoxPreference) mPrefSet.findPreference(
                SHOW_BRIGHTNESS_TOGGLESLIDER);
        mShowBrightnessToggleslider.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.SHOW_BRIGHTNESS_TOGGLESLIDER, 0) == 1);
        mShowBrightnessToggleslider.setOnPreferenceChangeListener(this);

        if (mTablet) {
            prefs.removePreference(mRotationLockTogglePreference);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        if (SHOW_BRIGHTNESS_TOGGLESLIDER.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.SHOW_BRIGHTNESS_TOGGLESLIDER, (Boolean) newValue ? 1 : 0);
        } else if (STATUS_BAR_CLOCK_STYLE.equals(key)) {
            int statusBarClockStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarClockStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_CLOCK_STYLE, statusBarClockStyle);
            mStatusBarClockStyle.setSummary(mStatusBarClockStyle.getEntries()[index]);
        } else if (ROTATIONLOCK_TOGGLE.equals(key)) {
            final String newToggleMode = (String) newValue;
            Settings.System.putString(getContentResolver(),
                    Settings.System.SYSTEMUI_INTERFACE_ROTATIONLOCK_TOGGLE,
                    newToggleMode);
        }
        return true;
    }

}
