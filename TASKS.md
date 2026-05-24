# OxyLife – TASKS.md (Round 6 - Architecture Fix)
# Read this entire file first before doing anything.
# Execute each task in order. Confirm after each one before continuing.
# Do NOT touch: SerialService.java, SerialSocket.java, SerialListener.java,
#   BluetoothUtil.java, TextUtil.java, Constants.java, SpeedometerView.java,
#   AlertsFragment.java, AboutFragment.java, fragment_terminal.xml

---

## TASK 1 — Replace MainActivity.java entirely

Replace the entire content of:
app/src/main/java/de/kai_morich/simple_bluetooth_le_terminal/MainActivity.java

With exactly this:

package de.kai_morich.simple_bluetooth_le_terminal;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TerminalFragment terminalFragment;
    private AlertsFragment alertsFragment;
    private SettingsFragment settingsFragment;
    private AboutFragment aboutFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        terminalFragment = new TerminalFragment();
        alertsFragment = new AlertsFragment();
        settingsFragment = new SettingsFragment();
        aboutFragment = new AboutFragment();

        getSupportFragmentManager().beginTransaction()
            .add(R.id.fragment, terminalFragment, "terminal")
            .add(R.id.fragment, alertsFragment, "alerts")
            .add(R.id.fragment, settingsFragment, "settings")
            .add(R.id.fragment, aboutFragment, "about")
            .hide(alertsFragment)
            .hide(settingsFragment)
            .hide(aboutFragment)
            .commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showTab(terminalFragment);
                return true;
            } else if (id == R.id.nav_alerts) {
                showTab(alertsFragment);
                return true;
            } else if (id == R.id.nav_settings) {
                showTab(settingsFragment);
                return true;
            } else if (id == R.id.nav_about) {
                showTab(aboutFragment);
                return true;
            }
            return false;
        });
    }

    private void showTab(Fragment target) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        for (Fragment f : getSupportFragmentManager().getFragments()) {
            if (f == target) ft.show(f);
            else ft.hide(f);
        }
        ft.commit();
    }

    public void connectDevice(String address) {
        terminalFragment.connectTo(address);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    public void disconnectDevice() {
        terminalFragment.disconnectFromSettings();
    }

    public boolean isConnected() {
        return terminalFragment != null && terminalFragment.isConnected();
    }
}

Confirm this file was replaced and say "Task 1 complete".

---

## TASK 2 — Add public methods to TerminalFragment

File: app/src/main/java/de/kai_morich/simple_bluetooth_le_terminal/TerminalFragment.java

CHANGE 1 — Fix the null crash in onCreate.
Find:
deviceAddress = getArguments().getString("device");
Replace with:
if (getArguments() != null)
deviceAddress = getArguments().getString("device");

CHANGE 2 — Fix connect() to handle null deviceAddress gracefully.
Find the start of the connect() method:
private void connect() {
try {
BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
Replace with:
private void connect() {
if (deviceAddress == null) {
if (tvStatus != null) tvStatus.setText("Go to Settings to connect a device");
return;
}
try {
BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

CHANGE 3 — Add these 3 public methods to the class.
Add them just before the final closing brace of the class:

    public void connectTo(String address) {
        this.deviceAddress = address;
        if (connected != Connected.False)
            disconnect();
        connect();
    }

    public void disconnectFromSettings() {
        if (connected != Connected.False)
            disconnect();
        deviceAddress = null;
        if (tvStatus != null) tvStatus.setText("Disconnected");
        if (tvLive != null) tvLive.setVisibility(View.GONE);
    }

    public boolean isConnected() {
        return connected == Connected.True;
    }

Do not touch any other part of TerminalFragment.java.
Confirm all 3 changes and say "Task 2 complete".

---

## TASK 3 — Fix DevicesFragment to use the existing TerminalFragment

Currently DevicesFragment.onListItemClick() creates a brand new TerminalFragment
and replaces everything. We need it to call MainActivity.connectDevice() instead.

File: app/src/main/java/de/kai_morich/simple_bluetooth_le_terminal/DevicesFragment.java

Find the entire onListItemClick method:
@Override
public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
stopScan();
BluetoothUtil.Device device = listItems.get(position-1);
Bundle args = new Bundle();
args.putString("device", device.getDevice().getAddress());
Fragment fragment = new TerminalFragment();
fragment.setArguments(args);
getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "terminal").addToBackStack(null).commit();
}

Replace it with:
@Override
public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
stopScan();
BluetoothUtil.Device device = listItems.get(position-1);
String address = device.getDevice().getAddress();
if (getActivity() instanceof MainActivity) {
((MainActivity) getActivity()).connectDevice(address);
}
}

Do not touch anything else in DevicesFragment.java.
Confirm the change and say "Task 3 complete".

---

## TASK 4 — Add Connect/Disconnect to SettingsFragment

### 4A — Update fragment_settings.xml

File: app/src/main/res/layout/fragment_settings.xml

Add this block as the VERY FIRST child inside the root LinearLayout,
before everything else:

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="CONNECTION"
            android:textSize="11sp"
            android:textStyle="bold"
            android:textColor="#02799A"
            android:letterSpacing="0.15"
            android:layout_marginBottom="4dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Manage your Bluetooth connection"
            android:textSize="12sp"
            android:textColor="#888888"
            android:layout_marginBottom="12dp"/>

        <LinearLayout
            android:id="@+id/connectRow"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:background="@drawable/bg_card_white"
            android:padding="16dp"
            android:clickable="true"
            android:focusable="true"
            android:layout_marginBottom="8dp">

            <TextView
                android:id="@+id/tvConnectAction"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Connect to Device"
                android:textSize="14sp"
                android:textColor="#02799A"
                android:textStyle="bold"/>

            <TextView
                android:id="@+id/tvConnectStatus"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="● Not connected"
                android:textSize="12sp"
                android:textColor="#AAAAAA"/>
        </LinearLayout>

        <LinearLayout
            android:id="@+id/disconnectRow"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:background="@drawable/bg_card_white"
            android:padding="16dp"
            android:clickable="true"
            android:focusable="true"
            android:layout_marginBottom="24dp"
            android:visibility="gone">

            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="Disconnect"
                android:textSize="14sp"
                android:textColor="#E53935"
                android:textStyle="bold"/>
        </LinearLayout>

Do not touch anything else in this file.

### 4B — Replace SettingsFragment.java entirely

Replace the entire content of:
app/src/main/java/de/kai_morich/simple_bluetooth_le_terminal/SettingsFragment.java

With exactly this:

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
            requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new DevicesFragment(), "devices")
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

Confirm both files were updated and say "Task 4 complete".

---

## TASK 5 — Final verification (read only, no changes)

Read these files and confirm the following. Do not change anything.

MainActivity.java:
- Uses show/hide pattern with showTab() method
- Has connectDevice(String address) method
- Has disconnectDevice() method
- Has isConnected() method
- No FragmentManager.OnBackStackChangedListener

TerminalFragment.java:
- getArguments() has null check
- connect() has null check for deviceAddress
- connectTo(String address) public method exists
- disconnectFromSettings() public method exists
- isConnected() public method exists

DevicesFragment.java:
- onListItemClick calls MainActivity.connectDevice() instead of creating new TerminalFragment

fragment_settings.xml:
- connectRow exists
- disconnectRow exists with visibility gone
- tvConnectStatus exists

SettingsFragment.java:
- updateConnectionUI() method exists
- onResume() refreshes connection status
- connectRow opens DevicesFragment
- disconnectRow calls MainActivity.disconnectDevice()

If everything is correct say "All checks passed. Ready to build."
If anything is wrong list the problems clearly.