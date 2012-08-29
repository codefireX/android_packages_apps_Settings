package com.android.settings;

import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;


import com.android.settings.R;
import com.android.settings.SettingsFragment;

public class CodefireSettings extends SettingsFragment
    implements Preference.OnPreferenceChangeListener {

    private final static String TAG = CodefireSettings.class.getSimpleName();

    private static final String TRACKBALL_WAKE_TOGGLE = "pref_trackball_wake_toggle";
    private static final String TRACKBALL_UNLOCK_TOGGLE = "pref_trackball_unlock_toggle";
    private static final String ENABLE_FAST_TORCH = "pref_enable_fast_torch";
    private static final String STATUSBAR_SIXBAR_SIGNAL = "pref_statusbar_sixbar_signal";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private CheckBoxPreference mTrackballWake;
    private CheckBoxPreference mTrackballUnlockScreen;
    private CheckBoxPreference mEnableQuickTorch;
    private CheckBoxPreference mUseSixbaricons;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.andromadus_settings);

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

        /* Fast torch */
        mEnableQuickTorch = (CheckBoxPreference) mPrefSet.findPreference(
                ENABLE_FAST_TORCH);
        mEnableQuickTorch.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.ENABLE_FAST_TORCH, 1) == 1);
        mEnableQuickTorch.setOnPreferenceChangeListener(this);

        /* Six bar pref */
        mUseSixbaricons = (CheckBoxPreference) mPrefSet.findPreference(
                STATUSBAR_SIXBAR_SIGNAL);
        mUseSixbaricons.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STATUSBAR_6BAR_SIGNAL, 1) == 1);
        mUseSixbaricons.setOnPreferenceChangeListener(this);

        /* Remove mTrackballWake on devices without trackballs */ 
        if (!getResources().getBoolean(R.bool.has_trackball)) {
            mPrefSet.removePreference(mTrackballWake);
            mPrefSet.removePreference(mTrackballUnlockScreen);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        
        if (TRACKBALL_WAKE_TOGGLE.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.TRACKBALL_WAKE_SCREEN, (Boolean) newValue ? 1 : 0);
        } else if (TRACKBALL_UNLOCK_TOGGLE.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.TRACKBALL_UNLOCK_SCREEN, (Boolean) newValue ? 1 : 0);
        } else if (ENABLE_FAST_TORCH.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.ENABLE_FAST_TORCH, (Boolean) newValue ? 1 : 0);
        } else if (STATUSBAR_SIXBAR_SIGNAL.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.STATUSBAR_6BAR_SIGNAL, (Boolean) newValue ? 1 : 0);
        }
        return true;
    }

}
