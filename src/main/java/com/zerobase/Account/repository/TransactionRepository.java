package com.zerobase.Account.repository;

import com.zerobase.Account.domain.Transaction;
import com.zerobase.Account.dto.UseBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
