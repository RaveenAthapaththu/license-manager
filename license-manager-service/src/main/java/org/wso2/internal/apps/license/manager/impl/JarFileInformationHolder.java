/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.internal.apps.license.manager.impl;

import org.apache.commons.lang.StringUtils;
import org.op4j.Op;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerRuntimeException;
import org.wso2.internal.apps.license.manager.models.JarFile;
import org.wso2.internal.apps.license.manager.util.LicenseManagerUtils;
import org.wso2.internal.apps.license.manager.util.crawler.FolderCrawler;
import org.wso2.internal.apps.license.manager.util.filters.ZipFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Java object to store the jar details of a pack.
 */
public class JarFileInformationHolder implements Serializable {

    private List<JarFile> jarFileList = new ArrayList<>();
    private List<JarFile> errorJarFileList = new ArrayList<>();
    private String productName;
    private String productVersion;
    private FolderCrawler folderCrawler = new FolderCrawler();

    /**
     * Extract the name of the jar from the file name.
     *
     * @param name file name of the jar
     * @return name of the jar
     */
    private static String getName(String name) {

        String extractedName = null;

        for (int i = 0; i < name.length(); i++) {
            if ((name.charAt(i) == '-' | name.charAt(i) == '_')
                    && (Character.isDigit(name.charAt(i + 1)) | name.charAt(i + 1) == 'S'
                    | name.charAt(i + 1) == 'r')) {

                extractedName = name.substring(0, i);
            }
        }
        return extractedName;
    }

    /**
     * Extract the version of the jar from the file name.
     *
     * @param name file name of the jar
     * @return version of the jar
     */
    private static String getVersion(String name) {

        String extractedVersion = null;

        name = name.replace(".jar", "");
        name = name.replace(".mar", "");

        for (int i = 0; i < name.length(); i++) {
            if ((name.charAt(i) == '-' | name.charAt(i) == '_')
                    && (Character.isDigit(name.charAt(i + 1)) | name.charAt(i + 1) == 'S'
                    | name.charAt(i + 1) == 'r')) {
                extractedVersion = name.substring(i + 1, name.length());
            }
        }
        return extractedVersion;
    }

    public List<JarFile> getJarFileList() {

        return jarFileList;
    }

    public void setJarFileList(List<JarFile> jarFileList) {

        this.jarFileList = jarFileList;
    }

    public List<JarFile> getErrorJarFileList() {

        return errorJarFileList;
    }

    public String getProductName() {

        return productName;
    }

    public String getProductVersion() {

        return productVersion;
    }

    /**
     * Recursively check all the jars in the product.
     *
     * @param product path to the pack.
     * @throws LicenseManagerRuntimeException If file unzipping or extraction fails.
     */
    public void extractJarsRecursively(String product) throws LicenseManagerRuntimeException {

        String targetFolder = new File(product).getName();
        String uuid = UUID.randomUUID().toString();
        String tempFolderToHoldJars = new File(product).getParent() + File.separator + uuid;
        productName = getName(targetFolder);
        productVersion = getVersion(targetFolder);
        findDirectJars(product);
        findAllJars(tempFolderToHoldJars);
        LicenseManagerUtils.deleteFolder(tempFolderToHoldJars);
    }

    /**
     * Obtain the direct jars contained in the pack.
     *
     * @param path path to the pack file
     */
    private void findDirectJars(String path) {

        ZipFilter zipFilter = new ZipFilter();
        List<File> directZips = folderCrawler.find(path, zipFilter);
        Iterator<File> i = directZips.iterator();
        JarFile currentJarFile;

        while (i.hasNext()) {
            File jarFile = i.next();
            currentJarFile = getJar(jarFile, null);
            jarFileList.add(currentJarFile);
        }
    }

    /**
     * Find all the jars including inner jars which are inside another jar.
     *
     * @param tempFolderToHoldJars File path to extract the jars.
     * @throws LicenseManagerRuntimeException if the jar extraction fails.
     */
    private void findAllJars(String tempFolderToHoldJars) throws LicenseManagerRuntimeException {

        new File(tempFolderToHoldJars).mkdir();

        Stack<JarFile> zipStack = new Stack<>();

        zipStack.addAll(jarFileList);
        jarFileList = new ArrayList<>();
        ZipFilter zipFilter = new ZipFilter();

        while (!zipStack.empty()) {
            JarFile jarFile = zipStack.pop();
            JarFile currentJarFile;

            File toBeExtracted = jarFile.getJarFile();
            if (!tempFolderToHoldJars.endsWith(File.separator)) {
                tempFolderToHoldJars = tempFolderToHoldJars + File.separator;
            }
            File extractTo;

            // Get information from the Manifest file.
            Manifest manifest;
            try {
                manifest = new java.util.jar.JarFile(toBeExtracted).getManifest();
            } catch (IOException e) {
                throw new LicenseManagerRuntimeException("Failed to get the Manifest of the jarFile.", e);
            }
            if (manifest != null) {
                currentJarFile = getJar(jarFile.getJarFile(), jarFile.getParent());
                jarFile = currentJarFile;
//                jarFile.setExtractedFolder(extractTo);
                jarFile.setType(getType(manifest, jarFile));
                jarFile.setIsBundle(getIsBundle(manifest));
                if (!currentJarFile.isValidName()) {
                    errorJarFileList.add(jarFile);
                } else {
                    jarFileList.add(jarFile);
                }
            }

            // If a jarFile contains jars inside, extract the parent jarFile.
            if (checkInnerJars(toBeExtracted.getAbsolutePath())) {
                extractTo = new File(tempFolderToHoldJars + toBeExtracted.getName());
                extractTo.mkdir();
                LicenseManagerUtils.unzip(toBeExtracted.getAbsolutePath(), extractTo.getAbsolutePath());
                Iterator<File> i = Op.onArray(extractTo.listFiles(zipFilter)).toList().get().iterator();
                File nextFile;
                while (i.hasNext()) {
                    nextFile = i.next();
                    zipStack.add(getJar(nextFile, jarFile));
                }
            }
        }
    }

    /**
     * Returns the type of the jarFile by evaluating the Manifest file.
     *
     * @param man     Manifest of the jarFile
     * @param jarFile jarFile for which the type is needed
     * @return type of the jarFile
     */
    private String getType(Manifest man, JarFile jarFile) {

        Attributes map = man.getMainAttributes();
        String name = map.getValue("Bundle-Name");
        if ((name != null && name.startsWith("org.wso2"))
                || (jarFile.getJarFile().getName().startsWith("org.wso2"))
                || jarFile.getVersion().contains("wso2")) {
            return "wso2";
        } else {
            return "outside";
        }
    }

    /**
     * Set the values for the attributes of the JarFile.java object.
     *
     * @param jarFile jar file to create a JarFile.java object
     * @param parent  parent jar of the corresponding jar
     * @return JarFile.java object
     */
    private JarFile getJar(File jarFile, JarFile parent) {

        JarFile jar = new JarFile();
        String jarName = getName(jarFile.getName());
        String jarVersion = getVersion(jarFile.getName());

        if (StringUtils.isEmpty(jarName) || StringUtils.isEmpty(jarVersion)) {
            jar.setValidName(false);
            jar.setProjectName(getDefaultName(jarFile.getName()));
            jar.setVersion("1.0.0");
        } else {
            jar.setValidName(true);
            jar.setProjectName(jarName);
            jar.setVersion(jarVersion);
        }

        jar.setJarFile(jarFile);
        jar.setParent(parent);
        return jar;
    }

    /**
     * Returns whether a given jar is a bundle or not
     *
     * @param manifest Manifest of the jar file
     * @return true/false
     */
    private boolean getIsBundle(Manifest manifest) {

        Attributes map = manifest.getMainAttributes();
        String bundleManifest = map.getValue("Bundle-ManifestVersion");
        return bundleManifest != null;
    }

    /**
     * Checks whether a jar file contains other jar files inside it.
     *
     * @param filePath absolute path to the jar
     * @return true/false
     * @throws LicenseManagerRuntimeException if file input stream fails.
     */
    private boolean checkInnerJars(String filePath) throws LicenseManagerRuntimeException {

        boolean containsJars = false;
        try {
            ZipInputStream zip = new ZipInputStream(new FileInputStream(filePath));
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.getName().endsWith(".jar") || entry.getName().endsWith(".mar")) {
                    containsJars = true;
                }
            }
        } catch (IOException e) {
            throw new LicenseManagerRuntimeException("Failed to check the inner jars. ", e);
        }
        return containsJars;
    }

    private String getDefaultName(String filename) {

        if (filename.endsWith(".jar") || filename.endsWith(".mar")) {
            filename = filename.replace(".jar", "");
            filename = filename.replace(".mar", "");
        }
        return filename;
    }
}
