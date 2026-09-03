package com.fulfilment.application.monolith.fulfillment.application;

import com.fulfilment.application.monolith.fulfillment.domain.model.Fulfillment;
import com.fulfilment.application.monolith.fulfillment.port.FulfillmentStore;
import com.fulfilment.application.monolith.fulfillment.validator.FulfillmentValidator;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class FulfillmentService {

    @Inject
    FulfillmentStore fulfillmentStore;

    @Inject
    WarehouseRepository warehouseRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    FulfillmentValidator fulfillmentValidator;

    @Transactional
    public Fulfillment create(
            Long storeId,
            Long productId,
            Long warehouseId) {

        Store store = Store.findById(storeId);

        if (store == null) {
            throw new WebApplicationException(
                    "Store not found",
                    404
            );
        }

        Product product =
                productRepository.findById(productId);

        if (product == null) {
            throw new WebApplicationException(
                    "Product not found",
                    404
            );
        }

        DbWarehouse warehouse =
                warehouseRepository.findById(warehouseId);

        if (warehouse == null) {
            throw new WebApplicationException(
                    "Warehouse not found",
                    404
            );
        }

        fulfillmentValidator.validateProductWarehouseLimit(
                storeId,
                productId
        );

        fulfillmentValidator.validateStoreWarehouseLimit(
                storeId,
                warehouseId
        );

        fulfillmentValidator.validateWarehouseProductLimit(
                warehouseId,
                productId
        );

        Fulfillment fulfillment =
                new Fulfillment(
                        store,
                        product,
                        warehouse
                );

        fulfillmentStore.save(fulfillment);

        return fulfillment;
    }
}