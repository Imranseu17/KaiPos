package com.kaicomsol.kpos.models;

public class CustomerDetails {
    private  String customerCode;
    private  String accountNo;
    private  String address;
    private  String apartment;
    private String house;
    private String road;
    private String block;
    private String sector;
    private String section;
    private String zipCode;
    private int metroId;
    private String metro;
    private int zoneId;
    private String zone;
    private int areaId;
    private String area;
    private String subArea;
    private String postalCode;
    private String title;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String identityNo;
    private double balance;
    private int statusId;
    private String status;


    public String getCustomerCode() {
        return customerCode;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public String getAddress() {
        return address;
    }

    public String getApartment() {
        return apartment;
    }

    public String getHouse() {
        return house;
    }

    public String getRoad() {
        return road;
    }

    public String getBlock() {
        return block;
    }

    public String getSector() {
        return sector;
    }

    public String getSection() {
        return section;
    }

    public String getZipCode() {
        return zipCode;
    }

    public int getMetroId() {
        return metroId;
    }

    public String getMetro() {
        return metro;
    }

    public int getZoneId() {
        return zoneId;
    }

    public String getZone() {
        return zone;
    }

    public int getAreaId() {
        return areaId;
    }

    public String getArea() {
        return area;
    }

    public String getSubArea() {
        return subArea;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getIdentityNo() {
        return identityNo;
    }

    public double getBalance() {
        return balance;
    }

    public int getStatusId() {
        return statusId;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "CustomerInfo{" +
                "customerCode='" + customerCode + '\'' +
                ", accountNo='" + accountNo + '\'' +
                ", address='" + address + '\'' +
                ", apartment='" + apartment + '\'' +
                ", house='" + house + '\'' +
                ", road='" + road + '\'' +
                ", block='" + block + '\'' +
                ", sector='" + sector + '\'' +
                ", section='" + section + '\'' +
                ", zipCode='" + zipCode + '\'' +
                ", metroId=" + metroId +
                ", metro='" + metro + '\'' +
                ", zoneId=" + zoneId +
                ", zone='" + zone + '\'' +
                ", areaId=" + areaId +
                ", area='" + area + '\'' +
                ", subArea='" + subArea + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", title='" + title + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", identityNo='" + identityNo + '\'' +
                ", balance=" + balance +
                ", statusId=" + statusId +
                ", status='" + status + '\'' +
                '}';
    }

}
