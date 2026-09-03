package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validators.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;

  private final WarehouseValidator warehouseValidator;

  public CreateWarehouseUseCase(
          WarehouseStore warehouseStore,
          LocationResolver locationResolver) {

    this.warehouseStore = warehouseStore;

    this.warehouseValidator =
            new WarehouseValidator(
                    warehouseStore,
                    locationResolver
            );
  }

  @Override
  public void create(Warehouse warehouse) {

    warehouseValidator.validate(warehouse);

    warehouseStore.create(warehouse);
  }
}