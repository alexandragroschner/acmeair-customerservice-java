/*******************************************************************************
* Copyright (c) 2017 IBM Corp.
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

package com.acmeair.mongo.services;

import com.acmeair.service.CustomerService;
import com.acmeair.web.dto.AddressInfo;
import com.acmeair.web.dto.CustomerInfo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.ConnectionDescription;
import org.bson.Document;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

@ApplicationScoped
public class CustomerServiceImpl extends CustomerService {

  private MongoCollection<Document> customer;
  private Boolean isPopulated = false;
  private final int WRITE_BATCH_SIZE = ConnectionDescription.getDefaultMaxWriteBatchSize();
    
  @Inject
  MongoDatabase database;


  @PostConstruct
  public void initialization() {
    customer = database.getCollection("customer");
  }

  @Override
  public Long count() {
    return customer.countDocuments();
  }

  @Override
  public void createCustomer(CustomerInfo customerInfo) {
    Document customerDoc = parseCustomerInfo(customerInfo);
    customer.insertOne(customerDoc);
  }

  @Override
  public void createCustomers(List<CustomerInfo> customers) {
    List<Document> documents = new ArrayList<>(WRITE_BATCH_SIZE);
    for (int i=0; i<customers.size(); i++) {
      documents.add(parseCustomerInfo(customers.get(i)));
      if ( i % WRITE_BATCH_SIZE == 0 ) {
        customer.insertMany(documents);
        documents.clear();
      }
    }
    if(!documents.isEmpty()) customer.insertMany(documents);
  }

  @Override
  public String createAddress(AddressInfo addressInfo) {
    Document addressDoc = parseAddressInfo(addressInfo);
    return addressDoc.toJson();
  }

  @Override
  public void updateCustomer(String username, CustomerInfo customerInfo) {
    Document address = parseAddressInfo(customerInfo.getAddress());

    customer.updateOne(eq("_id", customerInfo.get_id()),
        combine(set("status", customerInfo.getStatus()), 
            set("total_miles", customerInfo.getTotal_miles()),
            set("miles_ytd", customerInfo.getMiles_ytd()),
            set("loyaltyPoints", customerInfo.getLoyaltyPoints()),
            set("address", address),
            set("phoneNumber", customerInfo.getPhoneNumber()),
            set("phoneNumberType", customerInfo.getPhoneNumberType())));
  }

  @Override
  protected String getCustomer(String username) {
    return customer.find(eq("_id", username)).first().toJson();
  }

  @Override
  public String getCustomerByUsername(String username) {
    Document customerDoc = customer.find(eq("_id", username)).first();
    if (customerDoc != null) {
      customerDoc.remove("password");
      customerDoc.append("password", null);
    }
    return customerDoc.toJson();
  }

  @Override
  public void dropCustomers() {
    customer.deleteMany(new Document());

  }

  @Override
  public String getServiceType() {
    return "mongo";
  }

  @Override
  public boolean isPopulated() {
    if (isPopulated) {
      return true;
    }
        
    if (customer.countDocuments() > 0) {
      isPopulated = true;
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean isConnected() {
    return (customer.countDocuments() >= 0);
  }

  private Document parseCustomerInfo(CustomerInfo customerInfo) {
    return new Document("_id", customerInfo.get_id())
            .append("password", customerInfo.getPassword())
            .append("status", customerInfo.getStatus())
            .append("total_miles", customerInfo.getTotal_miles()).append("miles_ytd", customerInfo.getMiles_ytd())
            .append("loyaltyPoints", customerInfo.getLoyaltyPoints())
            .append("address", parseAddressInfo(customerInfo.getAddress()))
            .append("phoneNumber", customerInfo.getPhoneNumber())
            .append("phoneNumberType", customerInfo.getPhoneNumberType());
  }
  private Document parseAddressInfo(AddressInfo addressInfo) {
    return new Document("streetAddress1", addressInfo.getStreetAddress1())
            .append("streetAddress2", addressInfo.getStreetAddress2())
            .append("city", addressInfo.getCity())
            .append("stateProvince", addressInfo.getStateProvince())
            .append("country", addressInfo.getCountry())
            .append("postalCode", addressInfo.getPostalCode());
  }
}
