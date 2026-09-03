package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WarehouseRepositoryTest {

    @Inject
    WarehouseRepository warehouseRepository;

    @Test
    void shouldConvertDbWarehouseToWarehouse() {

        DbWarehouse dbWarehouse = new DbWarehouse();

        dbWarehouse.id = 100L;
        dbWarehouse.businessUnitCode = "TEST.BU.001";
        dbWarehouse.location = "TEST-LOCATION";
        dbWarehouse.capacity = 100;
        dbWarehouse.stock = 50;
        dbWarehouse.createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        dbWarehouse.archivedAt = LocalDateTime.of(2026, 2, 1, 10, 0);

        Warehouse warehouse = dbWarehouse.toWarehouse();

        assertNotNull(warehouse);

        assertEquals(
                "TEST.BU.001",
                warehouse.businessUnitCode
        );

        assertEquals(
                "TEST-LOCATION",
                warehouse.location
        );

        assertEquals(
                100,
                warehouse.capacity
        );

        assertEquals(
                50,
                warehouse.stock
        );

        assertEquals(
                dbWarehouse.createdAt,
                warehouse.createdAt
        );

        assertEquals(
                dbWarehouse.archivedAt,
                warehouse.archivedAt
        );
    }

    @Test
    @Transactional
    void shouldCreateWarehouse() {

        Warehouse warehouse = new Warehouse();

        warehouse.businessUnitCode = "TEST.CREATE.001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 200;
        warehouse.stock = 50;
        warehouse.createdAt = LocalDateTime.now();
        warehouse.archivedAt = null;

        warehouseRepository.create(warehouse);

        Warehouse result =
                warehouseRepository.findByBusinessUnitCode(
                        "TEST.CREATE.001"
                );

        assertNotNull(result);

        assertEquals(
                "TEST.CREATE.001",
                result.businessUnitCode
        );

        assertEquals(
                "ZWOLLE-001",
                result.location
        );

        assertEquals(
                200,
                result.capacity
        );

        assertEquals(
                50,
                result.stock
        );
    }

    @Test
    void shouldFindWarehouseByBusinessUnitCode() {

        Warehouse result =
                warehouseRepository.findByBusinessUnitCode(
                        "MWH.001"
                );

        assertNotNull(result);

        assertEquals(
                "MWH.001",
                result.businessUnitCode
        );

        assertEquals(
                "ZWOLLE-001",
                result.location
        );
    }

    @Test
    void shouldReturnNullWhenBusinessUnitCodeDoesNotExist() {

        Warehouse result =
                warehouseRepository.findByBusinessUnitCode(
                        "DOES.NOT.EXIST"
                );

        assertNull(result);
    }

    @Test
    void shouldFindWarehouseById() {

        Warehouse result =
                warehouseRepository.findWarehouseById(1L);

        assertNotNull(result);

        assertEquals(
                "MWH.001",
                result.businessUnitCode
        );

        assertEquals(
                "ZWOLLE-001",
                result.location
        );
    }

    @Test
    void shouldReturnNullWhenWarehouseIdDoesNotExist() {

        Warehouse result =
                warehouseRepository.findWarehouseById(999999L);

        assertNull(result);
    }

    @Test
    void shouldGetOnlyActiveWarehouses() {

        List<Warehouse> warehouses =
                warehouseRepository.getAll();

        assertFalse(warehouses.isEmpty());

        assertTrue(
                warehouses.stream()
                        .allMatch(w -> w.archivedAt == null)
        );

        assertTrue(
                warehouses.stream()
                        .anyMatch(
                                w -> "MWH.001".equals(w.businessUnitCode)
                        )
        );
    }

    @Test
    @Transactional
    void shouldUpdateExistingWarehouse() {

        Warehouse warehouse = new Warehouse();

        warehouse.businessUnitCode = "TEST.UPDATE.001";
        warehouse.location = "ORIGINAL-LOCATION";
        warehouse.capacity = 100;
        warehouse.stock = 20;
        warehouse.createdAt =
                LocalDateTime.of(2026, 1, 1, 10, 0);
        warehouse.archivedAt = null;

        warehouseRepository.create(warehouse);

        Warehouse updatedWarehouse = new Warehouse();

        updatedWarehouse.businessUnitCode = "TEST.UPDATE.001";
        updatedWarehouse.location = "UPDATED-LOCATION";
        updatedWarehouse.capacity = 500;
        updatedWarehouse.stock = 100;
        updatedWarehouse.createdAt =
                LocalDateTime.of(2026, 3, 1, 10, 0);
        updatedWarehouse.archivedAt = null;

        warehouseRepository.update(updatedWarehouse);

        Warehouse result =
                warehouseRepository.findByBusinessUnitCode(
                        "TEST.UPDATE.001"
                );

        assertNotNull(result);

        assertEquals(
                "TEST.UPDATE.001",
                result.businessUnitCode
        );

        assertEquals(
                "UPDATED-LOCATION",
                result.location
        );

        assertEquals(
                500,
                result.capacity
        );

        assertEquals(
                100,
                result.stock
        );

        assertEquals(
                LocalDateTime.of(2026, 3, 1, 10, 0),
                result.createdAt
        );
    }
    @Test
    @Transactional
    void shouldDoNothingWhenUpdatingUnknownWarehouse() {

        Warehouse warehouse = new Warehouse();

        warehouse.businessUnitCode = "UNKNOWN.UPDATE";
        warehouse.location = "UNKNOWN";
        warehouse.capacity = 100;
        warehouse.stock = 10;

        assertDoesNotThrow(
                () -> warehouseRepository.update(warehouse)
        );
    }

    @Test
    void shouldThrowExceptionWhenRemoveIsCalled() {

        Warehouse warehouse = new Warehouse();

        warehouse.businessUnitCode = "TEST.REMOVE";

        UnsupportedOperationException exception =
                assertThrows(
                        UnsupportedOperationException.class,
                        () -> warehouseRepository.remove(warehouse)
                );

        assertEquals(
                "Unimplemented method 'remove'",
                exception.getMessage()
        );
    }
}