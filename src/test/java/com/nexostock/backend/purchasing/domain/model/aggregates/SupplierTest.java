
package com.nexostock.backend.purchasing.domain.model.aggregates;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupplierTest {

    private Supplier createSupplier() {
        return new Supplier(
                1L,
                "Distribuidora Lima SAC",
                "20123456789",
                "ventas@proveedor.com",
                "987654321",
                "Av. Principal 123"
        );
    }

    @Test
    void shouldCreateSupplierSuccessfully() {
        Supplier supplier = createSupplier();

        assertEquals(1L, supplier.getBusinessId());
        assertEquals("Distribuidora Lima SAC", supplier.getName());
        assertEquals("20123456789", supplier.getTaxId());
        assertTrue(supplier.isActive());
    }

    @Test
    void shouldRejectEmptySupplierName() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Supplier(
                        1L, "", "20123456789",
                        "", "", ""
                )
        );
    }

    @Test
    void shouldRejectEmptyTaxId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Supplier(
                        1L, "Proveedor", "",
                        "", "", ""
                )
        );
    }

    @Test
    void shouldRejectInvalidBusinessId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Supplier(
                        0L, "Proveedor", "20123456789",
                        "", "", ""
                )
        );
    }

    @Test
    void shouldUpdateSupplierSuccessfully() {
        Supplier supplier = createSupplier();

        supplier.update(
                "Distribuidora Actualizada",
                "20987654321",
                "nuevo@proveedor.com",
                "999888777",
                "Lima"
        );

        assertEquals("Distribuidora Actualizada", supplier.getName());
        assertEquals("20987654321", supplier.getTaxId());
        assertEquals("nuevo@proveedor.com", supplier.getEmail());
    }

    @Test
    void shouldDeactivateSupplier() {
        Supplier supplier = createSupplier();

        supplier.deactivate();

        assertFalse(supplier.isActive());
    }

    @Test
    void invalidUpdateShouldPreservePreviousData() {
        Supplier supplier = createSupplier();

        assertThrows(
                IllegalArgumentException.class,
                () -> supplier.update(
                        "",
                        "20987654321",
                        "nuevo@proveedor.com",
                        "999888777",
                        "Lima"
                )
        );

        assertEquals("Distribuidora Lima SAC", supplier.getName());
        assertEquals("20123456789", supplier.getTaxId());
    }
}
