package com.hero.elias.conanapp;

import android.util.Log;

public class Tween {
    public static double linear(double x, double x0, double x1, double y0, double y1) {
        return y0 + (x - x0) * (y1 - y0) / (x1 - x0);
    }
    
    public static double sine(double minAmp, double maxAmp, double frequency, double phase, double increment){
        return (Math.sin((increment + phase) * frequency) * (maxAmp - minAmp)) + minAmp;
    }
    
    public static double smoothToTarget(double current, double target, double scalar) {
        current += (target - current) / scalar;
        return current;
    }
    
    public static double linearToTarget(double current, double target, double scalar) {
        if (current != target) {
            if (current > target) {
                current -= scalar;
                if (current < target) {
                    current = target;
                }
            } else {
                current += scalar;
                if (current > target) {
                    current = target;
                }
            }
        }
        return current;
    }
}
