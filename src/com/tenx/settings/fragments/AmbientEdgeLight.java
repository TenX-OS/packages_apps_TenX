/*
 * Copyright (C) 2021 TenX-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tenx.settings.fragments;

import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.tenx.support.colorpicker.ColorPickerPreference;
import com.tenx.support.preferences.CustomSeekBarPreference;
import com.tenx.support.preferences.SystemSettingListPreference;
import com.tenx.support.preferences.SystemSettingSwitchPreference;

public class AmbientEdgeLight extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "EdgeLight";

    private static final String AMBIENT_LIGHT_ENABLED = "ambient_notification_light_enabled";
    private static final String AMBIENT_LIGHT_HIDE_AOD = "ambient_notification_light_hide_aod";
    private static final String AMBIENT_LIGHT_LAYOUT = "ambient_light_layout";
    private static final String AMBIENT_LIGHT_COLOR = "ambient_notification_color_mode";
    private static final String AMBIENT_LIGHT_CUSTOM_COLOR = "ambient_notification_light_color";
    private static final String AMBIENT_LIGHT_DURATION = "ambient_notification_light_duration";
    private static final String AMBIENT_LIGHT_REPEAT_COUNT = "ambient_notification_light_repeats";
    private static final String AMBIENT_LIGHT_REPEAT_DIRECTION = "ambient_light_repeat_direction";
    private static final String AMBIENT_LIGHT_ALWAYS = "ambient_light_pulse_for_all";
    private static final String AMBIENT_LIGHT_TIMEOUT = "ambient_notification_light_timeout";

    private SystemSettingSwitchPreference mEdgeLightEnabled;
    private SystemSettingSwitchPreference mEdgeLightHideAod;
    private SystemSettingSwitchPreference mEdgeLightAlways;
    private SystemSettingListPreference mEdgeLightColorMode;
    private ColorPickerPreference mEdgeLightColor;
    private CustomSeekBarPreference mEdgeLightDuration;
    private CustomSeekBarPreference mEdgeLightRepeatCount;
    private SystemSettingListPreference mEdgeLightRepeatDirection;
    private SystemSettingListPreference mEdgeLightLayout;
    private SystemSettingListPreference mEdgeLightTimeout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ambient_edge_light);

        mEdgeLightEnabled = (SystemSettingSwitchPreference) findPreference(AMBIENT_LIGHT_ENABLED);
        mEdgeLightHideAod = (SystemSettingSwitchPreference) findPreference(AMBIENT_LIGHT_HIDE_AOD);
        mEdgeLightAlways = (SystemSettingSwitchPreference) findPreference(AMBIENT_LIGHT_ALWAYS);
        mEdgeLightRepeatDirection = (SystemSettingListPreference) findPreference(AMBIENT_LIGHT_REPEAT_DIRECTION);
        mEdgeLightLayout = (SystemSettingListPreference) findPreference(AMBIENT_LIGHT_LAYOUT);
        mEdgeLightTimeout = (SystemSettingListPreference) findPreference(AMBIENT_LIGHT_TIMEOUT);

        mEdgeLightColorMode = (SystemSettingListPreference) findPreference(AMBIENT_LIGHT_COLOR);
        int edgeLightColorMode = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_COLOR_MODE, 0, UserHandle.USER_CURRENT);
        mEdgeLightColorMode.setValue(String.valueOf(edgeLightColorMode));
        mEdgeLightColorMode.setSummary(mEdgeLightColorMode.getEntry());
        mEdgeLightColorMode.setOnPreferenceChangeListener(this);

        mEdgeLightColor = (ColorPickerPreference) findPreference(AMBIENT_LIGHT_CUSTOM_COLOR);
        int edgeLightColor = Settings.System.getInt(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_COLOR, 0xFFFFFFFF);
        mEdgeLightColor.setNewPreviewColor(edgeLightColor);
        String edgeLightColorHex = String.format("#%08x", (0xFFFFFFFF & edgeLightColor));
        if (edgeLightColorHex.equals("#ffffffff")) {
            mEdgeLightColor.setSummary(R.string.default_string);
        } else {
            mEdgeLightColor.setSummary(edgeLightColorHex);
        }
        mEdgeLightColor.setOnPreferenceChangeListener(this);

        mEdgeLightDuration = (CustomSeekBarPreference) findPreference(AMBIENT_LIGHT_DURATION);
        int lightDuration = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_DURATION, 2, UserHandle.USER_CURRENT);
        mEdgeLightDuration.setValue(lightDuration);
        mEdgeLightDuration.setOnPreferenceChangeListener(this);

        mEdgeLightRepeatCount = (CustomSeekBarPreference) findPreference(AMBIENT_LIGHT_REPEAT_COUNT);
        int edgeLightRepeatCount = Settings.System.getIntForUser(getContentResolver(),
                Settings.System.NOTIFICATION_PULSE_REPEATS, 0, UserHandle.USER_CURRENT);
        mEdgeLightRepeatCount.setValue(edgeLightRepeatCount);
        mEdgeLightRepeatCount.setOnPreferenceChangeListener(this);

        updateColorPrefs(edgeLightColorMode);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mEdgeLightColorMode) {
            int edgeLightColorMode = Integer.valueOf((String) newValue);
            int index = mEdgeLightColorMode.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_COLOR_MODE, edgeLightColorMode, UserHandle.USER_CURRENT);
            mEdgeLightColorMode.setSummary(mEdgeLightColorMode.getEntries()[index]);
            updateColorPrefs(edgeLightColorMode);
            return true;
        } else if (preference == mEdgeLightColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#ffffffff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_COLOR, intHex);
            return true;
        } else if (preference == mEdgeLightDuration) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_DURATION, value, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mEdgeLightRepeatCount) {
            int value = (Integer) newValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NOTIFICATION_PULSE_REPEATS, value, UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    private void updateColorPrefs(int edgeLightColorMode) {
        if (mEdgeLightColor != null) {
            if (edgeLightColorMode == 3) {
                getPreferenceScreen().addPreference(mEdgeLightColor);
            } else {
                getPreferenceScreen().removePreference(mEdgeLightColor);
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.TENX;
    }
}
