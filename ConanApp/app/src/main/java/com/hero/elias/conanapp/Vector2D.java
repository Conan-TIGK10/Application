package com.hero.elias.conanapp;

import androidx.annotation.NonNull;

public class Vector2D {
    public double x;
    public double y;
    
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Vector2D(Vector2D v) {
        this.x = v.x;
        this.y = v.y;
    }
    
    protected Vector2D clone() {
        return new Vector2D(this);
    }
    
    public static double vectorToDegree(Vector2D v) {
        return (Math.atan2(v.x, v.y) * 180.0 / Math.PI);
    }
    
    public static Vector2D degreeToVector(double degree) {
        double radian = (degree * (Math.PI / 180.0));
        return new Vector2D(Math.cos(radian), Math.sin(radian));
    }
    
    public static Vector2D normalDirection(Vector2D point) {
        Vector2D v = normalizeVector(point);
        return new Vector2D(-v.y, v.x);
    }
    
    public static Vector2D normalizeVector(Vector2D point) {
        double length = lengthOfVector(point);
        return new Vector2D(point.x / length, point.y / length);
    }
    
    public static double lengthBetweenVectors(Vector2D a, Vector2D b) {
        return Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
    }
    
    public static double lengthOfVector(Vector2D point) {
        return Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2));
    }
    
    public static float dot(Vector2D a, Vector2D b){
        return (float) ((a.x * b.x ) + (a.y * b.y));
    }
    
    public void add(Vector2D v) {
        this.x += v.x;
        this.y += v.y;
    }
    
    
    public void subtract(Vector2D v) {
        this.x -= v.x;
        this.y -= v.y;
    }
    
    public void multiply(Vector2D v) {
        this.x *= v.x;
        this.y *= v.y;
    }
    
    public void divide(Vector2D v) {
        this.x /= v.x;
        this.y /= v.y;
    }
}
