package org.licensemanager.store;

public class LicenseRequest {
    public String productName;
    public String productVersion;
    public String licenseRequestId;
    public String mailList;

    public String getLicenseRequestId() {
        return licenseRequestId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public String getMailList() {
        return mailList;
    }
}
