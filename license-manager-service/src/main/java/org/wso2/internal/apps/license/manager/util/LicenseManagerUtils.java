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
package org.wso2.internal.apps.license.manager.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerRuntimeException;
import org.wso2.internal.apps.license.manager.impl.JarFileInformationHolder;
import org.wso2.internal.apps.license.manager.models.JarFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Contains the functions required for the License Generation process.
 */
public class LicenseManagerUtils {

    private static final Logger log = LoggerFactory.getLogger(LicenseManagerUtils.class);

    /**
     * Static function to unzip a file to a given location.
     *
     * @param infile    the location of the zipped file.
     * @param outFolder location where the file should be unzipped.
     * @throws LicenseManagerRuntimeException if file extraction fails.
     */
    public static void unzip(String infile, String outFolder) throws LicenseManagerRuntimeException {

        Enumeration entries;
        ZipFile zipFile;

        try {
            zipFile = new ZipFile(infile);
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File f = new File(outFolder + File.separator + entry.getName());
                if (!entry.isDirectory()) {
                    f.getParentFile().mkdirs();
                    copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(f
                            .getAbsolutePath())));
                }
            }
            zipFile.close();
        } catch (IOException e) {
            throw new LicenseManagerRuntimeException("Failed to unzip the file. ", e);
        }
    }

    private static void copyInputStream(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

    /**
     * Delete folders.
     *
     * @param filePath path to the folder.
     */
    public static void deleteFolder(String filePath) {

        File file = new File(filePath);
        if (file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                log.error("Error while deleting the folder. " + e.getMessage(), e);
            }
        } else if (file.isFile()) {
            file.delete();
        }
    }

    /**
     * Recursively check the jars of a pack and creates a new JarFileInformationHolder object.
     *
     * @param file path to the pack
     * @return JarFileInformationHolder contains the details of jars.
     * @throws LicenseManagerRuntimeException if the jar extraction fails.
     */
    public static JarFileInformationHolder checkJars(String file) throws LicenseManagerRuntimeException {

        if (StringUtils.isEmpty(file) || !new File(file).exists() || !new File(file).isDirectory()) {
            throw new LicenseManagerRuntimeException("Folder is not found in the location");
        }
        JarFileInformationHolder jh = new JarFileInformationHolder();
        jh.extractJarsRecursively(file);
        return jh;
    }

    /**
     * Remove the duplicates in the faulty named jars.
     *
     * @param faultyNamedJars list of jars in which the names and version are missing
     * @return unique list of jars with faulty names.
     */
    public static List<JarFile> removeDuplicates(List<JarFile> faultyNamedJars) {

        List<JarFile> faultyNamedUniqueJarFiles = new ArrayList<>();
        for (JarFile jarFile : faultyNamedJars) {
            boolean newJar = true;
            for (JarFile uniqueJarFile : faultyNamedUniqueJarFiles) {
                if (jarFile.getProjectName().equals(uniqueJarFile.getProjectName())) {
                    newJar = false;
                }
            }
            if (newJar) {
                faultyNamedUniqueJarFiles.add(jarFile);
            }
        }
        return faultyNamedUniqueJarFiles;
    }

    /**
     * Remove files from local file storage and FTP server after generating the license text.
     *
     * @param fileName product name for which the licenses were generated
     */
    public static void cleanFileStorage(String fileName) {

        LicenseManagerUtils.deleteFolder(fileName + ".zip");
        LicenseManagerUtils.deleteFolder(fileName);
        FtpConnectionHandler ftpConnectionHandler = new FtpConnectionHandler();
        try {
            ftpConnectionHandler.initiateSftpConnection();
            ftpConnectionHandler.deleteFileFromFtpServer(fileName);
        } catch (LicenseManagerConfigurationException e) {
            log.error("Failed to remove the zip file from the FTP server. " + e.getMessage(), e);
        } finally {
            // Close the connections.
            ftpConnectionHandler.closeSftpConnection();
        }
    }

}
