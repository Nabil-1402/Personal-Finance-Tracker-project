package org.example.components;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import org.example.controllers.DashbordController;
import org.example.dialog.CreateOrEditTransactionDialog;
import org.example.models.Transaction;
import org.example.utils.SqlUtil;

public class TransactionComponent extends HBox {
    private Label transactionCategoryLabel, transactionNameLabel, transactionDateLabel, transactionAmountLabel;
    private Button editButton, deleteButton;

    private DashbordController dashbordController;
    private Transaction transaction;

    public TransactionComponent(DashbordController dashbordController, Transaction transaction){
        this.dashbordController = dashbordController;
        this.transaction = transaction;

        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().addAll("main-background", "rounded-border", "padding-10px");

        VBox categoryNameDateSection = createCategoryNameDateSection();
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);

        transactionAmountLabel = new Label("£" + transaction.getTransactionAmount());
        transactionAmountLabel.getStyleClass().add("text-size-md");
        if(transaction.getTransactionType().equalsIgnoreCase("expense")){
            transactionAmountLabel.getStyleClass().add("text-light-red");
        }else{
            transactionAmountLabel.getStyleClass().add("text-light-green");
        }

        HBox actionButtonSection = createActionButtons();

        getChildren().addAll(categoryNameDateSection, region, transactionAmountLabel, actionButtonSection);
    }

    private VBox createCategoryNameDateSection(){
        VBox categoryNameDateSection = new VBox();
        if(transaction.getTransactionCategory() == null){
            transactionCategoryLabel = new Label("Undefined");
            transactionCategoryLabel.getStyleClass().addAll("text-light-gray");

        }else{
            transactionCategoryLabel = new Label(transaction.getTransactionCategory().getCategoryName());
            transactionCategoryLabel.setTextFill(Paint.valueOf("#" + transaction.getTransactionCategory().getCategoryColour()));

        }

        transactionNameLabel = new Label(transaction.getTransactionName());
        transactionNameLabel.getStyleClass().addAll("text-light-gray", "text-size-md");

        transactionDateLabel = new Label(transaction.getTransactionDate().toString());
        transactionDateLabel.getStyleClass().addAll("text-light-gray");

        categoryNameDateSection.getChildren().addAll(transactionCategoryLabel, transactionNameLabel, transactionDateLabel);
        return categoryNameDateSection;
    }

    private HBox createActionButtons(){
        HBox actionButtonSection = new HBox(20);
        actionButtonSection.setAlignment(Pos.CENTER);

        editButton = new Button("Edit");
        editButton.getStyleClass().addAll("text-size-md", "rounded-border");
        editButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                new CreateOrEditTransactionDialog(dashbordController, TransactionComponent.this, true).showAndWait();
            }
        });

        deleteButton = new Button("Delete");
        deleteButton.getStyleClass().addAll("text-size-md", "rounded-border", "bg-light-red", "text-white");
        deleteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(!SqlUtil.deleteTransactionById(transaction.getId())){
                    return;
                }

                //remove the component from the dashbord
                setVisible(false);
                setManaged(false);
                if(getParent() instanceof VBox){
                    ((VBox) getParent()).getChildren().remove(TransactionComponent.this);
                }

                //refresh the dashboard
                dashbordController.fetchUserData();
            }
        });

        actionButtonSection.getChildren().addAll(editButton, deleteButton);
        return actionButtonSection;
    }

    public Transaction getTransaction(){
        return transaction;
    }

    public Label getTransactionCategoryLabel() {
        return transactionCategoryLabel;
    }

    public Label getTransactionNameLabel() {
        return transactionNameLabel;
    }

    public Label getTransactionDateLabel() {
        return transactionDateLabel;
    }

    public Label getTransactionAmountLabel() {
        return transactionAmountLabel;
    }
}
