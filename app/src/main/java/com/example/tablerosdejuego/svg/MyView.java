package com.example.tablerosdejuego.svg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class MyView extends View {

    public MyView(Context context, int kolki) {
        super(context);

        if (kolki == 0){
            //this.setBackgroundResource(R.drawable.distspeed);
        }
        if (kolki == 1){
            //this.setBackgroundResource(R.drawable.hxmdist);
        }
    }

    public void setBackgroundResource (int resid){
        super.setBackgroundResource(resid);
    }

    public void onDraw(Canvas c){
        super.onDraw(c);

        Paint paint = new Paint();
        Path path = new Path();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.TRANSPARENT);
        c.drawPaint(paint);
        for (int i = 50; i < 100; i++) {
            path.moveTo(i, i-1);
            path.lineTo(i, i);
        }
        path.close();
        paint.setStrokeWidth(3);
        paint.setPathEffect(null);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        c.drawPath(path, paint);
    }

}