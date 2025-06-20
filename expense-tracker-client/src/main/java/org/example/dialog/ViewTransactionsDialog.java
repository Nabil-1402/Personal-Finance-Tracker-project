package org.example.dialog;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.example.components.TransactionComponent;
import org.example.controllers.DashbordController;
import org.example.models.Transaction;
import org.example.models.User;
import org.example.utils.SqlUtil;

import java.time.Month;
import java.util.List;

public class ViewTransactionsDialog extends CustomDialog{
    private DashbordController dashbordController;
    private String monthName;

    public ViewTransactionsDialog(DashbordController dashbordController, String monthName){
        super(dashbordController.getUser());
        this.dashbordController = dashbordController;
        this.monthName = monthName;

        setTitle("View Transactions");
        setWidth(815);
        setHeight(500);

        ScrollPane transactionScrollPane = createTransactionScrollPane();
        getDialogPane().setContent(transactionScrollPane);
    }

    private ScrollPane createTransactionScrollPane(){
        VBox vBox = new VBox(20);

        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setMinHeight(getHeight() - 40);
        scrollPane.setFitToWidth(true);

        List<Transaction> monthTransactions = SqlUtil.getAllTransactionsByUserId(
                dashbordController.getUser().getId(),
                dashbordController.getCurrentYear(),
                Month.valueOf(monthName).getValue()
        );

        if(monthTransactions != null){
            for(Transaction transaction: monthTransactions){
                TransactionComponent transactionComponent = new TransactionComponent(
                        dashbordController,
                        transaction
                );
                transactionComponent.getStyleClass().addAll("border-light-gray");

                vBox.getChildren().add(transactionComponent);
            }
        }

        return scrollPane;
    }
}
