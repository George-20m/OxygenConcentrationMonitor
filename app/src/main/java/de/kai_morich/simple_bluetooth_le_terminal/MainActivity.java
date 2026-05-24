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
                getSupportFragmentManager().popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                return true;
            } else if (id == R.id.nav_alerts) {
                Fragment current = getSupportFragmentManager().findFragmentByTag("alerts");
                if (current == null || !current.isVisible()) {
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new AlertsFragment(), "alerts")
                        .addToBackStack(null)
                        .commit();
                }
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
                Fragment current = getSupportFragmentManager().findFragmentByTag("settings");
                if (current == null || !current.isVisible()) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment, new SettingsFragment(), "settings")
                            .addToBackStack(null)
                            .commit();
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}