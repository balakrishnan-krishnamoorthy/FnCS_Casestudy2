package com.fulfilment.application.monolith.fulfillment.adapter.out;

import com.fulfilment.application.monolith.fulfillment.domain.model.Fulfillment;
import com.fulfilment.application.monolith.fulfillment.port.FulfillmentStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class FulfillmentRepository
        implements PanacheRepository<Fulfillment>, FulfillmentStore {

    @Override
    public List<Fulfillment> findByStore(Long storeId) {
        return list("store.id", storeId);
    }

    @Override
    public List<Fulfillment> findByStoreAndProduct(
            Long storeId,
            Long productId) {

        return list(
                "store.id = ?1 and product.id = ?2",
                storeId,
                productId
        );
    }

    @Override
    public List<Fulfillment> findByWarehouse(Long warehouseId) {
        return list("warehouse.id", warehouseId);
    }

    @Override
    public void save(Fulfillment fulfillment) {
        persist(fulfillment);
    }
}