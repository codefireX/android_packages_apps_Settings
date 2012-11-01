
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
    private static final String FAT_FINGERS = "interface_systembar_fat_fingers";
    private static final String PREF_NOTIFICATION_SHOW_WIFI_SSID = "notification_show_wifi_ssid";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private CheckBoxPreference mFatFingers;
    private CheckBoxPreference mShowBrightnessToggleslider;
    private CheckBoxPreference mShowWifiName;
    private ListPreference mStatusBarClockStyle;
    private ListPreference mRotationLockTogglePreference;

    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.codefire_statusbar);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();
        mContext = (Context) getActivity();

        /* Clock Style */
        mStatusBarClockStyle = (ListPreference) mPrefSet.findPreference(STATUS_BAR_CLOCK_STYLE);
        int statusBarClockStyle = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_CLOCK_STYLE, 1);
        mStatusBarClockStyle.setValue(String.valueOf(statusBarClockStyle));
        mStatusBarClockStyle.setSummary(mStatusBarClockStyle.getEntry());
        mStatusBarClockStyle.setOnPreferenceChangeListener(this);

        /* Larger Clear-All Button (TabletUI ONLY) */
        mFatFingers = (CheckBoxPreference) mPrefSet.findPreference(
                FAT_FINGERS);
        mFatFingers.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.SYSTEMUI_TABLET_BIG_CLEAR_BUTTON, 0) == 1);
        mFatFingers.setOnPreferenceChangeListener(this);

        /* Notification Area Brightness Toggleslider pref */
        mShowBrightnessToggleslider = (CheckBoxPreference) mPrefSet.findPreference(
                SHOW_BRIGHTNESS_TOGGLESLIDER);
        mShowBrightnessToggleslider.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.SHOW_BRIGHTNESS_TOGGLESLIDER, 0) == 1);
        mShowBrightnessToggleslider.setOnPreferenceChangeListener(this);

        /* RotationLock Toggle */
        mRotationLockTogglePreference = (ListPreference) findPreference("interface_rotationlock_toggle");
        mRotationLockTogglePreference.setOnPreferenceChangeListener(this);

        /* Show Wifi SSID in Notification Shade */
        mShowWifiName = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_NOTIFICATION_SHOW_WIFI_SSID);
        mShowWifiName.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.NOTIFICATION_SHOW_WIFI_SSID, false));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        if (SHOW_BRIGHTNESS_TOGGLESLIDER.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.SHOW_BRIGHTNESS_TOGGLESLIDER, (Boolean) newValue ? 1 : 0);
        } else if (FAT_FINGERS.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.SYSTEMUI_TABLET_BIG_CLEAR_BUTTON, (Boolean) newValue ? 1 : 0);
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

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mShowWifiName) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.NOTIFICATION_SHOW_WIFI_SSID,
                    ((CheckBoxPreference) preference).isChecked());
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
