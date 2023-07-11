package com.zerobase.Account.repository;

import com.zerobase.Account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountUserRepository extends JpaRepository<AccountUser, Long> {
}
