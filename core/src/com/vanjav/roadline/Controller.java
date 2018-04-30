package com.vanjav.roadline;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by vveselin on 11/10/2016.
 */

public class Controller {
    private LinkedList<PointF> linePoints, roadPoints;
    private float score;

    private int width, height;
    private float dpi;
    private float crossTime;
    private float lineWidth, roadWidth, outlineWidth;

    private Random random;

    /* zone 1 */
    private LinkedList<TreePointF> treePoints;
    private LinkedList<PointF> treePointsToRemove;
    private float biggestTreeWidth, instructionsWidth;
    private LinkedList<PointF> noTreeZone;

    public Controller (
            int width,
            int height,
            float density,
            int biggestTreeWidth,
            float roadWidth,
            float outlineWidth,
            float lineWidth,
            float instructionsWidth
    ) {
        linePoints = new LinkedList<PointF>();
        roadPoints = new LinkedList<PointF>();
        firstLinePointToKeep = 0;
        firstRoadPointToKeep = 0;
        score = 0;

        this.width = width;
        this.height = height;
        this.dpi = density*160f;

        float widthInInches = this.width/this.dpi;

        crossTime = widthInInches / 2.75f;       // 2.75 inches/s

        this.roadWidth = roadWidth;       // 0.5 inches
        this.outlineWidth = outlineWidth;      // 0.1 of road
        this.lineWidth = lineWidth;  // 1.75 of road

        this.biggestTreeWidth = biggestTreeWidth;
        this.instructionsWidth = instructionsWidth;

        noTreeZone = new LinkedList<PointF>();
        noTreeZone.add(new PointF(width/2 - instructionsWidth/2 - biggestTreeWidth/2, height/2 - roadWidth));
        noTreeZone.add(new PointF(width/2 + instructionsWidth/2 + biggestTreeWidth/2, height/2 - roadWidth));

        random = new Random();

        /* zone 1* */
        treePoints = new LinkedList<TreePointF>();
        treePointsToRemove = new LinkedList<PointF>();

        initRoad();
        genRoad();

        for (int treeIndex = 0; treeIndex < treePoints.size(); treeIndex++) {
            if (isPointOnLineArray(treePoints.get(treeIndex).x, treePoints.get(treeIndex).y, noTreeZone, outlineWidth)) {
                treePointsToRemove.add(treePoints.get(treeIndex));
            }
        }
        treePoints.removeAll(treePointsToRemove);
        treePointsToRemove.clear();
    }

    public LinkedList<PointF> getLinePoints() {
        return linePoints;
    }

    public LinkedList<PointF> getRoadPoints() {
        return roadPoints;
    }

    public LinkedList<TreePointF> getTreePoints() {
        return treePoints;
    }

    public void addLinePoint(float x, float y) {
        linePoints.add(new PointF(x, y));
    }

    public float getScore() {
        return score;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public float getRoadWidth() {
        return roadWidth;
    }

    public float getOutlineWidth() { return outlineWidth; }

    private void updateScore(float numPixels) {
        score += numPixels/dpi;
    }

    private void initRoad() {
        roadPoints.add(new PointF(0, height/2));
        genItems(0, height/2);

        roadPoints.add(new PointF(width/5, height/2));
        genItems(width/5, height/2);

        roadPoints.add(new PointF(2*width/5, height/2));
        genItems(2*width/5, height/2);

        roadPoints.add(new PointF(3*width/5, height/2));
        genItems(3*width/5, height/2);

        roadPoints.add(new PointF(4*width/5, height/2));
        genItems(4*width/5, height/2);

        roadPoints.add(new PointF(width, height/2));
        genItems(width, height/2);

        roadPoints.add(new PointF(width+width/2, height/2));
        genItems(width+width/2, height/2);
    }

    private PointF lastPointInRoad;
    private float plusMinus, genX, genY;

    private void genRoad() {
        lastPointInRoad = roadPoints.getLast();

        while(lastPointInRoad.x < width*2) {
            plusMinus = random.nextInt(2);

            if (plusMinus == 0)
                plusMinus = -1;

            genX = random.nextFloat() * (lastPointInRoad.x+500-lastPointInRoad.x) + lastPointInRoad.x;
            genY = plusMinus*random.nextFloat() * (lastPointInRoad.y+250-lastPointInRoad.y)+lastPointInRoad.y;

            if (genY < roadWidth)
                genY = roadWidth;
            if (genY > height - height/8)
                genY = height - height/8;

            roadPoints.add(new PointF(genX, genY));
            lastPointInRoad = roadPoints.getLast();

            genItems(genX, genY);
        }
    }

    private int left, right;

    private int binarySearchTreePointListByY(LinkedList<TreePointF> list, float y) {
        if (list.isEmpty()) return 0;

        left = 0;
        right = list.size() - 1;

        while (right != left) {
            if (y > list.get(left + (right-left)/2).y)
                right = left + (right-left)/2;
            else
                left = left + (right-left)/2 + 1;
        }

        if (y < list.get(left).y) return left+1;
        else return left;
    }

    private float addX, addY;
    private int numItemsToAdd;
    private int positionToInsert;
    private int i;

    private void genItems(float x, float y) {
        //trees
        numItemsToAdd = random.nextInt(7);

        for (i = 0; i < numItemsToAdd; i++) {
            addX = x-width/2 + random.nextFloat()*width/2;
            addY = (float) (random.nextFloat()*1.1*height);

            if (!isPointOnOutline(addX, addY)) {
                positionToInsert = binarySearchTreePointListByY(treePoints, addY);
                treePoints.add(positionToInsert, new TreePointF(addX, addY, random.nextInt(8)));
            }
        }
    }

    private PointF prevPoint, currPoint;
    private float p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y;
    private float k, m;
    private int j;

    private boolean isPointOnRoad(float x, float y) {
        return isPointOnLineArray(x, y, roadPoints, roadWidth);
    }

    private boolean isPointOnOutline(float x, float y) {
        return isPointOnLineArray(x, y, roadPoints, outlineWidth);
    }

    private boolean isPointOnLineArray(float x, float y, LinkedList<PointF> points, float width) {
        for (j = 1; j < points.size(); j++) {
            prevPoint = points.get(j - 1);
            currPoint = points.get(j);

            if (x >= prevPoint.x - width && x <= currPoint.x + width) {
                if (currPoint.y - prevPoint.y == 0) {
                    p1x = prevPoint.x;
                    p1y = prevPoint.y - width / 2;
                    p2x = currPoint.x;
                    p2y = currPoint.y - width / 2;
                    p3x = currPoint.x;
                    p3y = currPoint.y + width / 2;
                    p4x = prevPoint.x;
                    p4y = prevPoint.y + width / 2;
                } else {
                    m = -1 * ((currPoint.x - prevPoint.x) / (currPoint.y - prevPoint.y));
                    k = (float) (width / (2 * Math.sqrt(1 + Math.pow(m, 2))));
                    p1x = prevPoint.x + k;
                    p1y = prevPoint.y + k * m;
                    p2x = currPoint.x + k;
                    p2y = currPoint.y + k * m;
                    p3x = currPoint.x - k;
                    p3y = currPoint.y - k * m;
                    p4x = prevPoint.x - k;
                    p4y = prevPoint.y - k * m;
                }

                float a, b, c, d1, d2, d3, d4;

                a = -1 * (p2y - p1y);
                b = p2x - p1x;
                c = -1 * (a * p1x + b * p1y);
                d1 = a * x + b * y + c;

                a = -1 * (p3y - p2y);
                b = p3x - p2x;
                c = -1 * (a * p2x + b * p2y);
                d2 = a * x + b * y + c;

                a = -1 * (p4y - p3y);
                b = p4x - p3x;
                c = -1 * (a * p3x + b * p3y);
                d3 = a * x + b * y + c;

                a = -1 * (p1y - p4y);
                b = p1x - p4x;
                c = -1 * (a * p4x + b * p4y);
                d4 = a * x + b * y + c;

                if ((d1 >= 0 && d2 >= 0 && d3 >= 0 && d4 >= 0) || (d1 <= 0 && d2 <= 0 && d3 <= 0 && d4 <= 0))
                    return true;

                if (Math.pow(x - prevPoint.x, 2) + Math.pow(y - prevPoint.y, 2) <= Math.pow(100, 2))
                    return true;
            }
        }

        return false;
    }

    private int firstLinePointToKeep, firstRoadPointToKeep;
    //private LinkedList<PointF> treePointsToRemove, flowerPointsToRemove;
    private float offsetX;
    private boolean pointToKeepFound = false;
    private int l;

    public boolean update(float deltaTimeSeconds) {
        genRoad();

        updateScore((float) width*(deltaTimeSeconds/crossTime));

        offsetX = -1*width*(deltaTimeSeconds/crossTime);

        if (roadPoints.size() > 0) {
            roadPoints.getFirst().offset(offsetX * (0.75f + 0.5f*((height - roadPoints.getFirst().y)/height)), 0);
            firstRoadPointToKeep = 0;

            for (l = 1; l < roadPoints.size(); l++) {
                currPoint = roadPoints.get(l);
                currPoint.offset(offsetX * (0.75f + 0.5f*((height - currPoint.y)/height)), 0);

                if (currPoint.x < 0 - outlineWidth)
                    firstRoadPointToKeep = l - 1;
            }

            roadPoints.subList(0, firstRoadPointToKeep).clear();
        }

        if (treePoints.size() > 0) {
            treePointsToRemove.clear();

            for (l = 0; l < treePoints.size(); l++) {
                currPoint = treePoints.get(l);
                currPoint.offset(offsetX * (0.75f + 0.5f*((height - currPoint.y)/height)), 0);

                if (currPoint.x < -1 * width - biggestTreeWidth)
                    treePointsToRemove.add(currPoint);
            }

            treePoints.removeAll(treePointsToRemove);
        }

        if (linePoints.size() > 0) {
            pointToKeepFound = false;

            linePoints.getFirst().offset(offsetX * (0.75f + 0.5f*((height - linePoints.getFirst().y)/height)), 0);
            firstLinePointToKeep = 0;

            for (l = 1; l < linePoints.size(); l++) {
                currPoint = linePoints.get(l);
                currPoint.offset(offsetX * (0.75f + 0.5f*((height - currPoint.y)/height)), 0);

                if (!pointToKeepFound) {
                    if (currPoint.x < 0 - lineWidth) {
                        firstLinePointToKeep = l - 1;
                    } else {
                        pointToKeepFound = true;
                    }
                }
            }

            linePoints.subList(0, firstLinePointToKeep).clear();

            return isPointOnRoad(linePoints.getLast().x, linePoints.getLast().y);
        }

        return false;
    }
}
