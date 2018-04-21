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
        this.color = colors[0];

        this.colors = new Color[50*colors.length];
        int iPlus1 = 1;
        for (int i = 0; i < colors.length; i++) {
            iPlus1 = i+1 == colors.length ? 0 : i+1;

            for (int j = 0; j < 50; j++) {
                this.colors[i*50 + j] = new Color(
                        colors[i].r,
                        colors[i].g,
                        colors[i].b,
                        1f
                );
            }
        }

        this.pointsToUnlock = pointsToUnlock;

        animated = true;
    }
}
