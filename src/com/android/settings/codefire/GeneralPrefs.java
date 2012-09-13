
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

public class GeneralPrefs extends SettingsFragment
    implements Preference.OnPreferenceChangeListener {

    private final static String TAG = GeneralPrefs.class.getSimpleName();

    private static final String TRACKBALL_WAKE_TOGGLE = "pref_trackball_wake_toggle";
    private static final String TRACKBALL_UNLOCK_TOGGLE = "pref_trackball_unlock_toggle";
    private static final String DISABLE_BOOTANIMATION_PREF = "pref_disable_bootanimation";
    private static final String DISABLE_BOOTANIMATION_PERSIST_PROP = "persist.sys.nobootanimation";
    private static final String DISABLE_BOOTANIMATION_DEFAULT = "0";
    private static final String PREF_RECENT_KILL_ALL = "recent_kill_all";
    private static final String KILL_APP_LONGPRESS_BACK_TIMEOUT = "pref_kill_app_longpress_back_timeout";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private CheckBoxPreference mTrackballWake;
    private CheckBoxPreference mTrackballUnlockScreen;
    private CheckBoxPreference mDisableBootanimPref;
    private CheckBoxPreference mRecentKillAll;

    private EditTextPreference mKillAppLongpressBackTimeout;

    private Preference mCustomLabel;
    String mCustomLabelText = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.codefire_general);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();

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

        /* Kill All button on recent apps */
        mRecentKillAll = (CheckBoxPreference) mPrefSet.findPreference(
                PREF_RECENT_KILL_ALL);
        mRecentKillAll.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.RECENT_KILL_ALL_BUTTON, 0) == 1);
        mRecentKillAll.setOnPreferenceChangeListener(this);

        /* Kill App Longpress Back timeout duration pref */
        mKillAppLongpressBackTimeout = (EditTextPreference) mPrefSet.findPreference(KILL_APP_LONGPRESS_BACK_TIMEOUT);
        mKillAppLongpressBackTimeout.setOnPreferenceChangeListener(this);

        /* Remove mTrackballWake on devices without trackballs */
        if (!getResources().getBoolean(R.bool.has_trackball)) {
            mPrefSet.removePreference(mTrackballWake);
            mPrefSet.removePreference(mTrackballUnlockScreen);
        }
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
        } else if (PREF_RECENT_KILL_ALL.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.RECENT_KILL_ALL_BUTTON, (Boolean) newValue ? 1 : 0);
        }
        return true;
    }

}
