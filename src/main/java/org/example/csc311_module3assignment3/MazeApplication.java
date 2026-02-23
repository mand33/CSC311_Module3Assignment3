package org.example.csc311_module3assignment3;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

public class MazeApplication extends Application {

    @Override
    public void start(Stage stage) {

        TabPane tabPane = new TabPane();

        // create both maze tabs
        Tab tab1 = buildTab("maze.png",  "robot.png");
        Tab tab2 = buildTab("maze2.png", "robot.png");
        tab1.setText("Maze 1");
        tab2.setText("Maze 2");
        // tabsnot closable
        tab1.setClosable(false);
        tab2.setClosable(false);
        // tabs to pane
        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);
        //set up scene
        Scene scene = new Scene(tabPane);
        stage.setTitle("Maze");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // builds one tab containing a MazePane
    private Tab buildTab(String mazeFile, String robotFile) {
        Tab tab = new Tab();
        MazePane mazePane = new MazePane(mazeFile, robotFile);
        tab.setContent(mazePane.getLayout());

        //tab selected, focus on canvas
        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                mazePane.requestCanvasFocus();
            }
        });

        return tab;
    }

}
