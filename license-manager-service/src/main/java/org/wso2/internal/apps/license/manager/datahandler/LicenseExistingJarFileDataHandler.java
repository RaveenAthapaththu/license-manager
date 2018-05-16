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

package org.wso2.internal.apps.license.manager.datahandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.util.SqlRelatedConstants;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Child class which contains operations specifically related to transferring data of the jar file which already has
 * licenses defined.
 */
public class LicenseExistingJarFileDataHandler extends JarFileDataHandler {

    private static final Logger log = LoggerFactory.getLogger(LicenseExistingJarFileDataHandler.class);

    public LicenseExistingJarFileDataHandler() throws SQLException {

        super();
    }

    /**
     * Get the id for a given product. If exists, returns the id otherwise insert and returns id.
     *
     * @param product name for the product
     * @param version version of the product
     * @return id of the product
     * @throws SQLException if the sql execution fails
     */
    public int getProductId(String product, String version) throws SQLException {

        int productId;
        productId = selectProductId(product, version);
        if (productId == -1) {
            productId = insertProduct(product, version);
        }
        return productId;
    }

    /**
     * Get the product id when the name and the version is given.
     *
     * @param product name of the product
     * @param version version of the product
     * @return id the product
     * @throws SQLException if the sql execution fails
     */
    private int selectProductId(String product, String version) throws SQLException {

        int productId = -1;

        String query = SqlRelatedConstants.SELECT_PRODUCT;
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, product);
        preparedStatement.setString(2, version);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            productId = rs.getInt(SqlRelatedConstants.PRIMARY_KEY_PRODUCT);
        }
        return productId;
    }

    /**
     * Insert a product entry to the LM_PRODUCT table.
     *
     * @param product name of the product.
     * @param version version of the product.
     * @return ID of the inserted product.
     * @throws SQLException if the sql query execution fails
     */
    private int insertProduct(String product, String version) throws SQLException {

        String insertProduct = SqlRelatedConstants.INSERT_PRODUCT;
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(insertProduct, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, product);
        preparedStatement.setString(2, version);
        preparedStatement.executeUpdate();
        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        int id = -1;
        while (resultSet.next()) {
            id = resultSet.getInt("GENERATED_KEY");
        }
        if (log.isDebugEnabled()) {
            log.debug("Successfully inserted the product " + product + " into LM_PRODUCT table.");
        }
        return id;
    }

    /**
     * Check whether license exists for a given library from LM_LIBRARY_LICENSE table.
     *
     * @param libraryId primary key of the library
     * @return true of license exists. false otherwise
     * @throws SQLException if sql execution fails
     */
    public boolean isLibraryLicenseExists(int libraryId) throws SQLException {

        boolean isExist = false;
        String query = SqlRelatedConstants.SELECT_LICENSE_FOR_LIB;
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, libraryId);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            isExist = true;
        }
        return isExist;
    }

    /**
     * Check whether license exists for a given component from LM_COMPONENT_LICENSE table.
     *
     * @param compKey primary key of the component
     * @return true of license exists. false otherwise
     * @throws SQLException if sql execution fails
     */
    public boolean isComponentLicenseExists(String compKey) throws SQLException {

        boolean isExist = false;
        String query = SqlRelatedConstants.SELECT_LICENSE_FOR_COMP;
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, compKey);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            isExist = true;
        }
        return isExist;
    }

    /**
     * Select licenses for component despite of its version.
     *
     * @param compName name of the component
     * @return license key of any version of the given component
     * @throws SQLException if sql execution fails
     */
    public String getComponentLicenseForAnyVersion(String compName) throws SQLException {

        String licenseKey = "NEW";
        String query = SqlRelatedConstants.SELECT_LICENSE_FOR_ANY_COMP;
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, compName);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            licenseKey = rs.getString(SqlRelatedConstants.PRIMARY_KEY_LICENSE);
        }
        return licenseKey;
    }

    /**
     * Select licenses for library despite of its version.
     *
     * @param libraryName name of the library
     * @return license key of any version of the given component
     * @throws SQLException if sql execution fails
     */
    public String getLibraryLicenseForAnyVersion(String libraryName) throws SQLException {

        String licenseKey = "NEW";
        String query = SqlRelatedConstants.SELECT_LICENSE_FOR_ANY_LIB;
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, libraryName);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            licenseKey = rs.getString(SqlRelatedConstants.PRIMARY_KEY_LICENSE);
        }
        return licenseKey;
    }

}
