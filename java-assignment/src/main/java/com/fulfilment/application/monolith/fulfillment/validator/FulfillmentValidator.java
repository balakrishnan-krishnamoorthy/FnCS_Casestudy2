package com.fulfilment.application.monolith.fulfillment.validator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import com.fulfilment.application.monolith.fulfillment.port.FulfillmentStore;

@ApplicationScoped
public class FulfillmentValidator {


    @Inject
    FulfillmentStore fulfillmentStore;

    public void validateProductWarehouseLimit(Long storeId, Long productId) {

        long count = fulfillmentStore
                .findByStoreAndProduct(storeId, productId)
                .stream()
                .map(f -> f.warehouse.id)
                .distinct()
                .count();

        if (count >= 2) {
            throw new WebApplicationException(
                    "A product can be fulfilled by maximum 2 warehouses for a store",
                    422
            );
        }
    }

    public void validateStoreWarehouseLimit(Long storeId, Long warehouseId) {

        boolean warehouseAlreadyUsed = fulfillmentStore
                .findByStore(storeId)
                .stream()
                .anyMatch(f -> f.warehouse.id.equals(warehouseId));

        long storeWarehouseCount = fulfillmentStore
                .findByStore(storeId)
                .stream()
                .map(f -> f.warehouse.id)
                .distinct()
                .count();

        if (!warehouseAlreadyUsed && storeWarehouseCount >= 3) {
            throw new WebApplicationException(
                    "A store can be fulfilled by maximum 3 different warehouses",
                    422
            );
        }
    }

    public void validateWarehouseProductLimit(Long warehouseId, Long productId) {

        boolean productAlreadyStored = fulfillmentStore
                .findByWarehouse(warehouseId)
                .stream()
                .anyMatch(f -> f.product.id.equals(productId));

        long warehouseProductCount = fulfillmentStore
                .findByWarehouse(warehouseId)
                .stream()
                .map(f -> f.product.id)
                .distinct()
                .count();

        if (!productAlreadyStored && warehouseProductCount >= 5) {
            throw new WebApplicationException(
                    "A warehouse can store maximum 5 different products",
                    422
            );
        }
    }
}