package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveWarehouseUseCaseTest {

    @Test
    void shouldArchiveWarehouse() {

        FakeWarehouseStore warehouseStore = new FakeWarehouseStore();

        ArchiveWarehouseUseCase useCase =
                new ArchiveWarehouseUseCase(warehouseStore);

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 40;
        warehouse.stock = 10;

        assertNull(warehouse.archivedAt);

        useCase.archive(warehouse);

        // 1. Archive timestamp should be set
        assertNotNull(warehouse.archivedAt);

        // 2. Repository update should be called
        assertSame(warehouse, warehouseStore.updatedWarehouse);
    }

    static class FakeWarehouseStore implements WarehouseStore {

        Warehouse updatedWarehouse;

        @Override
        public void update(Warehouse warehouse) {
            updatedWarehouse = warehouse;
        }

        @Override
        public java.util.List<Warehouse> getAll() {
            return java.util.List.of();
        }

        @Override
        public void create(Warehouse warehouse) {
        }

        @Override
        public void remove(Warehouse warehouse) {
        }

        @Override
        public Warehouse findByBusinessUnitCode(String buCode) {
            return null;
        }

        @Override
        public Warehouse findWarehouseById(Long id) {
            return null;
        }
    }
}
