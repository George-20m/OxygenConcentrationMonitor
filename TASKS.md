# OxyLife – Task List for Gemini CLI
# Read this entire file first, then execute each task in order.
# After each task, confirm what you did before moving to the next one.
# Do NOT skip ahead. Do NOT touch any file not mentioned in the task.
# These files are COMPLETELY OFF LIMITS — never touch them:
#   SerialService.java, SerialSocket.java, SerialListener.java,
#   BluetoothUtil.java, TextUtil.java, Constants.java, DevicesFragment.java

---

## TASK 0 — Validate previous work (do this first, make no changes)

Check that this file exists:
  app/src/main/java/de/kai_morich/simple_bluetooth_le_terminal/SpeedometerView.java

Read it and confirm it contains a class called SpeedometerView that extends View
and has a method called setValue(float val).

If the file does NOT exist, tell me and stop. Do not continue to Task 1.
If the file exists and is correct, say "SpeedometerView confirmed" and continue.

---

## TASK 1 — App name and colors

### 1A — strings.xml
File: app/src/main/res/values/strings.xml

Find this line:
  <string name="app_name">O2 Monitor</string>

Change it to:
  <string name="app_name">OxyLife</string>

Do not touch any other line in this file.

### 1B — colors.xml
File: app/src/main/res/values/colors.xml

Find and replace these 3 lines:
  <color name="colorPrimary">#378ADD</color>
  <color name="colorPrimaryDark">#185FA5</color>
  <color name="colorAccent">#378ADD</color>

Change them to:
  <color name="colorPrimary">#02799A</color>
  <color name="colorPrimaryDark">#025f7a</color>
  <color name="colorAccent">#02799A</color>

Do not touch any other line in this file.

### 1C — fragment_terminal.xml card color
File: app/src/main/res/layout/fragment_terminal.xml

Find this attribute:
  app:cardBackgroundColor="#1565A8"

Change it to:
  app:cardBackgroundColor="#02799A"

Do not touch any other line in this file.

### 1D — TerminalFragment.java alert color
File: app/src/main/java/de/kai_morich/simple_bluetooth_le_terminal/TerminalFragment.java

Inside the method setO2Alert(), find the 2 occurrences of 0xFF1565A8
and change both to 0xFF02799A.

There are exactly 2 occurrences. Do not touch anything else in this file.

When done with Task 1, show me the 4 changed values and say "Task 1 complete".

---

## TASK 2 — Speedometer gauge in the O2 card

### 2A — Update fragment_terminal.xml
File: app/src/main/res/layout/fragment_terminal.xml

Inside the CardView with android:id="@+id/o2Card",
replace the entire inner LinearLayout (and all its children) with exactly this:

<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="20dp"
    android:gravity="center_horizontal">

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="OXYGEN PURITY"
        android:textSize="11sp"
        android:textColor="#AACCEE"
        android:layout_marginBottom="8dp"/>

    <de.kai_morich.simple_bluetooth_le_terminal.SpeedometerView
        android:id="@+id/speedometerView"
        android:layout_width="220dp"
        android:layout_height="130dp"/>

    <TextView
        android:id="@+id/tvO2"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="--"
        android:textSize="56sp"
        android:textStyle="bold"
        android:textColor="#FFFFFF"
        android:layout_marginTop="4dp"/>

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="% O2 concentration"
        android:textSize="11sp"
        android:textColor="#AACCEE"
        android:layout_marginBottom="4dp"/>
</LinearLayout>

Important: the hidden views at the bottom of this file must remain untouched:
  receive_text, send_text, send_btn — do not remove or move them.

### 2B — Update TerminalFragment.java
File: app/src/main/java/de/kai_morich/simple_bluetooth_le_terminal/TerminalFragment.java

Addition 1 — add this import with the existing imports at the top:
  import de.kai_morich.simple_bluetooth_le_terminal.SpeedometerView;

Addition 2 — add this field with the existing private fields near the top of the class:
  private SpeedometerView speedometerView;

Addition 3 — in onCreateView(), find this line:
  o2Card = view.findViewById(R.id.o2Card);
Add this line directly after it:
  speedometerView = view.findViewById(R.id.speedometerView);

Addition 4 — in parseAndUpdateUI(), find this exact line:
  getActivity().runOnUiThread(() -> setO2Alert(o2Val < 80));
Add these 2 lines directly after it:
  float finalO2 = o2Val;
  getActivity().runOnUiThread(() -> { if (speedometerView != null) speedometerView.setValue(finalO2); });

Do not touch parseAndUpdateUI() logic, receive(), connect(), disconnect(),
or any Bluetooth-related code.

When done with Task 2, show me the 4 additions and say "Task 2 complete".

---

## TASK 3 — About screen

### 3A — Create AboutFragment.java
Create this new file:
  app/src/main/java/de/kai_morich/simple_bluetooth_le_terminal/AboutFragment.java

With this exact content:

package de.kai_morich.simple_bluetooth_le_terminal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }
}

### 3B — Create fragment_about.xml
Create this new file:
  app/src/main/res/layout/fragment_about.xml

With this exact content:

<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#F5F5F5">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="OxyLife"
            android:textSize="32sp"
            android:textStyle="bold"
            android:textColor="#02799A"
            android:gravity="center"
            android:layout_marginBottom="8dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Portable Oxygen Concentrator Monitor"
            android:textSize="14sp"
            android:textColor="#888888"
            android:gravity="center"
            android:layout_marginBottom="32dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="About This App"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="#000000"
            android:layout_marginBottom="8dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="OxyLife connects to your portable oxygen concentrator via Bluetooth and displays real-time data including oxygen purity, flow rate, temperature, and battery level. Designed to give patients and caregivers immediate visibility into device performance."
            android:textSize="14sp"
            android:textColor="#444444"
            android:lineSpacingMultiplier="1.5"
            android:layout_marginBottom="24dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Inspiration"
            android:textSize="16sp"
            android:textStyle="bold"
            android:textColor="#000000"
            android:layout_marginBottom="8dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Built on the open-source SimpleBluetoothLeTerminal by Kai Morich, adapted for real-time respiratory monitoring."
            android:textSize="14sp"
            android:textColor="#444444"
            android:lineSpacingMultiplier="1.5"
            android:layout_marginBottom="32dp"/>

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Version 1.0"
            android:textSize="12sp"
            android:textColor="#AAAAAA"
            android:gravity="center"/>

    </LinearLayout>
</ScrollView>

When done with Task 3, confirm both files were created and say "Task 3 complete".

---

## TASK 4 — Bottom navigation bar

### 4A — Create menu_bottom_nav.xml
Create this new file:
  app/src/main/res/menu/menu_bottom_nav.xml

With this exact content:

<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:id="@+id/nav_home"
        android:icon="@android:drawable/ic_menu_compass"
        android:title="Home"/>
    <item
        android:id="@+id/nav_settings"
        android:icon="@android:drawable/ic_menu_manage"
        android:title="Settings"/>
    <item
        android:id="@+id/nav_about"
        android:icon="@android:drawable/ic_menu_info_details"
        android:title="About"/>
</menu>

### 4B — Replace activity_main.xml
File: app/src/main/res/layout/activity_main.xml

Replace the entire file content with exactly this:

<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <androidx.appcompat.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary"
        android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar"/>

    <FrameLayout
        android:id="@+id/fragment"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"/>

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottom_nav"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#FFFFFF"
        app:itemIconTint="#02799A"
        app:itemTextColor="#02799A"
        app:menu="@menu/menu_bottom_nav"/>

</LinearLayout>

### 4C — Replace MainActivity.java
File: app/src/main/java/de/kai_morich/simple_bluetooth_le_terminal/MainActivity.java

Replace the entire file content with exactly this:

package de.kai_morich.simple_bluetooth_le_terminal;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, new DevicesFragment(), "devices").commit();
        else
            onBackStackChanged();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                getSupportFragmentManager().popBackStack("devices", 0);
                return true;
            } else if (id == R.id.nav_about) {
                Fragment current = getSupportFragmentManager().findFragmentByTag("about");
                if (current == null || !current.isVisible()) {
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new AboutFragment(), "about")
                        .addToBackStack(null)
                        .commit();
                }
                return true;
            } else if (id == R.id.nav_settings) {
                android.widget.Toast.makeText(this, "Settings coming soon", android.widget.Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

When done with Task 4, confirm all 3 files were updated and say "Task 4 complete".

---

## TASK 5 — Final verification (read only, no changes)

Read these files and check for the following. Report any problem found.
Do not change anything in this task.

Check MainActivity.java:
- Package is de.kai_morich.simple_bluetooth_le_terminal
- Imports AboutFragment and BottomNavigationView
- References R.id.fragment, R.id.toolbar, R.id.bottom_nav

Check TerminalFragment.java:
- Imports SpeedometerView
- Has field: private SpeedometerView speedometerView
- onCreateView binds speedometerView
- parseAndUpdateUI calls speedometerView.setValue()
- setO2Alert() uses color 0xFF02799A not 0xFF1565A8

Check fragment_terminal.xml:
- o2Card CardView has app:cardBackgroundColor="#02799A"
- SpeedometerView tag exists with id speedometerView
- tvO2 TextView still exists inside the o2Card
- Hidden views receive_text, send_text, send_btn are still present at the bottom

Check activity_main.xml:
- Contains Toolbar with id toolbar
- Contains FrameLayout with id fragment
- Contains BottomNavigationView with id bottom_nav

Check strings.xml:
- app_name is OxyLife

Check colors.xml:
- colorPrimary is #02799A

If everything is correct say "All checks passed. OxyLife is ready to build."
If anything is wrong, list the problems clearly.
