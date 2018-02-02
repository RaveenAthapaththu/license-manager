/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.licensemanager.work.main;


import com.workingdogs.village.DataSetException;
import java.io.File;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.licensemanager.conf.Configuration;
import org.licensemanager.conf.ConfigurationReader;
import org.licensemanager.work.enterData.EnterData;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;


/**
 * 
 * @author pubudu + buddhi
 */
public class Main {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);
    public EnterData enterData(JarHolder jh) throws IOException{

        ConfigurationReader configurationReader = new ConfigurationReader();
        Configuration configuration = configurationReader.getConfigurations();
        String driver = configuration.getDriver(),
                url = configuration.getUrl(),
                username = configuration.getUserName(),
                password = configuration.getPassword();

        try {
            EnterData enterData = new EnterData(driver, url, username, password, jh);
            enterData.enter();
            return enterData;
        } catch (ClassNotFoundException ex) {
            log.error("Main(ClassNotFoundException) - " + ex.getMessage());
        } catch (SQLException ex) {
            log.error("Main(SQLException) - " + ex.getMessage());
        }catch (DataSetException ex) {
            log.error("Main(DataSetException) - " + ex.getMessage());
        }
        return null;

    }

    public JarHolder checkJars(String file) throws IOException,ClassNotFoundException{

        if(StringUtils.isEmpty(file) || !new File(file).exists() || !new File(file).isDirectory()){
            log.error("Folder not found - Main - check JARs");
            System.exit(1);
        }
        String inputFile = file;
        JarHolder jh = new JarHolder();
        jh.generateMap(inputFile);
        return jh;
    }
}

