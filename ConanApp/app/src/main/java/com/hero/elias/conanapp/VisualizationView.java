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
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class VisualizationView extends View implements Choreographer.FrameCallback, MbotHandler.MbotCallback, RotationListener.OnRotationGestureListener, ScaleListener.OnScaleGestureListener {
    private Vector2D windowCenter;
    private Vector2D windowSize;
    
    private Vector2D cameraCenter;
    private double cameraZoom;
    private double cameraRotation;
    
    private int frameCounter;
    private long millisCounter;
    private double secondsCounter;
    
    private long deltaTimeMillis;
    private double deltaTimeSeconds;
    
    private long lastSystemTime;
    
    private long millisCurrent;
    private long millisStart;
    private long millisDestination;
    
    private Vector2D robotPositionCurrent;
    private Vector2D robotPositionStart;
    private Vector2D robotPositionDestination;
    
    private Vector2D robotHeadingCurrent;
    private Vector2D robotHeadingStart;
    private Vector2D robotHeadingDestination;

    private Bitmap robotBitmap;
    private Matrix robotMatrix;
    private float robotBaseScale;
    
    private Paint pathPaint;
    private List<Vector2D> pathList;
    private Path path;
    private CornerPathEffect pathCornerEffect;
    private double pathCornerEffectPhaseCounter;
    
    private long lidarMillisLast;
    
    private Bitmap collisionBitmap;
    private Matrix collisionMatrix;
    private float collisionBaseScale;
    private List<Vector2D> collisionList;
    private Paint collisionPaint;
    
    private ScaleGestureDetector scaleDetector;
    private RotationListener rotationDetector;
    
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
        this.scaleDetector = new ScaleGestureDetector(context, new ScaleListener(this));
        this.rotationDetector = new RotationListener(this);
        
        this.windowSize = new Vector2D(0.0, 0.0);
        this.windowCenter = new Vector2D(0.0, 0.0);
        
        this.cameraCenter = new Vector2D(0, 0);
        this.cameraZoom = 2.5;
        
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inMutable = true;
        
        this.robotBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icons8_gps_64px, bitmapOptions);
        this.robotMatrix = new Matrix();
        this.robotBaseScale = 0.25f;
        
        this.lidarMillisLast = 0;
        
        this.pathPaint = new Paint();
        this.pathPaint.setStrokeWidth(8f);
        this.pathPaint.setARGB(64, 255, 0, 0);
        this.pathPaint.setStyle(Paint.Style.STROKE);
        
        this.frameCounter = 0;
        this.secondsCounter = 0.0;
        this.deltaTimeSeconds = 0.0;
        this.deltaTimeMillis = 0;
    
        this.path = new Path();
        this.pathList = new ArrayList<Vector2D>();
        this.pathCornerEffectPhaseCounter = 0.0;
        this.pathCornerEffect = new CornerPathEffect(50);
        
        this.collisionBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icons8_vlc_64px, bitmapOptions);
        this.collisionMatrix = new Matrix();
        this.collisionList = new ArrayList<Vector2D>();
        this.collisionPaint = new Paint();
        this.collisionPaint.setARGB(255, 0, 255, 0);
        this.collisionBaseScale = 0.25f;
    
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
    
        this.updateWindow();
        
        MbotHandler.getInstance().addCallback(this);
        Choreographer.getInstance().postFrameCallback(this);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.scaleDetector.onTouchEvent(event);
        this.rotationDetector.onTouchEvent(event);
        
        return true; // hack, should return super. but scaleDetector only works if returns true
    }
    
    @Override
    public void OnRotation(float angle) {
        this.cameraRotation = angle;
    }
    
    @Override
    public void OnScale(double scale) {
        this.cameraZoom *= scale;
        this.cameraZoom = Math.max(0.01f, Math.min(this.cameraZoom, 50f));
    }
    
    @Override
    public void onNewData(Vector2D position, Vector2D heading, long millis, int lidar, int gap) {
        if (((lidar < 20f) || (gap > 0)) && ((millis - this.lidarMillisLast) > 1500)){
            synchronized (this.collisionList) {
                Vector2D v = new Vector2D(position.x, position.y);
                this.collisionList.add(v);
            
                if (this.collisionList.size() == 64) {
                    this.collisionList.remove(0);
                }
                
            }
    
            this.pathList.add(new Vector2D(
                    this.robotPositionCurrent.x,
                    this.robotPositionCurrent.y));
    
            this.lidarMillisLast = millis;
        }
    }
    
    private Vector2D cameraTranslation(Vector2D v) {
        Vector2D translatedVector = v.clone();
        
        translatedVector.subtract(this.cameraCenter);
        
        translatedVector.add(this.cameraCenter);
        translatedVector.multiply(this.cameraZoom);
        translatedVector.rotate(this.cameraCenter, this.cameraRotation);
        translatedVector.subtract(this.cameraCenter);
        
        translatedVector.add(this.windowCenter);
        
        return translatedVector;
    }
    
    private void updateWindow(){
        this.windowSize.x = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
        this.windowSize.y = this.getHeight() - this.getPaddingTop() - this.getPaddingBottom();
        
        this.windowCenter.x = this.windowSize.x / 2.0;
        this.windowCenter.y = this.windowSize.y / 2.0;
        
        Vector2D pos = this.robotPositionCurrent.clone();
        pos.multiply(this.cameraZoom);
        this.cameraCenter = pos;
    }
    
    private void updateTime(){
        long systemTime = System.currentTimeMillis();

        this.deltaTimeMillis = (int) (systemTime - this.lastSystemTime);
        this.deltaTimeSeconds =  (double) this.deltaTimeMillis / 1000.0;
    
        this.frameCounter++;
        this.millisCounter += this.deltaTimeMillis;
        this.secondsCounter += this.deltaTimeSeconds;
    
        this.lastSystemTime = systemTime;
    }
    
    private void updateGrid(Canvas canvas) {
        Paint gridPaint = new Paint();
        gridPaint.setARGB(127, 64, 64, 64);
        
        for (int i = 0; i < 64; i++){
            double xPos = Tweening.linear(i, 0, 64, -4096, 4096);
            for (int j = 0; j < 64; j++){
                double yPos = Tweening.linear(j, 0, 64, -4096, 4096);
                
                Vector2D pos = this.cameraTranslation(new Vector2D(xPos, yPos));
                canvas.drawCircle((float)pos.x, (float)pos.y, (float) (4f * this.cameraZoom), gridPaint);
            }
        }
    }
    
    private void updatePath(Canvas canvas){
        if (this.pathList.isEmpty()){
            this.pathList.add(new Vector2D(
                    this.robotPositionCurrent.x,
                    this.robotPositionCurrent.y));
        }
    
        if (this.frameCounter % 120 == 0){
            this.pathList.add(new Vector2D(
                    this.robotPositionCurrent.x,
                    this.robotPositionCurrent.y));
    
            if (this.pathList.size() > 64) {
                this.pathList.remove(0);
            }
        }
        
        this.path.reset();
        Vector2D pos = this.cameraTranslation(this.pathList.get(0));
        this.path.moveTo(
                (float) pos.x,
                (float) pos.y);
        
        for (int i = 1; i < this.pathList.size(); i++) {
            pos = this.cameraTranslation(this.pathList.get(i));
    
            this.path.lineTo(
                    (float) pos.x,
                    (float) pos.y);
        }
    
        pos = this.cameraTranslation(this.robotPositionCurrent);
        this.path.lineTo((float) pos.x, (float) pos.y);
        
        this.pathCornerEffectPhaseCounter -= 6.0;
        DashPathEffect dashPathEffect = new DashPathEffect(new float[]{(float) (64 * this.cameraZoom), (float) (32 * this.cameraZoom)}, (float) ((float)this.pathCornerEffectPhaseCounter * this.cameraZoom));
        ComposePathEffect composePathEffect = new ComposePathEffect(dashPathEffect, this.pathCornerEffect);
        this.pathPaint.setPathEffect(composePathEffect);
        
        this.pathPaint.setStrokeWidth((float) (5f * this.cameraZoom));
        
        canvas.drawPath(this.path, this.pathPaint);
    }
    
    private void updateCollision(Canvas canvas) {
        // Animated bounce animation scaling, to make scaling work properly yScale needs to be stretched 180 degrees out of phase in time
        float xScale = (float) ((float) Tweening.sine(0.90, 1, 4, 0, this.secondsCounter) * (this.collisionBaseScale * this.cameraZoom));
        float yScale = (float) ((float) Tweening.sine(0.90, 1, 4, Math.PI / 2.0, this.secondsCounter) * (this.collisionBaseScale * this.cameraZoom));
        
        synchronized (this.collisionList) { // lock access to list to avoid threading
            for (int i = 0; i < this.collisionList.size(); i++) {
                Vector2D pos = this.cameraTranslation(this.collisionList.get(i));
                
                this.collisionMatrix.setTranslate(
                        (float) pos.x - (this.collisionBitmap.getWidth() / 2f),
                        (float) pos.y - (this.collisionBitmap.getHeight() / 2f));
                
                this.collisionMatrix.preScale(
                        xScale, yScale, this.collisionBitmap.getWidth() / 2f,
                        this.collisionBitmap.getHeight() / 2f);
                
                canvas.drawBitmap(this.collisionBitmap, this.collisionMatrix, null);
            }
        }
    }
    
    private void updateRobot(Canvas canvas){
/*        if (this.millisCurrent < MbotHandler.getInstance().getMillis()){
            this.millisCurrent += this.deltaTimeMillis;
    
            if (this.millisCurrent >= this.millisDestination){
                
                this.millisStart = this.millisDestination;
                this.millisDestination = MbotHandler.getInstance().getMillis();
        
                this.robotPositionStart = this.robotPositionCurrent.clone();
                this.robotPositionDestination = MbotHandler.getInstance().getPosition().clone();
        
                this.robotHeadingStart = this.robotHeadingCurrent.clone();
                this.robotHeadingDestination = MbotHandler.getInstance().getHeading().clone();
            }
    
            this.robotPositionCurrent = Tweening.linear(this.millisCurrent, this.millisStart, this.millisDestination, this.robotPositionStart, this.robotPositionDestination);
    
            this.robotHeadingCurrent = Tweening.linear(this.millisCurrent, this.millisStart, this.millisDestination, this.robotHeadingStart, this.robotHeadingDestination);
            this.robotHeadingCurrent = Vector2D.normalizeVector(this.robotHeadingCurrent);
        }*/
    
        //this.robotPositionCurrent = MbotHandler.getInstance().getPosition().clone();
        //this.robotHeadingCurrent = MbotHandler.getInstance().getHeading().clone();
    
        this.robotPositionCurrent.x = Tweening.smoothToTarget(this.robotPositionCurrent.x, MbotHandler.getInstance().getPosition().x, 1.1f);
        this.robotPositionCurrent.y = Tweening.smoothToTarget(this.robotPositionCurrent.y, MbotHandler.getInstance().getPosition().y, 1.1f);
    
        this.robotHeadingCurrent.x = Tweening.smoothToTarget(this.robotHeadingCurrent.x, MbotHandler.getInstance().getHeading().x, 1.1f);
        this.robotHeadingCurrent.y = Tweening.smoothToTarget(this.robotHeadingCurrent.y, MbotHandler.getInstance().getHeading().y, 1.1f);
    
        float xScale = (float) ((float) Tweening.sine(1.85, 2, 4, 0, this.secondsCounter) * (this.robotBaseScale * this.cameraZoom));
        float yScale = (float) ((float) Tweening.sine(1.85, 2, 4, Math.PI / 2.0, this.secondsCounter) * (this.robotBaseScale * this.cameraZoom));
        
        double xAngle = this.robotHeadingCurrent.x;
        double yAngle = -this.robotHeadingCurrent.y;
    
        this.robotMatrix.setRotate((float) ((float) Vector2D.vectorToDegree(new Vector2D(xAngle, yAngle)) + this.cameraRotation), this.robotBitmap.getWidth() / 2f, this.robotBitmap.getHeight() / 2f);
        this.robotMatrix.postScale(xScale, yScale, this.robotBitmap.getWidth() / 2f, this.robotBitmap.getHeight() / 2f);
        
        Vector2D pos = this.cameraTranslation(this.robotPositionCurrent);
        this.robotMatrix.postTranslate(
                (float) pos.x - (this.robotBitmap.getWidth() / 2f),
                (float) pos.y - (this.robotBitmap.getHeight() / 2f));
        
        canvas.drawBitmap(this.robotBitmap, this.robotMatrix, null);
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
    }
    
    @Override
    public void doFrame(long frameTimeNanos) {
        Choreographer.getInstance().postFrameCallback(this);
        this.invalidate();
    }
}

class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private OnScaleGestureListener listener;
    
    public ScaleListener(OnScaleGestureListener listener){
        this.listener = listener;
    }
    
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (this.listener != null){
            listener.OnScale(detector.getScaleFactor());
        }
        
        return true;
    }
    
    interface OnScaleGestureListener {
        public void OnScale(double scale);
    }
}

class RotationListener {
    private static final int INVALID_POINTER_ID = -1;
    private float fX, fY, sX, sY;
    private int ptrID1, ptrID2;
    private float angle;
    private float lastAngle;
    private OnRotationGestureListener listener;
    
    public RotationListener(OnRotationGestureListener listener){
        this.listener = listener;
        ptrID1 = INVALID_POINTER_ID;
        ptrID2 = INVALID_POINTER_ID;
    }
    
    private float angleBetweenLines (float fX, float fY, float sX, float sY, float nfX, float nfY, float nsX, float nsY)
    {
        float angle1 = (float) Math.atan2( (fY - sY), (fX - sX) );
        float angle2 = (float) Math.atan2( (nfY - nsY), (nfX - nsX) );
        
        float angle = ((float)Math.toDegrees(angle1 - angle2)) % 360;
        if (angle < -180.f) angle += 360.0f;
        if (angle > 180.f) angle -= 360.0f;
        return angle;
    }
    
    public boolean onTouchEvent(MotionEvent event){
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                ptrID1 = event.getPointerId(event.getActionIndex());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                ptrID2 = event.getPointerId(event.getActionIndex());
                sX = event.getX(event.findPointerIndex(ptrID1));
                sY = event.getY(event.findPointerIndex(ptrID1));
                fX = event.getX(event.findPointerIndex(ptrID2));
                fY = event.getY(event.findPointerIndex(ptrID2));
                break;
            case MotionEvent.ACTION_MOVE:
                if(ptrID1 != INVALID_POINTER_ID && ptrID2 != INVALID_POINTER_ID){
                    float nfX, nfY, nsX, nsY;
                    nsX = event.getX(event.findPointerIndex(ptrID1));
                    nsY = event.getY(event.findPointerIndex(ptrID1));
                    nfX = event.getX(event.findPointerIndex(ptrID2));
                    nfY = event.getY(event.findPointerIndex(ptrID2));
                    
                    angle = angleBetweenLines(fX, fY, sX, sY, nfX, nfY, nsX, nsY) + lastAngle;
                    
                    if (listener != null) {
                        listener.OnRotation(this.angle);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                ptrID1 = INVALID_POINTER_ID;
                lastAngle = angle;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                ptrID2 = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_CANCEL:
                ptrID1 = INVALID_POINTER_ID;
                ptrID2 = INVALID_POINTER_ID;
                break;
        }
        return true;
    }
    
    interface OnRotationGestureListener {
        public void OnRotation(float angle);
    }
}
