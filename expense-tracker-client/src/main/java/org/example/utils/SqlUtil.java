package org.example.utils;

import com.google.gson.*;
import javafx.scene.control.Alert;
import org.example.models.Transaction;
import org.example.models.TransactionCategory;
import org.example.models.User;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqlUtil {
    //get
    public static User getUserByEmail(String userEmail){
        //authenticate email and password
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/user?email=" + userEmail,
                    ApiUtil.RequestMethod.GET, null
            );

            if(conn.getResponseCode() != 200){
                return null;
            }

            String userDataJson = ApiUtil.readApiResponse(conn);

            JsonObject jsonObject = JsonParser.parseString(userDataJson).getAsJsonObject();

            //extract the json data
            int id = jsonObject.get("id").getAsInt();
            String name = jsonObject.get("name").getAsString();
            String email = jsonObject.get("email").getAsString();
            String password = jsonObject.get("password").getAsString();
            LocalDateTime createdAt = new Gson().fromJson(jsonObject.get("created_at"), LocalDateTime.class);

            return new User(id, name, email, password, createdAt);


        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return null;
    }

    public static List<TransactionCategory> getAllTransactionCategoriesByUser(User user){
        List<TransactionCategory> categories = new ArrayList<>();
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction-category/user/" + user.getId(),
                    ApiUtil.RequestMethod.GET, null
            );

            if(conn.getResponseCode() != 200){
                System.out.println("Error(getAllTransactionCategoriesByUser): " + conn.getResponseCode());
            }

            String result = ApiUtil.readApiResponse(conn);
            JsonArray resultJsonArray = new JsonParser().parse(result).getAsJsonArray();

            for(JsonElement jsonElement: resultJsonArray){
                int categoryId = jsonElement.getAsJsonObject().get("id").getAsInt();
                String categoryName = jsonElement.getAsJsonObject().get("categoryName").getAsString();
                String categoryColour = jsonElement.getAsJsonObject().get("categoryColour").getAsString();

                categories.add(new TransactionCategory(categoryId, categoryName, categoryColour));
            }

            return categories;
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return null;
    }

    public static List<Transaction> getRecentTransactionByUserId(int userId, int startPage, int endPage, int size){
        List<Transaction> recentTransactions = new ArrayList<>();

        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction/recent/user/" + userId+
                            "?startPage=" + startPage +
                            "&endPage=" +endPage + "&size=" + size,
                    ApiUtil.RequestMethod.GET,
                    null
            );

            if(conn.getResponseCode() != 200){
                return null;
            }

            String results = ApiUtil.readApiResponse(conn);
            JsonArray resultJsonArray = new JsonParser().parse(results).getAsJsonArray();
            for(int i = 0; i < resultJsonArray.size(); i++){
                JsonObject transactionJsonObject = resultJsonArray.get(i).getAsJsonObject();
                int transactionId = transactionJsonObject.get("id").getAsInt();

                TransactionCategory transactionCategory = null;
                if(transactionJsonObject.has("transactionCategory")
                        && !transactionJsonObject.get("transactionCategory").isJsonNull()){
                    JsonObject transactionCategoryJsonObject = transactionJsonObject.get("transactionCategory").getAsJsonObject();
                    int transactionCategoryId = transactionCategoryJsonObject.get("id").getAsInt();
                    String transactionCategoryName = transactionCategoryJsonObject.get("categoryName").getAsString();
                    String transactionCategoryColour = transactionCategoryJsonObject.get("categoryColour").getAsString();

                    transactionCategory = new TransactionCategory(
                            transactionCategoryId,
                            transactionCategoryName,
                            transactionCategoryColour
                    );
                }

                String transactionName = transactionJsonObject.get("transactionName").getAsString();
                double transactionAmount = transactionJsonObject.get("transactionAmount").getAsDouble();
                LocalDate transactionDate = LocalDate.parse(transactionJsonObject.get("transactionDate").getAsString());
                String transactionType = transactionJsonObject.get("transactionType").getAsString();

                Transaction transaction = new Transaction(
                        transactionId,
                        transactionCategory,
                        transactionName,
                        transactionAmount,
                        transactionDate,
                        transactionType
                );

                recentTransactions.add(transaction);
            }
            return recentTransactions;
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return null;
    }

    public static List<Transaction> getAllTransactionsByUserId(int userId, int year, Integer month){
        List<Transaction> transactions = new ArrayList<>();

        HttpURLConnection conn = null;
        String apiPath = "/api/v1/transaction/user/" + userId + "?year=" + year;
        if(month != null){
            apiPath += "&month=" + month;
        }

        try{
            conn = ApiUtil.fetchApi(
                    apiPath,
                    ApiUtil.RequestMethod.GET,
                    null
            );

            if(conn.getResponseCode() != 200){
                return null;
            }

            String results = ApiUtil.readApiResponse(conn);
            JsonArray resultJson = new JsonParser().parse(results).getAsJsonArray();

            for(int i = 0; i < resultJson.size(); i++){
                JsonObject transactionJsonObject = resultJson.get(i).getAsJsonObject();
                int transactionId = transactionJsonObject.get("id").getAsInt();

                TransactionCategory transactionCategory = null;
                if(transactionJsonObject.has("transactionCategory") &&
                        !transactionJsonObject.get("transactionCategory").isJsonNull()){
                    JsonObject transactionCategoryJsonObj = transactionJsonObject.get("transactionCategory").getAsJsonObject();
                    int transactionCategoryId = transactionCategoryJsonObj.get("id").getAsInt();
                    String transactionCategoryName = transactionCategoryJsonObj.get("categoryName").getAsString();
                    String transactionCategoryColour = transactionCategoryJsonObj.get("categoryColour").getAsString();

                    transactionCategory = new TransactionCategory(
                            transactionCategoryId,
                            transactionCategoryName,
                            transactionCategoryColour
                    );
                }

                String transactionName = transactionJsonObject.get("transactionName").getAsString();
                Double transactionAmount = transactionJsonObject.get("transactionAmount").getAsDouble();
                LocalDate transactionDate = LocalDate.parse(transactionJsonObject.get("transactionDate").getAsString());
                String transactionType = transactionJsonObject.get("transactionType").getAsString();

                Transaction transaction = new Transaction(
                        transactionId,
                        transactionCategory,
                        transactionName,
                        transactionAmount,
                        transactionDate,
                        transactionType
                );

                transactions.add(transaction);
            }

            return transactions;
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return null;
    }

    public static List<Integer> getAllDistinctYears(int userId){
        List<Integer> distinctYears = new ArrayList<>();
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction/years/" + userId,
                    ApiUtil.RequestMethod.GET, null
            );

            if(conn.getResponseCode() != 200){
                System.out.println("Error(getAllTransactionCategoriesByUser): " + conn.getResponseCode());
            }

            String result = ApiUtil.readApiResponse(conn);
            JsonArray resultsArray = new JsonParser().parse(result).getAsJsonArray();

            for(int i = 0; i < resultsArray.size(); i++){
                int year = resultsArray.get(i).getAsInt();
                distinctYears.add(year);
            }

            return distinctYears;
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return null;
    }


    //post
    public static boolean postLoginUser(String email, String password){
        //authenticate email and password
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/user/login?email=" + email + "&password=" + password,
                    ApiUtil.RequestMethod.POST, null
            );

            if(conn.getResponseCode() != 200){
                return false;
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return true;
    }

    public static boolean postCreateUser(JsonObject userData){
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/user",
                    ApiUtil.RequestMethod.POST,
                    userData
            );

            if(conn.getResponseCode() != 200){
                return false; //failed to make an account
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }
        return true;//the account was successfully created and stored into our database

    }

    public static boolean postTransactionCategory(JsonObject transactionCategoryData){
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction-category",
                    ApiUtil.RequestMethod.POST,
                    transactionCategoryData
            );

            if(conn.getResponseCode() != 200){
                return false;
            }

            return true;
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return false;
    }

    public static boolean postTransaction(JsonObject transactionData){
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction",
                    ApiUtil.RequestMethod.POST,
                    transactionData
            );

            if(conn.getResponseCode() != 200){
                return false;
            }

            return true;
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return false;
    }

    //update
    public static boolean putTransaction(JsonObject newTransactionData){
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction",
                    ApiUtil.RequestMethod.PUT,
                    newTransactionData
            );

            if(conn.getResponseCode() != 200){
                System.out.println("Error(putTransaction): " + conn.getResponseCode());
                return false;
            }

            return true;
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return false;
    }

    public static boolean putTransactionCategory(int categoryId, String newCategoryName, String newCategoryColour){
        HttpURLConnection conn = null;

        String encodedCategoryName = URLEncoder.encode(newCategoryName, StandardCharsets.UTF_8);
        String encodedCategoryColour = URLEncoder.encode(newCategoryColour, StandardCharsets.UTF_8);

        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction-category/" + categoryId + "?newCategoryName=" + encodedCategoryName
                    + "&newCategoryColour=" + encodedCategoryColour,
                    ApiUtil.RequestMethod.PUT,
                    null
            );

            if(conn.getResponseCode() != 200){
                System.out.println("Error(putTransactionCategory): " + conn.getResponseCode());
                return false;
            }

            return true;
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return false;
    }

    //delete
    public static boolean deleteTransactionCategoryById(int categoryId){
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction-category/" + categoryId,
                    ApiUtil.RequestMethod.DELETE,
                    null
            );

            if(conn.getResponseCode() != 200){
                System.out.println("Error(deleteTransactionCategory): " + conn.getResponseCode());
                return false;
            }

            return true;
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return false;
    }

    public static boolean deleteTransactionById(int transactionId){
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction/" + transactionId,
                    ApiUtil.RequestMethod.DELETE,
                    null
            );

            if(conn.getResponseCode() != 200){
                System.out.println("Error(deleteTransactionById): " + conn.getResponseCode());
                return false;
            }

            return true;
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return false;
    }


}
