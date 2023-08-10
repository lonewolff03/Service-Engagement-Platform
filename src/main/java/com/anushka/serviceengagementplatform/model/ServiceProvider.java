package com.anushka.serviceengagementplatform.model;

public class ServiceProvider {
    private int serviceProviderId;
    private String companyName;
    private int industryId;
    private String corporateIdentificationNumber;
    private String businessType;
    private String address;
    private int cityCode;
    private int stateCode;
    private int countryCode;
    private int pinCode;
    private String emailAddress;
    private String password;
    private String userId;

    public ServiceProvider() {
    }

    public ServiceProvider(int serviceProviderId, String companyName, int industryId, String corporateIdentificationNumber, String businessType, String address, int cityCode, int stateCode, int countryCode, int pinCode, String emailAddress, String password, String userId) {
        this.serviceProviderId = serviceProviderId;
        this.companyName = companyName;
        this.industryId = industryId;
        this.corporateIdentificationNumber = corporateIdentificationNumber;
        this.businessType = businessType;
        this.address = address;
        this.cityCode = cityCode;
        this.stateCode = stateCode;
        this.countryCode = countryCode;
        this.pinCode = pinCode;
        this.emailAddress = emailAddress;
        this.password = password;
        this.userId = userId;
    }

    // Getters and setters

    public int getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(int serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public int getIndustryId() {
        return industryId;
    }

    public void setIndustryId(int industryId) {
        this.industryId = industryId;
    }

    public String getCorporateIdentificationNumber() {
        return corporateIdentificationNumber;
    }

    public void setCorporateIdentificationNumber(String corporateIdentificationNumber) {
        this.corporateIdentificationNumber = corporateIdentificationNumber;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getStateCode() {
        return stateCode;
    }

    public void setStateCode(int stateCode) {
        this.stateCode = stateCode;
    }

    public int getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    public int getPinCode() {
        return pinCode;
    }

    public void setPinCode(int pinCode) {
        this.pinCode = pinCode;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
