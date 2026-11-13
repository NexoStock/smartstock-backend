package com.nexostock.backend.iam.infrastructure.persistence.jpa;

import com.nexostock.backend.iam.domain.model.aggregates.Account;
import com.nexostock.backend.iam.domain.model.valueobjects.EmailAddress;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(EmailAddress email);

    Optional<Account> findFirstByBusinessIdOrderByIdAsc(Long businessId);

    boolean existsByEmail(EmailAddress email);
}
