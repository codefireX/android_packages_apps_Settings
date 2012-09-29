
package com.android.settings.codefire;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.widget.Toast;

import com.android.settings.codefire.CMDProcessor;
import com.android.settings.codefire.Helpers;

import com.android.settings.R;
import com.android.settings.SettingsFragment;
import com.android.settings.SettingsPreferenceFragment;

import com.android.settings.colorpicker.ColorPickerPreference;

import java.io.IOException;
import java.util.List;

public class ThemePrefs extends SettingsFragment
    implements Preference.OnPreferenceChangeListener {

    private final static String TAG = ThemePrefs.class.getSimpleName();

    private static final int REQUEST_PICK_BOOT_ANIMATION = 203;

    private static final String KEY_DUAL_PANE = "dual_pane";
    private static final String KEY_NAVIGATION_BAR = "navigation_bar";
    private static final String KEY_LCD_DENSITY = "lcd_density";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String PREF_MODE_TABLET_UI = "mode_tabletui";
    private static final String PREF_NAVBAR_COLOR_DEF = "interface_navbar_color_default";

    private ContentResolver mCr;
    private PreferenceScreen mPrefSet;
    private PreferenceScreen mNavigationBar;
    private CheckBoxPreference mDualPane;
    private CheckBoxPreference mTabletui;

    private Preference mCustomBootAnimation;
    private Preference mCustomLabel;
    String mCustomLabelText = null;

    ColorPreference mNavBar;
    Preference mStockColor;
    Context mContext;
    ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.codefire_theme);

        mContext = (Context) getActivity();
        mResolver = mContext.getContentResolver();
        mPrefSet = getPreferenceScreen();
        mCr = getContentResolver();
        mNavigationBar = (PreferenceScreen) findPreference(KEY_NAVIGATION_BAR);

        /* Custom Boot Animation */
        mCustomBootAnimation = findPreference("custom_bootanimation");

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
        mTabletui.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.DUAL_PANE_SETTINGS, 0) == 1);
        mTabletui.setOnPreferenceChangeListener(this);

        /* Navigation Bar Custom Colors */
        mNavBar = (ColorPreference) findPreference("interface_navbar_color");
        mNavBar.setProviderTarget(Settings.System.SYSTEMUI_NAVBAR_COLOR,
                Settings.System.SYSTEMUI_NAVBAR_COLOR_DEF);
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
        } else if (preference == mCustomBootAnimation) {
            PackageManager packageManager = getActivity().getPackageManager();
            Intent test = new Intent(Intent.ACTION_GET_CONTENT);
            test.setType("file/*");
            List<ResolveInfo> list = packageManager.queryIntentActivities(test, PackageManager.GET_ACTIVITIES);
            if(list.size() > 0) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
                intent.setType("file/*");
                startActivityForResult(intent, REQUEST_PICK_BOOT_ANIMATION);
            } else {
                //No app installed to handle the intent - file explorer required
                Toast.makeText(getActivity().getApplicationContext(), R.string.install_file_manager_error, Toast.LENGTH_SHORT).show();
            }
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_PICK_BOOT_ANIMATION) {
                if (data==null) {
                    //Nothing returned by user, probably pressed back button in file manager
                    return;
                }

                String path = data.getData().getEncodedPath();

                Helpers.getMount("rw");
                //backup old boot animation
                new CMDProcessor().su.runWaitFor("mv /system/media/bootanimation.zip /system/media/bootanimation.backup");

                //Copy new bootanimation, give proper permissions
                new CMDProcessor().su.runWaitFor("cp "+ path +" /system/media/bootanimation.zip");
                new CMDProcessor().su.runWaitFor("chmod 644 /system/media/bootanimation.zip");

                Helpers.getMount("ro");
            }
        }
    }
}
