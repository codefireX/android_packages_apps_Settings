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

    private static final String STATUSBAR_SIXBAR_SIGNAL = "pref_statusbar_sixbar_signal";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;

    private CheckBoxPreference mUseSixbaricons;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.codefire_settings);

        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();

        /* Six bar pref */
        mUseSixbaricons = (CheckBoxPreference) mPrefSet.findPreference(
                STATUSBAR_SIXBAR_SIGNAL);
        mUseSixbaricons.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STATUSBAR_6BAR_SIGNAL, 1) == 1);
        mUseSixbaricons.setOnPreferenceChangeListener(this);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        
        if (STATUSBAR_SIXBAR_SIGNAL.equals(key)) {
            Settings.System.putInt(mCr, Settings.System.STATUSBAR_6BAR_SIGNAL, (Boolean) newValue ? 1 : 0);
        }
        return true;
    }

}
