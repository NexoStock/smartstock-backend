
package com.nexostock.backend.catalog.infrastructure.persistence.jpa.converters;

import com.nexostock.backend.catalog.domain.model.valueobjects.Money;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.math.BigDecimal;

/**
 * Converts Money to a decimal column and back.
 */
@Converter
public class MoneyAttributeConverter
        implements AttributeConverter<Money, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Money attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Money convertToEntityAttribute(BigDecimal dbData) {
        return dbData == null ? null : new Money(dbData);
    }
}
