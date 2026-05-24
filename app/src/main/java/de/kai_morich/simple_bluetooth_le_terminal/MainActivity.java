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
