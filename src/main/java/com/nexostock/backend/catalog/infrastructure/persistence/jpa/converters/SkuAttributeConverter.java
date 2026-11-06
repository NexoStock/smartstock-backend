
package com.nexostock.backend.catalog.infrastructure.persistence.jpa.converters;

import com.nexostock.backend.catalog.domain.model.valueobjects.Sku;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts Sku to a database column and back.
 */
@Converter
public class SkuAttributeConverter
        implements AttributeConverter<Sku, String> {

    @Override
    public String convertToDatabaseColumn(Sku attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public Sku convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new Sku(dbData);
    }
}
