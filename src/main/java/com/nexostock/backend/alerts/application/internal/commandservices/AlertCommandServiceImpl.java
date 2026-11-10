package com.nexostock.backend.alerts.application.internal.commandservices;

import com.nexostock.backend.alerts.application.commandservices.AlertCommandService;
import com.nexostock.backend.alerts.domain.model.aggregates.Alert;
import com.nexostock.backend.alerts.domain.model.aggregates.AlertType;
import com.nexostock.backend.alerts.domain.model.aggregates.OutboundMessage;
import com.nexostock.backend.alerts.domain.model.commands.EvaluateLowStockCommand;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.AlertRepository;
import com.nexostock.backend.alerts.infrastructure.persistence.jpa.OutboundMessageRepository;
import com.nexostock.backend.iam.interfaces.acl.IamContextFacade;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertCommandServiceImpl implements AlertCommandService {

    private final AlertRepository alerts;
    private final OutboundMessageRepository queue;
    private final IamContextFacade iam;

    public AlertCommandServiceImpl(
            AlertRepository alerts,
            OutboundMessageRepository queue,
            IamContextFacade iam) {
        this.alerts = alerts;
        this.queue = queue;
        this.iam = iam;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result<Boolean, ApplicationError> handle(
            EvaluateLowStockCommand command) {

        var open = alerts.findByBusinessIdAndProductIdAndTypeAndActiveTrue(
                command.businessId(),
                command.productId(),
                AlertType.LOW_STOCK);

        boolean low =
                command.referenceStock() <= command.minimumThreshold();

        if (!low) {
            open.ifPresent(alert -> {
                alert.resolve();
                alerts.save(alert);
            });
            return Result.success(false);
        }

        if (open.isPresent()) {
            open.get().refresh(command.referenceStock());
            alerts.save(open.get());
            return Result.success(false);
        }

        alerts.save(new Alert(
                command.businessId(),
                command.productId(),
                AlertType.LOW_STOCK,
                command.referenceStock()));

        String text = "Low stock: " + command.productName()
                + " has " + command.referenceStock()
                + " units (minimum " + command.minimumThreshold() + ").";

        iam.findContact(command.businessId()).ifPresent(contact -> {
            queue.save(new OutboundMessage(
                    command.businessId(),
                    "email",
                    contact.email(),
                    "NexoStock: low stock",
                    text));

            if (contact.phone() != null) {
                queue.save(new OutboundMessage(
                        command.businessId(),
                        "whatsapp",
                        contact.phone(),
                        null,
                        text));
            }
        });

        return Result.success(true);
    }
}