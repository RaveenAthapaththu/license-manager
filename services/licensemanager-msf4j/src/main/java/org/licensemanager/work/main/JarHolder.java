/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.licensemanager.work.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.lang.StringUtils;
import org.licensemanager.work.filters.ZipFilter;
import org.licensemanager.work.folderCrawler.Crawler;
import org.op4j.Op;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;

/**
 *
 * @author pubudu
 */
public class JarHolder implements Serializable {

    private Crawler crawler = new Crawler();
    List<MyJar> jarList = new ArrayList<MyJar>();
    List<MyJar> errorJarList = new ArrayList<MyJar>();
    public static Scanner scan = new Scanner(System.in);
    String productName, productVersion;
    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);


    public List<MyJar> getJarList() {
        return jarList;
    }

    public void setJarList(List<MyJar> jarList) {
        this.jarList = jarList;
    }

    public List<MyJar> getErrorJarList() {
        return errorJarList;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void generateMap(String target) throws FileNotFoundException, IOException {
        String targetFolder = new File(target).getName();
        String dest = new File(target).getParent()+File.separator+"jars";
        productName = getName(targetFolder);
        productVersion = getVersion(targetFolder);
        findDirectJars(target);
        extractJarsRecursively(dest);
    }

    void findDirectJars(String path) {
        ZipFilter zipFilter = new ZipFilter();
        List<File> directZips = crawler.find(path, zipFilter);
        Iterator<File> i = directZips.iterator();
        MyJar currentJar;


        while (i.hasNext()) {
            File jarFile = i.next();
            currentJar = getDefaultJar(jarFile, null);
            jarList.add(currentJar);
        }
    }

    private void extractJarsRecursively(String dest) throws FileNotFoundException, IOException {
        log.info("Extracting JARs recursively");
        new File(dest).mkdir();

        Stack<MyJar> zipStack = new Stack<MyJar>();

        zipStack.addAll(jarList);
        jarList = new ArrayList<MyJar>();
        ZipFilter zipFilter = new ZipFilter();

        while (!zipStack.empty()) {
            MyJar jar = zipStack.pop();
            MyJar currentJar = jar;


            File toBeExtracted = jar.getJarFile();
            if (!dest.endsWith(File.separator)) {
                dest = dest + File.separator;
            }
            File extraxtTo = new File(dest + toBeExtracted.getName());
            extraxtTo.mkdir();
            Unzip.unzip(toBeExtracted.getAbsolutePath(),extraxtTo.getAbsolutePath());
            Manifest man = new JarFile(toBeExtracted).getManifest();
            if (man != null) {
                currentJar = getActualJar(jar.getJarFile(),jar.getParent());
                if(!currentJar.isValidName()){
                    jar = currentJar;
                    jar.setExtractedFolder(extraxtTo);
                    jar.setType(getType(man,jar));
                    jar.setIsBundle(getIsBundle(man));
                    errorJarList.add(jar);
                }else{
                    jar = currentJar;
                    jar.setExtractedFolder(extraxtTo);
                    jar.setType(getType(man,jar));
                    jar.setIsBundle(getIsBundle(man));
                    jarList.add(jar);
                }

            }
            Iterator<File> i = Op.onArray(extraxtTo.listFiles(zipFilter)).toList().get().iterator();
            File nextFile;
            while (i.hasNext()) {

                nextFile = i.next();
                zipStack.add(getDefaultJar(nextFile, jar));
            }
        }
    }

    private String getType(Manifest man,MyJar jar) {
        Attributes map = man.getMainAttributes();
        String name = map.getValue("Bundle-Name");
        if ((name != null && name.startsWith("org.wso2"))
                || ( jar.getJarFile().getName().startsWith("org.wso2"))
                || jar.getVersion().contains("wso2")) {
            return "wso2";
        } else {
            return "outside";
        }
    }

    private MyJar getJar(File jarFile, MyJar parent) {
        MyJar jar = new MyJar();
        String jarName = getName(jarFile.getName());
        String jarVersion = getVersion(jarFile.getName());

        jar.setJarFile(jarFile);
        jar.setProjectName(jarName);
        jar.setVersion(jarVersion);
        jar.setParent(parent);
        return jar;
    }

    private MyJar getDefaultJar(File jarFile, MyJar parent) {
        MyJar jar = new MyJar();
        String jarName = jarFile.getName();
        String jarVersion = jarFile.getName();
        jar.setJarFile(jarFile);
        jar.setProjectName(jarName);
        jar.setVersion(jarVersion);
        jar.setParent(parent);
        return jar;
    }

    private MyJar getActualJar(File jarFile, MyJar parent) {
        MyJar jar = new MyJar();
        String jarName = getName(jarFile.getName());
        String jarVersion = getVersion(jarFile.getName());

        if(StringUtils.isEmpty(jarName) || StringUtils.isEmpty(jarVersion)){
            jar.setValidName(false);
            jar.setProjectName(jarFile.getName());
            jar.setVersion(jarFile.getName());
        }else{
            jar.setValidName(true);
            jar.setProjectName(jarName);
            jar.setVersion(jarVersion);
        }

        jar.setJarFile(jarFile);
        jar.setParent(parent);
        return jar;
    }

    public static String getName(String name) {
        if("pdepublishing.jar".equals(name) || "pdepublishing-ant.jar".equals(name)){
            return name;
        }

        for (int i = 0; i < name.length(); i++) {
            if ((name.charAt(i) == '-' | name.charAt(i) == '_')
                    && (Character.isDigit(name.charAt(i + 1)) | name.charAt(i + 1) == 'S'
                        | name.charAt(i + 1) == 'r')) {

                return name.substring(0, i);

            }
        }
        return null;
    }

    public static String getVersion(String name) {
        name = name.replace(".jar", "");
        name = name.replace(".mar", "");
        if("pdepublishing".equals(name) || "pdepublishing-ant".equals(name)){
            return "1.0.0.v20110511";
        }

        for (int i = 0; i < name.length(); i++) {
            if ((name.charAt(i) == '-' | name.charAt(i) == '_')
                    && (Character.isDigit(name.charAt(i + 1)) | name.charAt(i + 1) == 'S'
                    | name.charAt(i + 1) == 'r')) {
                return name.substring(i + 1, name.length());
            }
        }
        return null;
    }

    private boolean getIsBundle(Manifest man) {
         Attributes map = man.getMainAttributes();
         String bundleManifest = map.getValue("Bundle-ManifestVersion");
         if(bundleManifest==null){
             return false;
         }
         return true;
    }
}