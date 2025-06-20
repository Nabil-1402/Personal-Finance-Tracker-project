package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.utils.ViewNavigator;
import org.example.views.DashbordView;
import org.example.views.LoginView;
import org.example.views.SignUpView;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception{
        stage.setTitle("Expense Tracker");
        ViewNavigator.setMainStage(stage);
        new LoginView().show();
    }
}