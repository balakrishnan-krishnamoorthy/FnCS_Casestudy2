package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocationGatewayTest {

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertEquals(location.identification, "ZWOLLE-001");
  }

  @Test
  public void testWhenResolveNonExistingLocationShouldReturnNull() {

    LocationGateway locationGateway = new LocationGateway();

    Location location =
            locationGateway.resolveByIdentifier("INVALID-LOCATION");

    assertEquals(null, location);
  }

  @Test
  public void testWhenResolveNullIdentifierShouldReturnNull() {

    LocationGateway locationGateway = new LocationGateway();

    Location location =
            locationGateway.resolveByIdentifier(null);

    assertEquals(null, location);
  }
}
