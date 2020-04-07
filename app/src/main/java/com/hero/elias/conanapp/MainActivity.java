package com.hero.elias.conanapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.Open, R.string.Close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        Toast h = Toast.makeText(getApplicationContext(), menuItem.toString(), Toast.LENGTH_SHORT);
        h.show();
        Intent intent;

        switch (menuItem.getItemId()){
            case R.id.nav_home:
                onBackPressed();
                break;
            case R.id.nav_command:
                onBackPressed();
                intent = new Intent(this, CommandActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_visualization:
                onBackPressed();
                intent = new Intent(this, VisualizationActivity.class);
                startActivity(intent);
                break;
        }

        switch(menuItem.getItemId()){

        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else{
            super.onBackPressed();
        }
    }


}
