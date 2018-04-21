package com.vanjav.roadline;

import com.badlogic.gdx.graphics.Color;

public class LinePaint {
    protected String name;
    protected Color color;
    protected int pointsToUnlock;

    protected boolean animated;

    protected Color[] colors;

    public LinePaint(String name, Color color, int pointsToUnlock) {
        this.name = name;
        this.color = color;
        this.pointsToUnlock = pointsToUnlock;

        animated = false;
    }

    public LinePaint(String name, Color[] colors, int pointsToUnlock) {
        this.name = name;
        this.colors = colors;
        this.pointsToUnlock = pointsToUnlock;

        animated = true;
    }
}
