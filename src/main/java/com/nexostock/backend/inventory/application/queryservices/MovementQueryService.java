
package com.nexostock.backend.inventory.application.queryservices;

import com.nexostock.backend.inventory.domain.model.aggregates.Movement;
import com.nexostock.backend.inventory.domain.model.queries.GetMovementsByProductQuery;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;

public interface MovementQueryService {

    Result<List<Movement>, ApplicationError> handle(
            GetMovementsByProductQuery query
    );
}
