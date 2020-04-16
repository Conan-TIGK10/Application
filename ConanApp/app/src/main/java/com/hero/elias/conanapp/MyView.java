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
import android.view.View;

import androidx.core.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

class Vector2D {
    public Vector2D(float x, float y){
        this.x = x;
        this.y = y;
    }
    
    float x;
    float y;
}

public class MyView extends View implements Choreographer.FrameCallback {
    private Bitmap bitmap;
    private Matrix matrix;
    private Paint gridPaint;
    
    private Vector2D currentHeading;
    private Vector2D destinationHeading;
    
    private Vector2D currentPosition;
    
    private List<Vector2D> pathList;
    
    private int lidarCount;
    private int lidarStrength;
    
    int frameCounter;
    
    public MyView(Context context) {
        super(context);
        this.init(context, null, 0);
    }
    
    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }
    
    public MyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context, attrs, defStyle);
    }
    
    private void init(Context context, AttributeSet attrs, int defStyle) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;
        this.bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icons8_gps_64px, opt);
    
        this.matrix = new Matrix();
        
        this.gridPaint = new Paint();
    
        this.frameCounter = 0;
        this.lidarCount = 12;
        this.lidarStrength = 127;
        
        this.pathList = new ArrayList<Vector2D>();
        
        this.currentPosition = new Vector2D(0f, 0f);
        this.currentHeading = new Vector2D(0f, 1f);
        this.destinationHeading = new Vector2D(0f, -1f);
    
        Choreographer.getInstance().postFrameCallback(this);
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int paddingLeft = this.getPaddingLeft();
        int paddingTop = this.getPaddingTop();
        int paddingRight = this.getPaddingRight();
        int paddingBottom = this.getPaddingBottom();
        int contentWidth = this.getWidth() - paddingLeft - paddingRight;
        int contentHeight = this.getHeight() - paddingTop - paddingBottom;
    
        Vector2D contentCenter = new Vector2D(contentWidth / 2f, contentHeight / 2f);
    
        this.frameCounter++;
        if (this.frameCounter > (60 * 3)){
            this.frameCounter = 0;
            this.lidarStrength = (int)(Math.random() * 255f);
            this.pathList.add(new Vector2D(contentCenter.x + (this.currentPosition.x * ( 4f)), contentCenter.y - (this.currentPosition.y * ( 4f))));
            this.destinationHeading = this.degreeToVector(this.getRandFloat(0, 360));
            this.gridPaint.setARGB(255, (int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
        }
        
        this.currentHeading.x = this.smoothToTarget(this.currentHeading.x, this.destinationHeading.x, 12f);
        this.currentHeading.y = this.smoothToTarget(this.currentHeading.y, this.destinationHeading.y, 12f);
        
        this.currentPosition.x += this.currentHeading.x;
        this.currentPosition.y += this.currentHeading.y;
        

        if (this.pathList.size() > 2){
            Paint pathPaint = new Paint();
            pathPaint.setStrokeWidth(8f);
            pathPaint.setARGB(127, 255, 0, 0);
            pathPaint.setStyle(Paint.Style.STROKE);
            Path path = new Path();
    
            Vector2D lStart = new Vector2D(this.pathList.get(0).x - (this.currentPosition.x * ( 4f)), this.pathList.get(0).y + (this.currentPosition.y * (  4f)));
            path.moveTo(lStart.x, lStart.y);
    
            for (int i = 1; i < this.pathList.size(); i++){
        
                lStart = new Vector2D(this.pathList.get(i).x - (this.currentPosition.x * ( 4f)), this.pathList.get(i).y + (this.currentPosition.y * (  4f)));
        
                path.lineTo(lStart.x, lStart.y);
                //float length = this.lengthBetweenVectors(lEnd, contentCenter);
                //pathPaint.setAlpha((int)this.lerp(MathUtils.clamp(length, 0f,500f), 0f, 500f, 255f, 0f));
                //canvas.drawLine(lStart.x, lStart.y, lEnd.x, lEnd.y, pathPaint);
            }
            path.lineTo(contentCenter.x, contentCenter.y);
    
            CornerPathEffect cornerPathEffect = new CornerPathEffect(400);
            DashPathEffect dashPathEffect = new DashPathEffect(new float[]{64, 32}, 0);
            ComposePathEffect composePathEffect = new ComposePathEffect(dashPathEffect, cornerPathEffect);
            pathPaint.setPathEffect(composePathEffect);
            canvas.drawPath(path, pathPaint);
        }
        if(this.pathList.size() == 128){
            this.pathList.remove(0);
        }

        float scaleX = -((this.currentPosition.x % 16f) / 16f);
        float scaleY = ((this.currentPosition.y % 16f) / 16f);
        
        for (int i = 0; i < (16 + 1); i++){
            for (int j = 0; j < 32; j++){
                Vector2D circle = new Vector2D((i * (contentWidth / 16f) + ((contentWidth / 16f) * scaleX)),(j * (contentWidth / 16f) + ((contentWidth / 16f) * scaleY)));
                float length = this.lengthBetweenVectors(circle, contentCenter);
                this.gridPaint.setAlpha((int)this.lerp(MathUtils.clamp(length, 0f,750f), 0f, 750f, 127f, 0f));
                canvas.drawCircle(circle.x, circle.y, 16f, this.gridPaint);
            }
        }
        
        this.matrix.setRotate(this.vectorToDegree(this.currentHeading), this.bitmap.getWidth() / 2f, this.bitmap.getHeight() / 2f);
        this.matrix.postScale(2f, 2f);
        this.matrix.postTranslate((contentWidth / 2f) - ((this.bitmap.getWidth() * 2f) / 2f), contentHeight / 2f - ((this.bitmap.getHeight() * 2f) / 2f));
        canvas.drawBitmap(this.bitmap, this.matrix, null);
    
        Paint wallPaint = new Paint();
        wallPaint.setMaskFilter(new BlurMaskFilter(24f, BlurMaskFilter.Blur.NORMAL));
        //Vector2D wallStart = new Vector2D(contentCenter.x + (this.currentHeading.x * (contentWidth / 2f)), contentCenter.y + (-this.currentHeading.y * (contentWidth / 2f)));
        //canvas.drawCircle(wallStart.x, wallStart.y,32f, wallPaint);
    
        float f = 256 + 128;
        float vd = 512;
        float fDec = f / this.lidarCount;
        float vdDec = vd / (this.lidarCount * 1.5f);
        
        for (int i = 0; i < this.lidarCount; i++){
            wallPaint.setARGB((i * ((this.lidarStrength) / this.lidarCount)), 64, 64, 64);
            wallPaint.setStrokeWidth(24f);
    
            Vector2D lineStart = new Vector2D(
                    contentCenter.x + (this.currentHeading.x * vd) + this.normalDirection(this.currentHeading).x * -f,
                    contentCenter.y + (-this.currentHeading.y * vd) + -this.normalDirection(this.currentHeading).y * -f);
    
            Vector2D lineEnd = new Vector2D(
                    contentCenter.x + this.currentHeading.x * vd + this.normalDirection(this.currentHeading).x * f,
                    contentCenter.y + -this.currentHeading.y * vd + -this.normalDirection(this.currentHeading).y * f);
    
            canvas.drawLine(lineStart.x, lineStart.y, lineEnd.x, lineEnd.y, wallPaint);
    
            f -= fDec;
            vd -= vdDec;
        }
    
    }
    
    private float vectorToDegree(Vector2D v){
        return (float) (Math.atan2(v.x, v.y) * 180 / Math.PI);
    }
    
    private Vector2D degreeToVector(float degree){
        float radian = (float) (degree * (Math.PI / 180f));
        Vector2D v = new Vector2D((float)Math.cos(radian), (float)Math.sin(radian));
        return v;
    }
    
    private Vector2D normalDirection(Vector2D point) {
        Vector2D v = this.normalizeVector(point);
        return new Vector2D(-v.y, v.x);
    }
    
    private Vector2D normalizeVector(Vector2D point) {
        float length = this.lengthOfVector(point);
        Vector2D v = new Vector2D(point.x / length, point.y / length);
        return v;
    }
    
    private float getRandFloat(float min, float max){
        return min + (float)Math.random() * (max - min);
    }
    
    private float lerp(float x, float x0, float x1, float y0, float y1) {
        return y0 + (x - x0) * (y1 - y0) / (x1 - x0);
    }
    
    private float smoothToTarget(float current, float target, float scalar){
        current += (target - current) / scalar;
        return current;
    }
    
    private float lengthBetweenVectors(Vector2D a, Vector2D b){
        return (float)Math.sqrt(Math.pow(b.x - a.x, 2) + Math.pow(b.y - a.y, 2));
    }
    
    public float lengthOfVector(Vector2D point) {
        return (float)Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2));

    }
    
    private float linearToTarget(float current, float target, float scalar){
        if (current != target){
            if (current > target){
                current -= scalar;
                if (current < target){
                    current = target;
                }
            }else{
                current += scalar;
                if (current > target){
                    current = target;
                }
            }
        }
        return current;
    }
    
    @Override
    public void doFrame(long frameTimeNanos) {
        this.invalidate();
        Choreographer.getInstance().postFrameCallback(this);
    }
}
