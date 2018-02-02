package org.licensemanager.conf;

public class Configuration {
    String driver;
    String url;
    String userName;
    String password;
    String path;
    String origin;
    String licenseId;
    String bpmnOrigin;
    String bpmnUrl;
    String bpmnToken;
    String emailAddress;
    String emailPassword;
    String smtpPort;
    String smtpHost;
    String publicKey;

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }

    public String getBpmnOrigin() {
        return bpmnOrigin;
    }

    public void setBpmnOrigin(String bpmnOrigin) {
        this.bpmnOrigin = bpmnOrigin;
    }

    public String getBpmnUrl() {
        return bpmnUrl;
    }

    public void setBpmnUrl(String bpmnUrl) {
        this.bpmnUrl = bpmnUrl;
    }

    public String getBpmnToken() {
        return bpmnToken;
    }

    public void setBpmnToken(String bpmnToken) {
        this.bpmnToken = bpmnToken;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String email) {
        this.emailAddress = email;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}

