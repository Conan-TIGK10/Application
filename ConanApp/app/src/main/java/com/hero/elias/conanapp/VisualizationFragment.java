package com.hero.elias.conanapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class VisualizationFragment extends Fragment {
    
    public VisualizationFragment() {
    }
    
    public static VisualizationFragment newInstance() {
        VisualizationFragment fragment = new VisualizationFragment();
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visualization, container, false);
    }
}
