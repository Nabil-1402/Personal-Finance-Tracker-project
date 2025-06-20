package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import org.example.components.TransactionComponent;
import org.example.dialog.*;
import org.example.models.MonthlyFinance;
import org.example.models.Transaction;
import org.example.models.User;
import org.example.utils.SqlUtil;
import org.example.views.DashbordView;
import org.example.views.LoginView;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public class DashbordController {
    private final int recentTransactionSize = 10;

    private DashbordView dashbordView;
    private User user;

    private List<Transaction> recentTransactions, currentTransactionsByYear;

    private int currentPage;
    private int currentYear;


    public DashbordController(DashbordView dashbordView){
        this.dashbordView= dashbordView;
        currentYear = dashbordView.getYearComboBox().getValue();
        fetchUserData();
        initialize();
    }

    public void fetchUserData(){
        //load the loading animation
        dashbordView.getLoadingAnimationPane().setVisible(true);

        //remove all children from the dashbord view
        dashbordView.getRecenetTransactionBox().getChildren().clear();

        user = SqlUtil.getUserByEmail(dashbordView.getEmail());

        //get the transactions for the year
        currentTransactionsByYear = SqlUtil.getAllTransactionsByUserId(user.getId(), currentYear, null);
        calculateDistinctYears();
        calculateBalanceAndIncomeAndExpense();

        dashbordView.getTransactionTable().setItems(calculateMonthlyFinances());

        createRecentTransactionComponents();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(1000);
                    dashbordView.getLoadingAnimationPane().setVisible(false);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void calculateDistinctYears(){
        List<Integer> distinctYears = SqlUtil.getAllDistinctYears(user.getId());
        for(Integer integer: distinctYears){
            if(!dashbordView.getYearComboBox().getItems().contains(integer)){
                dashbordView.getYearComboBox().getItems().add(integer);
            }
        }
    }

    private void calculateBalanceAndIncomeAndExpense(){
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        if(currentTransactionsByYear != null){
            for(Transaction transaction: currentTransactionsByYear){
                BigDecimal transactionAmount = BigDecimal.valueOf(transaction.getTransactionAmount());
                if(transaction.getTransactionType().equalsIgnoreCase("income")){
                    totalIncome = totalIncome.add(transactionAmount);
                }else{
                    totalExpense = totalExpense.add(transactionAmount);
                }
            }
        }
        //round up to 2 decimal places
        totalIncome = totalIncome.setScale(2, RoundingMode.HALF_UP);
        totalExpense = totalExpense.setScale(2,RoundingMode.HALF_UP);

        BigDecimal currentBalance = totalIncome.subtract(totalExpense);
        currentBalance = currentBalance.setScale(2, RoundingMode.HALF_UP);

        //update view
        dashbordView.getTotalExpense().setText("£" + totalExpense);
        dashbordView.getTotalIncome().setText("£" + totalIncome);
        dashbordView.getCurrentBalance().setText("£" + currentBalance);

    }

    public void createRecentTransactionComponents(){
        recentTransactions = SqlUtil.getRecentTransactionByUserId(
                user.getId(),
                0,
                currentPage,
                recentTransactionSize
        );

        if(recentTransactions == null) return;

        for(Transaction transaction: recentTransactions){
            dashbordView.getRecenetTransactionBox().getChildren().add(
                    new TransactionComponent(this, transaction));

        }
    }

    private ObservableList<MonthlyFinance> calculateMonthlyFinances(){
        double[] incomeCounter = new double[12];
        double[] expenseCounter = new double[12];

        for(Transaction transaction: currentTransactionsByYear){
            LocalDate transactionDate = transaction.getTransactionDate();
            if(transaction.getTransactionType().equalsIgnoreCase("income")){
                incomeCounter[transactionDate.getMonth().getValue() - 1] += transaction.getTransactionAmount();
            }else{
                expenseCounter[transactionDate.getMonth().getValue() - 1] += transaction.getTransactionAmount();
            }
        }

        ObservableList<MonthlyFinance> monthlyFinances = FXCollections.observableArrayList();
        for(int i = 0; i <12 ; i++){
            MonthlyFinance monthlyFinance = new MonthlyFinance(
                    Month.of(i + 1).name(),
                    new BigDecimal(String.valueOf(incomeCounter[i])),
                    new BigDecimal(String.valueOf(expenseCounter[i]))
            );

            monthlyFinances.add(monthlyFinance);
        }

        return monthlyFinances;
    }

    private void initialize(){
        addMenuActions();
        addRecentTransactionActions();
        addComboBoxActions();
        addViewChartAction();
        addTableActions();
    }

    private void addMenuActions(){
        dashbordView.getCreateCategoryMenuItem().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                new CreateNewCatagoryDialog(user).showAndWait();
            }
        });

        dashbordView.getViewCategoriesMenuItem().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                new ViewOrEditTransactionCategoryDialog(user, DashbordController.this).showAndWait();
            }
        });

        dashbordView.getLogoutMenuItem().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                new LoginView().show();
            }
        });
    }

    private void addRecentTransactionActions(){
        dashbordView.getAddTransactionButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                new CreateOrEditTransactionDialog(DashbordController.this, false).showAndWait();
            }
        });
    }

    private void addComboBoxActions(){
        dashbordView.getYearComboBox().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                //update current year
                currentYear = dashbordView.getYearComboBox().getValue();

                //refresh  the data
                fetchUserData();
            }
        });
    }

    private void addViewChartAction(){
        dashbordView.getViewChartButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                new ViewChartDialog(user, dashbordView.getTransactionTable().getItems()).showAndWait();
            }
        });
    }

    private void addTableActions(){
        dashbordView.getTransactionTable().setRowFactory(new Callback<TableView<MonthlyFinance>, TableRow<MonthlyFinance>>() {
            @Override
            public TableRow<MonthlyFinance> call(TableView<MonthlyFinance> monthlyFinanceTableView) {
                TableRow<MonthlyFinance> row = new TableRow<>();
                row.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if(!row.isEmpty() && mouseEvent.getClickCount() == 2){
                            MonthlyFinance monthlyFinance = row.getItem();
                            new ViewTransactionsDialog(
                                    DashbordController.this,
                                    monthlyFinance.getMonth()
                                    ).showAndWait();
                        }
                    }
                });

                return row;
            }
        });
    }

    public User getUser(){
        return user;
    }

    public int getCurrentYear(){
        return currentYear;
    }
}
