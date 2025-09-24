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

        String username = getIntent().getStringExtra("username");

        bottomNavigation = findViewById(R.id.bottomNavigation);

        // 默认加载“记账”页面，并传递用户名
        loadFragment(new RecordFragment(), username);

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_record) {
                fragment = new RecordFragment();
            } else if (id == R.id.nav_ledger) {
                fragment = new LedgerFragment();
            } else if (id == R.id.nav_stats) {
                fragment = new StatsFragment();
            }

            if (fragment != null) {
                loadFragment(fragment, username);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment, String username) {
        Bundle bundle = new Bundle();
        bundle.putString("username", username);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.fragmentContainer, fragment);
        t.commit();
    }
}
