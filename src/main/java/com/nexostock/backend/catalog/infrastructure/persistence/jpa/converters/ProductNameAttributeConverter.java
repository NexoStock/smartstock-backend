
package com.nexostock.backend.catalog.infrastructure.persistence.jpa.converters;

import com.nexostock.backend.catalog.domain.model.valueobjects.ProductName;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts ProductName to a database column and back.
 */
@Converter
public class ProductNameAttributeConverter
        implements AttributeConverter<ProductName, String> {

    @Override
    public String convertToDatabaseColumn(ProductName attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public ProductName convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new ProductName(dbData);
    }
}
