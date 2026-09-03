package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StoreResourceTest {

    @Inject
    StoreResource resource;


    @Test
    @Transactional
    void shouldGetAllStores() {

        Store store1 = new Store("ZZZ Test Store");
        store1.quantityProductsInStock = 10;
        store1.persist();

        Store store2 = new Store("AAA Test Store");
        store2.quantityProductsInStock = 20;
        store2.persist();

        List<Store> result = resource.get();

        assertFalse(result.isEmpty());

        // Verify our stores are present
        assertTrue(
                result.stream()
                        .anyMatch(store -> "AAA Test Store".equals(store.name))
        );

        assertTrue(
                result.stream()
                        .anyMatch(store -> "ZZZ Test Store".equals(store.name))
        );

        // Verify non-null names are sorted alphabetically
        List<String> names = result.stream()
                .map(store -> store.name)
                .filter(name -> name != null)
                .toList();

        for (int i = 1; i < names.size(); i++) {
            assertTrue(
                    names.get(i - 1).compareTo(names.get(i)) <= 0
            );
        }
    }


    @Test
    @Transactional
    void shouldGetSingleStore() {

        Store store = new Store("Single Test Store");
        store.quantityProductsInStock = 25;
        store.persist();

        Store result = resource.getSingle(store.id);

        assertNotNull(result);
        assertEquals(store.id, result.id);
        assertEquals("Single Test Store", result.name);
        assertEquals(25, result.quantityProductsInStock);
    }


    @Test
    void shouldRejectGetSingleWhenStoreDoesNotExist() {

        WebApplicationException exception =
                assertThrows(
                        WebApplicationException.class,
                        () -> resource.getSingle(999999L)
                );

        assertEquals(
                404,
                exception.getResponse().getStatus()
        );

        assertEquals(
                "Store with id of 999999 does not exist.",
                exception.getMessage()
        );
    }


    @Test
    @Transactional
    void shouldCreateStore() {

        Store store = new Store("Create Test Store");
        store.quantityProductsInStock = 50;

        Response response = resource.create(store);

        assertEquals(
                201,
                response.getStatus()
        );

        assertNotNull(store.id);

        Store savedStore = Store.findById(store.id);

        assertNotNull(savedStore);
        assertEquals(
                "Create Test Store",
                savedStore.name
        );

        assertEquals(
                50,
                savedStore.quantityProductsInStock
        );
    }


    @Test
    void shouldRejectCreateWhenIdIsAlreadySet() {

        Store store = new Store("Invalid Create Store");

        store.id = 123L;

        WebApplicationException exception =
                assertThrows(
                        WebApplicationException.class,
                        () -> resource.create(store)
                );

        assertEquals(
                422,
                exception.getResponse().getStatus()
        );

        assertEquals(
                "Id was invalidly set on request.",
                exception.getMessage()
        );
    }


    @Test
    @Transactional
    void shouldUpdateStore() {

        Store store = new Store("Old Test Store");
        store.quantityProductsInStock = 10;
        store.persist();

        Store updatedStore =
                new Store("Updated Test Store");

        updatedStore.quantityProductsInStock = 50;

        Store result =
                resource.update(
                        store.id,
                        updatedStore
                );

        assertEquals(
                "Updated Test Store",
                result.name
        );

        assertEquals(
                50,
                result.quantityProductsInStock
        );
    }


    @Test
    void shouldRejectUpdateWhenNameIsMissing() {

        Store updatedStore = new Store();

        updatedStore.quantityProductsInStock = 50;

        WebApplicationException exception =
                assertThrows(
                        WebApplicationException.class,
                        () -> resource.update(
                                999999L,
                                updatedStore
                        )
                );

        assertEquals(
                422,
                exception.getResponse().getStatus()
        );

        assertEquals(
                "Store Name was not set on request.",
                exception.getMessage()
        );
    }


    @Test
    void shouldRejectUpdateWhenStoreDoesNotExist() {

        Store updatedStore =
                new Store("Updated Missing Store");

        WebApplicationException exception =
                assertThrows(
                        WebApplicationException.class,
                        () -> resource.update(
                                999999L,
                                updatedStore
                        )
                );

        assertEquals(
                404,
                exception.getResponse().getStatus()
        );

        assertEquals(
                "Store with id of 999999 does not exist.",
                exception.getMessage()
        );
    }


    @Test
    @Transactional
    void shouldPatchStore() {

        Store store = new Store("Old Patch Store");
        store.quantityProductsInStock = 10;
        store.persist();

        Store updatedStore =
                new Store("Patched Store");

        updatedStore.quantityProductsInStock = 50;

        Store result =
                resource.patch(
                        store.id,
                        updatedStore
                );

        assertEquals(
                "Patched Store",
                result.name
        );

        assertEquals(
                50,
                result.quantityProductsInStock
        );
    }


    @Test
    @Transactional
    void shouldPatchStoreWithoutChangingEmptyFields() {

        Store store = new Store();

        store.name = null;
        store.quantityProductsInStock = 0;

        store.persist();

        Store updatedStore =
                new Store("New Patch Name");

        updatedStore.quantityProductsInStock = 100;

        Store result =
                resource.patch(
                        store.id,
                        updatedStore
                );

        // Existing name is null.
        // Therefore this condition is false:
        // if (entity.name != null)
        assertNull(result.name);

        // Existing quantity is 0.
        // Therefore this condition is false:
        // if (entity.quantityProductsInStock != 0)
        assertEquals(
                0,
                result.quantityProductsInStock
        );
    }


    @Test
    void shouldRejectPatchWhenNameIsMissing() {

        Store updatedStore = new Store();

        updatedStore.quantityProductsInStock = 50;

        WebApplicationException exception =
                assertThrows(
                        WebApplicationException.class,
                        () -> resource.patch(
                                999999L,
                                updatedStore
                        )
                );

        assertEquals(
                422,
                exception.getResponse().getStatus()
        );

        assertEquals(
                "Store Name was not set on request.",
                exception.getMessage()
        );
    }


    @Test
    void shouldRejectPatchWhenStoreDoesNotExist() {

        Store updatedStore =
                new Store("Patched Missing Store");

        WebApplicationException exception =
                assertThrows(
                        WebApplicationException.class,
                        () -> resource.patch(
                                999999L,
                                updatedStore
                        )
                );

        assertEquals(
                404,
                exception.getResponse().getStatus()
        );

        assertEquals(
                "Store with id of 999999 does not exist.",
                exception.getMessage()
        );
    }


    @Test
    @Transactional
    void shouldDeleteStore() {

        Store store =
                new Store("Delete Test Store");

        store.persist();

        Long id = store.id;

        Response response =
                resource.delete(id);

        assertEquals(
                204,
                response.getStatus()
        );

        assertNull(
                Store.findById(id)
        );
    }


    @Test
    void shouldRejectDeleteWhenStoreDoesNotExist() {

        WebApplicationException exception =
                assertThrows(
                        WebApplicationException.class,
                        () -> resource.delete(999999L)
                );

        assertEquals(
                404,
                exception.getResponse().getStatus()
        );

        assertEquals(
                "Store with id of 999999 does not exist.",
                exception.getMessage()
        );
    }


    @Test
    void shouldMapWebApplicationException() {

        StoreResource.ErrorMapper mapper =
                new StoreResource.ErrorMapper();

        mapper.objectMapper =
                new ObjectMapper();

        WebApplicationException exception =
                new WebApplicationException(
                        "Test error",
                        422
                );

        Response response =
                mapper.toResponse(exception);

        assertEquals(
                422,
                response.getStatus()
        );

        assertNotNull(
                response.getEntity()
        );
    }


    @Test
    void shouldMapGenericException() {

        StoreResource.ErrorMapper mapper =
                new StoreResource.ErrorMapper();

        mapper.objectMapper =
                new ObjectMapper();

        Exception exception =
                new RuntimeException(
                        "Something went wrong"
                );

        Response response =
                mapper.toResponse(exception);

        assertEquals(
                500,
                response.getStatus()
        );

        assertNotNull(
                response.getEntity()
        );
    }
}