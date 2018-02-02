package org.licensemanager.store;

public class JarFile {
    private int ID;
    private String name;
    private String version;

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
