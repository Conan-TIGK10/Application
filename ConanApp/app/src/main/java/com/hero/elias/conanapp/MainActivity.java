package com.hero.elias.conanapp;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    
    private BottomNavigationView bottomNavigation;
    private String currentScreen;
    
    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_command:
                            MainActivity.this.openFragment(CommandFragment.newInstance(), "Command");
                            return true;
                        case R.id.navigation_home:
                            MainActivity.this.openFragment(HomeFragment.newInstance(), "Home");
                            return true;
                        case R.id.navigation_visualization:
                            MainActivity.this.openFragment(VisualizationFragment.newInstance(), "Visualization");
                            return true;
                    }
                    return false;
                }
            };
    
    public void openFragment(Fragment fragment, String toFragment) {
        if (this.currentScreen != toFragment) {
            FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
            
            if (toFragment == "Command") {
                transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
            } else if (toFragment == "Visualization") {
                transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
            } else if (toFragment == "Home") {
                if (this.currentScreen == "Command") {
                    transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
                } else if (this.currentScreen == "Visualization") {
                    transaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
                }
            }
            
            this.currentScreen = toFragment;
            
            transaction.replace(R.id.bottom_nav_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        
        this.bottomNavigation = this.findViewById(R.id.bottom_navigation);
        this.bottomNavigation.setItemIconTintList(null);
        this.bottomNavigation.setOnNavigationItemSelectedListener(this.navigationItemSelectedListener);
        
        this.openFragment(HomeFragment.newInstance(), "Home");
        this.currentScreen = "Home";
        
        BluetoothHandler.getInstance().setMainActivity(this);
    }
    
    @Override
    protected void onPause() {
        BluetoothHandler.getInstance().stopThread();
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        BluetoothHandler.getInstance().startThread();
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
        BluetoothHandler.getInstance().unregisterReceivers();
        super.onDestroy();
    }
}

