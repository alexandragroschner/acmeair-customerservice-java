/*******************************************************************************
* Copyright (c) 2015 IBM Corp.
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

package com.acmeair.web.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.util.Objects;


@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlRootElement(name = "Customer")
public class CustomerInfo implements Serializable {


  private static final long serialVersionUID = 1L;

  // Ignore checkstyle warning, only works on tomee like this
  @XmlElement(name = "_id")
  private String _id;

  @XmlElement(name = "password")
  private String password;

  @XmlElement(name = "address")
  private AddressInfo address;

  @XmlElement(name = "phoneNumber")
  private String phoneNumber;

  @XmlElement(name = "phoneNumberType")
  private String phoneNumberType;

  public CustomerInfo() {
    //CDI
  }

  /**
   * Create CustomerInfo.
   */
  public CustomerInfo(String username, String password, AddressInfo address, String phoneNumber, String phoneNumberType) {
    this._id = username;
    this.password = password;
    this.address = address;
    this.phoneNumber = phoneNumber;
    this.phoneNumberType = phoneNumberType;
  }

  public String get_id() {
    return _id;
  }

  public void set_id(String username) {
    this._id = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getPhoneNumberType() {
    return phoneNumberType;
  }

  public void setPhoneNumberType(String phoneNumberType) {
    this.phoneNumberType = phoneNumberType;
  }

  public AddressInfo getAddress() {
    return address;
  }

  public void setAddress(AddressInfo address) {
    this.address = address;
  }

  @Override
  public String toString() {
    return "Customer [id=" + _id + ", password=" + password + ", address=" + address + ", phoneNumber="
            + phoneNumber + ", phoneNumberType=" + phoneNumberType + "]";
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CustomerInfo other = (CustomerInfo) obj;
    if (address == null) {
      if (other.address != null) {
        return false;
      }
    } else if (!address.equals(other.address)) {
      return false;
    }
    if (_id == null) {
      if (other._id != null) {
        return false;
      }
    } else if (!_id.equals(other._id)) {
      return false;
    }
    if (password == null) {
      if (other.password != null) {
        return false;
      }
    } else if (!password.equals(other.password)) {
      return false;
    }
    if (phoneNumber == null) {
      if (other.phoneNumber != null) {
        return false;
      }
    } else if (!phoneNumber.equals(other.phoneNumber)) {
      return false;
    }
    return Objects.equals(phoneNumberType, other.phoneNumberType);
  }
}
