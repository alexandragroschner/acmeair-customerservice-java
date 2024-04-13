/*******************************************************************************
 * Copyright (c) 2013 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.acmeair.web;

import com.acmeair.service.CustomerService;
import com.acmeair.web.dto.AddressInfo;
import com.acmeair.web.dto.CustomerInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.metrics.annotation.Timed;

import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;



@Path("/internal")
@ApplicationScoped
public class CustomerServiceRestInternal {

  // This class contains endpoints that are called by other services.
  // In the real world, these should be secured somehow, but for simplicity and to avoid too much overhead, they are not.
  // the other endpoints generate enough JWT/security work for this benchmark.

  @Inject
  CustomerService customerService;

  private static final Logger logger = Logger.getLogger(CustomerServiceRestInternal.class.getName());
  private static final JsonReaderFactory rfactory = Json.createReaderFactory(null);

  /**
   * Validate user/password.
   */
  @POST
  @Path("/validateid")
  @Consumes({"application/x-www-form-urlencoded"})
  @Produces("application/json")
  @Timed(name = "com.acmeair.web.CustomerServiceRestInternal.validateCustomer", tags = {"app=acmeair-customerservice-java"})
  public LoginResponse validateCustomer(
          @FormParam("login") String login,
          @FormParam("password") String password) {

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("validateid : login " + login + " password " + password);
    }

    if (!customerService.isPopulated()) {
      throw new RuntimeException("Customer DB has not been populated");
    }

    Boolean validCustomer = customerService.validateCustomer(login, password);

    return new LoginResponse(validCustomer);
  }

//USER ADDED CODE:

  /**
   * Update reward miles and loyalty points of a customer.
   */
  @POST
  @Path("/updateCustomerTotalMiles/{custid}")
  @Consumes({"application/x-www-form-urlencoded"})
  @Produces("application/json")
  @Timed(name = "com.acmeair.web.CustomerServiceRestInternal.updateCustomerTotalMiles", tags = {"app=acmeair-customerservice-java"})
  public CustomerMilesResponse updateCustomerTotalMiles(
          @PathParam("custid") String customerid,
          @FormParam("miles") Long miles,
          @FormParam("loyalty") Long loyaltyPoints) {

    JsonReader jsonReader = rfactory.createReader(new StringReader(customerService
            .getCustomerByUsername(customerid)));

    JsonObject customerJson = jsonReader.readObject();
    jsonReader.close();

    JsonObject addressJson = customerJson.getJsonObject("address");

    String streetAddress2 = null;

    if (addressJson.get("streetAddress2") != null
            && !addressJson.get("streetAddress2").toString().equals("null")) {
      streetAddress2 = addressJson.getString("streetAddress2");
    }

    AddressInfo addressInfo = new AddressInfo(addressJson.getString("streetAddress1"),
            streetAddress2,
            addressJson.getString("city"),
            addressJson.getString("stateProvince"),
            addressJson.getString("country"),
            addressJson.getString("postalCode"));

    Long milesUpdate = customerJson.getInt("total_miles") + miles;
    Long loyaltyUpdate = customerJson.getInt("loyaltyPoints") + loyaltyPoints;
    CustomerInfo customerInfo = new CustomerInfo(customerid,
            null,
            milesUpdate.intValue(),
            customerJson.getInt("miles_ytd"),
            addressInfo,
            customerJson.getString("phoneNumber"),
            customerJson.getString("phoneNumberType"),
            loyaltyUpdate.intValue());

    String mongoSessionId = customerService.updateCustomerPrep(customerid, customerInfo);
    logger.warning("Customer updated (miles: " + milesUpdate + ", loyaltyPoints: " + loyaltyUpdate + ")");

    return new CustomerMilesResponse(milesUpdate, loyaltyUpdate, mongoSessionId);
  }

  //USER ADDED CODE:
  @GET
  @Path("/getCustomerTotalMiles")
  @Consumes({"application/x-www-form-urlencoded"})
  @Produces("application/json")
  public CustomerMilesResponse getCustomerTotalMiles(@QueryParam("custid") String customerid) {
    JsonReader jsonReader = rfactory.createReader(new StringReader(customerService
            .getCustomerByUsername(customerid)));

    JsonObject customerJson = jsonReader.readObject();
    jsonReader.close();

    Long miles = Long.valueOf(customerJson.getInt("total_miles"));
    Long loyalty = Long.valueOf(customerJson.getInt("total_miles"));

    return new CustomerMilesResponse(miles, loyalty);
  }

  @POST
  @Path("/abort")
  @Consumes({"application/json"})
  @Produces("application/json")
  public Response abortMongo(String mongoSessionId) {
    customerService.abortMongoTransaction(mongoSessionId);
    return Response.ok("MongoDB transaction aborted").build();
  }

  @POST
  @Path("/commit")
  @Consumes({"application/json"})
  @Produces("application/json")
  public Response commitMongo(String mongoSessionId) {
    customerService.commitMongoTransaction(mongoSessionId);
    return Response.ok("MongoDB transaction committed").build();
  }
}