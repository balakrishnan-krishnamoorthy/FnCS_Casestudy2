package com.fulfilment.application.monolith.fulfillment.domain.model;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Fulfillment {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne
    public Store store;

    @ManyToOne
    public Product product;

    @ManyToOne
    public DbWarehouse warehouse;

    public Fulfillment() {
    }

    public Fulfillment(Store store, Product product, DbWarehouse warehouse) {
        this.store = store;
        this.product = product;
        this.warehouse = warehouse;
    }
}