package com.nexostock.backend.sales.application.queryservices;

import com.nexostock.backend.sales.domain.model.aggregates.Sale;
import com.nexostock.backend.sales.domain.model.queries.GetSaleByCodeQuery;
import com.nexostock.backend.sales.domain.model.queries.GetSalesQuery;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;

public interface SaleQueryService {

    Result<List<Sale>, ApplicationError> handle(GetSalesQuery query);

    Result<Sale, ApplicationError> handle(GetSaleByCodeQuery query);
}