//package com.fulfilment.application.monolith.warehouses.domain.usecases;
//
//import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
//import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
//import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
//import jakarta.enterprise.context.ApplicationScoped;
//import jakarta.transaction.Transactional;
//
//import java.time.LocalDateTime;
//
//@ApplicationScoped
//public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {
//
//  private final WarehouseStore warehouseStore;
//
//  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
//    this.warehouseStore = warehouseStore;
//  }
//
//  @Override
//  @Transactional
//  public void replace(Warehouse newWarehouse) {
//
//    Warehouse oldWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
//
//    if (oldWarehouse == null) {
//      throw new IllegalArgumentException("Warehouse with business unit code " + newWarehouse.businessUnitCode + " does not exist");
//    }
//
//    if (newWarehouse.stock == null || !newWarehouse.stock.equals(oldWarehouse.stock)) {
//      throw new IllegalArgumentException("New warehouse stock must match the stock of the warehouse being replaced");
//    }
//
//    if (newWarehouse.capacity == null || newWarehouse.capacity < oldWarehouse.stock) {
//      throw new IllegalArgumentException("New warehouse capacity cannot accommodate the existing stock");
//    }
//
//    // Archive old warehouse
//    oldWarehouse.archivedAt = LocalDateTime.now();
//    warehouseStore.update(oldWarehouse);
//
//    // Create new warehouse
//    newWarehouse.createdAt = LocalDateTime.now();
//    newWarehouse.archivedAt = null;
//    warehouseStore.create(newWarehouse);
//  }
//}

package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validators.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator warehouseValidator;

  public ReplaceWarehouseUseCase(
          WarehouseStore warehouseStore,
          WarehouseValidator warehouseValidator) {

    this.warehouseStore = warehouseStore;
    this.warehouseValidator = warehouseValidator;
  }

  @Override
  @Transactional
  public void replace(Warehouse newWarehouse) {

    Warehouse oldWarehouse =
            warehouseStore.findByBusinessUnitCode(
                    newWarehouse.businessUnitCode);

    warehouseValidator.validateReplace(
            newWarehouse,
            oldWarehouse);

    // Archive old warehouse
    oldWarehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(oldWarehouse);

    // Create new warehouse
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}