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
import org.wso2.internal.apps.license.manager.model.JarFile;
import org.wso2.internal.apps.license.manager.model.JarFilesHolder;
import org.wso2.internal.apps.license.manager.util.LicenseManagerUtils;
import org.wso2.internal.apps.license.manager.util.crawler.FolderCrawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Extract jar file information of a pack recursively.
 */
public class JarFileExtractor {

    /**
     * Extract the name of the jar from the file name.
     *
     * @param name file name of the jar
     * @return name of the jar
     */
    private static String getName(String name) {

        String extractedName = null;

        for (int i = 0; i < name.length(); i++) {
            if ((name.charAt(i) == '-' | name.charAt(i) == '_') && (Character.isDigit(name.charAt(i + 1)) |
                    name.charAt(i + 1) == 'S' | name.charAt(i + 1) == 'r')) {

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
            if ((name.charAt(i) == '-' | name.charAt(i) == '_') && (Character.isDigit(name.charAt(i + 1)) |
                    name.charAt(i + 1) == 'S' | name.charAt(i + 1) == 'r')) {
                extractedVersion = name.substring(i + 1, name.length());
            }
        }
        return extractedVersion;
    }

    /**
     * Recursively check all the jars in the product.
     *
     * @param product path to the pack.
     * @throws LicenseManagerRuntimeException If file unzipping or extraction fails.
     */
    public JarFilesHolder extractJarsRecursively(String product) throws LicenseManagerRuntimeException {

        JarFilesHolder jarFilesHolder = new JarFilesHolder();

        String targetFolder = new File(product).getName();
        String uuid = UUID.randomUUID().toString();
        String tempFolderToHoldJars = new File(product).getParent() + File.separator + uuid;

        List<JarFile> jarFilesInPack = findDirectJars(product);
        List<JarFile> faultyNamedJars = findAllJars(tempFolderToHoldJars, jarFilesInPack);
        LicenseManagerUtils.deleteFolder(tempFolderToHoldJars);

        jarFilesHolder.setProductName(getName(targetFolder));
        jarFilesHolder.setProductVersion(getVersion(targetFolder));
        jarFilesHolder.setJarFilesInPack(jarFilesInPack);
        jarFilesHolder.setFaultyNamedJars(faultyNamedJars);
        return jarFilesHolder;
    }

    /**
     * Obtain the direct jars contained in the pack.
     *
     * @param path path to the pack file
     */
    private List<JarFile> findDirectJars(String path) {

        FolderCrawler folderCrawler = new FolderCrawler();
        List<File> directZips = folderCrawler.find(path);
        List<JarFile> listOfDirectJarsInPack = new ArrayList<>();
        for (File directZip : directZips) {
            JarFile currentJarFile = createJarObjectFromFile(directZip, null);
            listOfDirectJarsInPack.add(currentJarFile);
        }
        return listOfDirectJarsInPack;
    }

    /**
     * Find all the jars including inner jars which are inside another jar.
     *
     * @param tempFolderToHoldJars File path to extract the jars.
     * @throws LicenseManagerRuntimeException if the jar extraction fails.
     */
    private List<JarFile> findAllJars(String tempFolderToHoldJars, List<JarFile> jarFilesInPack) throws
            LicenseManagerRuntimeException {

        new File(tempFolderToHoldJars).mkdir();

        Stack<JarFile> zipStack = new Stack<>();
        List<JarFile> faultyNamedJars = new ArrayList<>();

        zipStack.addAll(jarFilesInPack);
        jarFilesInPack.clear();
        tempFolderToHoldJars = tempFolderToHoldJars + File.separator;

        while (!zipStack.empty()) {
            JarFile jarFile = zipStack.pop();
            File fileToBeExtracted = jarFile.getJarFile();
            File extractTo;

            // Get information from the Manifest file.
            Manifest manifest;
            try {
                manifest = new java.util.jar.JarFile(fileToBeExtracted).getManifest();
            } catch (IOException e) {
                throw new LicenseManagerRuntimeException("Failed to get the Manifest of the jarFile.", e);
            }
            if (manifest != null) {
                setNameAndVersionOfJar(jarFile.getJarFile(), jarFile);
                jarFile.setType(getType(manifest, jarFile));
                jarFile.setIsBundle(getIsBundle(manifest));
                if (!jarFile.isValidName()) {
                    faultyNamedJars.add(jarFile);
                } else {
                    jarFilesInPack.add(jarFile);
                }
            }

            // If a jarFile contains jars inside, extract the parent jarFile.
            if (checkInnerJars(fileToBeExtracted.getAbsolutePath())) {
                extractTo = new File(tempFolderToHoldJars + fileToBeExtracted.getName());
                extractTo.mkdir();
                LicenseManagerUtils.unzip(fileToBeExtracted.getAbsolutePath(), extractTo.getAbsolutePath());
                List<File> listOfInnerFiles = Op.onArray(extractTo
                        .listFiles(file -> file.getName().endsWith(".jar") || file.getName().endsWith(".mar")))
                        .toList().get();
                for (File nextFile : listOfInnerFiles) {
                    zipStack.add(createJarObjectFromFile(nextFile, jarFile));
                }
            }
        }
        return faultyNamedJars;
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
     * Set the values for the attributes of the Jar object.
     *
     * @param fileContainingJar jar file to create a JarFile.java object
     * @param jar               JarFile java object
     */
    private void setNameAndVersionOfJar(File fileContainingJar, JarFile jar) {

        String jarName = getName(fileContainingJar.getName());
        String jarVersion = getVersion(fileContainingJar.getName());

        if (StringUtils.isEmpty(jarName) || StringUtils.isEmpty(jarVersion)) {
            jar.setValidName(false);
            jar.setProjectName(getDefaultName(fileContainingJar.getName()));
            jar.setVersion("1.0.0");
        } else {
            jar.setValidName(true);
            jar.setProjectName(jarName);
            jar.setVersion(jarVersion);
        }

    }

    /**
     * Set the values for the attributes of the Jar object.
     *
     * @param fileContainingJar jar file to create a JarFile.java object
     * @param parent            parent jar of the corresponding jar
     * @return JarFile object
     */
    private JarFile createJarObjectFromFile(File fileContainingJar, JarFile parent) {

        JarFile jar = new JarFile();
        setNameAndVersionOfJar(fileContainingJar, jar);
        jar.setJarFile(fileContainingJar);
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

        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(filePath))) {
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (entry.getName().endsWith(".jar") || entry.getName().endsWith(".mar")) {
                    containsJars = true;
                    break;
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
