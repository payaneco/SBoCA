package jp.payaneco.sboca;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsActivity extends Activity {
    private SettingsFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, fragment).commit();
    }

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.layout.preferences);

            getLuid();
            getGhost();
            getPlace();
            //setListSummary(getString(R.string.preference_speed_key));

            Preference button = findPreference(getString(R.string.preference_luid_get_key));
            button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference pref) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            NewIdResult result = SstpClient.getNewId();
                            if(!result.success()) {
                                showToast(result.getExtraMessage(), Toast.LENGTH_SHORT);
                            }
                            Settings.setLuid(getActivity(), result.getNewId());
                            getLuid();
                            showToast(result.getMessage(), Toast.LENGTH_SHORT);
                        }
                    });
                    thread.start();
                    return true;
                }
            });
        }

        private void showToast(final String msg, final int duration) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), msg, duration).show();
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }
        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.preference_luid_edit_key))) {
                setLuid();
            } else if (key.equals(getString(R.string.preference_ghost_key))) {
                setGhost();
            } else if (key.equals(getString(R.string.preference_place_key))) {
                setPlace();
            } else if (key.equals(getString(R.string.preference_speed_key))) {
                setListSummary(key);
            }
        }

        private void getLuid() {
            String key = getString(R.string.preference_luid_edit_key);
            EditTextPreference pref = (EditTextPreference) findPreference(key);
            pref.setText(Settings.getLuid(getActivity()));
            setLuidSummary();
        }

        private void setLuid() {
            String key = getString(R.string.preference_luid_edit_key);
            EditTextPreference pref = (EditTextPreference) findPreference(key);
            Settings.setLuid(getActivity(), pref.getText().trim());
            setLuidSummary();
        }

        private void getGhost() {
            String key = getString(R.string.preference_ghost_key);
            EditTextPreference pref = (EditTextPreference) findPreference(key);
            pref.setText(Settings.getCurrentGhost(getActivity()));
            setTextSummary(key);
        }

        private void setGhost() {
            String key = getString(R.string.preference_ghost_key);
            EditTextPreference pref = (EditTextPreference) findPreference(key);
            Settings.setCurrentGhost(getActivity(), pref.getText().trim());
            setTextSummary(key);
        }

        private void getPlace() {
            String key = getString(R.string.preference_place_key);
            ListPreference pref = (ListPreference) findPreference(key);
            ChannelMap map = Settings.getChannelMap();
            if(map == null) {
                //todo ちゃんとチャンネルを設定する
                String[] channels = {"駅前繁華街", "海浜公園街", "学園街", "山の温泉街"};
                Arrays.sort(channels);
                pref.setEntries(channels);
                pref.setEntryValues(channels);
                return;
            }
            pref.setEntries(map.getChannels());
            pref.setEntryValues(map.getChannels());
            pref.setValue(Settings.getCurrentChannel(getActivity()));
            setListSummary(key);
        }

        private void setPlace() {
            String key = getString(R.string.preference_place_key);
            ListPreference pref = (ListPreference) findPreference(key);
            Settings.setCurrentChannel(getActivity(), pref.getValue());
            setListSummary(key);
        }

        private void setLuidSummary() {
            String key = getString(R.string.preference_luid_edit_key);
            EditTextPreference pref = (EditTextPreference) findPreference(key);
            if(pref.getText() != null && !pref.getText().isEmpty()) {
                setSummary(pref, getString(R.string.luid_exists));
            }
        }

        private void setTextSummary(String key) {
            EditTextPreference pref = (EditTextPreference) findPreference(key);
            setSummary(pref, pref.getText());
        }

        private void setListSummary(String key) {
            ListPreference pref = (ListPreference) findPreference(key);
            setSummary(pref, pref.getValue());
        }

        private void setSummary(Preference pref, String value) {
            if(value == null || value.isEmpty()) {
                return;
            }
            pref.setSummary(value);
        }
    }
}
