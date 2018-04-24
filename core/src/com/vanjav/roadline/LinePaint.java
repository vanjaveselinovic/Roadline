package com.vanjav.roadline;

import com.badlogic.gdx.graphics.Color;

public class LinePaint {
    protected String name;
    protected Color color;
    protected int pointsToUnlock;
    protected boolean showUnlock;

    protected boolean animated;

    protected Color[] colors;

    public LinePaint(String name, Color color, int pointsToUnlock) {
        this.name = name;
        this.color = color;
        this.pointsToUnlock = pointsToUnlock;
        this.showUnlock = false;

        animated = false;
    }

    public LinePaint(String name, Color[] colors, int framesPerColor, int pointsToUnlock) {
        this(name, colors[0], pointsToUnlock);

        this.colors = new Color[framesPerColor*colors.length];

        int iPlus1 = 1;
        float weight = 1;

        for (int i = 0; i < colors.length; i++) {
            iPlus1 = i+1;
            if (iPlus1 >= colors.length) iPlus1 = 0;

            for (int j = 0; j < framesPerColor; j++) {
                weight = 1f - (float) j/framesPerColor;
                this.colors[i*framesPerColor + j] = new Color(
                        colors[i].r*weight + colors[iPlus1].r*(1-weight),
                        colors[i].g*weight + colors[iPlus1].g*(1-weight),
                        colors[i].b*weight + colors[iPlus1].b*(1-weight),
                        1f
                );
            }
        }

        animated = true;
    }
}
