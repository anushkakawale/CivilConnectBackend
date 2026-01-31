package com.example.CivicConnect.dto;

import lombok.Data;

@Data
public class CitizenAddressDTO {
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String pincode;
}
