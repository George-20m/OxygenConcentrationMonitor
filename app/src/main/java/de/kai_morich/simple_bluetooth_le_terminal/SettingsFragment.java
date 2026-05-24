package de.kai_morich.simple_bluetooth_le_terminal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

        // Connect row
        TextView tvConnectStatus = view.findViewById(R.id.tvConnectStatus);
        View connectRow = view.findViewById(R.id.connectRow);
        View disconnectRow = view.findViewById(R.id.disconnectRow);

        updateConnectionUI(tvConnectStatus, connectRow, disconnectRow);

        connectRow.setOnClickListener(v -> {
            if (requireActivity().getSupportFragmentManager().findFragmentByTag("devices") != null) {
                return;
            }
            requireActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment, new DevicesFragment(), "devices")
                .addToBackStack(null)
                .commit();
        });

        disconnectRow.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).disconnectDevice();
            }
            updateConnectionUI(tvConnectStatus, connectRow, disconnectRow);
        });

        // Temp unit spinner
        Spinner tempSpinner = view.findViewById(R.id.tempUnitSpinner);
        ArrayAdapter<String> tempAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new String[]{"°C", "°F"});
        tempAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tempSpinner.setAdapter(tempAdapter);
        tempSpinner.setSelection(prefs.getString(KEY_TEMP_UNIT, "C").equals("C") ? 0 : 1);
        tempSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean first = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View v2, int position, long id) {
                if (first) { first = false; return; }
                prefs.edit().putString(KEY_TEMP_UNIT, position == 0 ? "C" : "F").apply();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Flow unit spinner
        Spinner flowSpinner = view.findViewById(R.id.flowUnitSpinner);
        ArrayAdapter<String> flowAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new String[]{"L/min", "mL/min"});
        flowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        flowSpinner.setAdapter(flowAdapter);
        flowSpinner.setSelection(prefs.getString(KEY_FLOW_UNIT, "L").equals("L") ? 0 : 1);
        flowSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean first = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View v2, int position, long id) {
                if (first) { first = false; return; }
                prefs.edit().putString(KEY_FLOW_UNIT, position == 0 ? "L" : "mL").apply();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
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

    @Override
    public void onResume() {
        super.onResume();
        View view = getView();
        if (view != null) {
            TextView tvConnectStatus = view.findViewById(R.id.tvConnectStatus);
            View connectRow = view.findViewById(R.id.connectRow);
            View disconnectRow = view.findViewById(R.id.disconnectRow);
            updateConnectionUI(tvConnectStatus, connectRow, disconnectRow);
        }
    }

    private void updateConnectionUI(TextView tvConnectStatus, View connectRow, View disconnectRow) {
        boolean connected = getActivity() instanceof MainActivity
                && ((MainActivity) getActivity()).isConnected();
        if (connected) {
            tvConnectStatus.setText("● Connected");
            tvConnectStatus.setTextColor(0xFF43A047);
            disconnectRow.setVisibility(View.VISIBLE);
        } else {
            tvConnectStatus.setText("● Not connected");
            tvConnectStatus.setTextColor(0xFFAAAAAA);
            disconnectRow.setVisibility(View.GONE);
        }
    }
}
