package com.example.jose.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by jose on 16/11/15.
 */


public class DrawingView extends View {

    //Arraylist to save all touch events
    public static ArrayList<Float> pointsStartXList = new ArrayList<>();
    public static ArrayList<Float> pointsStartYList = new ArrayList<>();
    public static ArrayList<Float> pointsEndXList = new ArrayList<>();
    public static ArrayList<Float> pointsEndYList = new ArrayList<>();


    //Arraylist to redo
    public static ArrayList<Float> redoStartXList = new ArrayList<>();
    public static ArrayList<Float> redoStartYList = new ArrayList<>();
    public static ArrayList<Float> redoEndXList = new ArrayList<>();
    public static ArrayList<Float> redoEndYList = new ArrayList<>();
    public float brushSize, lastBrushSize;
    //canvas
    public Canvas drawCanvas;
    //Events
    private float startX, startY, endX, endY, endxTemp, endyTemp;
    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas bitmap
    private Bitmap canvasBitmap;


    public DrawingView(Context context) {
        this(context, null);


    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();


    }

    private void setupDrawing() {
        //get drawing area setup for interaction

        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);

        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        brushSize = 30;


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //view given size

        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);


    }

    @Override
    public void onDraw(Canvas canvas) {
        //draw view
        super.onDraw(canvas);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        // canvas.drawPath(drawPath, drawPaint);
        canvas.drawLine(startX, startY, endX, endY, drawPaint);
        invalidate();


    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        //detect user touch


        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();


                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                endX = event.getX();
                endY = event.getY();
                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                endX = event.getX();
                endY = event.getY();

                //Agregar todos los puntos
                pointsStartXList.add(startX);
                pointsStartYList.add(startY);
                pointsEndXList.add(endX);
                pointsEndYList.add(endY);

                drawCanvas.drawLine(startX, startY, endX, endY, drawPaint);
                invalidate();
                drawPath.reset();
                break;


        }

        return true;
    }

    public void setColor(String newColor) {

        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    //Crear nuevo Drawing
    public void startNew() {

        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        startX = 0;
        startY = 0;
        endX = 0;
        endY = 0;
        pointsStartXList.clear();
        pointsStartYList.clear();
        pointsEndXList.clear();
        pointsEndYList.clear();
        drawCanvas.drawLine(startX, startY, endX, endY, drawPaint);
        invalidate();


    }

    public void undo() {


        invalidate();
        //Agregar los elementos a eliminar a la lista de redo antes de ser eliminados
        redoStartXList.add(pointsStartXList.get(pointsStartXList.size() - 1));
        redoStartYList.add(pointsStartYList.get(pointsStartYList.size() - 1));
        redoEndXList.add(pointsEndXList.get(pointsEndXList.size() - 1));
        redoEndYList.add(pointsEndYList.get(pointsEndYList.size() - 1));

        //Remover cada punto de la lista
        pointsStartXList.remove(pointsStartXList.size() - 1);
        pointsStartYList.remove(pointsStartYList.size() - 1);
        pointsEndXList.remove(pointsEndXList.size() - 1);
        pointsEndYList.remove(pointsEndYList.size() - 1);

        //Limpiar para luego dibujar
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        startX = 0;
        startY = 0;
        endX = 0;
        endY = 0;


        //Dibujar todos los puntos de los arrays list otra vez
        for (int i = 0; i <= pointsStartXList.size() - 1; i++) {

            startX = pointsStartXList.get(i);
            startY = pointsStartYList.get(i);
            endX = pointsEndXList.get(i);
            endY = pointsEndYList.get(i);
            drawCanvas.drawLine(startX, startY, endX, endY, drawPaint);

        }

        invalidate();
        drawPaint.setColor(paintColor);


    }

    public void redo() {

        //Agregar los elementos removidos en el undo
        pointsStartXList.add(redoStartXList.get(redoStartXList.size() - 1));
        pointsStartYList.add(redoStartYList.get(redoStartYList.size() - 1));
        pointsEndXList.add(redoEndXList.get(redoEndXList.size() - 1));
        pointsEndYList.add(redoEndYList.get(redoEndYList.size() - 1));

        //Remover los elementos que ya fueron redo de la lista redo
        redoStartXList.remove(redoStartXList.get(redoStartXList.size() - 1));
        redoStartYList.remove(redoStartYList.get(redoStartYList.size() - 1));
        redoEndXList.remove(redoEndXList.get(redoEndXList.size() - 1));
        redoEndYList.remove(redoEndYList.get(redoEndYList.size() - 1));


        //Dibujar otra vez los puntos
        startX = pointsStartXList.get(pointsStartXList.size() - 1);
        startY = pointsStartYList.get(pointsStartYList.size() - 1);
        endX = pointsEndXList.get(pointsEndXList.size() - 1);
        endY = pointsEndYList.get(pointsEndYList.size() - 1);
        drawCanvas.drawLine(startX, startY, endX, endY, drawPaint);

        invalidate();


    }


}

