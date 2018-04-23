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

        this.colors = new Color[30*colors.length];

        int iPlus1 = 1;
        float weight = 1;

        for (int i = 0; i < colors.length; i++) {
            iPlus1 = i+1;
            if (iPlus1 >= colors.length) iPlus1 = 0;

            for (int j = 0; j < 30; j++) {
                weight = 1f - j/30f;
                this.colors[i*30 + j] = new Color(
                        colors[i].r*weight + colors[iPlus1].r*(1-weight),
                        colors[i].g*weight + colors[iPlus1].g*(1-weight),
                        colors[i].b*weight + colors[iPlus1].b*(1-weight),
                        1f
                );
            }
        }

        this.pointsToUnlock = pointsToUnlock;

        animated = true;
    }
}
