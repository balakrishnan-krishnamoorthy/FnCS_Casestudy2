package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class WarehouseResourceImplTest {

    @Inject
    WarehouseResourceImpl resource;

    @InjectMock
    WarehouseRepository warehouseRepository;

    @InjectMock
    CreateWarehouseOperation createWarehouseOperation;

    @InjectMock
    ArchiveWarehouseOperation archiveWarehouseOperation;

    @InjectMock
    ReplaceWarehouseOperation replaceWarehouseOperation;


    @Test
    void shouldListAllWarehouses() {

        Warehouse warehouse = createWarehouse(
                "MWH.001",
                "ZWOLLE-001",
                100,
                50
        );

        when(warehouseRepository.getAll())
                .thenReturn(List.of(warehouse));

        List<com.warehouse.api.beans.Warehouse> result =
                resource.listAllWarehousesUnits();

        assertEquals(1, result.size());
        assertEquals("MWH.001", result.get(0).getBusinessUnitCode());
        assertEquals("ZWOLLE-001", result.get(0).getLocation());
        assertEquals(100, result.get(0).getCapacity());
        assertEquals(50, result.get(0).getStock());
    }


    @Test
    void shouldCreateNewWarehouse() {

        com.warehouse.api.beans.Warehouse request =
                new com.warehouse.api.beans.Warehouse();

        request.setBusinessUnitCode("MWH.001");
        request.setLocation("ZWOLLE-001");
        request.setCapacity(100);
        request.setStock(50);

        com.warehouse.api.beans.Warehouse result =
                resource.createANewWarehouseUnit(request);

        assertEquals("MWH.001", result.getBusinessUnitCode());
        assertEquals("ZWOLLE-001", result.getLocation());
        assertEquals(100, result.getCapacity());
        assertEquals(50, result.getStock());

        verify(createWarehouseOperation)
                .create(any(Warehouse.class));
    }


    @Test
    void shouldGetWarehouseById() {

        Warehouse warehouse = createWarehouse(
                "MWH.001",
                "ZWOLLE-001",
                100,
                50
        );

        when(warehouseRepository.findWarehouseById(1L))
                .thenReturn(warehouse);

        com.warehouse.api.beans.Warehouse result =
                resource.getAWarehouseUnitByID("1");

        assertEquals("MWH.001", result.getBusinessUnitCode());
        assertEquals("ZWOLLE-001", result.getLocation());
        assertEquals(100, result.getCapacity());
        assertEquals(50, result.getStock());
    }


    @Test
    void shouldRejectWhenWarehouseDoesNotExist() {

        when(warehouseRepository.findWarehouseById(999L))
                .thenReturn(null);

        WebApplicationException exception =
                assertThrows(
                        WebApplicationException.class,
                        () -> resource.getAWarehouseUnitByID("999")
                );

        assertEquals(404, exception.getResponse().getStatus());

        assertEquals(
                "Warehouse with id of 999 does not exist.",
                exception.getMessage()
        );
    }


    @Test
    void shouldArchiveWarehouse() {

        Warehouse warehouse = createWarehouse(
                "MWH.001",
                "ZWOLLE-001",
                100,
                50
        );

        when(warehouseRepository.findWarehouseById(1L))
                .thenReturn(warehouse);

        resource.archiveAWarehouseUnitByID("1");

        verify(archiveWarehouseOperation)
                .archive(warehouse);
    }


    @Test
    void shouldRejectArchiveWhenWarehouseDoesNotExist() {

        when(warehouseRepository.findWarehouseById(999L))
                .thenReturn(null);

        WebApplicationException exception =
                assertThrows(
                        WebApplicationException.class,
                        () -> resource.archiveAWarehouseUnitByID("999")
                );

        assertEquals(404, exception.getResponse().getStatus());

        verify(archiveWarehouseOperation, never())
                .archive(any());
    }


    @Test
    void shouldReplaceWarehouse() {

        com.warehouse.api.beans.Warehouse request =
                new com.warehouse.api.beans.Warehouse();

        request.setLocation("ZWOLLE-002");
        request.setCapacity(200);
        request.setStock(100);

        com.warehouse.api.beans.Warehouse result =
                resource.replaceTheCurrentActiveWarehouse(
                        "MWH.001",
                        request
                );

        assertEquals("MWH.001", result.getBusinessUnitCode());
        assertEquals("ZWOLLE-002", result.getLocation());
        assertEquals(200, result.getCapacity());
        assertEquals(100, result.getStock());

        verify(replaceWarehouseOperation)
                .replace(any(Warehouse.class));
    }


    private Warehouse createWarehouse(
            String businessUnitCode,
            String location,
            int capacity,
            int stock) {

        Warehouse warehouse = new Warehouse();

        warehouse.businessUnitCode = businessUnitCode;
        warehouse.location = location;
        warehouse.capacity = capacity;
        warehouse.stock = stock;

        return warehouse;
    }
}