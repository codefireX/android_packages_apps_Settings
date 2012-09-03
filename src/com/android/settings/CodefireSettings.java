package com.android.settings;

import android.content.ContentResolver;
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

import com.android.settings.R;
import com.android.settings.SettingsFragment;

import com.android.settings.colorpicker.ColorPickerPreference;

public class CodefireSettings extends SettingsFragment
    implements Preference.OnPreferenceChangeListener {

    private final static String TAG = CodefireSettings.class.getSimpleName();

    private static final String TRACKBALL_WAKE_TOGGLE = "pref_trackball_wake_toggle";
    private static final String TRACKBALL_UNLOCK_TOGGLE = "pref_trackball_unlock_toggle";
    private static final String STATUSBAR_SIXBAR_SIGNAL = "pref_statusbar_sixbar_signal";
    private static final String DISABLE_BOOTANIMATION_PREF = "pref_disable_bootanimation";
    private static final String DISABLE_BOOTANIMATION_PERSIST_PROP = "persist.sys.nobootanimation";
    private static final String DISABLE_BOOTANIMATION_DEFAULT = "0";
    private static final String PREF_RECENT_KILL_ALL = "recent_kill_all";
    private static final String KEY_DUAL_PANE = "dual_pane";
    private static final String SHOW_BRIGHTNESS_TOGGLESLIDER = "pref_show_brightness_toggleslider";
    private static final String KILL_APP_LONGPRESS_BACK_TIMEOUT = "pref_kill_app_longpress_back_timeout";
    private static final String KEY_NAVIGATION_BAR = "navigation_bar";
    private static final String KEY_LCD_DENSITY = "lcd_density";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;
    private PreferenceScreen mNavigationBar;

    private CheckBoxPreference mTrackballWake;
    private CheckBoxPreference mTrackballUnlockScreen;
    private CheckBoxPreference mUseSixbaricons;
    private CheckBoxPreference mDisableBootanimPref;
    private CheckBoxPreference mRecentKillAll;
    private CheckBoxPreference mDualPane;
    private CheckBoxPreference mShowBrightnessToggleslider;

    private EditTextPreference mKillAppLongpressBackTimeout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.codefire_settings);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();
        mNavigationBar = (PreferenceScreen) findPreference(KEY_NAVIGATION_BAR);

        /* Trackball wake pref */
        mTrackballWake = (CheckBoxPreference) mPrefSet.findPreference(
                TRACKBALL_WAKE_TOGGLE);
        mTrackballWake.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.TRACKBALL_WAKE_SCREEN, 1) == 1);
        mTrackballWake.setOnPreferenceChangeListener(this);

        /* Trackball unlock pref */
        mTrackballUnlockScreen = (CheckBoxPreference) mPrefSet.findPreference(
                TRACKBALL_UNLOCK_TOGGLE);
        mTrackballUnlockScreen.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.TRACKBALL_UNLOCK_SCREEN, 1) == 1);
        mTrackballUnlockScreen.setOnPreferenceChangeListener(this);

        /* Six bar pref */
        mUseSixbaricons = (CheckBoxPreference) mPrefSet.findPreference(
                STATUSBAR_SIXBAR_SIGNAL);
        mUseSixbaricons.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STATUSBAR_6BAR_SIGNAL, 0) == 1);
        mUseSixbaricons.setOnPreferenceChangeListener(this);

        /* Kill All button on recent apps */
        mRecentKillAll = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_RECENT_KILL_ALL);
        mRecentKillAll.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_KILL_ALL_BUTTON, 0) == 1);
        mRecentKillAll.setOnPreferenceChangeListener(this);

        /* Dual pane toggle */
        mDualPane = (CheckBoxPreference) mPrefSet.findPreference(
                KEY_DUAL_PANE);
        mDualPane.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.DUAL_PANE_SETTINGS, 0) == 1);
        mDualPane.setOnPreferenceChangeListener(this);

        /* Notification Area Brightness Toggleslider pref */
        mShowBrightnessToggleslider = (CheckBoxPreference) mPrefSet.findPreference(
                SHOW_BRIGHTNESS_TOGGLESLIDER);
        mShowBrightnessToggleslider.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.SHOW_BRIGHTNESS_TOGGLESLIDER, 0) == 1);
        mShowBrightnessToggleslider.setOnPreferenceChangeListener(this);

        /* Disable BootAnimation Toggle */
        mDisableBootanimPref = (CheckBoxPreference) mPrefSet.findPreference(DISABLE_BOOTANIMATION_PREF);
        String disableBootanim = SystemProperties.get(DISABLE_BOOTANIMATION_PERSIST_PROP, DISABLE_BOOTANIMATION_DEFAULT);
        mDisableBootanimPref.setChecked("1".equals(disableBootanim));

        /* Kill App Longpress Back timeout duration pref */
        mKillAppLongpressBackTimeout = (EditTextPreference) mPrefSet.findPreference(KILL_APP_LONGPRESS_BACK_TIMEOUT);
        mKillAppLongpressBackTimeout.setOnPreferenceChangeListener(this);

        /* Remove mTrackballWake on devices without trackballs */
        if (!getResources().getBoolean(R.bool.has_trackball)) {
            mPrefSet.removePreference(mTrackballWake);
            mPrefSet.removePreference(mTrackballUnlockScreen);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // This kinda permanently sets the summary in english & makes the definition in strings.xml useless.. should probably fix
        mKillAppLongpressBackTimeout.setSummary("Hold down back button for " + mKillAppLongpressBackTimeout.getText() + "ms to kill a process");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mDisableBootanimPref) {
            SystemProperties.set(DISABLE_BOOTANIMATION_PERSIST_PROP,
                    mDisableBootanimPref.isChecked() ? "1" : "0");
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        
        if (KILL_APP_LONGPRESS_BACK_TIMEOUT.equals(key)) {
            try {
                int timeout = Integer.parseInt((String) newValue);
                if (timeout < 500 || timeout > 2000) {
                    // Out of bounds, bail!
                    return false;
                }
                Settings.System.putInt(mCr, KILL_APP_LONGPRESS_BACK_TIMEOUT, timeout);
                mKillAppLongpressBackTimeout.setSummary("Hold down back button for " + timeout + "ms to kill a process");
                mKillAppLongpressBackTimeout.setText(Integer.toString(timeout));
            } finally {
                Log.d(TAG, "Exception error on preference change.");
                return false;
            }
        } else if (TRACKBALL_WAKE_TOGGLE.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.TRACKBALL_WAKE_SCREEN, (Boolean) newValue ? 1 : 0);
        } else if (TRACKBALL_UNLOCK_TOGGLE.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.TRACKBALL_UNLOCK_SCREEN, (Boolean) newValue ? 1 : 0);
        } else if (STATUSBAR_SIXBAR_SIGNAL.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.STATUSBAR_6BAR_SIGNAL, (Boolean) newValue ? 1 : 0);
        } else if (PREF_RECENT_KILL_ALL.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.RECENT_KILL_ALL_BUTTON, (Boolean) newValue ? 1 : 0);
        } else if (KEY_DUAL_PANE.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.DUAL_PANE_SETTINGS, (Boolean) newValue ? 1 : 0);
        } else if (SHOW_BRIGHTNESS_TOGGLESLIDER.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.SHOW_BRIGHTNESS_TOGGLESLIDER, (Boolean) newValue ? 1 : 0);
        }
        return true;
    }

}
