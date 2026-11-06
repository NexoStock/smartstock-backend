
package com.nexostock.backend.shared.infrastructure.persistence.jpa.strategy;

import static io.github.encryptorcode.pluralize.Pluralize.pluralize;

import java.util.Locale;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Converts names to snake_case and table names to plural.
 * Convierte nombres a snake_case y las tablas al plural.
 */
public class SnakeCasePhysicalNamingStrategy
        implements PhysicalNamingStrategy {

    @Override
    public Identifier toPhysicalCatalogName(
            Identifier identifier,
            JdbcEnvironment env) {
        return identifier;
    }

    @Override
    public Identifier toPhysicalSchemaName(
            Identifier identifier,
            JdbcEnvironment env) {
        return toSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalTableName(
            Identifier identifier,
            JdbcEnvironment env) {
        return toSnakeCase(toPlural(identifier));
    }

    @Override
    public Identifier toPhysicalSequenceName(
            Identifier identifier,
            JdbcEnvironment env) {
        return toSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalColumnName(
            Identifier identifier,
            JdbcEnvironment env) {
        return toSnakeCase(identifier);
    }

    private Identifier toSnakeCase(Identifier identifier) {
        if (identifier == null) {
            return null;
        }

        String name = identifier.getText()
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT);

        return Identifier.toIdentifier(
                name, identifier.isQuoted());
    }

    private Identifier toPlural(Identifier identifier) {
        if (identifier == null) {
            return null;
        }

        return Identifier.toIdentifier(
                pluralize(identifier.getText()),
                identifier.isQuoted());
    }
}
