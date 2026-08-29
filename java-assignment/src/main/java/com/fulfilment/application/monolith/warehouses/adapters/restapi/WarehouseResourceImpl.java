package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

    @Inject
    private WarehouseRepository warehouseRepository;

    @Inject
    private CreateWarehouseOperation createWarehouseOperation;

    @Inject
    private ArchiveWarehouseOperation archiveWarehouseOperation;

    @Inject
    private ReplaceWarehouseOperation replaceWarehouseOperation;

    @Override
    public List<Warehouse> listAllWarehousesUnits() {
        return warehouseRepository.getAll()
                .stream().map(this::toWarehouseResponse)
                .toList();
    }

    @Override
    public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
        var warehouse =
                new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();

        warehouse.businessUnitCode = data.getBusinessUnitCode();
        warehouse.location = data.getLocation();
        warehouse.capacity = data.getCapacity();
        warehouse.stock = data.getStock();

        createWarehouseOperation.create(warehouse);
        return toWarehouseResponse(warehouse);
    }

    @Override
    public Warehouse getAWarehouseUnitByID(String id) {

        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse
                = warehouseRepository.findWarehouseById(Long.valueOf(id));

        if (warehouse == null) {
            throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
        }
        return toWarehouseResponse(warehouse);
    }

    @Override
    public void archiveAWarehouseUnitByID(String id) {
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse =
                warehouseRepository.findWarehouseById(Long.valueOf(id));

        if (warehouse == null) {
            throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
        }
        archiveWarehouseOperation.archive(warehouse);
    }

    @Override
    public Warehouse replaceTheCurrentActiveWarehouse(
            String businessUnitCode, @NotNull Warehouse data) {
        var warehouse =
                new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();

        warehouse.businessUnitCode = businessUnitCode;
        warehouse.location = data.getLocation();
        warehouse.capacity = data.getCapacity();
        warehouse.stock = data.getStock();

        replaceWarehouseOperation.replace(warehouse);

        return toWarehouseResponse(warehouse);
    }

    private Warehouse toWarehouseResponse(
            com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
        var response = new Warehouse();
        response.setBusinessUnitCode(warehouse.businessUnitCode);
        response.setLocation(warehouse.location);
        response.setCapacity(warehouse.capacity);
        response.setStock(warehouse.stock);

        return response;
    }
}
