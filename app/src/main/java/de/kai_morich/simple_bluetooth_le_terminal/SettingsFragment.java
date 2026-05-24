package de.kai_morich.simple_bluetooth_le_terminal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private SharedPreferences prefs;

    public static final String KEY_TEMP_UNIT = "temp_unit";
    public static final String KEY_FLOW_UNIT = "flow_unit";
    public static final String KEY_KEEP_SCREEN_ON = "keep_screen_on";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

        // Temp unit toggle
        TextView tempToggle = view.findViewById(R.id.tempUnitToggle);
        String tempUnit = prefs.getString(KEY_TEMP_UNIT, "C");
        tempToggle.setText(tempUnit.equals("C") ? "°C" : "°F");
        tempToggle.setOnClickListener(v -> {
            String current = prefs.getString(KEY_TEMP_UNIT, "C");
            String next = current.equals("C") ? "F" : "C";
            prefs.edit().putString(KEY_TEMP_UNIT, next).apply();
            tempToggle.setText(next.equals("C") ? "°C" : "°F");
        });

        // Flow unit toggle
        TextView flowToggle = view.findViewById(R.id.flowUnitToggle);
        String flowUnit = prefs.getString(KEY_FLOW_UNIT, "L");
        flowToggle.setText(flowUnit.equals("L") ? "L/min" : "mL/min");
        flowToggle.setOnClickListener(v -> {
            String current = prefs.getString(KEY_FLOW_UNIT, "L");
            String next = current.equals("L") ? "mL" : "L";
            prefs.edit().putString(KEY_FLOW_UNIT, next).apply();
            flowToggle.setText(next.equals("L") ? "L/min" : "mL/min");
        });

        // Keep screen on
        Switch screenSwitch = view.findViewById(R.id.keepScreenSwitch);
        screenSwitch.setChecked(prefs.getBoolean(KEY_KEEP_SCREEN_ON, false));
        screenSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, isChecked).apply();
            if (isChecked) {
                requireActivity().getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                requireActivity().getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });

        return view;
    }
}
