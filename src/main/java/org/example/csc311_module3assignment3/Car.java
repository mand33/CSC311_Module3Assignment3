package org.example.csc311_module3assignment3;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Car {

    public static final int RIGHT = 0;
    public static final int DOWN  = 1;
    public static final int LEFT  = 2;
    public static final int UP    = 3;

    private int x;
    private int y;
    private int heading;
    private int size = 20;

    public Car(int startX, int startY) {
        x = startX;
        y = startY;
        heading = RIGHT;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getSize() { return size; }

    public void setX(int newX) { x = newX; }
    public void setY(int newY) { y = newY; }

    public void setHeading(int newHeading) {
        heading = newHeading;
    }

    // draws the car at its current x, y position
    // rotates around the center based on heading direction
    public void draw(GraphicsContext gc) {

        // figure out the rotation angle in degrees
        double angle = 0;
        if (heading == RIGHT) angle = 0;
        if (heading == DOWN)  angle = 90;
        if (heading == LEFT)  angle = 180;
        if (heading == UP)    angle = 270;

        double cx = x + size / 2.0;
        double cy = y + size / 2.0;

        // save current state so the rotation only affects the car
        gc.save();

        // translate to center, rotate, translate back
        gc.translate(cx, cy);
        gc.rotate(angle);
        gc.translate(-cx, -cy);

        // car body (purple rectangle)
        gc.setFill(Color.rgb(100, 0, 180));
        gc.fillRect(x - 1, y + 5, 22, 10);

        // roof trapezoid using a polygon
        double[] roofX = { x + 2, x + 5, x + 16, x + 19 };
        double[] roofY = { y + 5, y,      y,       y + 5  };
        gc.fillPolygon(roofX, roofY, 4);

        // left window (green)
        gc.setFill(Color.rgb(50, 200, 50));
        gc.fillRect(x + 3, y + 1, 4, 4);

        // right window
        gc.fillRect(x + 10, y + 1, 6, 4);

        // wheels (black ovals)
        gc.setFill(Color.BLACK);
        gc.fillOval(x,      y + 12, 7, 7);
        gc.fillOval(x + 13, y + 12, 7, 7);

        // headlight (yellow dot on the front)
        gc.setFill(Color.YELLOW);
        gc.fillOval(x + 19, y + 7, 3, 3);

        // restore graphics state
        gc.restore();
    }

}
