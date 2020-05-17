package com.hero.elias.conanapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    
    private BottomNavigationView bottomNavigation;
    private String currentScreen;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    
    private void alertDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        builder.setTitle("Location Permission")
                .setMessage("Location Permission is Required To Connect to the Mbot")
                .setPositiveButton("OK", null);
        
        final AlertDialog dialog = builder.create();
        
        dialog.show();
    }
    
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        
        BluetoothHandler.getInstance().setMainActivity(this);
        WifiHandler.getInstance().setMainActivity(this);
        
        BluetoothHandler.getInstance().checkConnection();
        WifiHandler.getInstance().checkConnection();
        
        MbotHandler.getInstance();
        
        this.bottomNavigation = this.findViewById(R.id.bottom_navigation);
        this.bottomNavigation.setItemIconTintList(null);
        this.bottomNavigation.setOnNavigationItemSelectedListener(this);
        
        this.openFragment(HomeFragment.newInstance(), "Home");
        this.currentScreen = "Home";
        this.bottomNavigation.setSelectedItemId(R.id.navigation_home);
        
        this.checkLocationPermission();
    }
    
    @Override
    protected void onStart() {
        BluetoothHandler.getInstance().start();
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        BluetoothHandler.getInstance().stop();
        super.onStop();
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        item.setChecked(true);
        item.setEnabled(true);
        
        switch (item.getItemId()) {
            case R.id.navigation_steer:
                this.openFragment(CommandFragment.newInstance(), "Command");
                return true;
            case R.id.navigation_home:
                this.openFragment(HomeFragment.newInstance(), "Home");
                return true;
            case R.id.navigation_map:
                this.openFragment(VisualizationFragment.newInstance(), "Visualization");
                return true;
        }
        return false;
    }
    
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    } else {
                        this.alertDialog();
                    }
                } else {
                    this.alertDialog();
                }
                return;
            }
        }
    }
    
    private void openFragment(final Fragment fragment, final String toFragment) {
        if (this.currentScreen != toFragment) {
            final FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
            
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
            transaction.addToBackStack(toFragment);
            transaction.commit();
        }
    }
}

