package org.example.csc311_module3assignment3;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MazeController {

    public interface MazeListener {
        void positionChanged();
        void statusChanged(String message);
        void exitReached();
    }

    private Image mazeImage;
    private PixelReader pixelReader;
    private String mazeFile;

    private int x;
    private int y;
    private int currentDir;
    public int getSpriteSize() {
        return car.getSize();
    }
    private int moveSpeed = 2;
    private boolean running = false;

    // Pre-computed BFS path for maze2 (avoids wall-follow spinning in open space)
    private List<int[]> bfsPath = null;
    private int bfsIndex = 0;

    private Car car;
    private MazeListener listener;

    public MazeController(String mazeFile, MazeListener listener) {
        this.listener = listener;
        this.mazeFile = mazeFile;
        this.currentDir = Car.RIGHT;

        try {
            String path = "/org/example/csc311_module3assignment3/" + mazeFile;
            mazeImage = new Image(getClass().getResourceAsStream(path));
            pixelReader = mazeImage.getPixelReader();
        } catch (Exception e) {
            System.out.println("Could not load maze: " + e.getMessage());
        }

        findStartPosition();
        car = new Car(x, y);
        currentDir = findOpenDirection();
        if (isMaze2()) {
            bfsPath = computeBfsPath(x, y);
        }
    }

    private boolean isMaze2() {
        return mazeFile != null && mazeFile.contains("maze2");
    }

    private void findStartPosition() {
        x = 3;
        y = 10;
        if (mazeImage == null) return;

        if (isMaze2()) {

            for (int row = 0; row < (int) mazeImage.getHeight(); row++) {
                for (int col = 0; col < (int) mazeImage.getWidth(); col++) {
                    Color c = pixelReader.getColor(col, row);
                    if (c.getRed() > 0.7 && c.getGreen() > 0.35 && c.getGreen() < 0.6 && c.getBlue() < 0.15) {

                        x = col + 25;
                        y = row;
                        return;
                    }
                }
            }
        } else {
            x = 0;
            y = 260;
        }
    }

    // maze1= white pixels are the path
    // maze2=anything not a blue poxel is walkable
    public boolean isPathPixel(int px, int py) {
        if (pixelReader == null) return false;
        if (px < 0 || py < 0) return false;
        if (px >= (int) mazeImage.getWidth()) return false;
        if (py >= (int) mazeImage.getHeight()) return false;

        Color c = pixelReader.getColor(px, py);

        if (isMaze2()) {

            // Purple exit square must be checked FIRST — its high blue value would
            // otherwise make it look like a blue wall, blocking the sprite from entering
            boolean isPurpleExit = c.getRed() > 0.35 && c.getGreen() < 0.15 && c.getBlue() > 0.35;
            if (isPurpleExit) return true;

            boolean isBlueWall =
                    c.getBlue() > 0.5 &&
                            c.getBlue() > c.getRed() * 1.5 &&
                            c.getBlue() > c.getGreen() * 1.5;

            return !isBlueWall;
        } else {
            return c.getRed() > 0.78 && c.getGreen() > 0.78 && c.getBlue() > 0.78;
        }
    }

    public boolean canGoTo(int newX, int newY) {
        int sz = car.getSize();
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
        listener.positionChanged();
        checkExit();
    }

    private void checkExit() {
        if (mazeImage == null) return;

        if (isMaze2()) {
            // exit = the purple square in the bottom right corner
            int sz = car.getSize();
            if (isPurple(x + sz / 2, y + sz / 2) ||
                    isPurple(x,          y         )  ||
                    isPurple(x + sz,     y         )  ||
                    isPurple(x,          y + sz    )  ||
                    isPurple(x + sz,     y + sz    )) {
                running = false;
                listener.statusChanged("You made it out!");
                listener.exitReached();
            }
        } else {
            // maze1: exit is the opening on the right edge
            if (x + car.getSize() >= (int) mazeImage.getWidth() - 2) {
                running = false;
                listener.statusChanged("You made it out!");
                listener.exitReached();
            }
        }
    }

    // purple: high red, very low green, high blue
    // tighter threshold so it doesn't confuse orange circle with purple square
    private boolean isPurple(int px, int py) {
        if (px < 0 || py < 0) return false;
        if (px >= (int) mazeImage.getWidth()) return false;
        if (py >= (int) mazeImage.getHeight()) return false;
        Color c = pixelReader.getColor(px, py);
        return c.getRed() > 0.35 && c.getGreen() < 0.15 && c.getBlue() > 0.35;
    }

    public void reset() {
        running = false;
        findStartPosition();
        car.setX(x);
        car.setY(y);
        car.setHeading(Car.RIGHT);
        currentDir = findOpenDirection();
        bfsPath = null;
        bfsIndex = 0;
        if (isMaze2()) {
            bfsPath = computeBfsPath(x, y);
        }
        listener.positionChanged();
        listener.statusChanged("Ready - use arrow keys to move");
    }

    private List<int[]> computeBfsPath(int startX, int startY) {
        Map<Long, Long> parent = new HashMap<>();
        Deque<long[]> queue = new ArrayDeque<>();
        long startKey = encode(startX, startY);
        parent.put(startKey, -1L);
        queue.add(new long[]{ startX, startY });
        int[][] deltas = { {moveSpeed,0},{-moveSpeed,0},{0,moveSpeed},{0,-moveSpeed} };
        long goalKey = -1;
        int goalX = -1, goalY = -1;

        outer:
        while (!queue.isEmpty()) {
            long[] cur = queue.poll();
            int cx = (int) cur[0], cy = (int) cur[1];
            int sz = car.getSize();
            int[][] checks = { {cx+sz/2,cy+sz/2},{cx,cy},{cx+sz,cy},{cx,cy+sz},{cx+sz,cy+sz} };
            for (int[] ch : checks) {
                if (isPurple(ch[0], ch[1])) {
                    goalKey = encode(cx, cy);
                    goalX = cx; goalY = cy;
                    break outer;
                }
            }
            for (int[] d : deltas) {
                int nx = cx + d[0], ny = cy + d[1];
                long nk = encode(nx, ny);
                if (!parent.containsKey(nk) && canGoTo(nx, ny)) {
                    parent.put(nk, encode(cx, cy));
                    queue.add(new long[]{ nx, ny });
                }
            }
        }

        if (goalX == -1) return null;

        List<int[]> path = new ArrayList<>();
        long cur = goalKey;
        while (cur != -1L) {
            int px = (int)(cur >> 16), py = (int)(cur & 0xFFFFL);
            path.add(0, new int[]{ px, py });
            Long p = parent.get(cur);
            cur = (p != null) ? p : -1L;
        }
        return path;
    }

    private long encode(int x, int y) {
        return ((long) x << 16) | (y & 0xFFFFL);
    }

    // looks at the 4 directions from the start and returns the first one that is open
    private int findOpenDirection() {
        int[] dirs = { Car.RIGHT, Car.DOWN, Car.UP, Car.LEFT };
        for (int d : dirs) {
            int[] delta = getMoveDelta(d);
            if (canGoTo(x + delta[0], y + delta[1])) {
                return d;
            }
        }
        return Car.RIGHT;
    }

    public void doAutoStep() {
        if (!running) return;

        if (isMaze2()) {
            // Follow  BFS path
            if (bfsPath == null || bfsIndex >= bfsPath.size()) {
                running = false;
                listener.statusChanged("Stuck! Press Reset to try again.");
                return;
            }
            int[] next = bfsPath.get(bfsIndex++);
            int nx = next[0], ny = next[1];
            // Determine heading from movement direction
            int dx = nx - x, dy = ny - y;
            int dir = currentDir;
            if (dx > 0) dir = Car.RIGHT;
            else if (dx < 0) dir = Car.LEFT;
            else if (dy > 0) dir = Car.DOWN;
            else if (dy < 0) dir = Car.UP;
            currentDir = dir;
            x = nx; y = ny;
            car.setX(x); car.setY(y);
            car.setHeading(dir);
            listener.positionChanged();
            checkExit();
            return;
        }

        // Maze1: left-hand wall-following
        int leftDir = turnLeft(currentDir);
        int rightDir = turnRight(currentDir);
        int backDir = turnBack(currentDir);
        int[] tryOrder = new int[]{ leftDir, currentDir, rightDir, backDir };

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
                listener.positionChanged();
                checkExit();
                return;
            }
        }

        running = false;
        listener.statusChanged("Stuck! Press Reset to try again.");
    }

    public void setRunning(boolean val) { running = val; }
    public boolean isRunning() { return running; }

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

    public int getX() { return x; }
    public int getY() { return y; }
    public int getMoveSpeed() { return moveSpeed; }
    public Car getCar() { return car; }
    public Image getMazeImage() { return mazeImage; }
}