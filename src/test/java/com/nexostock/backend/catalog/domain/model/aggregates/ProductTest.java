
package com.nexostock.backend.catalog.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nexostock.backend.catalog.domain.model.commands.CreateProductCommand;
import com.nexostock.backend.catalog.domain.model.commands.UpdateProductCommand;
import com.nexostock.backend.catalog.domain.model.valueobjects.Money;
import com.nexostock.backend.catalog.domain.model.valueobjects.ProductName;
import com.nexostock.backend.catalog.domain.model.valueobjects.Sku;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductTest {

    private Product product() {
        return new Product(
                new CreateProductCommand(
                        1L,
                        new ProductName("Rice"),
                        new Sku("ARR-001"),
                        "food",
                        BigDecimal.ONE,
                        new Money(new BigDecimal("3.50")),
                        Money.zero(),
                        "",
                        0,
                        5,
                        50
                )
        );
    }

    @Test
    void invalidUpdateLeavesTheProductUntouched() {
        Product p = product();

        var invalid = new UpdateProductCommand(
                1L,
                10L,
                new ProductName("Changed"),
                new Sku("NEW-1"),
                "x",
                BigDecimal.ONE,
                Money.zero(),
                Money.zero(),
                "",
                90,
                10
        );

        assertThrows(
                IllegalStateException.class,
                () -> p.update(invalid)
        );

        assertEquals("Rice", p.getName().value());
        assertEquals("ARR-001", p.getSku().value());
        assertEquals(5, p.getMinimumThreshold());
    }

    @Test
    void nullSkuKeepsTheCurrentSku() {
        Product p = product();

        p.update(
                new UpdateProductCommand(
                        1L,
                        10L,
                        new ProductName("Rice 2"),
                        null,
                        "x",
                        BigDecimal.ONE,
                        Money.zero(),
                        Money.zero(),
                        "",
                        5,
                        50
                )
        );

        assertEquals("ARR-001", p.getSku().value());
        assertEquals("Rice 2", p.getName().value());
    }

    @Test
    void stockCannotGoNegative() {
        Product p = product();

        var error = assertThrows(
                IllegalStateException.class,
                () -> p.applyStock(-1)
        );

        assertEquals(
                "INSUFFICIENT_STOCK",
                error.getMessage()
        );

        assertEquals(0, p.getRegisteredStock());
    }

    @Test
    void stockCannotExceedMaximumCapacity() {
        Product p = product();

        var error = assertThrows(
                IllegalStateException.class,
                () -> p.applyStock(51)
        );

        assertEquals(
                "MAXIMUM_CAPACITY_EXCEEDED",
                error.getMessage()
        );

        assertEquals(0, p.getRegisteredStock());
    }

    @Test
    void stockCanReachMaximumCapacity() {
        Product p = product();

        p.applyStock(50);

        assertEquals(50, p.getRegisteredStock());
        assertEquals(50, p.getMaximumCapacity());
    }

    @Test
    void cannotReduceCapacityBelowCurrentStock() {
        Product p = product();

        p.applyStock(30);

        var invalid = new UpdateProductCommand(
                1L,
                10L,
                new ProductName("Changed"),
                new Sku("NEW-1"),
                "x",
                BigDecimal.ONE,
                Money.zero(),
                Money.zero(),
                "",
                5,
                20
        );

        var error = assertThrows(
                IllegalStateException.class,
                () -> p.update(invalid)
        );

        assertEquals(
                "MAXIMUM_CAPACITY_BELOW_CURRENT_STOCK",
                error.getMessage()
        );

        assertEquals("Rice", p.getName().value());
        assertEquals("ARR-001", p.getSku().value());
        assertEquals(30, p.getRegisteredStock());
        assertEquals(50, p.getMaximumCapacity());
    }
}
