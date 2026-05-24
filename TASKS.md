# OxyLife – TASKS.md (Round 4)
# Read this entire file first, then execute each task in order.
# After each task confirm what you did before moving to the next one.
# Do NOT skip ahead. Do NOT touch any file not mentioned in the task.
# These files are COMPLETELY OFF LIMITS — never touch them:
#   SerialService.java, SerialSocket.java, SerialListener.java,
#   BluetoothUtil.java, TextUtil.java, Constants.java, DevicesFragment.java,
#   SpeedometerView.java, AlertsFragment.java, AboutFragment.java

---

## TASK 1 — Fix bottom nav icons to match the design

File: app/src/main/res/menu/menu_bottom_nav.xml

Replace the entire content with:

<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/nav_home"
        android:icon="@android:drawable/ic_menu_home"
        android:title="Home"/>
    <item
        android:id="@+id/nav_alerts"
        android:icon="@android:drawable/ic_dialog_alert"
        android:title="Alerts"/>
    <item
        android:id="@+id/nav_settings"
        android:icon="@android:drawable/ic_menu_preferences"
        android:title="Settings"/>
    <item
        android:id="@+id/nav_about"
        android:icon="@android:drawable/ic_menu_info_details"
        android:title="About"/>
</menu>

Do not touch any other file.
Confirm the change and say "Task 1 complete".

---

## TASK 2 — Fix SettingsFragment to use Spinners instead of TextViews

The current SettingsFragment.java still uses tempUnitToggle and flowUnitToggle TextViews.
The current fragment_settings.xml also still has these as TextViews.
We need to replace both with Spinners.

### 2A — Update fragment_settings.xml

File: app/src/main/res/layout/fragment_settings.xml

Find the Temperature row. It is a LinearLayout containing a TextView with
android:text="Temperature" and a TextView with android:id="@+id/tempUnitToggle".
Replace that entire LinearLayout with:

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:background="@drawable/bg_card_white"
            android:padding="16dp"
            android:layout_marginBottom="2dp">

            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Temperature"
                android:textSize="14sp"
                android:textColor="#333333"/>

            <Spinner
                android:id="@+id/tempUnitSpinner"
                android:layout_width="120dp"
                android:layout_height="wrap_content"
                android:backgroundTint="#02799A"/>
        </LinearLayout>

Find the Flow Rate row. It is a LinearLayout containing a TextView with
android:text="Flow Rate" and a TextView with android:id="@+id/flowUnitToggle".
Replace that entire LinearLayout with:

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:background="@drawable/bg_card_white"
            android:padding="16dp"
            android:layout_marginBottom="24dp">

            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Flow Rate"
                android:textSize="14sp"
                android:textColor="#333333"/>

            <Spinner
                android:id="@+id/flowUnitSpinner"
                android:layout_width="120dp"
                android:layout_height="wrap_content"
                android:backgroundTint="#02799A"/>
        </LinearLayout>

Also find the subtitle under the UNITS label. It currently says either
"Tap to toggle between units" or "Select your preferred units".
Make sure it says:
android:text="Select your preferred units"

Do not touch anything else in this file.

### 2B — Replace SettingsFragment.java

File: app/src/main/java/de/kai_morich/simple_bluetooth_le_terminal/SettingsFragment.java

Replace the entire file content with:

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
}

Confirm both files were updated and say "Task 2 complete".

---

## TASK 3 — Make flow unit setting actually change the dashboard display

File: app/src/main/java/de/kai_morich/simple_bluetooth_le_terminal/TerminalFragment.java

Do NOT touch: receive(), connect(), disconnect(), send(), onSerialConnect(),
onSerialConnectError(), onSerialRead(), onSerialIoError(), setO2Alert(),
onCreateOptionsMenu(), onOptionsItemSelected(), or any Bluetooth logic.

Make these 4 surgical additions only:

ADDITION 1 — Add this import with the existing imports:
import android.preference.PreferenceManager;

ADDITION 2 — Add this field with the existing private fields near the top of the class:
private android.content.SharedPreferences prefs;

ADDITION 3 — In onCreateView(), find this line:
speedometerView = view.findViewById(R.id.speedometerView);
Add this line directly after it:
prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());

ADDITION 4 — In parseAndUpdateUI(), find this exact line:
tvFlow.setText(parts[1].replace("L/min", "").trim());
Replace it with these 6 lines:
float flowVal = Float.parseFloat(parts[1].replace("L/min", "").trim());
String flowUnit = prefs.getString(SettingsFragment.KEY_FLOW_UNIT, "L");
if (flowUnit.equals("mL")) {
tvFlow.setText(String.valueOf((int)(flowVal * 1000)));
} else {
tvFlow.setText(String.valueOf(flowVal));
}

Also find this exact line:
tvTemp.setText(parts[2].replace("C", "").trim());
Replace it with these 7 lines:
float tempVal = Float.parseFloat(parts[2].replace("C", "").trim());
String tempUnit = prefs.getString(SettingsFragment.KEY_TEMP_UNIT, "C");
if (tempUnit.equals("F")) {
float tempF = (tempVal * 9f / 5f) + 32f;
tvTemp.setText(String.valueOf((int)tempF));
} else {
tvTemp.setText(String.valueOf((int)tempVal));
}

Do not touch anything else in this file.
Confirm all 4 additions and say "Task 3 complete".

---

## TASK 4 — Final verification (read only, no changes)

Read these files and confirm the following. Do not change anything.

menu_bottom_nav.xml:
- nav_home uses @android:drawable/ic_menu_home
- nav_settings uses @android:drawable/ic_menu_preferences

fragment_settings.xml:
- tempUnitSpinner Spinner exists
- flowUnitSpinner Spinner exists
- No tempUnitToggle TextView exists
- No flowUnitToggle TextView exists

SettingsFragment.java:
- No reference to tempUnitToggle or flowUnitToggle
- Spinner and ArrayAdapter imports exist
- KEY_TEMP_UNIT and KEY_FLOW_UNIT are defined

TerminalFragment.java:
- PreferenceManager import exists
- prefs field exists
- parseAndUpdateUI reads KEY_FLOW_UNIT and converts mL if needed
- parseAndUpdateUI reads KEY_TEMP_UNIT and converts F if needed

If everything is correct say "All checks passed. Ready to build."
If anything is wrong list the problems clearly.