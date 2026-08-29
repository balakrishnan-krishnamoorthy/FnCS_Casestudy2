package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;

  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore ,LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {

    Warehouse existingWarehouse = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    //BU code duplicate?
    if (existingWarehouse != null) {
      throw new IllegalArgumentException("Warehouse with business unit code " + warehouse.businessUnitCode + " already exists");
    }

    //Location exists?
    Location location = locationResolver.resolveByIdentifier(warehouse.location);

    if (location == null) {
      throw new IllegalArgumentException("Location does not exist: " + warehouse.location);
    }

    //Max warehouse count?
    long warehouseCount = warehouseStore.getAll()
            .stream()
            .filter(w -> warehouse.location.equals(w.location))
            .count();

    if (warehouseCount >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException("Maximum number of warehouses reached for location " + warehouse.location);
    }

    //Capacity valid?
    if (warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException("Warehouse capacity exceeds location maximum capacity");
    }

    //Stock validation?
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Warehouse stock cannot exceed warehouse capacity");
    }

    // if all went well, create the warehouse
    warehouseStore.create(warehouse);
  }
}
