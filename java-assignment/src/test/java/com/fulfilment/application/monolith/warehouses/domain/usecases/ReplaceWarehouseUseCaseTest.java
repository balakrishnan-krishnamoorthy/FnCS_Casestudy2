package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validators.WarehouseValidator;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@QuarkusTest
class ReplaceWarehouseUseCaseTest {

    @Test
    void shouldRejectWhenWarehouseDoesNotExist() {

        FakeWarehouseStore warehouseStore = new FakeWarehouseStore();

        Warehouse newWarehouse = new Warehouse();
        newWarehouse.businessUnitCode = "MWH.999";
        newWarehouse.stock = 10;
        newWarehouse.capacity = 20;

        WarehouseValidator validator = mock(WarehouseValidator.class);

        doThrow(new IllegalArgumentException(
                "Warehouse with business unit code MWH.999 does not exist"))
                .when(validator)
                .validateReplace(newWarehouse, null);

        ReplaceWarehouseUseCase useCase =
                new ReplaceWarehouseUseCase(warehouseStore, validator);

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.replace(newWarehouse)
        );

        assertNull(warehouseStore.updatedWarehouse);
        assertNull(warehouseStore.createdWarehouse);
    }

    @Test
    void shouldRejectWhenStockDoesNotMatch() {

        FakeWarehouseStore warehouseStore = new FakeWarehouseStore();

        Warehouse oldWarehouse = createWarehouse(
                "MWH.001", 40, 20
        );

        warehouseStore.existingWarehouse = oldWarehouse;

        Warehouse newWarehouse = createWarehouse(
                "MWH.001", 50, 10
        );

        WarehouseValidator validator = mock(WarehouseValidator.class);

        doThrow(new IllegalArgumentException(
                "New warehouse stock must match the stock of the warehouse being replaced"))
                .when(validator)
                .validateReplace(newWarehouse, oldWarehouse);

        ReplaceWarehouseUseCase useCase =
                new ReplaceWarehouseUseCase(warehouseStore, validator);

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.replace(newWarehouse)
        );

        assertNull(warehouseStore.updatedWarehouse);
        assertNull(warehouseStore.createdWarehouse);
    }

    @Test
    void shouldRejectWhenNewCapacityCannotAccommodateOldStock() {

        FakeWarehouseStore warehouseStore = new FakeWarehouseStore();

        Warehouse oldWarehouse = createWarehouse(
                "MWH.001", 40, 30
        );

        warehouseStore.existingWarehouse = oldWarehouse;

        Warehouse newWarehouse = createWarehouse(
                "MWH.001", 20, 30
        );

        WarehouseValidator validator = mock(WarehouseValidator.class);

        doThrow(new IllegalArgumentException(
                "New warehouse capacity cannot accommodate the existing stock"))
                .when(validator)
                .validateReplace(newWarehouse, oldWarehouse);

        ReplaceWarehouseUseCase useCase =
                new ReplaceWarehouseUseCase(warehouseStore, validator);

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.replace(newWarehouse)
        );

        assertNull(warehouseStore.updatedWarehouse);
        assertNull(warehouseStore.createdWarehouse);
    }

    @Test
    void shouldArchiveOldWarehouseAndCreateNewWarehouse() {

        FakeWarehouseStore warehouseStore = new FakeWarehouseStore();

        Warehouse oldWarehouse = createWarehouse(
                "MWH.001", 40, 20
        );

        warehouseStore.existingWarehouse = oldWarehouse;

        Warehouse newWarehouse = createWarehouse(
                "MWH.001", 50, 20
        );

        WarehouseValidator validator = mock(WarehouseValidator.class);

        ReplaceWarehouseUseCase useCase =
                new ReplaceWarehouseUseCase(warehouseStore, validator);

        assertNull(oldWarehouse.archivedAt);
        assertNull(newWarehouse.createdAt);

        useCase.replace(newWarehouse);

        // Old warehouse should be archived
        assertNotNull(oldWarehouse.archivedAt);

        // Old warehouse should be updated
        assertSame(oldWarehouse, warehouseStore.updatedWarehouse);

        // New warehouse should be created
        assertSame(newWarehouse, warehouseStore.createdWarehouse);

        // New warehouse should be active
        assertNull(newWarehouse.archivedAt);

        // New warehouse should have creation timestamp
        assertNotNull(newWarehouse.createdAt);
    }

    private Warehouse createWarehouse(
            String businessUnitCode,
            int capacity,
            int stock) {

        Warehouse warehouse = new Warehouse();

        warehouse.businessUnitCode = businessUnitCode;
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = capacity;
        warehouse.stock = stock;

        return warehouse;
    }

    static class FakeWarehouseStore implements WarehouseStore {

        Warehouse existingWarehouse;
        Warehouse updatedWarehouse;
        Warehouse createdWarehouse;

        List<Warehouse> warehouses = new ArrayList<>();

        @Override
        public List<Warehouse> getAll() {
            return warehouses;
        }

        @Override
        public void create(Warehouse warehouse) {
            createdWarehouse = warehouse;
        }

        @Override
        public void update(Warehouse warehouse) {
            updatedWarehouse = warehouse;
        }

        @Override
        public void remove(Warehouse warehouse) {
        }

        @Override
        public Warehouse findByBusinessUnitCode(String buCode) {
            if (existingWarehouse != null &&
                    buCode.equals(existingWarehouse.businessUnitCode)) {
                return existingWarehouse;
            }

            return null;
        }

        @Override
        public Warehouse findWarehouseById(Long id) {
            return null;
        }
    }
}