
package com.android.settings.codefire;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
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
import com.android.settings.codefire.ShortcutPickerHelper;
import com.android.settings.SettingsFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatusBarPrefs extends SettingsFragment implements
    ShortcutPickerHelper.OnPickListener, Preference.OnPreferenceChangeListener {

    private final static String TAG = StatusBarPrefs.class.getSimpleName();

    private static final String SHOW_BRIGHTNESS_TOGGLESLIDER = "pref_show_brightness_toggleslider";
    private static final String STATUS_BAR_CLOCK_STYLE = "status_bar_clock_style";
    private static final String ROTATIONLOCK_TOGGLE = "interface_rotationlock_toggle";
    private static final String FAT_FINGERS = "interface_systembar_fat_fingers";
    private static final String PREF_NOTIFICATION_SHOW_WIFI_SSID = "notification_show_wifi_ssid";
    private static final String PREF_DATE_SHORTCLICK = "date_shortclick";
    private static final String PREF_DATE_LONGCLICK = "date_longclick";
    private static final String PREF_CLOCK_SHORTCLICK = "clock_shortclick";
    private static final String PREF_CLOCK_LONGCLICK = "clock_longclick";

    private ShortcutPickerHelper mPicker;
    private Preference mPreference;
    private String mString;
    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private CheckBoxPreference mFatFingers;
    private CheckBoxPreference mShowBrightnessToggleslider;
    private CheckBoxPreference mShowWifiName;
    private ListPreference mStatusBarClockStyle;
    private ListPreference mRotationLockTogglePreference;
    private ListPreference mDateShortClick;
    private ListPreference mDateLongClick;
    private ListPreference mClockShortClick;
    private ListPreference mClockLongClick;

    Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.codefire_statusbar);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();
        mContext = (Context) getActivity();
        mPicker = new ShortcutPickerHelper(this, this);

        /* Clock Style */
        mStatusBarClockStyle = (ListPreference) mPrefSet.findPreference(STATUS_BAR_CLOCK_STYLE);
        int statusBarClockStyle = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_CLOCK_STYLE, 1);
        mStatusBarClockStyle.setValue(String.valueOf(statusBarClockStyle));
        mStatusBarClockStyle.setSummary(mStatusBarClockStyle.getEntry());
        mStatusBarClockStyle.setOnPreferenceChangeListener(this);

        /* Custom Clock/Date Intents */
        mDateShortClick = (ListPreference) mPrefSet.findPreference(PREF_DATE_SHORTCLICK);
        mDateShortClick.setOnPreferenceChangeListener(this);
        mDateShortClick.setSummary(getProperSummary(mDateShortClick));

        mDateLongClick = (ListPreference) mPrefSet.findPreference(PREF_DATE_LONGCLICK);
        mDateLongClick.setOnPreferenceChangeListener(this);
        mDateLongClick.setSummary(getProperSummary(mDateLongClick));

        mClockShortClick = (ListPreference) mPrefSet.findPreference(PREF_CLOCK_SHORTCLICK);
        mClockShortClick.setOnPreferenceChangeListener(this);
        mClockShortClick.setSummary(getProperSummary(mClockShortClick));

        mClockLongClick = (ListPreference) mPrefSet.findPreference(PREF_CLOCK_LONGCLICK);
        mClockLongClick.setOnPreferenceChangeListener(this);
        mClockLongClick.setSummary(getProperSummary(mClockLongClick));

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
        boolean result = false;
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
        } else if (preference == mDateShortClick) {
            mPreference = preference;
            mString = Settings.System.NOTIFICATION_DATE_SHORTCLICK;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_DATE_SHORTCLICK, (String) newValue);
            mDateShortClick.setSummary(getProperSummary(mDateShortClick));
            }
        } else if (preference == mDateLongClick) {
            mPreference = preference;
            mString = Settings.System.NOTIFICATION_DATE_LONGCLICK;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_DATE_LONGCLICK, (String) newValue);
            mDateLongClick.setSummary(getProperSummary(mDateLongClick));
            }
        } else if (preference == mClockShortClick) {
            mPreference = preference;
            mString = Settings.System.NOTIFICATION_CLOCK_SHORTCLICK;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_CLOCK_SHORTCLICK, (String) newValue);
            mClockShortClick.setSummary(getProperSummary(mClockShortClick));
            }
        } else if (preference == mClockLongClick) {
            mPreference = preference;
            mString = Settings.System.NOTIFICATION_CLOCK_LONGCLICK;
            if (newValue.equals("**app**")) {
             mPicker.pickShortcut();
            } else {
            result = Settings.System.putString(getContentResolver(), Settings.System.NOTIFICATION_CLOCK_LONGCLICK, (String) newValue);
            mClockLongClick.setSummary(getProperSummary(mClockLongClick));
            }
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

    public void shortcutPicked(String uri, String friendlyName, Bitmap bmp, boolean isApplication) {
          mPreference.setSummary(friendlyName);
          Settings.System.putString(getContentResolver(), mString, (String) uri);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getProperSummary(Preference preference) {
        if (preference == mDateLongClick) {
            mString = Settings.System.NOTIFICATION_DATE_LONGCLICK;
        } else if (preference == mClockLongClick) {
            mString = Settings.System.NOTIFICATION_CLOCK_LONGCLICK;
        } else if (preference == mDateShortClick) {
            mString = Settings.System.NOTIFICATION_DATE_SHORTCLICK;
        } else if (preference == mClockShortClick) {
            mString = Settings.System.NOTIFICATION_CLOCK_SHORTCLICK;
        }

        String uri = Settings.System.getString(getActivity().getContentResolver(),mString);
        String empty = "";

        if (uri == null)
            return empty;

        if (uri.startsWith("**")) {
            if (uri.equals("**alarm**"))
                return getResources().getString(R.string.alarm);
            else if (uri.equals("**event**"))
                return getResources().getString(R.string.event);
            else if (uri.equals("**assist**"))
                return getResources().getString(R.string.voiceassist);
            else if (uri.equals("**today**"))
                return getResources().getString(R.string.today);
            else if (uri.equals("**nothing**"))
                return getResources().getString(R.string.nothing);
        } else {
            return mPicker.getFriendlyNameForUri(uri);
        }
        return null;
    }
}
