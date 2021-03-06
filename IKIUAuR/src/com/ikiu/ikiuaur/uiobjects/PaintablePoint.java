package com.ikiu.ikiuaur.uiobjects;

import android.graphics.Canvas;



public class PaintablePoint extends PaintableObject {

    private static int width = 2;
    private static int height = 2;

    private int color = 0;
    private boolean fill = false;

    public PaintablePoint(int color, boolean fill) {
        set(color, fill);
    }


    public void set(int color, boolean fill) {
        this.color = color;
        this.fill = fill;
    }


    @Override
    public void paint(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        canvas.save();
        canvas.translate(-width/2, -height/2);

        setFill(fill);
        setColor(color);
        paintRect(canvas, x, y, width, height);

        canvas.restore();
    }


    @Override
    public float getWidth() {
        return width;
    }


    @Override
    public float getHeight() {
        return height;
    }
}
