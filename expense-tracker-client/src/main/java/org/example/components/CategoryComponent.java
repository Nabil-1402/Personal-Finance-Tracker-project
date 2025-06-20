package org.example.components;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.example.controllers.DashbordController;
import org.example.models.TransactionCategory;
import org.example.utils.SqlUtil;
import org.example.utils.Utilities;

public class CategoryComponent extends HBox {
    private DashbordController dashbordController;
    private TransactionCategory transactionCategory;

    private TextField categoryTextField;
    private ColorPicker colourPicker;
    private Button editButton, saveButton, deleteButton;

    private boolean isEditing;

    public CategoryComponent(DashbordController dashbordController, TransactionCategory transactionCategory){
        this.dashbordController = dashbordController;
        this.transactionCategory = transactionCategory;

        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().addAll("rounded-border", "field-background", "padding-10px");

        categoryTextField = new TextField();
        categoryTextField.setText(transactionCategory.getCategoryName());
        categoryTextField.setMaxWidth(Double.MAX_VALUE);
        categoryTextField.setEditable(false);
        categoryTextField.getStyleClass().addAll("field-background", "text-size-md", "text-light-gray");

        colourPicker = new ColorPicker();
        colourPicker.setDisable(true);
        colourPicker.setValue(Color.valueOf(transactionCategory.getCategoryColour()));
        colourPicker.getStyleClass().addAll("text-size-sm");

        editButton = new Button("Edit");
        editButton.setMinWidth(50);
        editButton.getStyleClass().addAll("text-size-sm");
        editButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                handleToggle();
            }
        });

        saveButton = new Button("Save");
        saveButton.setMinWidth(50);
        saveButton.getStyleClass().addAll("text-size-sm");
        saveButton.setVisible(false);
        saveButton.setManaged(false);
        saveButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                handleToggle();

                //extract data
                String newCategoryName = categoryTextField.getText();
                String newCategoryColour = Utilities.getHexColourValue(colourPicker);

                //update database
                SqlUtil.putTransactionCategory(transactionCategory.getId(), newCategoryName, newCategoryColour);

                //refresh dashboard
                dashbordController.fetchUserData();
            }
        });


        deleteButton = new Button("Del");
        deleteButton.setMinWidth(50);
        deleteButton.getStyleClass().addAll("text-size-sm", "bg-light-red", "text-white");
        deleteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(!SqlUtil.deleteTransactionCategoryById(transactionCategory.getId())){
                    return;
                }

                //remove this component from dialog
                setVisible(false);
                setManaged(false);

                if(getParent() instanceof VBox){
                    ((VBox) getParent()).getChildren().remove(CategoryComponent.this);
                }
            }
        });


        getChildren().addAll(categoryTextField,colourPicker,editButton,saveButton,deleteButton);
    }

    private void handleToggle(){
        if(!isEditing){
            isEditing = true;

            //enable the category text field
            categoryTextField.setEditable(true);
            categoryTextField.setStyle("-fx-background-color: #fff; -fx-text-fill: #000");

            //enable the colour picker
            colourPicker.setDisable(false);

            //hide the edit button
            editButton.setVisible(false);
            editButton.setManaged(false);

            //display the save button
            saveButton.setVisible(true);
            saveButton.setManaged(true);
        }else{
            isEditing = false;

            //disable the category text field
            categoryTextField.setEditable(false);
            categoryTextField.setStyle("-fx-background-color: #515050; -fx-text-fill: #BEB9B9");

            //enable the colour picker
            colourPicker.setDisable(true);

            //display the edit button
            editButton.setVisible(true);
            editButton.setManaged(true);

            //hide the save button
            saveButton.setVisible(false);
            saveButton.setManaged(false);
        }
    }
}
