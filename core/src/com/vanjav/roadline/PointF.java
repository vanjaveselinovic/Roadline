package com.vanjav.roadline;

public class PointF {
    public float x;
    public float y;

    public PointF(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void offset(float x, float y) {
        this.x += x;
        this.y += y;
    }
}
