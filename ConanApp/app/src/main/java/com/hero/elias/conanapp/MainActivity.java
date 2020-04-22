package com.hero.elias.conanapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    
    private BottomNavigationView bottomNavigation;
    private String currentScreen;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        
        BluetoothHandler.getInstance().setMainActivity(this);
        WifiHandler.setMainActivity(this);
        
        this.bottomNavigation = this.findViewById(R.id.bottom_navigation);
        // Disables icon tinting, allowing for textured icons
        this.bottomNavigation.setItemIconTintList(null);
        
        this.bottomNavigation.setOnNavigationItemSelectedListener(this);
        
        this.openFragment(HomeFragment.newInstance(), "Home");
        this.currentScreen = "Home";
        this.bottomNavigation.setSelectedItemId(R.id.navigation_home);
    
        this.checkLocationPermission();
    }
    
    @Override
    protected void onPause() {
        BluetoothHandler.getInstance().dissconnect();
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        BluetoothHandler.getInstance().connect();
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
        BluetoothHandler.getInstance().dissconnect();
        super.onDestroy();
    }
    
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
    
    private void openFragment(Fragment fragment, String toFragment) {
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
    
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        } else {
            return true;
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
    
                        builder.setMessage("Location Permission")
                                .setTitle("Location Permission is Required For Application to Function, Please Accept.");
                        AlertDialog dialog = builder.create();
    
                        dialog.show();
                        this.checkLocationPermission();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    
                    builder.setMessage("Location Permission")
                            .setTitle("Location Permission is Required For Application to Function, Please Accept.");
                    AlertDialog dialog = builder.create();
    
                    dialog.show();
                    this.checkLocationPermission();
                }
                return;
            }
        }
    }
}

