package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.fulfillment.adapter.out.FulfillmentRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.inject.Inject;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;

import org.junit.jupiter.api.AfterEach;

@QuarkusTest
class FulfillmentResourceTest {

    @Inject
    WarehouseRepository warehouseRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    FulfillmentRepository fulfillmentRepository;

    @AfterEach
    @Transactional
    void cleanup() {

        fulfillmentRepository.deleteAll();
        productRepository.delete("name like ?1", "FULFILLMENT.TEST.%");
        productRepository.delete("name like ?1", "PRODUCT.TEST.%");
    }


    @Test
    void shouldCreateFulfillment() {

        Long productId = createTestProduct("FULFILLMENT.TEST.001");

        createFulfillment(1, productId, 1, 201);
    }

    @Test
    void shouldRejectThirdWarehouseForSameStoreAndProduct() {
        // Store 3 + Product 3

        // Warehouse 1
        createFulfillment(3, 3, 1, 201);

        // Warehouse 2
        createFulfillment(3, 3, 2, 201);

        // Warehouse 3 -> should fail
        createFulfillment(3, 3, 3, 422);
    }



    @Test
    void shouldRejectFourthWarehouseForSameStore() {

        Long warehouse4Id = createTestWarehouse();
        // Store 2 -> Warehouse 1
        createFulfillment(2, 1, 1, 201);

        // Store 2 -> Warehouse 2
        createFulfillment(2, 2, 2, 201);

        // Store 2 -> Warehouse 3
        createFulfillment(2, 3, 3, 201);

        // Store 2 -> Warehouse 4
        // Fourth DIFFERENT warehouse -> should fail
        createFulfillment(2, 1, warehouse4Id, 422);
    }

    @Transactional
    Long createTestWarehouse() {

        DbWarehouse warehouse4 = new DbWarehouse();
        warehouse4.businessUnitCode = "MWH.TEST.004";
        warehouse4.location = "ZWOLLE-002";
        warehouse4.capacity = 30;
        warehouse4.stock = 5;
        warehouse4.createdAt = java.time.LocalDateTime.now();

        warehouseRepository.persist(warehouse4);
        return warehouse4.id;
    }

    private void createFulfillment(
            long storeId,
            long productId,
            long warehouseId,
            int expectedStatus) {

        given()
                .contentType("application/json")
                .body("""
                    {
                      "storeId": %d,
                      "productId": %d,
                      "warehouseId": %d
                    }
                    """.formatted(storeId, productId, warehouseId))
                .when()
                .post("/fulfillment")
                .then()
                .statusCode(expectedStatus);
    }

    @Test
    void shouldRejectSixthProductForSameWarehouse() {

        // Warehouse 1 -> Product 1
        createFulfillment(1, 1, 1, 201);

        // Warehouse 1 -> Product 2
        createFulfillment(1, 2, 1, 201);

        // Warehouse 1 -> Product 3
        createFulfillment(1, 3, 1, 201);

        // We need Product 4, Product 5 and Product 6
        Long product4Id = createTestProduct("PRODUCT.TEST.004");
        Long product5Id = createTestProduct("PRODUCT.TEST.005");
        Long product6Id = createTestProduct("PRODUCT.TEST.006");

        // Product 4
        createFulfillment(1, product4Id, 1, 201);

        // Product 5
        createFulfillment(1, product5Id, 1, 201);

        // Product 6 -> should fail
        createFulfillment(1, product6Id, 1, 422);
    }

    @Transactional
    Long createTestProduct(String name) {

        Product product = new Product(name);
        product.stock = 10;

        productRepository.persist(product);

        return product.id;
    }

    @Test
    void shouldRejectWhenStoreDoesNotExist() {

        createFulfillment(999, 1, 1, 404);
    }

    @Test
    void shouldRejectWhenProductDoesNotExist() {

        createFulfillment(1, 999, 1, 404);
    }
    @Test
    void shouldRejectWhenWarehouseDoesNotExist() {

        createFulfillment(1, 1, 999, 404);
    }

    @Test
    void shouldAllowExistingProductWhenWarehouseHasFiveProducts() {

        // Warehouse 1 -> Product 1
        createFulfillment(1, 1, 1, 201);

        // Warehouse 1 -> Product 2
        createFulfillment(1, 2, 1, 201);

        // Warehouse 1 -> Product 3
        createFulfillment(1, 3, 1, 201);

        // Product 4
        Long product4Id = createTestProduct("PRODUCT.TEST.004");
        createFulfillment(1, product4Id, 1, 201);

        // Product 5
        Long product5Id = createTestProduct("PRODUCT.TEST.005");
        createFulfillment(1, product5Id, 1, 201);

        // Product 1 already exists in Warehouse 1.
        // warehouseProductCount is already 5.
        // productAlreadyStored = true.
        //
        // Therefore, Rule 3 should NOT reject it.
        createFulfillment(1, 1, 1, 201);
    }
}