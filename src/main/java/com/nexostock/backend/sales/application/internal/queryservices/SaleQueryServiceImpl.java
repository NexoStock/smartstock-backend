package com.nexostock.backend.sales.application.internal.queryservices;

import com.nexostock.backend.sales.application.queryservices.SaleQueryService;
import com.nexostock.backend.sales.domain.model.aggregates.Sale;
import com.nexostock.backend.sales.domain.model.queries.GetSaleByCodeQuery;
import com.nexostock.backend.sales.domain.model.queries.GetSalesQuery;
import com.nexostock.backend.sales.infrastructure.persistence.jpa.SaleRepository;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SaleQueryServiceImpl implements SaleQueryService {

    private final SaleRepository sales;

    public SaleQueryServiceImpl(SaleRepository sales) {
        this.sales = sales;
    }

    @Override
    public Result<List<Sale>, ApplicationError> handle(GetSalesQuery query) {
        return Result.success(
                sales.findByBusinessAndDates(
                        query.businessId(),
                        query.from(),
                        query.to()));
    }

    @Override
    public Result<Sale, ApplicationError> handle(GetSaleByCodeQuery query) {
        var sale = sales.findByBusinessIdAndCode(
                query.businessId(), query.code());

        if (sale.isEmpty()) {
            return Result.failure(
                    ApplicationError.notFound("SALE_NOT_FOUND"));
        }

        return Result.success(sale.get());
    }
}