package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class FulfillmentRepository implements PanacheRepository<Fulfillment> {

    public List<Fulfillment> findByStore(Long storeId) {
        return list("store.id", storeId);
    }

    public List<Fulfillment> findByStoreAndProduct(Long storeId, Long productId) {
        return list(
                "store.id = ?1 and product.id = ?2",
                storeId,
                productId
        );
    }

    public List<Fulfillment> findByWarehouse(Long warehouseId) {
        return list("warehouse.id", warehouseId);
    }
}