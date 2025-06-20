package org.example.dialog;

import com.google.gson.JsonObject;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.components.TransactionComponent;
import org.example.controllers.DashbordController;
import org.example.models.Transaction;
import org.example.models.TransactionCategory;
import org.example.models.User;
import org.example.utils.SqlUtil;
import org.example.utils.Utilities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CreateOrEditTransactionDialog extends CustomDialog{
    private List<TransactionCategory> transactionCategories;

   private TextField transactionNameField, transactionAmountField;
   private DatePicker transactionDatePicker;
   private ComboBox<String> transactionCategoryBox;
   private ToggleGroup transactionTypeToggleGroup;
   private TransactionComponent transactionComponent;
   private DashbordController dashbordController;
   private boolean isEditing;

    public CreateOrEditTransactionDialog(DashbordController dashbordController,
                                         TransactionComponent transactionComponent,
                                         boolean isEditing){
        super(dashbordController.getUser());
        this.isEditing = isEditing;
        this.transactionComponent = transactionComponent;
        this.dashbordController = dashbordController;

        setTitle(isEditing ? "Edit Transaction" : "Create New Transaction");
        setWidth(700);
        setHeight(595);

        transactionCategories = SqlUtil.getAllTransactionCategoriesByUser(user);

        VBox mainContentBox = createMainContenBox();
        getDialogPane().setContent(mainContentBox);

    }

    //use for creating transactions
    public CreateOrEditTransactionDialog(DashbordController dashbordController, boolean isEditing){
        this(dashbordController, null, isEditing);
    }


    private VBox createMainContenBox(){
        VBox mainContentBox = new VBox(30);
        mainContentBox.setAlignment(Pos.CENTER);

        transactionNameField = new TextField();
        transactionNameField.setPromptText("Enter Transaction Name" );
        transactionNameField.getStyleClass().addAll("field-background", "text-light-gray", "text-size-md");

        transactionAmountField = new TextField();
        transactionAmountField.setPromptText("Enter Transaction Amount" );
        transactionAmountField.getStyleClass().addAll("field-background", "text-light-gray", "text-size-md");

        transactionDatePicker = new DatePicker();
        transactionDatePicker.setPromptText("Enter Transaction Date" );
        transactionDatePicker.getStyleClass().addAll("field-background", "text-light-gray", "text-size-md");
        transactionDatePicker.setMaxWidth(Double.MAX_VALUE);

        transactionCategoryBox = new ComboBox<>();
        transactionCategoryBox.setPromptText("Choose Category");
        transactionCategoryBox.getStyleClass().addAll("field-background", "text-light-gray", "text-size-md");
        transactionCategoryBox.setMaxWidth(Double.MAX_VALUE);
        for(TransactionCategory transactionCategory: transactionCategories){
            transactionCategoryBox.getItems().add(transactionCategory.getCategoryName());
        }

        if(isEditing){
            Transaction transaction = transactionComponent.getTransaction();
            transactionNameField.setText(transaction.getTransactionName());
            transactionAmountField.setText(String.valueOf(transaction.getTransactionAmount()));
            transactionDatePicker.setValue(transaction.getTransactionDate());
            transactionCategoryBox.setValue(
                    transaction.getTransactionCategory() == null ? "" : transaction.getTransactionCategory().getCategoryName()
            );

        }


        mainContentBox.getChildren().addAll(transactionNameField, transactionAmountField, transactionDatePicker, transactionCategoryBox,
                createTransactionTypeRadioButtonGroup(), createConfirmAndCancelButtonsBox());
        return mainContentBox;
    }

    private HBox createTransactionTypeRadioButtonGroup(){
        HBox radioButtonsBox = new HBox(50);
        radioButtonsBox.setAlignment(Pos.CENTER);

        transactionTypeToggleGroup = new ToggleGroup();

        RadioButton incomeRadioButton = new RadioButton("Income");
        incomeRadioButton.setToggleGroup(transactionTypeToggleGroup);
        incomeRadioButton.getStyleClass().addAll("text-size-md", "text-light-gray");

        RadioButton expenseRadioButton = new RadioButton("Expense");
        expenseRadioButton.setToggleGroup(transactionTypeToggleGroup);
        expenseRadioButton.getStyleClass().addAll("text-size-md", "text-light-gray");

        if(isEditing){
            Transaction transaction = transactionComponent.getTransaction();
            if(transaction.getTransactionType().equalsIgnoreCase("income")){
                incomeRadioButton.setSelected(true);
            }else{
                expenseRadioButton.setSelected(true);
            }

        }

        radioButtonsBox.getChildren().addAll(incomeRadioButton, expenseRadioButton);
        return radioButtonsBox;
    }

    private HBox createConfirmAndCancelButtonsBox(){
        HBox confirmAndCancelBox = new HBox(50);
        confirmAndCancelBox.setAlignment(Pos.CENTER);

        Button saveButton = new Button("Save");
        saveButton.setPrefWidth(200);
        saveButton.getStyleClass().addAll("bg-light-blue", "text-white", "text-size-md", "rounded-border");
        saveButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                JsonObject transactionDataObject = new JsonObject();

                if(isEditing){
                    transactionDataObject.addProperty("id", transactionComponent.getTransaction().getId());
                }

                //extract the data from the nodes
                String transactionName = transactionNameField.getText();
                transactionDataObject.addProperty("transactionName", transactionName);

                Double transactionAmount = Double.parseDouble(transactionAmountField.getText());
                transactionDataObject.addProperty("transactionAmount", transactionAmount);

                LocalDate transactionDate = transactionDatePicker.getValue();
                transactionDataObject.addProperty("transactionDate", transactionDate.format(
                        DateTimeFormatter.ISO_LOCAL_DATE
                ));

                String transactionType = ((RadioButton) transactionTypeToggleGroup.getSelectedToggle()).getText();
                transactionDataObject.addProperty("transactionType", transactionType);


                String transactionCategoryName = transactionCategoryBox.getValue();
                if(transactionCategoryName != null){
                    TransactionCategory transactionCategory = Utilities.findTransactionCategoryByName(transactionCategories,
                            transactionCategoryName);

                    if(transactionCategory != null){
                        JsonObject transactionCategoryData = new JsonObject();
                        transactionCategoryData.addProperty("id", transactionCategory.getId());
                        transactionDataObject.add("transactionCategory", transactionCategoryData);
                    }
                }

                JsonObject userData = new JsonObject();
                userData.addProperty("id", user.getId());
                transactionDataObject.add("user", userData);

                //perform the post request to create the transaction
                if(!isEditing ? SqlUtil.postTransaction(transactionDataObject) :
                        SqlUtil.putTransaction(transactionDataObject)){
                    //display our message
                    Utilities.showAlertDialog(Alert.AlertType.INFORMATION,
                            isEditing ? "Successfully saved transaction" : "Successfully created transaction");
                    //refresh dashbord
                    dashbordController.fetchUserData();

                    //refresh the transaction component
                    if(isEditing){
                        transactionComponent.getTransactionCategoryLabel().setText(
                                transactionCategoryName.isEmpty() ? "Undefined" : transactionCategoryName);
                        transactionComponent.getTransactionNameLabel().setText(transactionName);
                        transactionComponent.getTransactionDateLabel().setText(transactionDate.toString());
                        transactionComponent.getTransactionAmountLabel().setText(String.valueOf(transactionAmount));
                    }
                }else{
                    Utilities.showAlertDialog(Alert.AlertType.ERROR,
                            isEditing ? "Error: failed to save transaction" : "Error: failed to create transaction...");
                }

            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefWidth(200);
        cancelButton.getStyleClass().addAll("text-size-md", "rounded-border");
        cancelButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                CreateOrEditTransactionDialog.this.close();
            }
        });

        confirmAndCancelBox.getChildren().addAll(saveButton, cancelButton);
        return confirmAndCancelBox;
    }


}
