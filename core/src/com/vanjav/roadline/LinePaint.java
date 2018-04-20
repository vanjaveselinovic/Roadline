package com.vanjav.roadline;

import com.badlogic.gdx.graphics.Color;

public class LinePaint {
    private String name;
    private Color color;

    private boolean multicolor;
    private float segmentLength;

    private boolean animated;
    private float period;

    private Color[] colors;

    public LinePaint(String name, Color color) {
        this.name = name;
        this.color = color;
        multicolor = false;
        animated = false;
    }

    public LinePaint(String name, Color[] colors, boolean multicolorOrAnimated, float segmentLengthOrPeriod) {
        this.name = name;
        this.colors = colors;
        if (multicolorOrAnimated) {
            multicolor = true;
            animated = false;
            segmentLength = segmentLengthOrPeriod;
        }
        else {
            animated = true;
            multicolor = false;
            period = segmentLengthOrPeriod;
        }
    }
}
