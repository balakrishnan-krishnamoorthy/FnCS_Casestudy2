package com.fulfilment.application.monolith.fulfillment.port;

import com.fulfilment.application.monolith.fulfillment.domain.model.Fulfillment;

import java.util.List;

public interface FulfillmentStore {

    List<Fulfillment> findByStore(Long storeId);

    List<Fulfillment> findByStoreAndProduct(
            Long storeId,
            Long productId
    );

    List<Fulfillment> findByWarehouse(Long warehouseId);

    void save(Fulfillment fulfillment);
}