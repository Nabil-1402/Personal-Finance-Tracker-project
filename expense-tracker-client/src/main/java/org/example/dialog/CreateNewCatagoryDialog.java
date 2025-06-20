package org.example.dialog;

import com.google.gson.JsonObject;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.example.models.User;
import org.example.utils.SqlUtil;
import org.example.utils.Utilities;

public class CreateNewCatagoryDialog extends CustomDialog{
    private TextField newCategoryTextField;
    private ColorPicker colourPicker;
    private Button createCatagoryButton;

    public CreateNewCatagoryDialog(User user){
        super(user);
        setTitle("Create New Catagory");
        getDialogPane().setContent(createDialogContentBox());
    }

    private VBox createDialogContentBox(){
        VBox dialogContentBox = new VBox(20);

        newCategoryTextField = new TextField();
        newCategoryTextField.setPromptText("Enter Category Name");
        newCategoryTextField.setTooltip(new Tooltip("Enter Category Name"));
        newCategoryTextField.getStyleClass().addAll("text-size-md", "field-background", "text-light-gray");

        colourPicker = new ColorPicker();
        colourPicker.getStyleClass().add("text-size-md");
        colourPicker.setMaxWidth(Double.MAX_VALUE);

        createCatagoryButton = new Button("Create");
        createCatagoryButton.getStyleClass().addAll("bg-light-blue", "text-size-mid", "text-white");
        createCatagoryButton.setMaxWidth(Double.MAX_VALUE);
        createCatagoryButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                //extract the data
                String categoryName = newCategoryTextField.getText();
                String colour = Utilities.getHexColourValue(colourPicker);
                System.out.println(colour);

                JsonObject userData = new JsonObject();
                userData.addProperty("id", user.getId());

                JsonObject transactionCategoryData = new JsonObject();
                transactionCategoryData.add("user", userData);
                transactionCategoryData.addProperty("categoryName", categoryName);
                transactionCategoryData.addProperty("categoryColour", colour);

                boolean postTransactionCategoryStatus = SqlUtil.postTransactionCategory(transactionCategoryData);
                if(postTransactionCategoryStatus){
                    Utilities.showAlertDialog(Alert.AlertType.INFORMATION,
                            "Success: create a Transaction Category");
                }else{
                    Utilities.showAlertDialog(Alert.AlertType.ERROR,
                            "Error: Failed to create Transaction Category");
                }
            }
        });

        dialogContentBox.getChildren().addAll(newCategoryTextField, colourPicker, createCatagoryButton);
        return dialogContentBox;
    }
}
