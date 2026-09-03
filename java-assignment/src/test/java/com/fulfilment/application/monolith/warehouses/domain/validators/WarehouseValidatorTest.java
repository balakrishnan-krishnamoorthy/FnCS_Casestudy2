package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class WarehouseValidatorTest {

    @Test
    void shouldValidateSuccessfully() {

        WarehouseStore warehouseStore = mock(WarehouseStore.class);
        LocationResolver locationResolver = mock(LocationResolver.class);

        Location location = new Location(
                "ZWOLLE-001",
                3,
                100
        );
        location.maxNumberOfWarehouses = 3;
        location.maxCapacity = 100;

        Warehouse warehouse = createWarehouse("MWH.001", 50, 20);

        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(location);

        when(warehouseStore.getAll())
                .thenReturn(new ArrayList<>());

        WarehouseValidator validator =
                new WarehouseValidator(warehouseStore, locationResolver);

        assertDoesNotThrow(() -> validator.validate(warehouse));
    }

    @Test
    void shouldRejectDuplicateWarehouse() {

        WarehouseStore warehouseStore = mock(WarehouseStore.class);
        LocationResolver locationResolver = mock(LocationResolver.class);

        Warehouse warehouse = createWarehouse("MWH.001", 50, 20);

        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(warehouse);

        WarehouseValidator validator =
                new WarehouseValidator(warehouseStore, locationResolver);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(warehouse)
                );

        assertEquals(
                "Warehouse with business unit code MWH.001 already exists",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectWhenLocationDoesNotExist() {

        WarehouseStore warehouseStore = mock(WarehouseStore.class);
        LocationResolver locationResolver = mock(LocationResolver.class);

        Warehouse warehouse = createWarehouse("MWH.001", 50, 20);

        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(null);

        WarehouseValidator validator =
                new WarehouseValidator(warehouseStore, locationResolver);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(warehouse)
                );

        assertEquals(
                "Location does not exist: ZWOLLE-001",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectWhenMaximumWarehousesReached() {

        WarehouseStore warehouseStore = mock(WarehouseStore.class);
        LocationResolver locationResolver = mock(LocationResolver.class);

        Location location = new Location(
                "ZWOLLE-001",
                2,
                100
        );
        location.maxNumberOfWarehouses = 2;
        location.maxCapacity = 100;

        Warehouse warehouse = createWarehouse("MWH.003", 50, 20);

        Warehouse warehouse1 = createWarehouse("MWH.001", 50, 20);
        Warehouse warehouse2 = createWarehouse("MWH.002", 50, 20);

        when(warehouseStore.findByBusinessUnitCode("MWH.003"))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(location);

        when(warehouseStore.getAll())
                .thenReturn(List.of(warehouse1, warehouse2));

        WarehouseValidator validator =
                new WarehouseValidator(warehouseStore, locationResolver);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(warehouse)
                );

        assertEquals(
                "Maximum number of warehouses reached for location ZWOLLE-001",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectWhenCapacityExceedsLocationLimit() {

        WarehouseStore warehouseStore = mock(WarehouseStore.class);
        LocationResolver locationResolver = mock(LocationResolver.class);

        Location location = new Location(
                "ZWOLLE-001",
                5,
                100
        );
        location.maxNumberOfWarehouses = 5;
        location.maxCapacity = 100;

        Warehouse warehouse = createWarehouse("MWH.001", 150, 20);

        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(location);

        when(warehouseStore.getAll())
                .thenReturn(new ArrayList<>());

        WarehouseValidator validator =
                new WarehouseValidator(warehouseStore, locationResolver);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(warehouse)
                );

        assertEquals(
                "Warehouse capacity exceeds location maximum capacity",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectWhenStockExceedsCapacity() {

        WarehouseStore warehouseStore = mock(WarehouseStore.class);
        LocationResolver locationResolver = mock(LocationResolver.class);

        Location location = new Location(
                "ZWOLLE-001",
                5,
                100
        );
        location.maxNumberOfWarehouses = 5;
        location.maxCapacity = 100;

        Warehouse warehouse = createWarehouse("MWH.001", 50, 60);

        when(warehouseStore.findByBusinessUnitCode("MWH.001"))
                .thenReturn(null);

        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(location);

        when(warehouseStore.getAll())
                .thenReturn(new ArrayList<>());

        WarehouseValidator validator =
                new WarehouseValidator(warehouseStore, locationResolver);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validate(warehouse)
                );

        assertEquals(
                "Warehouse stock cannot exceed warehouse capacity",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectReplaceWhenOldWarehouseDoesNotExist() {

        WarehouseStore warehouseStore = mock(WarehouseStore.class);
        LocationResolver locationResolver = mock(LocationResolver.class);

        Warehouse newWarehouse = createWarehouse(
                "MWH.001", 50, 20
        );

        WarehouseValidator validator =
                new WarehouseValidator(warehouseStore, locationResolver);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validateReplace(
                                newWarehouse,
                                null
                        )
                );

        assertEquals(
                "Warehouse with business unit code MWH.001 does not exist",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectReplaceWhenStockDoesNotMatch() {

        WarehouseStore warehouseStore = mock(WarehouseStore.class);
        LocationResolver locationResolver = mock(LocationResolver.class);

        Warehouse oldWarehouse = createWarehouse(
                "MWH.001", 50, 20
        );

        Warehouse newWarehouse = createWarehouse(
                "MWH.001", 50, 30
        );

        WarehouseValidator validator =
                new WarehouseValidator(warehouseStore, locationResolver);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validateReplace(
                                newWarehouse,
                                oldWarehouse
                        )
                );

        assertEquals(
                "New warehouse stock must match the stock of the warehouse being replaced",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectReplaceWhenCapacityCannotAccommodateStock() {

        WarehouseStore warehouseStore = mock(WarehouseStore.class);
        LocationResolver locationResolver = mock(LocationResolver.class);

        Warehouse oldWarehouse = createWarehouse(
                "MWH.001", 50, 40
        );

        Warehouse newWarehouse = createWarehouse(
                "MWH.001", 30, 40
        );

        WarehouseValidator validator =
                new WarehouseValidator(warehouseStore, locationResolver);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> validator.validateReplace(
                                newWarehouse,
                                oldWarehouse
                        )
                );

        assertEquals(
                "New warehouse capacity cannot accommodate the existing stock",
                exception.getMessage()
        );
    }

    @Test
    void shouldValidateReplaceSuccessfully() {

        WarehouseStore warehouseStore = mock(WarehouseStore.class);
        LocationResolver locationResolver = mock(LocationResolver.class);

        Warehouse oldWarehouse = createWarehouse(
                "MWH.001", 50, 40
        );

        Warehouse newWarehouse = createWarehouse(
                "MWH.001", 60, 40
        );

        WarehouseValidator validator =
                new WarehouseValidator(warehouseStore, locationResolver);

        assertDoesNotThrow(
                () -> validator.validateReplace(
                        newWarehouse,
                        oldWarehouse
                )
        );
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
}