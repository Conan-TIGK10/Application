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

import si.inova.neatle.operation.CharacteristicSubscription;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    
    private BottomNavigationView bottomNavigation;
    private String currentScreen;
    private CharacteristicSubscription subscription;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        
/*        WifiHandler.getInstance().createSession("Test Session", () -> {
            WifiHandler.getInstance().postPosition(0.1, 0.2, (posId) -> {
                WifiHandler.getInstance().getLastPosition((id, x, y, sessionId) -> {
                    Log.i("WIFI", "SESSION POST GET WORKS");
                });
            });
    
            WifiHandler.getInstance().postCollision(2, 4, () -> {
                Log.i("WIFI", "COLLISION WORKS");
            });
        });
    */
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

