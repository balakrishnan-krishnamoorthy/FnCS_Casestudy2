package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CreateWarehouseUseCaseTest {

    @Test
    void shouldCreateWarehouseWhenDataIsValid() {

        FakeWarehouseStore warehouseStore = new FakeWarehouseStore();
        FakeLocationResolver locationResolver =
                new FakeLocationResolver(
                        new Location("ZWOLLE-001", 1, 40)
                );

        CreateWarehouseUseCase useCase =
                new CreateWarehouseUseCase(warehouseStore, locationResolver);

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.100";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 30;
        warehouse.stock = 10;

        useCase.create(warehouse);

        assertEquals(1, warehouseStore.createdWarehouses.size());
        assertSame(warehouse, warehouseStore.createdWarehouses.get(0));
    }

    @Test
    void shouldRejectDuplicateBusinessUnitCode() {

        FakeWarehouseStore warehouseStore = new FakeWarehouseStore();

        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "MWH.001";
        warehouseStore.existingWarehouse = existing;

        FakeLocationResolver locationResolver =
                new FakeLocationResolver(
                        new Location("ZWOLLE-001", 1, 40)
                );

        CreateWarehouseUseCase useCase =
                new CreateWarehouseUseCase(warehouseStore, locationResolver);

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 30;
        warehouse.stock = 10;

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.create(warehouse)
        );

        assertEquals(0, warehouseStore.createdWarehouses.size());
    }

    @Test
    void shouldRejectInvalidLocation() {

        FakeWarehouseStore warehouseStore = new FakeWarehouseStore();

        FakeLocationResolver locationResolver =
                new FakeLocationResolver(null);

        CreateWarehouseUseCase useCase =
                new CreateWarehouseUseCase(warehouseStore, locationResolver);

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.100";
        warehouse.location = "INVALID";
        warehouse.capacity = 30;
        warehouse.stock = 10;

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.create(warehouse)
        );
    }

    @Test
    void shouldRejectWhenMaximumWarehousesReached() {

        FakeWarehouseStore warehouseStore = new FakeWarehouseStore();

        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "MWH.001";
        existing.location = "ZWOLLE-001";

        warehouseStore.warehouses.add(existing);

        FakeLocationResolver locationResolver =
                new FakeLocationResolver(
                        new Location("ZWOLLE-001", 1, 40)
                );

        CreateWarehouseUseCase useCase =
                new CreateWarehouseUseCase(warehouseStore, locationResolver);

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.100";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 30;
        warehouse.stock = 10;

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.create(warehouse)
        );
    }

    @Test
    void shouldRejectCapacityAboveLocationMaximum() {

        FakeWarehouseStore warehouseStore = new FakeWarehouseStore();

        FakeLocationResolver locationResolver =
                new FakeLocationResolver(
                        new Location("ZWOLLE-001", 1, 40)
                );

        CreateWarehouseUseCase useCase =
                new CreateWarehouseUseCase(warehouseStore, locationResolver);

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.100";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 50;
        warehouse.stock = 10;

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.create(warehouse)
        );
    }

    @Test
    void shouldRejectStockAboveCapacity() {

        FakeWarehouseStore warehouseStore = new FakeWarehouseStore();

        FakeLocationResolver locationResolver =
                new FakeLocationResolver(
                        new Location("ZWOLLE-001", 1, 40)
                );

        CreateWarehouseUseCase useCase =
                new CreateWarehouseUseCase(warehouseStore, locationResolver);

        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.100";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 20;
        warehouse.stock = 30;

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.create(warehouse)
        );
    }

    // ---------- Fake implementations ----------

    static class FakeWarehouseStore implements WarehouseStore {

        List<Warehouse> warehouses = new ArrayList<>();
        List<Warehouse> createdWarehouses = new ArrayList<>();
        Warehouse existingWarehouse;

        @Override
        public List<Warehouse> getAll() {
            return warehouses;
        }

        @Override
        public void create(Warehouse warehouse) {
            createdWarehouses.add(warehouse);
            warehouses.add(warehouse);
        }

        @Override
        public void update(Warehouse warehouse) {
        }

        @Override
        public void remove(Warehouse warehouse) {
        }

        @Override
        public Warehouse findByBusinessUnitCode(String buCode) {
            if (existingWarehouse != null &&
                    existingWarehouse.businessUnitCode.equals(buCode)) {
                return existingWarehouse;
            }

            return warehouses.stream()
                    .filter(w -> buCode.equals(w.businessUnitCode))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Warehouse findWarehouseById(Long id) {
            return null;
        }
    }

    static class FakeLocationResolver implements LocationResolver {

        private final Location location;

        FakeLocationResolver(Location location) {
            this.location = location;
        }

        @Override
        public Location resolveByIdentifier(String identifier) {
            return location;
        }
    }
}