package org.example.csc311_module3assignment3;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

//maze logic here
public class MazeController {


    public interface MazeListener {
        void onPositionChanged();
        void onStatusChanged(String message);
        void onExitReached();
    }

    private Image mazeImage;
    private PixelReader pixelReader;

    // x, y pos
    private int x;
    private int y;

    // direction
    private int currentDir;

    private int spriteSize = 18;
    private int moveSpeed = 2;
    //is true whennauto animate
    private boolean running = false;

    private Car car;
    private MazeListener listener;

    public MazeController(String mazeFile, MazeListener listener) {
        this.listener = listener;
        this.currentDir = Car.RIGHT;

        try {
            String path = "/org/example/csc311_module3assignment3/" + mazeFile;
            Image img = new Image(getClass().getResourceAsStream(path));
            mazeImage = img;
            pixelReader = mazeImage.getPixelReader();
        } catch (Exception e) {
            System.out.println("couldnt load maze image");
        }

        findStartPosition();
        car = new Car(x, y);
    }
    private void findStartPosition() {
        x = 3;
        y = 10;
        if (mazeImage == null) return;

        for (int row = 0; row < (int) mazeImage.getHeight(); row++) {
            if (isPathPixel(3, row)) {
                x = 3;
                y = row;
                return;
            }
        }
    }

    //checks if pixel is white, then can move
    public boolean isPathPixel(int px, int py) {
        if (pixelReader == null) return false;
        if (px < 0 || py < 0) return false;
        if (px >= (int) mazeImage.getWidth()) return false;
        if (py >= (int) mazeImage.getHeight()) return false;

        Color c = pixelReader.getColor(px, py);
        return c.getRed() > 0.78 && c.getGreen() > 0.78 && c.getBlue() > 0.78;
    }

    public boolean canGoTo(int newX, int newY) {
        int sz = spriteSize;
        boolean tl  = isPathPixel(newX,           newY);
        boolean tr  = isPathPixel(newX + sz - 1,  newY);
        boolean bl  = isPathPixel(newX,            newY + sz - 1);
        boolean br  = isPathPixel(newX + sz - 1,   newY + sz - 1);
        boolean mid = isPathPixel(newX + sz / 2,   newY + sz / 2);
        return tl && tr && bl && br && mid;
    }

    public void tryMove(int dx, int dy, int dir) {
        int steps = Math.abs(dx != 0 ? dx : dy);

        int stepX = 0;
        int stepY = 0;
        if (dx > 0) stepX =  1;
        if (dx < 0) stepX = -1;
        if (dy > 0) stepY =  1;
        if (dy < 0) stepY = -1;

        for (int i = 0; i < steps; i++) {
            int nx = x + stepX;
            int ny = y + stepY;
            if (canGoTo(nx, ny)) {
                x = nx;
                y = ny;
                car.setX(x);
                car.setY(y);
                car.setHeading(dir);
            } else {
                break;
            }
        }

        currentDir = dir;
        listener.onPositionChanged();
        checkExit();
    }


    private void checkExit() {
        if (mazeImage == null) return;
        if (x + spriteSize >= (int) mazeImage.getWidth() - 2) {
            running = false;
            listener.onStatusChanged("You made it out!");
            listener.onExitReached();
        }
    }

    // resets
    public void reset() {
        running = false;
        findStartPosition();
        car.setX(x);
        car.setY(y);
        car.setHeading(Car.RIGHT);
        currentDir = Car.RIGHT;
        listener.onPositionChanged();
        listener.onStatusChanged("Ready - use arrow keys to move");
    }


    public void doAutoStep() {
        if (!running) return;

        int leftDir  = turnLeft(currentDir);
        int rightDir = turnRight(currentDir);
        int backDir  = turnBack(currentDir);

        int[] tryOrder = { leftDir, currentDir, rightDir, backDir };

        for (int d : tryOrder) {
            int[] delta = getMoveDelta(d);
            int nx = x + delta[0];
            int ny = y + delta[1];

            if (canGoTo(nx, ny)) {
                currentDir = d;
                x = nx;
                y = ny;
                car.setX(x);
                car.setY(y);
                car.setHeading(d);
                listener.onPositionChanged();
                checkExit();
                return;
            }
        }

        // no valid move - stuck
        running = false;
        listener.onStatusChanged("Stuck! Press Reset to try again.");
    }

    public void setRunning(boolean val) { running = val; }
    public boolean isRunning() { return running; }

    // direction helpers
    private int turnLeft(int dir) {
        if (dir == Car.RIGHT) return Car.UP;
        if (dir == Car.UP)    return Car.LEFT;
        if (dir == Car.LEFT)  return Car.DOWN;
        return Car.RIGHT;
    }

    private int turnRight(int dir) {
        if (dir == Car.RIGHT) return Car.DOWN;
        if (dir == Car.DOWN)  return Car.LEFT;
        if (dir == Car.LEFT)  return Car.UP;
        return Car.RIGHT;
    }

    private int turnBack(int dir) {
        if (dir == Car.RIGHT) return Car.LEFT;
        if (dir == Car.LEFT)  return Car.RIGHT;
        if (dir == Car.UP)    return Car.DOWN;
        return Car.UP;
    }

    private int[] getMoveDelta(int dir) {
        if (dir == Car.RIGHT) return new int[]{ moveSpeed, 0 };
        if (dir == Car.LEFT)  return new int[]{ -moveSpeed, 0 };
        if (dir == Car.DOWN)  return new int[]{ 0, moveSpeed };
        if (dir == Car.UP)    return new int[]{ 0, -moveSpeed };
        return new int[]{ 0, 0 };
    }

    // getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getSpriteSize() { return spriteSize; }
    public int getMoveSpeed() { return moveSpeed; }
    public Car getCar() { return car; }
    public Image getMazeImage() { return mazeImage; }

}
