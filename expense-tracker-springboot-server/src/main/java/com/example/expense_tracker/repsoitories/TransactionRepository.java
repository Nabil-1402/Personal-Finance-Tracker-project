package com.example.expense_tracker.repsoitories;

import com.example.expense_tracker.entities.Transaction;
import jakarta.websocket.server.PathParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findAllByUserIdOrderByTransactionDateDesc(int userId, Pageable pageable);
    List<Transaction> findAllByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            int userId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query(value = "SELECT DISTINCT YEAR(transaction_date) " +
    "FROM Transaction WHERE user_id = :userId ORDER BY YEAR(transaction_date) DESC", nativeQuery = true)
    List<Integer> findDistinctYear(@PathParam("userId") int userId);
}
