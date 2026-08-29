package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path("fulfillment")
public class FulfillmentResource {

    @Inject
    FulfillmentRepository fulfillmentRepository;

    @Inject
    WarehouseRepository warehouseRepository;

    @Inject
    ProductRepository productRepository;


    @POST
    @Transactional
    public Response create(FulfillmentRequest request) {

        Store store = Store.findById(request.storeId);
        if (store == null) {
            throw new WebApplicationException("Store not found", 404);
        }

        Product product = productRepository.findById(request.productId);
        if (product == null) {
            throw new WebApplicationException("Product not found", 404);
        }

        DbWarehouse warehouse = warehouseRepository.findById(request.warehouseId);
        if (warehouse == null) {
            throw new WebApplicationException("Warehouse not found", 404);
        }

        //Rule 1
        long count = fulfillmentRepository
                .findByStoreAndProduct(request.storeId, request.productId)
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

        // Rule 2: One Store can have maximum 3 different Warehouses
        boolean warehouseAlreadyUsed = fulfillmentRepository
                .findByStore(request.storeId)
                .stream()
                .anyMatch(f -> f.warehouse.id.equals(request.warehouseId));

        long storeWarehouseCount = fulfillmentRepository
                .findByStore(request.storeId)
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

        // Rule 3: One Warehouse can store maximum 5 different Products
        boolean productAlreadyStored = fulfillmentRepository
                .findByWarehouse(request.warehouseId)
                .stream()
                .anyMatch(f -> f.product.id.equals(request.productId));

        long warehouseProductCount = fulfillmentRepository
                .findByWarehouse(request.warehouseId)
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

        Fulfillment fulfillment =
                new Fulfillment(store, product, warehouse);

        fulfillmentRepository.persist(fulfillment);

        return Response.status(201)
                .entity(fulfillment)
                .build();
    }



}