package org.licensemanager.store;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;
import com.workingdogs.village.TableDataSet;
import org.licensemanager.conf.Configuration;
import org.licensemanager.conf.ConfigurationReader;
import org.licensemanager.work.main.MyJar;
import org.licensemanager.work.tables.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.*;
import java.util.Base64;

public class DataManager {

    Connection con;
    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);

    public DataManager(String driver, String url, String userName, String password) throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        this.con = DriverManager.getConnection(url, userName, password);

    }

    public void closeConection(){
        try{
            con.close();
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    public boolean isLibraryExists(String name, String version, String type) throws  DataSetException{
        LM_LIBRARY libTable = new LM_LIBRARY();
        TableDataSet tds;
        try {

            tds = new TableDataSet(con, libTable.table);
            tds.where(libTable.LIB_NAME +"='"+ name +"' AND " + libTable.LIB_VERSION + "='"+ version  +"' AND " + libTable.LIB_TYPE + "='" + type + "'");
            tds.fetchRecords();
            if(tds.size() == 0){
                return false;
            }else{
                return true;
            }
        } catch (SQLException ex) {

            log.error("isLibraryExists " + ex.getMessage());
            return false;
        }


    }

    public JsonArray selectAllLicense(){
        LM_LICENSE licenseTable = new LM_LICENSE();
        TableDataSet tds;

        JsonArray resultArray = new JsonArray();
        try {
            tds = new TableDataSet(con,licenseTable.table);
            tds.fetchRecords();
            for(int i = 0; i < tds.size(); i++) {
                Record record = tds.getRecord(i);
                JsonObject licenseJson = new JsonObject();
                licenseJson.addProperty("LICENSE_ID",record.getValue("LICENSE_ID").asInt());
                licenseJson.addProperty("LICENSE_KEY",record.getValue("LICENSE_KEY").toString());
                licenseJson.addProperty("LICENSE_NAME",record.getValue("LICENSE_NAME").toString());
                resultArray.add(licenseJson);
            }
            return resultArray;

        } catch (SQLException ex) {
            log.error("selectAllLicense(SQLException) " + ex.getMessage());

        }catch (DataSetException ex) {
            log.error("selectAllLicense(DataSetException) " + ex.getMessage());

        }

        return resultArray;
    }

    public String selectLicenseFromId(int id){
        LM_LICENSE licenseTable = new LM_LICENSE();
        TableDataSet tds;
        Record rec;

        try {
            tds = new TableDataSet(con,licenseTable.table);
            tds.where(licenseTable.LICENSE_ID + "=" + Integer.toString(id));
            tds.fetchRecords(1);
            rec = tds.getRecord(0);
            String licenseKey = rec.getValue(licenseTable.LICENSE_KEY).toString();


            return licenseKey;

        } catch (SQLException ex) {
            log.error("selectLicenseFromId(SQLException) " + ex.getMessage());

        }catch (DataSetException ex) {
            log.error("selectLicenseFromId(DataSetException) " + ex.getMessage());

        }

        return "";
    }

    public String selectLicenseNameFromId(int id){
        LM_LICENSE licenseTable = new LM_LICENSE();
        TableDataSet tds;
        Record rec;

        try {
            tds = new TableDataSet(con,licenseTable.table);
            tds.where(licenseTable.LICENSE_ID + "=" + Integer.toString(id));
            tds.fetchRecords(1);
            rec = tds.getRecord(0);
            String licenseKey = rec.getValue(licenseTable.LICENSE_NAME).toString();


            return licenseKey;

        } catch (SQLException ex) {
            log.error("selectLicenseNameFromId(SQLException) " + ex.getMessage());

        }catch (DataSetException ex) {
            log.error("selectLicenseNameFromId(DataSetException) " + ex.getMessage());

        }

        return "";
    }

    public int selectTempLib(String name, String version,String type){
        LM_TEMPLIB libTable = new LM_TEMPLIB();
        TableDataSet tds;
        Record rec;

        try {
            tds = new TableDataSet(con,libTable.table);
            tds.where(libTable.TEMPLIB_NAME +"='"+ name +"' AND " + libTable.TEMPLIB_VERSION + "='"+ version + "' AND " + libTable.TEMPLIB_TYPE + "='" + type + "'");
            tds.fetchRecords(1);
            rec = tds.getRecord(0);
            int libraryId = rec.getValue(libTable.TEMPLIB_ID).asInt();
            return libraryId;
        } catch (SQLException ex) {
            log.error("selectLicenseNameFromId(SQLException) " + ex.getMessage());
        }catch (DataSetException ex) {
            log.error("selectLicenseNameFromId(DataSetException) " + ex.getMessage());
        }

        return -1;
    }

    public int selectLibraryId(String name, String version, String type) throws  DataSetException{
        LM_LIBRARY libTable = new LM_LIBRARY();
        TableDataSet tds;
        Record record;
        int id = -1;

        try {

            tds = new TableDataSet(con, libTable.table);
            tds.where(libTable.LIB_NAME +"='"+ name +"' AND " + libTable.LIB_VERSION + "='"+ version   +"' AND " + libTable.LIB_TYPE + "='" + type + "'");
            tds.fetchRecords();
            record = tds.getRecord(0);
            id = record.getValue(libTable.LIB_ID).asInt();

        } catch (SQLException ex) {
            log.error("selectLibraryId(SQLException) " + ex.getMessage());
        }
        return id;
    }

    public String selectLibraryAdmins() throws  DataSetException{
        LM_ROLE lmRole = new LM_ROLE();
        TableDataSet tds;
        int id = -1;
        String mailList = "";
        try {
            tds = new TableDataSet(con, lmRole.table);
            tds.where(lmRole.ROLE_TYPE + " = 'LIBRARY' OR " + lmRole.ROLE_TYPE + " = 'LICENSE'");
            tds.fetchRecords();
            for(int i = 0; i < tds.size(); i++){
                Record record = tds.getRecord(i);
                mailList += record.getValue(lmRole.ROLE_EMAIL).asString() + ",";
            }
        } catch (SQLException ex) {
            log.error("selectLibraryAdmins(SQLException) " + ex.getMessage());
        }
        return mailList;
    }

    public JsonArray selectWaitingLicenseRequests(){
        String query;
        LM_LICENSEREQUEST lr = new LM_LICENSEREQUEST();
        LM_PRODUCT lp = new LM_PRODUCT();
        JsonArray responseJson = new JsonArray();
        try{
            query = "SELECT LM_LICENSEREQUEST.*, LM_PRODUCT.* FROM LM_LICENSEREQUEST \n" +
                    "INNER JOIN LM_PRODUCT ON LM_LICENSEREQUEST.LR_PRODUCT_ID = LM_PRODUCT.PRODUCT_ID \n" +
                    "WHERE LM_LICENSEREQUEST.LR_STATE = 'WAITING'";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                JsonObject waitingRequestJson = new JsonObject();
                waitingRequestJson.addProperty(lr.LR_ID,rs.getString(lr.LR_ID));
                waitingRequestJson.addProperty(lr.LR_PRODUCT_ID,rs.getString(lr.LR_PRODUCT_ID));
                waitingRequestJson.addProperty(lr.LR_REQUEST_BY,rs.getString(lr.LR_REQUEST_BY));
                waitingRequestJson.addProperty(lp.PRODUCT_NAME,rs.getString(lp.PRODUCT_NAME));
                waitingRequestJson.addProperty(lp.PRODUCT_VERSION,rs.getString(lp.PRODUCT_VERSION));
                responseJson.add(waitingRequestJson);
            }
        }catch (SQLException ex){
            log.error("selectWaitingLicenseRequests(SQLException) " + ex.getMessage());
        }
        return responseJson;
    }

    public String selectLicenseRequestTaskIdFromId(int requestId){
        String query;
        LM_LICENSEREQUEST lr = new LM_LICENSEREQUEST();
        String taskId = "";
        try{
            query = "SELECT * FROM LM_LICENSEREQUEST WHERE LR_ID = ?";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1,requestId);
            ResultSet rs = preparedStatement.executeQuery();
            if(rs.next()){
                taskId = rs.getString(lr.LR_BPMN_TASK_ID);
            }
        }catch (SQLException ex){
            log.error("selectLicenseRequestTaskIdFromId(SQLException) " + ex.getMessage());
        }
        return taskId;
    }

    public JsonArray insertTempLib(String libraryName, String libraryVersion,String libraryFileName,String parent,String type,int licenseRequestId){
        String query;
        LM_LICENSEREQUEST lr = new LM_LICENSEREQUEST();
        LM_PRODUCT lp = new LM_PRODUCT();
        JsonArray responseJson = new JsonArray();
        try{
            query = "INSERT INTO LM_TEMPLIB(TEMPLIB_NAME,TEMPLIB_VERSION,TEMPLIB_FILE_NAME,TEMPLIB_PARENT,TEMPLIB_TYPE,TEMPLIB_LR_ID) \n" +
                    "VALUES(?,?,?,?,?,?)";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1,libraryName);
            preparedStatement.setString(2,libraryVersion);
            preparedStatement.setString(3,libraryFileName);
            preparedStatement.setString(4,parent);
            preparedStatement.setString(5,type);
            preparedStatement.setInt(6,licenseRequestId);
            preparedStatement.execute();

        }catch (SQLException ex){
            log.error("insertTempLib(SQLException) " + ex.getMessage());
        }
        return responseJson;
    }

    public JsonArray insertTempComponent(String key, String name,String type,String version,String fileName,int licenseRequestId){
        String query;
        LM_LICENSEREQUEST lr = new LM_LICENSEREQUEST();
        LM_PRODUCT lp = new LM_PRODUCT();
        JsonArray responseJson = new JsonArray();
        try{
            query = "INSERT INTO LM_TEMPCOMPONENT(TC_KEY,TC_NAME,TC_TYPE,TC_VERSION,TC_FILE_NAME,TC_LR_ID) \n" +
                    "VALUES(?,?,?,?,?,?);";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1,key);
            preparedStatement.setString(2,name);
            preparedStatement.setString(3,type);
            preparedStatement.setString(4,version);
            preparedStatement.setString(5,fileName);
            preparedStatement.setInt(6,licenseRequestId);
            preparedStatement.execute();

        }catch (SQLException ex){
            log.error("insertTempComponent(SQLException) " + ex.getMessage());
        }
        return responseJson;
    }

    public JsonArray selectWaitingLicenseComponents(int licenseRequestId){
        String query;
        LM_TEMPCOMPONENT tc = new LM_TEMPCOMPONENT();
        JsonArray responseJson = new JsonArray();
        try{
            query = "SELECT LM_LICENSEREQUEST.*, LM_TEMPCOMPONENT.* FROM LM_TEMPCOMPONENT \n" +
                    "INNER JOIN LM_LICENSEREQUEST ON LM_TEMPCOMPONENT.TC_LR_ID = LM_LICENSEREQUEST.LR_ID\n" +
                    "WHERE LM_LICENSEREQUEST.LR_ID = ?";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1,licenseRequestId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                JsonObject waitingComponent = new JsonObject();
                waitingComponent.addProperty(tc.TC_ID,rs.getString(tc.TC_ID));
                waitingComponent.addProperty(tc.TC_KEY,rs.getString(tc.TC_KEY));
                waitingComponent.addProperty(tc.TC_NAME,rs.getString(tc.TC_NAME));
                waitingComponent.addProperty(tc.TC_VERSION,rs.getString(tc.TC_VERSION));
                waitingComponent.addProperty(tc.TC_FILE_NAME,rs.getString(tc.TC_FILE_NAME));
                waitingComponent.addProperty("licenseId",0);
                responseJson.add(waitingComponent);
            }

        }catch (SQLException ex){
            log.error("selectWaitingLicenseComponents(SQLException) " + ex.getMessage());
        }
        return responseJson;
    }

    public JsonArray selectWaitingLicenseLibraries(int licenseRequestId){
        String query;
        LM_TEMPLIB tl = new LM_TEMPLIB();
        JsonArray responseJson = new JsonArray();
        try{
            query = "SELECT LM_LICENSEREQUEST.*, LM_TEMPLIB.* FROM LM_TEMPLIB \n" +
                    "INNER JOIN LM_LICENSEREQUEST ON LM_TEMPLIB.TEMPLIB_LR_ID = LM_LICENSEREQUEST.LR_ID\n" +
                    "WHERE LM_LICENSEREQUEST.LR_ID = ?";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1,licenseRequestId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                JsonObject waitingLibrary = new JsonObject();
                waitingLibrary.addProperty(tl.TEMPLIB_ID,rs.getString(tl.TEMPLIB_ID));
                waitingLibrary.addProperty(tl.TEMPLIB_NAME,rs.getString(tl.TEMPLIB_NAME));
                waitingLibrary.addProperty(tl.TEMPLIB_VERSION,rs.getString(tl.TEMPLIB_VERSION));
                waitingLibrary.addProperty(tl.TEMPLIB_TYPE,rs.getString(tl.TEMPLIB_TYPE));
                waitingLibrary.addProperty(tl.TEMPLIB_FILE_NAME,rs.getString(tl.TEMPLIB_FILE_NAME));
                waitingLibrary.addProperty(tl.TEMPLIB_PARENT,rs.getString(tl.TEMPLIB_PARENT));
                waitingLibrary.addProperty("licenseId",0);
                responseJson.add(waitingLibrary);
            }
        }catch (SQLException ex){
            log.error("selectWaitingLicenseLibraries(SQLException) " + ex.getMessage());
        }
        return responseJson;
    }

    public JsonArray selectWaitingLicenseComponentsWithLicense(int lrId){
        String query;
        LM_TEMPCOMPONENT tc = new LM_TEMPCOMPONENT();
        LM_TEMPCOMPONENT_LICENSE tcl = new LM_TEMPCOMPONENT_LICENSE();
        LM_LICENSE l = new LM_LICENSE();
        JsonArray responseJson = new JsonArray();
        try{
            query = "SELECT LM_LICENSEREQUEST.*, LM_TEMPCOMPONENT.*,LM_TEMPCOMPONENT_LICENSE.*,LM_LICENSE.LICENSE_NAME FROM LM_TEMPCOMPONENT\n" +
                    "INNER JOIN LM_LICENSEREQUEST ON LM_TEMPCOMPONENT.TC_LR_ID = LM_LICENSEREQUEST.LR_ID\n" +
                    "INNER JOIN LM_TEMPCOMPONENT_LICENSE ON LM_TEMPCOMPONENT_LICENSE.TC_KEY = LM_TEMPCOMPONENT.TC_KEY\n" +
                    "INNER JOIN LM_LICENSE ON LM_TEMPCOMPONENT_LICENSE.LICENSE_KEY = LM_LICENSE.LICENSE_KEY\n" +
                    "WHERE LM_LICENSEREQUEST.LR_ID = ?";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1,lrId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                JsonObject waitingComponent = new JsonObject();
                waitingComponent.addProperty(tc.TC_ID,rs.getString(tc.TC_ID));
                waitingComponent.addProperty(tc.TC_KEY,rs.getString(tc.TC_KEY));
                waitingComponent.addProperty(tc.TC_NAME,rs.getString(tc.TC_NAME));
                waitingComponent.addProperty(tc.TC_VERSION,rs.getString(tc.TC_VERSION));
                waitingComponent.addProperty(tc.TC_FILE_NAME,rs.getString(tc.TC_FILE_NAME));
                waitingComponent.addProperty(tcl.LICENSE_KEY,rs.getString(tcl.LICENSE_KEY));
                waitingComponent.addProperty(l.LICENSE_NAME,rs.getString(l.LICENSE_NAME));
                waitingComponent.addProperty("licenseId",0);
                responseJson.add(waitingComponent);
            }

        }catch (SQLException ex){
            log.error("selectWaitingLicenseComponentsWithLicense(SQLException) " + ex.getMessage());
        }
        return responseJson;
    }

    public JsonArray selectWaitingLicenseLibrariesWithLicense(int lrId){
        String query;
        LM_TEMPLIB tl = new LM_TEMPLIB();
        LM_TEMPLIB_LICENSE tll = new LM_TEMPLIB_LICENSE();
        LM_LICENSE l = new LM_LICENSE();
        JsonArray responseJson = new JsonArray();
        try{
            query = "SELECT LM_LICENSEREQUEST.*, LM_TEMPLIB.*, LM_TEMPLIB_LICENSE.*,LM_LICENSE.LICENSE_NAME FROM LM_TEMPLIB \n" +
                    "INNER JOIN LM_LICENSEREQUEST ON LM_TEMPLIB.TEMPLIB_LR_ID = LM_LICENSEREQUEST.LR_ID \n" +
                    "INNER JOIN LM_TEMPLIB_LICENSE ON LM_TEMPLIB_LICENSE.TEMPLIB_ID = LM_TEMPLIB.TEMPLIB_ID\n" +
                    "INNER JOIN LM_LICENSE ON LM_TEMPLIB_LICENSE.LICENSE_KEY = LM_LICENSE.LICENSE_KEY\n" +
                    "WHERE LM_LICENSEREQUEST.LR_ID = ?";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1,lrId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()){
                JsonObject waitingLibrary = new JsonObject();
                waitingLibrary.addProperty(tl.TEMPLIB_ID,rs.getString(tl.TEMPLIB_ID));
                waitingLibrary.addProperty(tl.TEMPLIB_NAME,rs.getString(tl.TEMPLIB_NAME));
                waitingLibrary.addProperty(tl.TEMPLIB_VERSION,rs.getString(tl.TEMPLIB_VERSION));
                waitingLibrary.addProperty(tl.TEMPLIB_TYPE,rs.getString(tl.TEMPLIB_TYPE));
                waitingLibrary.addProperty(tl.TEMPLIB_FILE_NAME,rs.getString(tl.TEMPLIB_FILE_NAME));
                waitingLibrary.addProperty(tl.TEMPLIB_PARENT,rs.getString(tl.TEMPLIB_PARENT));
                waitingLibrary.addProperty(tll.LICENSE_KEY,rs.getString(tll.LICENSE_KEY));
                waitingLibrary.addProperty(l.LICENSE_NAME,rs.getString(l.LICENSE_NAME));
                waitingLibrary.addProperty("licenseId",0);
                responseJson.add(waitingLibrary);
            }
        }catch (SQLException e){
            log.error("selectWaitingLicenseLibrariesWithLicense(SQLException) " + e.getMessage());
        }
        return responseJson;
    }

    public void insertComponent(String name, String fileName, String version) throws DataSetException {
        LM_COMPONENT compTab = new LM_COMPONENT();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, compTab.table);
            record = tds.addRecord();
            record.setValue(compTab.COMP_NAME, name)
                    .setValue(compTab.COMP_FILE_NAME, fileName)
                    .setValue(compTab.COMP_KEY, fileName)
                    .setValue(compTab.COMP_TYPE,"bundle")
                    .setValue(compTab.COMP_VERSION, version).save(con);
        } catch (SQLException ex) {
            log.error("insertComponent(SQLException) " + ex.getMessage());
        }
    }

    public int insertLibrary(String name, String fileName, String version,String type) throws DataSetException {
        LM_LIBRARY libTab = new LM_LIBRARY();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, libTab.table);
            record = tds.addRecord();
            record.setValue(libTab.LIB_NAME, name)
                    .setValue(libTab.LIB_FILE_NAME, fileName)
                    .setValue(libTab.LIB_TYPE,type)
                    .setValue(libTab.LIB_VERSION, version)
                    .save(con);
            Statement stmt;
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID();");
            rs.next();
            return rs.getInt("LAST_INSERT_ID()");
        } catch (SQLException ex) {
            log.error("insertLibrary(SQLException) " + ex.getMessage());
        }
        return -1;
    }

    public void insertProductComponent(String compKey, int productId) throws  DataSetException{
        LM_COMPONENT_PRODUCT compprodtab = new LM_COMPONENT_PRODUCT();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, compprodtab.table);
            record = tds.addRecord();
            record.setValue(compprodtab.COMP_KEY, compKey)
                    .setValue(compprodtab.PRODUCT_ID,productId)
                    .save();
        } catch (SQLException ex) {
            log.error("insertProductComponent(SQLException) " + ex.getMessage());
        }


    }

    public void insertProductLibrary(int libId, int productId) throws DataSetException{
        LM_LIBRARY_PRODUCT libprodtab = new LM_LIBRARY_PRODUCT();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, libprodtab.table);
            record = tds.addRecord();
            record.setValue(libprodtab.LIB_ID, libId)
                    .setValue(libprodtab.PRODUCT_ID,productId)
                    .save();
        } catch (SQLException ex) {
            log.error("insertProductComponent(SQLException) " + ex.getMessage());
        }
    }

    public void insertComponentLibrary(String component,int libraryId) throws DataSetException{
        LM_COMPONENT_LIBRARY complibtab=new LM_COMPONENT_LIBRARY();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, complibtab.table);
            record = tds.addRecord();
            record.setValue(complibtab.LIB_ID, libraryId)
                    .setValue(complibtab.COMP_KEY,component)
                    .save();
        } catch (SQLException ex) {
            log.error("insertComponentLibrary(SQLException) " + ex.getMessage());
        }
    }

    public boolean insertTempLib(String name, String fileName, String version,boolean isBundle,MyJar parent, int licenseRequestId) throws DataSetException {
        LM_TEMPLIB libTab = new LM_TEMPLIB();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, libTab.table);
            record = tds.addRecord();
            record.setValue(libTab.TEMPLIB_NAME, name)
                    .setValue(libTab.TEMPLIB_FILE_NAME, fileName)
                    .setValue(libTab.TEMPLIB_TYPE,(parent==null)?((isBundle)?"bundle":"jar"):"jarinbundle")
                    .setValue(libTab.TEMPLIB_VERSION, version)
                    .setValue(libTab.TEMPLIB_LR_ID,licenseRequestId)
                    .save(con);
        } catch (SQLException ex) {
            log.error("insertTempLib(SQLException) " + ex.getMessage());
            return false;
        }
        return true;
    }

    public int insertTempLibAndGetId(String name, String fileName, String version,boolean isBundle,MyJar parent, int licenseRequestId) throws DataSetException {
        LM_TEMPLIB libTab = new LM_TEMPLIB();
        TableDataSet tds;
        Record record;
        String parentStr = "";
        try {
            tds = new TableDataSet(con, libTab.table);
            record = tds.addRecord();
            if(parent != null){
                parentStr = parent.getProjectName();
            }
            record.setValue(libTab.TEMPLIB_NAME, name)
                    .setValue(libTab.TEMPLIB_FILE_NAME, fileName)
                    .setValue(libTab.TEMPLIB_TYPE,(parent==null)?((isBundle)?"bundle":"jar"):"jarinbundle")
                    .setValue(libTab.TEMPLIB_VERSION, version)
                    .setValue(libTab.TEMPLIB_PARENT, parentStr)
                    .setValue(libTab.TEMPLIB_LR_ID,licenseRequestId)
                    .save(con);
            Statement stmt;
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID();");
            rs.next();
            return rs.getInt("LAST_INSERT_ID()");
        } catch (SQLException ex) {
            log.error("insertTempLibAndGetId(SQLException) " + ex.getMessage());

        }
        return -1;
    }

    public void insertTempLibLicense(String licenseKey, String libId) throws DataSetException {
        LM_TEMPLIB_LICENSE liblicTab = new LM_TEMPLIB_LICENSE();
        TableDataSet tds;
        Record record;
        try {

            tds = new TableDataSet(con, liblicTab.table);
            record = tds.addRecord();
            record.setValue(liblicTab.TEMPLIB_ID, libId)
                    .setValue(liblicTab.LICENSE_KEY,licenseKey)
                    .save();
        } catch (SQLException ex) {
            log.error("insertTempLibLicense(SQLException) " + ex.getMessage());

        }
    }

    public void insertProductTempLib(int libId,int productId) throws DataSetException{
        LM_TEMPLIB_PRODUCT libprodtab = new LM_TEMPLIB_PRODUCT();
        TableDataSet tds;
        Record record;
        try {

            tds = new TableDataSet(con, libprodtab.table);
            record = tds.addRecord();
            record.setValue(libprodtab.TEMPLIB_ID, libId)
                    .setValue(libprodtab.PRODUCT_ID,productId)
                    .save();
        } catch (SQLException ex) {
            log.error("insertProductTempLib(SQLException) " + ex.getMessage());
        }
    }

    public void insertComponentTempLib(String component,int libraryId) throws DataSetException{
        LM_COMPONENT_TEMPLIB complibtab=new LM_COMPONENT_TEMPLIB();
        TableDataSet tds;
        Record record;
        try {

            tds = new TableDataSet(con, complibtab.table);
            record = tds.addRecord();
            record.setValue(complibtab.TEMPLIB_ID, libraryId)
                    .setValue(complibtab.COMP_KEY,component)
                    .save();
        } catch (SQLException ex) {
            log.error("insertComponentTempLib(SQLException) " + ex.getMessage());

        }
    }

    public void insertComponentLicense(String compKey, String licenseKey) throws DataSetException {
        LM_COMPONENT_LICENSE complicTab = new LM_COMPONENT_LICENSE();
        TableDataSet tds;
        Record record;
        try {

            tds = new TableDataSet(con, complicTab.table);
            record = tds.addRecord();
            record.setValue(complicTab.COMP_KEY, compKey).setValue(
                    complicTab.LICENSE_KEY, licenseKey).save();
        } catch (SQLException ex) {
            log.error("insertComponentLicense(SQLException) " + ex.getMessage());
        }
    }

    public void insertTempComponentLicense(String compKey, String licenseKey) throws DataSetException {
        LM_TEMPCOMPONENT_LICENSE complicTab = new LM_TEMPCOMPONENT_LICENSE();
        TableDataSet tds;
        Record record;
        try {

            tds = new TableDataSet(con, complicTab.table);
            record = tds.addRecord();
            record.setValue(complicTab.TC_KEY, compKey)
                    .setValue(complicTab.LICENSE_KEY, licenseKey)
                    .save();
        } catch (SQLException ex) {
            log.error("insertTempComponentLicense(SQLException) " + ex.getMessage());
        }
    }

    public int insertLicenseRequest(String requestBy, int productId) throws DataSetException {
        LM_LICENSEREQUEST licenserequest = new LM_LICENSEREQUEST();
        TableDataSet tds;
        Record record;
        try {

            tds = new TableDataSet(con, licenserequest.table);
            record = tds.addRecord();
            record.setValue(licenserequest.LR_REQUEST_BY, requestBy)
                    .setValue(licenserequest.LR_PRODUCT_ID, productId)
                    .save();
            Statement stmt;
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID();");
            rs.next();
            return rs.getInt("LAST_INSERT_ID()");
        } catch (SQLException ex) {
            log.error("insertLicenseRequest(SQLException) " + ex.getMessage());
        }
        return -1;
    }

    public void insertLibraryLicense(String licenseKey, String libId) throws DataSetException {
        LM_LIBRARY_LICENSE liblicTab = new LM_LIBRARY_LICENSE();
        TableDataSet tds;
        Record record;
        try {

            tds = new TableDataSet(con, liblicTab.table);
            record = tds.addRecord();
            record.setValue(liblicTab.LIB_ID, libId)
                    .setValue(liblicTab.LICENSE_KEY,licenseKey)
                    .save();
        } catch (SQLException ex) {
            log.error("insertLibraryLicense(SQLException) " + ex.getMessage());
        }
    }

    public void updateLicenseRequestIds(String processId, String taskId, int licenseId){
        String query;
        try{
            query = "UPDATE LM_LICENSEREQUEST SET LR_BPMN_PROCESS_ID = ?, LR_BPMN_TASK_ID = ? WHERE LR_ID = ?";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1,processId);
            preparedStatement.setString(2,taskId);
            preparedStatement.setInt(3,licenseId);
            preparedStatement.executeUpdate();
        }catch (SQLException e){
            log.error("updateLicenseRequestIds(SQLException) " + e.getMessage());
        }
    }

    public void updateLicenseRequestAccept(int licenseId){
        String query;
        try{
            query = "UPDATE LM_LICENSEREQUEST SET LR_STATE = 'ACCEPT' WHERE LR_ID = ?";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1,licenseId);
            preparedStatement.executeUpdate();
        }catch (SQLException e){
            log.error("updateLicenseRequestAccept(SQLException) " + e.getMessage());
        }
    }

    public void updateLicenseRequestReject(String rejectBy,int licenseId){
        String query;
        try{
            query = "UPDATE LM_LICENSEREQUEST SET LR_STATE = 'REJECT',LR_ACCEPT_OR_REJECT_BY = ? WHERE LR_ID = ?";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1,rejectBy);
            preparedStatement.setInt(2,licenseId);
            preparedStatement.executeUpdate();
        }catch (SQLException e){
            log.error("updateLicenseRequestReject(SQLException) " + e.getMessage());
        }
    }

    public boolean isLicenseAdmin(String token){
        String[] tokenValues;
        byte[] keyBytes, payloadBytes;
        String message,key,query,email;
        boolean returnValue = false;
        ConfigurationReader configurationReader = new ConfigurationReader();
        JsonParser jsonParser = new JsonParser();
        JsonObject payloadJson;
        try{
            Configuration configuration = configurationReader.getConfigurations();
            tokenValues = token.split("\\.");
            message = tokenValues[0] + "." + tokenValues[1];
            key = configuration.getPublicKey();
            keyBytes = Base64.getDecoder().decode(key.getBytes());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPublicKey rsaPublicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(keyBytes));
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initVerify(rsaPublicKey);
            sign.update(message.getBytes("UTF-8"));
            byte[] signBytes = Base64.getDecoder().decode(tokenValues[2].replace('-', '+').replace('_', '/').getBytes(StandardCharsets.UTF_8));
            returnValue = sign.verify(signBytes);
            payloadBytes = Base64.getDecoder().decode(tokenValues[1]);
            String payloadString = new String(payloadBytes,StandardCharsets.UTF_8);
            payloadJson = jsonParser.parse(payloadString).getAsJsonObject();
            email = payloadJson.get("http://wso2.org/claims/emailaddress").getAsString();
            query = "SELECT * FROM LM_ROLE WHERE ROLE_EMAIL = ? AND ROLE_TYPE = 'LICENSE' AND ROLE_PERMISSION = 'ADMIN'";
            PreparedStatement preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1,email);
            ResultSet rs = preparedStatement.executeQuery();
            rs.last();
            int rowCount = rs.getRow();
            if(rowCount > 0){
                returnValue = true;
            }else{
                returnValue = false;
            }

        }catch (Exception e ){
            returnValue = false;
            log.error("updateLicenseRequestReject(SQLException) " + e.getMessage());
        }

        return returnValue;

    }
}
