package org.example.csc311_module3assignment3;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

// MazePane handles drawing and key input for one maze
// It creates a MazeController to handle all the logic
public class MazePane implements MazeController.MazeListener {

    private MazeController controller;

    private Canvas canvas;
    private GraphicsContext gc;

    private Image robotImage;
    private boolean useRobot = true;

    private Label statusLabel;

    private AnimationTimer animTimer;
    private boolean animRunning = false;
    private long lastAutoStep = 0;

    // the layout that gets placed inside the Tab
    private BorderPane layout;

    public MazePane(String mazeFile, String robotFile) {

        // load robot image from resources
        try {
            String path = "/org/example/csc311_module3assignment3/" + robotFile;
            robotImage = new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.out.println("Could not load robot image: " + e.getMessage());
        }

        // controller loads the maze image and handles all logic
        controller = new MazeController(mazeFile, this);

        // size canvas to match the maze image
        double w = 620;
        double h = 420;
        if (controller.getMazeImage() != null) {
            w = controller.getMazeImage().getWidth();
            h = controller.getMazeImage().getHeight();
        }

        canvas = new Canvas(w, h);
        gc = canvas.getGraphicsContext2D();

        // canvas needs focus so it can receive key events
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(e -> handleKey(e.getCode()));

        // status label at top
        statusLabel = new Label("Ready - use arrow keys to move");
        statusLabel.setStyle("-fx-padding: 5 8 5 8; -fx-font-size: 13px;");

        HBox buttonBar = buildButtonBar();

        Pane canvasPane = new Pane(canvas);

        layout = new BorderPane();
        layout.setTop(statusLabel);
        layout.setCenter(canvasPane);
        layout.setBottom(buttonBar);

        redraw();
    }

    // builds the row of buttons at the bottom
    private HBox buildButtonBar() {
        HBox bar = new HBox(10);
        bar.setStyle("-fx-padding: 6 8 6 8; -fx-background-color: #e0e0e0;");

        Label navLabel = new Label("Navigator:");

        RadioButton rbRobot = new RadioButton("Robot");
        RadioButton rbCar   = new RadioButton("Car");

        ToggleGroup group = new ToggleGroup();
        rbRobot.setToggleGroup(group);
        rbCar.setToggleGroup(group);
        rbRobot.setSelected(true);

        rbRobot.setFocusTraversable(false);
        rbCar.setFocusTraversable(false);

        rbRobot.setOnAction(e -> {
            useRobot = true;
            redraw();
            canvas.requestFocus();
        });

        rbCar.setOnAction(e -> {
            useRobot = false;
            redraw();
            canvas.requestFocus();
        });

        Button btnStart = new Button("Start Auto");
        Button btnStop  = new Button("Stop");
        Button btnReset = new Button("Reset");

        btnStart.setFocusTraversable(false);
        btnStop.setFocusTraversable(false);
        btnReset.setFocusTraversable(false);

        btnStart.setStyle("-fx-background-color: #3a9632; -fx-text-fill: white;");
        btnStop.setStyle("-fx-background-color: #b42d2d; -fx-text-fill: white;");

        btnStart.setOnAction(e ->{
            startAnimation();
            canvas.requestFocus();
        });

        btnStop.setOnAction(e ->{
            stopAnimation();
            canvas.requestFocus();
        });

        btnReset.setOnAction(e-> {
            stopAnimation();
            controller.reset();
            canvas.requestFocus();
        });

        bar.getChildren().addAll(navLabel, rbRobot, rbCar, btnStart, btnStop, btnReset);
        return bar;
    }

    // starts the auto timer
    public void startAnimation() {
        if (animRunning) return;
        animRunning = true;
        controller.reset();
        controller.setRunning(true);
        statusLabel.setText("Auto solving...");
        lastAutoStep = 0;

        animTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // step every ~15ms
                if (now - lastAutoStep >= 15_000_000L) {
                    lastAutoStep = now;
                    controller.doAutoStep();
                }
            }
        };
        animTimer.start();
    }

    // stops the animation timer
    public void stopAnimation() {
        animRunning = false;
        controller.setRunning(false);
        if (animTimer != null) {
            animTimer.stop();
        }
    }

    private void redraw() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        //sets up bg as maze images
        Image mazeImg = controller.getMazeImage();
        if (mazeImg != null) {
            gc.drawImage(mazeImg, 0, 0);
        }

        int x  = controller.getX();
        int y  = controller.getY();
        int sz = controller.getSpriteSize();

        // draw robot image or the drawn car
        if (useRobot) {
            if (robotImage != null) {
                gc.drawImage(robotImage, x, y, sz, sz);
            } else {
                gc.setFill(Color.RED);
                gc.fillOval(x, y, sz, sz);
            }
        } else {
            controller.getCar().draw(gc);
        }
    }

    //handles arrow key presses
    private void handleKey(KeyCode code) {
        int spd = controller.getMoveSpeed() * 2;

        if (code == KeyCode.RIGHT) controller.tryMove( spd,   0, Car.RIGHT);
        if (code == KeyCode.LEFT)  controller.tryMove(-spd,   0, Car.LEFT);
        if (code == KeyCode.DOWN)  controller.tryMove(   0, spd, Car.DOWN);
        if (code == KeyCode.UP)    controller.tryMove(   0,-spd, Car.UP);
    }

    @Override
    public void onPositionChanged() {
        redraw();
    }

    @Override
    public void onStatusChanged(String message) {
        statusLabel.setText(message);
    }

    @Override
    public void onExitReached() {
        stopAnimation();
    }

    // used by MazeApplication to put this view inside a Tab
    public BorderPane getLayout() {
        return layout;
    }

    // called when the tab is selected so arrow keys work immediately
    public void requestCanvasFocus() {
        canvas.requestFocus();
    }

}





