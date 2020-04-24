package com.hero.elias.conanapp;

public class Tweening {
    // fraction is between 0 and 1
    public static double linear(double fromMin, double fromMax, double fraction){
        return (fromMin * (1.0 - fraction) + fromMax * fraction);
    }
    
    // fraction is between 0 and 1
    public static double cosine(double fromMin, double fromMax, double fraction){
        fraction = (1.0 - Math.cos(fraction * Math.PI)) / 2.0;
        return (fromMin * (1.0 - fraction) + fromMax * fraction);
    }
    
    // scales a value from one scale to another scale linearly
    public static double linear(double fromValue, double fromMin, double fromMax, double toMin, double toMax) {
        return linear(toMin, toMax, (fromValue - fromMin) / (fromMax - fromMin));
    }
    
    // scales a value from one scale to another scale using cosine function (smoother)
    public static double cosine(double fromValue, double fromMin, double fromMax, double toMin, double toMax) {
        return cosine(toMin, toMax, (fromValue - fromMin) / (fromMax - fromMin));
    }
    
    public static double sine(double minAmp, double maxAmp, double frequency, double phase, double increment){
        return (Math.sin((increment + phase) * frequency) * (maxAmp - minAmp)) + minAmp;
    }
    
    // Scalar can scale infinitly, the higher the slower to reach
    public static double smoothToTarget(double current, double target, double scalar) {
        current += (target - current) / scalar;
        return current;
    }
    
    // Scalar can scale infinitly, the higher the faster to reach
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
