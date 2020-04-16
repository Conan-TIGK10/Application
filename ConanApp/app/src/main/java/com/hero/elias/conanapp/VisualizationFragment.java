package com.hero.elias.conanapp;

import android.graphics.Matrix;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentViewHolder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class VisualizationFragment extends Fragment {
    
    ImageView imageView;
    
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
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.imageView = (ImageView) view.findViewById(R.id.visualization_imageview);
        
        super.onViewCreated(view, savedInstanceState);
    }
}
