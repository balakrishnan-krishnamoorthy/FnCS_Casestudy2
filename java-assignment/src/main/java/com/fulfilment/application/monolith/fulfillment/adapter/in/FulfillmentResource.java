package com.fulfilment.application.monolith.fulfillment.adapter.in;

import com.fulfilment.application.monolith.fulfillment.domain.model.Fulfillment;
import com.fulfilment.application.monolith.fulfillment.FulfillmentRequest;
import com.fulfilment.application.monolith.fulfillment.application.FulfillmentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("fulfillment")
public class FulfillmentResource {

    @Inject
    FulfillmentService fulfillmentService;

    @POST
    public Response create(FulfillmentRequest request) {

        Fulfillment fulfillment =
                fulfillmentService.create(
                        request.storeId,
                        request.productId,
                        request.warehouseId
                );

        return Response.status(201)
                .entity(fulfillment)
                .build();
    }
}