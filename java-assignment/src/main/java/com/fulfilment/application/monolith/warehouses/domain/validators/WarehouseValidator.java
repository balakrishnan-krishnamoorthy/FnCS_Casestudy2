package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WarehouseValidator {

    private final WarehouseStore warehouseStore;
    private final LocationResolver locationResolver;

    public WarehouseValidator(
            WarehouseStore warehouseStore,
            LocationResolver locationResolver) {

        this.warehouseStore = warehouseStore;
        this.locationResolver = locationResolver;
    }

    public void validate(Warehouse warehouse) {

        // 1. Business Unit Code duplicate validation
        Warehouse existingWarehouse =
                warehouseStore.findByBusinessUnitCode(
                        warehouse.businessUnitCode
                );

        if (existingWarehouse != null) {
            throw new IllegalArgumentException(
                    "Warehouse with business unit code "
                            + warehouse.businessUnitCode
                            + " already exists"
            );
        }

        // 2. Location validation
        Location location =
                locationResolver.resolveByIdentifier(warehouse.location);

        if (location == null) {
            throw new IllegalArgumentException(
                    "Location does not exist: " + warehouse.location
            );
        }

        // 3. Maximum warehouse count for location
        long warehouseCount = warehouseStore.getAll()
                .stream()
                .filter(w -> warehouse.location.equals(w.location))
                .count();

        if (warehouseCount >= location.maxNumberOfWarehouses) {
            throw new IllegalArgumentException(
                    "Maximum number of warehouses reached for location "
                            + warehouse.location
            );
        }

        // 4. Warehouse capacity validation
        if (warehouse.capacity > location.maxCapacity) {
            throw new IllegalArgumentException(
                    "Warehouse capacity exceeds location maximum capacity"
            );
        }

        // 5. Stock cannot exceed warehouse capacity
        if (warehouse.stock > warehouse.capacity) {
            throw new IllegalArgumentException(
                    "Warehouse stock cannot exceed warehouse capacity"
            );
        }

    }

    public void validateReplace(Warehouse newWarehouse, Warehouse oldWarehouse) {

        if (oldWarehouse == null) {
            throw new IllegalArgumentException(
                    "Warehouse with business unit code "
                            + newWarehouse.businessUnitCode
                            + " does not exist");
        }

        if (newWarehouse.stock == null
                || !newWarehouse.stock.equals(oldWarehouse.stock)) {

            throw new IllegalArgumentException(
                    "New warehouse stock must match the stock of the warehouse being replaced");
        }

        if (newWarehouse.capacity == null
                || newWarehouse.capacity < oldWarehouse.stock) {

            throw new IllegalArgumentException(
                    "New warehouse capacity cannot accommodate the existing stock");
        }
    }
}