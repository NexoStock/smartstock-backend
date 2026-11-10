package com.nexostock.backend.iam.interfaces.acl;

import com.nexostock.backend.iam.infrastructure.persistence.jpa.AccountRepository;
import com.nexostock.backend.iam.infrastructure.persistence.jpa.BusinessRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Door of the IAM context: who must be notified for a business (A quién notificar). */
@Service
public class IamContextFacade {

    /** Contact of ONE business; phone can be null. */
    public record BusinessContact(Long businessId, String email, String phone) {}

    private final AccountRepository accounts;
    private final BusinessRepository businesses;

    public IamContextFacade(AccountRepository accounts, BusinessRepository businesses) {
        this.accounts = accounts;
        this.businesses = businesses;
    }

    @Transactional(readOnly = true)
    public Optional<BusinessContact> findContact(Long businessId) {
        var account = accounts.findFirstByBusinessIdOrderByIdAsc(businessId);
        var business = businesses.findById(businessId);
        if (account.isEmpty() || business.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(
                new BusinessContact(businessId, account.get().getEmail().value(), business.get().getContactPhone()));
    }
}
