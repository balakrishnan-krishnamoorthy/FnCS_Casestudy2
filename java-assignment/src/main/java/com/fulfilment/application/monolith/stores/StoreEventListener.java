package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class StoreEventListener {
    @Inject
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    public void handleStoreEvent(@Observes(during = TransactionPhase.AFTER_SUCCESS) StoreEvent event) {

        if (event.getOperation() == StoreEvent.Operation.CREATE) {
            legacyStoreManagerGateway.createStoreOnLegacySystem(event.getStore());
        }
        if (event.getOperation() == StoreEvent.Operation.UPDATE) {
            legacyStoreManagerGateway.updateStoreOnLegacySystem(event.getStore());
        }
    }
}
