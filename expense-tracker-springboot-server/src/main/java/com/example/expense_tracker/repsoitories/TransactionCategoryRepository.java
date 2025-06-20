package com.example.expense_tracker.repsoitories;

import com.example.expense_tracker.entities.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Integer> {
    List<TransactionCategory> findAllByUserId(int userId);
}
