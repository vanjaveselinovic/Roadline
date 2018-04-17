package com.vanjav.roadline;

/**
 * Created by Vanja on 2017-10-26.
 */

public class TreePointF extends PointF {
    public final int size;

    public TreePointF(float x, float y, int size) {
        super(x, y);
        this.size = size;
    }

    /*
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!TreePointF.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (x != ((TreePointF) obj).x) {
            return false;
        }
        if (y != ((TreePointF) obj).y) {
            return false;
        }
        if (size != ((TreePointF) obj).size) {
            return false;
        }
        return true;
    }
    */
}
