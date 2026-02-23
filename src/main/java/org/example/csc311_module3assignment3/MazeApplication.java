package org.example.csc311_module3assignment3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.stage.Stage;

import java.io.IOException;

public class MazeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        //creates two tabs for each maze1 + 2
        Tab tab1 = buildTab("maze.png",  "robot.png");
        Tab tab2 = buildTab("maze2.png", "robot.png");
        tab1.setText("Maze 1");
        tab2.setText("Maze 2");
        tabPane.getTabs().add(tab1);
        tabPane.getTabs().add(tab2);


        FXMLLoader fxmlLoader = new FXMLLoader(MazeApplication.class.getResource("maze.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Maze Application");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    //creates a tab with maze pane
    private Tab buildTab(String image, String filename) {
        Tab tab = new Tab();
        MazePane mazePane = new MazePane(image, filename);
        tab.setContent(mazePane.getLayout());
        //when tab selected, focus tab
        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                view.requestCanvasFocus();
            }
        });
        return tab;
    }
}
