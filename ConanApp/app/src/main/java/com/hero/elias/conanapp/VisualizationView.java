package com.hero.elias.conanapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Choreographer;
import android.view.View;

import androidx.core.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class VisualizationView extends View implements Choreographer.FrameCallback, MbotHandler.MbotCallback {
    
    private int windowWidth;
    private int windowHeight;
    private Vector2D windowCenter;
    
    private int frameCounter;
    private double secondsCounter;
    
    private double deltaTimeSeconds;
    private int deltaTimeMillis;
    private long lastSystemTime;
    
    private int millisCurrent;
    private int millisStart;
    private int millisDestination;
    
    private Vector2D robotPositionCurrent;
    private Vector2D robotPositionStart;
    private Vector2D robotPositionDestination;
    
    private Vector2D robotHeadingCurrent;
    private Vector2D robotHeadingStart;
    private Vector2D robotHeadingDestination;

    private Bitmap robotBitmap;
    private Matrix robotMatrix;
    
    private Paint pathPaint;
    private List<Vector2D> pathList;
    private Path path;
    private CornerPathEffect pathCornerEffect;
    private double pathCornerEffectPhaseCounter;
    
    private Paint lidarPaint;
    private float lidarStrength;
    private float lidarDestinationStrength;
    private int lidarMillisTimestamp;
    
    private Bitmap collisionBitmap;
    private Matrix collisionMatrix;
    private List<Vector2D> collisionList;
    private Paint collisionPaint;
    
    private Paint gridPaint;
    private double gridSpacing;
    private double gridMultiplier;
    private double gridDivider;
    
    public VisualizationView(Context context) {
        super(context);
        this.init(context, null, 0);
    }
    
    public VisualizationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }
    
    public VisualizationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs, defStyle);
    }
    
    private void init(Context context, AttributeSet attrs, int defStyle) {
        this.windowCenter = new Vector2D(0.0, 0.0);
        this.updateWindow();
        
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inMutable = true;
        
        this.robotBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icons8_gps_64px, bitmapOptions);
        this.robotMatrix = new Matrix();
        
        this.lidarPaint = new Paint();
        this.lidarPaint.setMaskFilter(new BlurMaskFilter(64f, BlurMaskFilter.Blur.NORMAL));
        this.lidarPaint.setStrokeWidth(32f);
        this.lidarStrength = 0f;
        this.lidarDestinationStrength = 0f;
        this.lidarMillisTimestamp = -5001;
        
        this.pathPaint = new Paint();
        this.pathPaint.setStrokeWidth(8f);
        this.pathPaint.setARGB(64, 255, 0, 0);
        this.pathPaint.setStyle(Paint.Style.STROKE);
        
        this.frameCounter = 0;
        this.secondsCounter = 0.0;
        this.deltaTimeSeconds = 0.0;
        this.deltaTimeMillis = 0;
    
        this.gridPaint = new Paint();
        this.gridPaint.setARGB(255, 64, 64, 64);
        this.gridSpacing = 8.0;
        this.gridMultiplier = this.gridSpacing * 2.0;
        this.gridDivider = this.gridMultiplier / 2.0;
        
        this.path = new Path();
        this.pathList = new ArrayList<Vector2D>();
        this.pathCornerEffectPhaseCounter = 0.0;
        this.pathCornerEffect = new CornerPathEffect(100);
        
        this.collisionBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icons8_explosion_64px, bitmapOptions);
        this.collisionMatrix = new Matrix();
        this.collisionList = new ArrayList<Vector2D>();
        this.collisionPaint = new Paint();
        this.collisionPaint.setARGB(255, 0, 255, 0);
        
        this.millisCurrent = MbotHandler.getInstance().getMillis();
        this.millisStart = 0;
        this.millisDestination = 0;
        
        this.robotPositionCurrent = MbotHandler.getInstance().getPosition();
        this.robotPositionStart = new Vector2D(0.0, 0.0);
        this.robotPositionDestination = new Vector2D(0.0, 0.0);
    
        this.robotHeadingCurrent = MbotHandler.getInstance().getHeading();
        this.robotHeadingStart = new Vector2D(0.0, 0.0);
        this.robotHeadingDestination = new Vector2D(0.0, 1.0);
    
        this.lastSystemTime = System.currentTimeMillis();
        
        MbotHandler.getInstance().addCallback(this);
        Choreographer.getInstance().postFrameCallback(this);
    }
    
    @Override
    public void onNewData(Vector2D position, Vector2D heading, int millis, float lidar, boolean gap) {
        this.lidarDestinationStrength = lidar;
        
        if (lidar < 0.1f && ((millis - this.lidarMillisTimestamp) > 5000)){
            synchronized (this.collisionList) {
            this.collisionList.add(new Vector2D(this.windowCenter.x + (position.x * this.gridDivider), this.windowCenter.y - (position.y * this.gridDivider)));
            if (this.collisionList.size() == 32) {
                this.collisionList.remove(0);
            }
            }
    
            this.lidarMillisTimestamp = millis;
        }
        
        if (gap){
        
        }
    }
    
    private void updateWindow(){
        this.windowWidth = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
        this.windowHeight = this.getHeight() - this.getPaddingTop() - this.getPaddingBottom();
        this.windowCenter.x = this.windowWidth / 2f;
        this.windowCenter.y = this.windowHeight / 2f;
    }
    
    private void updateTime(){
        this.frameCounter++;
        
        long systemTime = System.currentTimeMillis();
        this.deltaTimeMillis = (int) (systemTime - this.lastSystemTime);
        this.deltaTimeSeconds =  (double)this.deltaTimeMillis / 1000.0;
        
        this.lastSystemTime = systemTime;
        
        this.secondsCounter += this.deltaTimeSeconds;
    }
    
    private void updatePath(Canvas canvas){
        if (this.pathList.isEmpty()){
            this.pathList.add(new Vector2D(this.windowCenter.x + (this.robotPositionCurrent.x * this.gridDivider), this.windowCenter.y - (this.robotPositionCurrent.y * this.gridDivider)));
        }
        
        this.path.reset();
        this.path.moveTo((float)(this.pathList.get(0).x - (this.robotPositionCurrent.x * this.gridDivider)), (float)(this.pathList.get(0).y + (this.robotPositionCurrent.y * this.gridDivider)));
        for (int i = 1; i < this.pathList.size(); i++) {
            this.path.lineTo((float)(this.pathList.get(i).x - (this.robotPositionCurrent.x * this.gridDivider)), (float)(this.pathList.get(i).y + (this.robotPositionCurrent.y * this.gridDivider)));
        }
        this.path.lineTo((float)this.windowCenter.x, (float)this.windowCenter.y);
        
        this.pathCornerEffectPhaseCounter -= 6.0;
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{64, 32}, (float)this.pathCornerEffectPhaseCounter);
        ComposePathEffect composePathEffect = new ComposePathEffect(dashPathEffect, this.pathCornerEffect);
        this.pathPaint.setPathEffect(composePathEffect);
        
        canvas.drawPath(this.path, this.pathPaint);
    }
    
    private void updateRobot(Canvas canvas){
        if (this.millisCurrent < MbotHandler.getInstance().getMillis()){
            this.millisCurrent += this.deltaTimeMillis;
    
            if (this.millisCurrent >= this.millisDestination){
                
                this.millisStart = this.millisDestination;
                this.millisDestination = MbotHandler.getInstance().getMillis();
        
                this.robotPositionStart = this.robotPositionCurrent.clone();
                this.robotPositionDestination = MbotHandler.getInstance().getPosition().clone();
        
                this.robotHeadingStart = this.robotHeadingCurrent.clone();
                this.robotHeadingDestination = MbotHandler.getInstance().getHeading().clone();
        
                this.pathList.add(new Vector2D(this.windowCenter.x + (this.robotPositionCurrent.x * this.gridDivider), this.windowCenter.y - (this.robotPositionCurrent.y * this.gridDivider)));
                if (this.pathList.size() == 64) {
                    this.pathList.remove(0);
                }
            }
    
            this.robotPositionCurrent = Tweening.cosine(this.millisCurrent, this.millisStart, this.millisDestination, this.robotPositionStart, this.robotPositionDestination);
    
            this.robotHeadingCurrent = Tweening.cosine(this.millisCurrent, this.millisStart, this.millisDestination, this.robotHeadingStart, this.robotHeadingDestination);
            this.robotHeadingCurrent = Vector2D.normalizeVector(this.robotHeadingCurrent);
        }
    
        float xScale = (float) Tweening.sine(1.85, 2, 4, 0, this.secondsCounter);
        float yScale = (float) Tweening.sine(1.85, 2, 4, Math.PI / 2.0, this.secondsCounter);
        
        this.robotMatrix.setRotate((float) Vector2D.vectorToDegree(this.robotHeadingCurrent), this.robotBitmap.getWidth() / 2f, this.robotBitmap.getHeight() / 2f);
        this.robotMatrix.postScale(xScale, yScale, this.robotBitmap.getWidth() / 2f, this.robotBitmap.getHeight() / 2f);
        this.robotMatrix.postTranslate((float) this.windowCenter.x - this.robotBitmap.getWidth() / 2f, (float) this.windowCenter.y - this.robotBitmap.getHeight() / 2f);
        
        canvas.drawBitmap(this.robotBitmap, this.robotMatrix, null);
    }
    
    private void updateGrid(Canvas canvas){
        double scaleX = -((this.robotPositionCurrent.x % this.gridMultiplier) / this.gridMultiplier);
        double scaleY = ((this.robotPositionCurrent.y % this.gridMultiplier) / this.gridMultiplier);
        
        for (int i = 0; i < (this.gridSpacing + 1); i++) {
            for (int j = 0; j < (this.gridSpacing * 2); j++) {
                Vector2D circle = new Vector2D((i * (this.windowWidth / this.gridSpacing) + ((this.windowWidth / this.gridSpacing) * scaleX)), (j * (this.windowWidth / this.gridSpacing) + ((this.windowWidth / this.gridSpacing) * scaleY)));
                double length = Vector2D.lengthBetweenVectors(circle, this.windowCenter);
                this.gridPaint.setAlpha((int) Tweening.cosine(MathUtils.clamp(length, 0.0, 750.0), 0.0, 750.0, 127.0, 0.0));
                
                canvas.drawCircle((float)circle.x, (float)circle.y, 16f, this.gridPaint);
            }
        }
    }
    
    private void updateLidar(Canvas canvas){
        int lidarCount = 6;
        
        double fov = 256 + 128;
        double viewDistance = 512;
        double fovDecrease = fov / lidarCount;
        double viewDistanceDecrease = viewDistance / (lidarCount * 1.5);
        
        Vector2D robotHeadingNormal = Vector2D.normalDirection(this.robotHeadingCurrent);
        
        this.lidarStrength = (float) Tweening.smoothToTarget(this.lidarStrength, this.lidarDestinationStrength, 12f);
        
        for (int i = 0; i < lidarCount; i++) {
            this.lidarPaint.setARGB((int) (i * (255f / lidarCount)), 0, 0, (int) Tweening.clamp(this.lidarStrength * 255f, 0, 255));
            
            canvas.drawLine(
                    (float) (this.windowCenter.x + this.robotHeadingCurrent.x * viewDistance + robotHeadingNormal.x * -fov),
                    (float) (this.windowCenter.y + -this.robotHeadingCurrent.y * viewDistance + -robotHeadingNormal.y * -fov),
                    (float) (this.windowCenter.x + this.robotHeadingCurrent.x * viewDistance + robotHeadingNormal.x * fov),
                    (float) (this.windowCenter.y + -this.robotHeadingCurrent.y * viewDistance + -robotHeadingNormal.y * fov),
                    this.lidarPaint);
            
            fov -= fovDecrease;
            viewDistance -= viewDistanceDecrease;
        }
    }
    
    private void updateCollision(Canvas canvas) {
        float xScale = (float) Tweening.sine(0.90, 1, 4, 0, this.secondsCounter);
        float yScale = (float) Tweening.sine(0.85, 1, 4, Math.PI / 2.0, this.secondsCounter);
    
        synchronized (this.collisionList) {
            for (int i = 1; i < this.collisionList.size(); i++) {
                this.collisionMatrix.setTranslate((float) (this.collisionList.get(i).x - (this.robotPositionCurrent.x * this.gridDivider)) - (this.collisionBitmap.getWidth() / 2f), (float) (this.collisionList.get(i).y + (this.robotPositionCurrent.y * this.gridDivider)) - (this.collisionBitmap.getHeight() / 2f));
                this.collisionMatrix.preScale(xScale, yScale, this.collisionBitmap.getWidth() / 2f, this.collisionBitmap.getHeight() / 2f);
        
                canvas.drawBitmap(this.collisionBitmap, this.collisionMatrix, null);
            }
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        this.updateWindow();
        this.updateTime();
        
        this.updateGrid(canvas);
        this.updatePath(canvas);
        this.updateCollision(canvas);
        this.updateRobot(canvas);
        this.updateLidar(canvas);
    }
    
    @Override
    public void doFrame(long frameTimeNanos) {
        Choreographer.getInstance().postFrameCallback(this);
        this.invalidate();
    }
}
