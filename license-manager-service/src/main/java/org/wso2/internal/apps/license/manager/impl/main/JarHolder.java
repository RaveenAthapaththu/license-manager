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

package org.wso2.internal.apps.license.manager.impl.main;

import org.apache.commons.lang.StringUtils;
import org.op4j.Op;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.impl.exception.LicenseManagerRuntimeException;
import org.wso2.internal.apps.license.manager.impl.filters.ZipFilter;
import org.wso2.internal.apps.license.manager.impl.folderCrawler.Crawler;
import org.wso2.internal.apps.license.manager.util.LicenseManagerUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author pubudu
 */
public class JarHolder implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(JarHolder.class);
    private List<Jar> jarList = new ArrayList<>();
    private List<Jar> errorJarList = new ArrayList<>();
    private String productName;
    private String productVersion;
    private Crawler crawler = new Crawler();

    public List<Jar> getJarList() {

        return jarList;
    }

    public void setJarList(List<Jar> jarList) {

        this.jarList = jarList;
    }

    public List<Jar> getErrorJarList() {

        return errorJarList;
    }

    public String getProductName() {

        return productName;
    }

    public String getProductVersion() {

        return productVersion;
    }

    public void generateMap(String target) throws IOException, LicenseManagerRuntimeException {

        String targetFolder = new File(target).getName();
        String dest = new File(target).getParent() + File.separator + "jars";
        productName = getName(targetFolder);
        productVersion = getVersion(targetFolder);
        findDirectJars(target);
        extractJarsRecursively(dest);
    }

    private void findDirectJars(String path) {

        ZipFilter zipFilter = new ZipFilter();
        List<File> directZips = crawler.find(path, zipFilter);
        Iterator<File> i = directZips.iterator();
        Jar currentJar;

        while (i.hasNext()) {
            File jarFile = i.next();
            currentJar = getJar(jarFile, null);
            jarList.add(currentJar);
        }
    }

    private void extractJarsRecursively(String dest) throws IOException,
            LicenseManagerRuntimeException {

        new File(dest).mkdir();

        Stack<Jar> zipStack = new Stack<>();

        zipStack.addAll(jarList);
        jarList = new ArrayList<>();
        ZipFilter zipFilter = new ZipFilter();

        while (!zipStack.empty()) {
            Jar jar = zipStack.pop();
            Jar currentJar;

            File toBeExtracted = jar.getJarFile();
            if (!dest.endsWith(File.separator)) {
                dest = dest + File.separator;
            }
            File extractTo = null;

            // If a jar contains jars inside, extract the parent jar.
            if (checkInnerJars(toBeExtracted.getAbsolutePath())) {

                extractTo = new File(dest + toBeExtracted.getName());
                extractTo.mkdir();
                LicenseManagerUtils.unzip(toBeExtracted.getAbsolutePath(), extractTo.getAbsolutePath());
                Iterator<File> i = Op.onArray(extractTo.listFiles(zipFilter)).toList().get().iterator();
                File nextFile;
                while (i.hasNext()) {

                    nextFile = i.next();
                    zipStack.add(getJar(nextFile, jar));
                }
            }

            // Get information from the Manifest file.
            Manifest man = new JarFile(toBeExtracted).getManifest();
            if (man != null) {
                currentJar = getJar(jar.getJarFile(), jar.getParent());
                jar = currentJar;
                jar.setExtractedFolder(extractTo);
                jar.setType(getType(man, jar));
                jar.setIsBundle(getIsBundle(man));
                if (!currentJar.isValidName()) {
                    errorJarList.add(jar);
                } else {
                    jarList.add(jar);
                }
            }
        }
    }

    /**
     * Extract the name of the jar from the file name.
     *
     * @param name  file name of the jar
     * @return  name of the jar
     */
    private static String getName(String name) {

        String extractedName = null;
//        if ("pdepublishing.jar".equals(name) || "pdepublishing-ant.jar".equals(name)) {
//            extractedName = name;
//        }

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
     * @param name  file name of the jar
     * @return  version of the jar
     */
    private static String getVersion(String name) {

        String extractedVersion = null;

        name = name.replace(".jar", "");
        name = name.replace(".mar", "");

//        if ("pdepublishing".equals(name) || "pdepublishing-ant".equals(name)) {
//            extractedVersion = "1.0.0.v20110511";
//        }

        for (int i = 0; i < name.length(); i++) {
            if ((name.charAt(i) == '-' | name.charAt(i) == '_')
                    && (Character.isDigit(name.charAt(i + 1)) | name.charAt(i + 1) == 'S'
                    | name.charAt(i + 1) == 'r')) {
                extractedVersion = name.substring(i + 1, name.length());
            }
        }
        return extractedVersion;
    }

    /**
     * Returns the type of the jar by evaluating the Manifest file.
     * @param man   Manifest of the jar
     * @param jar   jar for which the type is needed
     * @return  type of the jar
     */
    private String getType(Manifest man, Jar jar) {

        Attributes map = man.getMainAttributes();
        String name = map.getValue("Bundle-Name");
        if ((name != null && name.startsWith("org.wso2"))
                || (jar.getJarFile().getName().startsWith("org.wso2"))
                || jar.getVersion().contains("wso2")) {
            return "wso2";
        } else {
            return "outside";
        }
    }

    /**
     * Set the values for the attributes of the Jar object.
     *
     * @param jarFile   jar file to create a Jar object
     * @param parent    parent jar of the corresponding jar
     * @return  Jar object
     */
    private Jar getJar(File jarFile, Jar parent) {

        Jar jar = new Jar();
        String jarName = getName(jarFile.getName());
        String jarVersion = getVersion(jarFile.getName());

        if (StringUtils.isEmpty(jarName) || StringUtils.isEmpty(jarVersion)) {
            jar.setValidName(false);
            jar.setProjectName(jarFile.getName());
            jar.setVersion(jarFile.getName());
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
     * @param filePath  absolute path to the jar
     * @return true/false
     * @throws IOException if file input stream fails.
     */
    private boolean checkInnerJars(String filePath) throws IOException {

        boolean containsJars = false;
        ZipInputStream zip = new ZipInputStream(new FileInputStream(filePath));
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
            if (entry.getName().endsWith(".jar") || entry.getName().endsWith(".mar")) {
                containsJars = true;
            }
        }
        return containsJars;
    }
}
