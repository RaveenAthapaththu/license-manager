/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.licensemanager.work.main;

import java.io.File;
import java.io.Serializable;

/**
 * 
 * @author pubudu
 */
public class MyJar implements Serializable {

    private String projectName, type, version, product, vendor, description, url;
    private File jarFile, extractedFolder;
    private MyJar parent;
    private boolean isBundle = false;
    private boolean isValidName = false;

    public void setIsBundle(boolean isBundle) {
        this.isBundle = isBundle;
    }

    public boolean isBundle(){
        return isBundle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public MyJar getParent() {
        return parent;
    }

    public void setParent(MyJar parent) {
        this.parent = parent;

    }

    public File getExtractedFolder() {
        return extractedFolder;
    }

    public void setExtractedFolder(File extractedFolder) {
        this.extractedFolder = extractedFolder;        
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;

    }

    public File getJarFile() {
        return jarFile;

    }

    public void setJarFile(File jarFile) {
        this.jarFile = jarFile;

    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;

    }

    public void print() {
        System.out.println("jarFile = " + jarFile.getName());
        System.out.println("version = " + version);
        System.out.println("type = " + type);
        System.out.println("product = " + product);
        System.out.println("projectName = " + projectName);
        System.out.println("extractedFolder = " + extractedFolder);
        String s = (parent == null) ? null : parent.getJarFile().getName();
        System.out.println("parent = " + s);
        System.out.println("url = " + url);
        System.out.println("description = " + description);
        System.out.println("vendor = " + vendor);
        System.out.println("");
    }

    public boolean isValidName() {
        return this.isValidName;
    }

    public void setValidName(boolean validName) {
        this.isValidName = validName;
    }
}
