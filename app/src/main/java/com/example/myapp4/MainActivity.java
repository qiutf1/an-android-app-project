package com.example.myapp4;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.myapp4.fragments.LedgerFragment;
import com.example.myapp4.fragments.RecordFragment;
import com.example.myapp4.fragments.StatsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (savedInstanceState == null) {
            loadFragment(new RecordFragment());
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment f = null;
            if (id == R.id.nav_record) f = new RecordFragment();
            else if (id == R.id.nav_ledger) f = new LedgerFragment();
            else if (id == R.id.nav_stats) f = new StatsFragment();

            if (f != null) {
                loadFragment(f);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.fragmentContainer, fragment);
        t.commit();
    }
}
