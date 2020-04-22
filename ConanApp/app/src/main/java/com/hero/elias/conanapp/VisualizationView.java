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

public class VisualizationView extends View implements Choreographer.FrameCallback {
    
    private int frameCounter;
    private double secondsCounter;
    private double deltaTime;
    private long previousNanoTime;
    
    private Bitmap robotBitmap;
    private Matrix robotMatrix;
    
    private Paint gridPaint;
    private Paint lidarPaint;
    private Paint pathPaint;
    
    private Vector2D robotPosition;
    private Vector2D robotHeading;
    private Vector2D robotDestinationHeading;
    
    private List<Vector2D> pathList;
    private Path path;
    private CornerPathEffect cornerPathEffect;
    private double phaseCounter;
    
    private double gridSpacing;
    private double gridMultiplier;
    private double gridDivider;
    
    private int windowWidth;
    private int windowHeight;
    private Vector2D windowCenter;
    
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
        
        this.gridPaint = new Paint();
        this.gridPaint.setARGB(255, 64, 64, 64);
    
        this.lidarPaint = new Paint();
        this.lidarPaint.setMaskFilter(new BlurMaskFilter(32f, BlurMaskFilter.Blur.NORMAL));
        this.lidarPaint.setStrokeWidth(32f);
        
        this.pathPaint = new Paint();
        this.pathPaint.setStrokeWidth(8f);
        this.pathPaint.setARGB(64, 255, 0, 0);
        this.pathPaint.setStyle(Paint.Style.STROKE);
        
        this.frameCounter = 0;
        this.previousNanoTime = 0;
        this.secondsCounter = 0.0;
        this.deltaTime = 0.0;
        
        this.gridSpacing = 8.0;
        this.gridMultiplier = this.gridSpacing * 2.0;
        this.gridDivider = this.gridMultiplier / 2.0;
        
        this.path = new Path();
        this.pathList = new ArrayList<Vector2D>();
        this.pathList.add(this.windowCenter);
        this.phaseCounter = 0.0;
        this.cornerPathEffect = new CornerPathEffect(100);
    
        this.robotPosition = new Vector2D(0.0, 0.0);
        this.robotHeading = new Vector2D(0.0, 1.0);
        this.robotDestinationHeading = new Vector2D(0.0, 1.0);
    
        Choreographer.getInstance().postFrameCallback(this);
    }
    
    private void updateWindow(){
        this.windowWidth = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
        this.windowHeight = this.getHeight() - this.getPaddingTop() - this.getPaddingBottom();
        this.windowCenter.x = this.windowWidth / 2f;
        this.windowCenter.y = this.windowHeight / 2f;
    }
    
    private void updateTime(){
        this.frameCounter++;
        this.secondsCounter += this.deltaTime;
    }
    
    private void updateRandom(){
        if (this.frameCounter % (60 * 3) == 0) {
            this.pathList.add(new Vector2D(this.windowCenter.x + (this.robotPosition.x * this.gridDivider), this.windowCenter.y - (this.robotPosition.y * this.gridDivider)));
            this.robotDestinationHeading = Vector2D.degreeToVector(this.getRandDouble(0.0, 360.0));
        }
    }
    
    private void updatePath(Canvas canvas){
        this.path.reset();
        this.path.moveTo((float)(this.pathList.get(0).x - (this.robotPosition.x * this.gridDivider)), (float)(this.pathList.get(0).y + (this.robotPosition.y * this.gridDivider)));
        for (int i = 1; i < this.pathList.size(); i++) {
            this.path.lineTo((float)(this.pathList.get(i).x - (this.robotPosition.x * this.gridDivider)), (float)(this.pathList.get(i).y + (this.robotPosition.y * this.gridDivider)));
        }
        this.path.lineTo((float)this.windowCenter.x, (float)this.windowCenter.y);
    
        this.phaseCounter -= 6.0;
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{64, 32}, (float)this.phaseCounter);
        ComposePathEffect composePathEffect = new ComposePathEffect(dashPathEffect, this.cornerPathEffect);
        this.pathPaint.setPathEffect(composePathEffect);
        canvas.drawPath(this.path, this.pathPaint);
    
        if (this.pathList.size() == 64) {
            this.pathList.remove(0);
        }
    }
    
    private void updateRobot(Canvas canvas){
        this.robotHeading.x = Tween.smoothToTarget(this.robotHeading.x, this.robotDestinationHeading.x, 12.0);
        this.robotHeading.y = Tween.smoothToTarget(this.robotHeading.y, this.robotDestinationHeading.y, 12.0);
        this.robotHeading = Vector2D.normalizeVector(this.robotHeading);
        this.robotPosition.x += this.robotHeading.x;
        this.robotPosition.y += this.robotHeading.y;
        
        float xScale = (float) Tween.sine(1.85, 2, 4, 0, this.secondsCounter);
        float yScale = (float) Tween.sine(1.85, 2, 4, Math.PI / 2.0, this.secondsCounter);
        
        
        this.robotMatrix.setRotate((float) Vector2D.vectorToDegree(this.robotHeading), this.robotBitmap.getWidth() / 2f, this.robotBitmap.getHeight() / 2f);
        this.robotMatrix.postScale(xScale, yScale, this.robotBitmap.getWidth() / 2f, this.robotBitmap.getHeight() / 2f);
        this.robotMatrix.postTranslate((float) this.windowCenter.x - this.robotBitmap.getWidth() / 2f, (float) this.windowCenter.y - this.robotBitmap.getHeight() / 2f);
        canvas.drawBitmap(this.robotBitmap, this.robotMatrix, null);
    }
    
    private void updateGrid(Canvas canvas){
        double scaleX = -((this.robotPosition.x % this.gridMultiplier) / this.gridMultiplier);
        double scaleY = ((this.robotPosition.y % this.gridMultiplier) / this.gridMultiplier);
        
        for (int i = 0; i < (this.gridSpacing + 1); i++) {
            for (int j = 0; j < (this.gridSpacing * 2); j++) {
                Vector2D circle = new Vector2D((i * (this.windowWidth / this.gridSpacing) + ((this.windowWidth / this.gridSpacing) * scaleX)), (j * (this.windowWidth / this.gridSpacing) + ((this.windowWidth / this.gridSpacing) * scaleY)));
                double length = Vector2D.lengthBetweenVectors(circle, this.windowCenter);
                this.gridPaint.setAlpha((int) Tween.cosine(MathUtils.clamp(length, 0.0, 750.0), 0.0, 750.0, 127.0, 0.0));
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
        
        Vector2D robotHeadingNormal = Vector2D.normalDirection(this.robotHeading);
    
        for (int i = 0; i < lidarCount; i++) {
            if (((this.frameCounter + i) / 16) % lidarCount == 0) {
                this.lidarPaint.setARGB((i * (255 / lidarCount)), 64, 64, 255);
            } else {
                this.lidarPaint.setARGB((i * (255 / lidarCount)), 64, 64, 64);
            }
            
            canvas.drawLine(
                    (float) (this.windowCenter.x + this.robotHeading.x * viewDistance + robotHeadingNormal.x * -fov),
                    (float) (this.windowCenter.y + -this.robotHeading.y * viewDistance + -robotHeadingNormal.y * -fov),
                    (float) (this.windowCenter.x + this.robotHeading.x * viewDistance + robotHeadingNormal.x * fov),
                    (float) (this.windowCenter.y + -this.robotHeading.y * viewDistance + -robotHeadingNormal.y * fov),
                    this.lidarPaint);
        
            fov -= fovDecrease;
            viewDistance -= viewDistanceDecrease;
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        this.updateWindow();
        this.updateTime();
        this.updateRandom();
    
        this.updateGrid(canvas);
        this.updatePath(canvas);
        this.updateRobot(canvas);
        this.updateLidar(canvas);
    }
    
    private double getRandDouble(double min, double max) {
        return min + Math.random() * (max - min);
    }
    
    @Override
    public void doFrame(long frameTimeNanos) {
        if (this.previousNanoTime != 0){
            this.deltaTime = (frameTimeNanos - this.previousNanoTime) / 1000000000.0;
        }else{
            this.deltaTime = 0.016;
        }
        
        this.previousNanoTime = frameTimeNanos;
        Choreographer.getInstance().postFrameCallback(this);
        this.invalidate();
    }
}
