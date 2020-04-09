package com.hero.elias.conanapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.hero.elias.conanapp.mbot.Vector2D;

public class DrawView extends View {
    Paint paint = new Paint();
    int viewWidth;
    int viewHeight;
    Vector2D pointPos;
    
    public DrawView(Context context) {
        super(context);
    }
    
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public DrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    private void init() {
        paint.setColor(Color.BLACK);
        this.viewWidth = this.getWidth();
        this.viewHeight = this.getHeight();
        this.pointPos = new Vector2D(50, 50);
    }
    
    public void DrawCircle(Vector2D pos) {
        this.pointPos = pos;
        this.ReDraw();
    }
    
    public void ReDraw() {
        this.invalidate();
    }
    
    @Override
    public void onDraw(Canvas canvas) {
        init();
        canvas.drawCircle((float) this.pointPos.x, (float) this.pointPos.y, 6, this.paint);
        canvas.drawLine(0, 0, viewWidth, viewHeight, paint);
        canvas.drawLine(viewWidth, 0, 0, viewHeight, paint);
    }
    
}
