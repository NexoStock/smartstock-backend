package com.nexostock.backend.sales.integration;

import com.nexostock.backend.sales.infrastructure.persistence.jpa.SaleItemRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.notifications.enabled=false")
@ActiveProfiles("local")
class SalesMySqlIT {

    private static final ParameterizedTypeReference<Map<String, Object>> JSON =
            new ParameterizedTypeReference<>() {};

    @LocalServerPort
    private int port;

    @Autowired
    private SaleItemRepository saleItems;

    private RestClient api;
    private Long productId;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () ->
                "jdbc:mysql://localhost:3306/nexostock_sales_it"
                        + "?createDatabaseIfNotExist=true"
                        + "&serverTimezone=UTC"
                        + "&allowPublicKeyRetrieval=true"
                        + "&sslMode=DISABLED");

        properties.add("spring.datasource.username", () ->
                System.getenv().getOrDefault("TEST_MYSQL_USER", "root"));

        properties.add("spring.datasource.password", () ->
                System.getenv().getOrDefault("TEST_MYSQL_PASSWORD", ""));

        properties.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + port;
        RestClient anonymous = RestClient.create(baseUrl);

        Map<String, Object> session = anonymous.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "businessName", "Sales integration test",
                        "businessType", "bodega",
                        "email", UUID.randomUUID() + "@example.com",
                        "password", "Integration-Test-2026!"))
                .retrieve()
                .body(JSON);

        assertNotNull(session);
        assertNotNull(session.get("token"));

        api = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(
                        "Authorization", "Bearer " + session.get("token"))
                .build();

        productId = createProduct(10);
    }

    private Long createProduct(int stock) {
        Map<String, Object> product = api.post()
                .uri("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "nombre", "Producto de prueba",
                        "sku", "IT-" + UUID.randomUUID(),
                        "categoria", "Abarrotes",
                        "pesoUnitario", 1,
                        "precioVenta", 5,
                        "costoCompra", 3,
                        "stockRegistrado", stock,
                        "umbralMinimo", 2,
                        "capacidadMaxima", 100))
                .retrieve()
                .body(JSON);

        assertNotNull(product);
        return ((Number) product.get("id")).longValue();
    }

    private Map<String, Object> line(Long id, int quantity) {
        return Map.of(
                "productoId", id,
                "cantidad", quantity,
                "precioUnitario", 999);
    }

    private ResponseEntity<Map<String, Object>> registerSale(
            String key, List<Map<String, Object>> lines) {
        return api.post()
                .uri("/api/ventas")
                .header("Idempotency-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("items", lines))
                .retrieve()
                .toEntity(JSON);
    }

    private Map<String, Object> get(String path) {
        Map<String, Object> response = api.get()
                .uri(path)
                .retrieve()
                .body(JSON);

        assertNotNull(response);
        return response;
    }

    private int stock(Long id) {
        return ((Number) get("/api/productos/" + id)
                .get("stockRegistrado")).intValue();
    }

    private int saleCount() {
        return ((List<?>) get("/api/ventas").get("ventas")).size();
    }

    private BigDecimal decimal(Object value) {
        return new BigDecimal(value.toString());
    }

    @Test
    void registersSaleUsingCatalogPriceAndDiscountsStock() {
        long itemsBefore = saleItems.count();

        var response = registerSale(
                UUID.randomUUID().toString(),
                List.of(line(productId, 2)));

        assertEquals(201, response.getStatusCode().value());
        assertNotNull(response.getBody());

        String code = response.getBody().get("id").toString();
        var sale = get("/api/ventas/" + code);

        assertEquals("COMPLETED", sale.get("estado"));
        assertEquals(0, new BigDecimal("10.00")
                .compareTo(decimal(sale.get("total"))));

        List<?> lines = (List<?>) sale.get("items");
        assertEquals(1, lines.size());

        Map<?, ?> item = (Map<?, ?>) lines.getFirst();
        assertEquals(2, ((Number) item.get("cantidad")).intValue());
        assertEquals(0, new BigDecimal("5.00")
                .compareTo(decimal(item.get("precioUnitario"))));

        assertEquals(8, stock(productId));
        assertEquals(1, saleCount());
        assertEquals(itemsBefore + 1, saleItems.count());
    }

    @Test
    void rejectsInsufficientStockWithoutPersistingSale() {
        long itemsBefore = saleItems.count();

        var error = assertThrows(RestClientResponseException.class, () ->
                registerSale(
                        UUID.randomUUID().toString(),
                        List.of(line(productId, 11))));

        assertEquals(422, error.getStatusCode().value());
        assertEquals(10, stock(productId));
        assertEquals(0, saleCount());
        assertEquals(itemsBefore, saleItems.count());
    }

    @Test
    void secondLineFailureRollsBackFirstLineAndEntireSale() {
        Long emptyProductId = createProduct(0);
        long itemsBefore = saleItems.count();

        var error = assertThrows(RestClientResponseException.class, () ->
                registerSale(
                        UUID.randomUUID().toString(),
                        List.of(
                                line(productId, 1),
                                line(emptyProductId, 1))));

        assertEquals(422, error.getStatusCode().value());
        assertEquals(10, stock(productId));
        assertEquals(0, stock(emptyProductId));
        assertEquals(0, saleCount());
        assertEquals(itemsBefore, saleItems.count());
    }

    @Test
    void repeatedKeyReturnsSameSaleWithoutAnotherStockDiscount() {
        String key = UUID.randomUUID().toString();
        var lines = List.of(line(productId, 2));
        long itemsBefore = saleItems.count();

        var first = registerSale(key, lines);
        var second = registerSale(key, lines);

        assertEquals(201, first.getStatusCode().value());
        assertEquals(201, second.getStatusCode().value());
        assertNotNull(first.getBody());
        assertNotNull(second.getBody());
        assertEquals(
                first.getBody().get("id"),
                second.getBody().get("id"));

        assertEquals(8, stock(productId));
        assertEquals(1, saleCount());
        assertEquals(itemsBefore + 1, saleItems.count());
    }
}