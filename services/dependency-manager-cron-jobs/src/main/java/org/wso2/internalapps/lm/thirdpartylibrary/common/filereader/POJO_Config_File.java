package org.wso2.internalapps.lm.thirdpartylibrary.common.filereader;

public class POJO_Config_File {
    private String DBURL;
    private String USER;
    private String PASS;
    private String RECIEVER_EMAIL;
    private String CSV_FILEPATH;
    private String CSV_FILENAME;
    private String NUM_THREAD;
    private String ROUTER_URL;

    public String getROUTER_URL() {
        return ROUTER_URL;
    }

    public void setROUTER_URL(String ROUTER_URL) {
        this.ROUTER_URL = ROUTER_URL;
    }

    public String getCSV_FILEPATH() {
        return CSV_FILEPATH;
    }

    public void setCSV_FILEPATH(String CSV_FILEPATH) {
        this.CSV_FILEPATH = CSV_FILEPATH;
    }

    public String getCSV_FILENAME() {
        return CSV_FILENAME;
    }

    public void setCSV_FILENAME(String CSV_FILENAME) {
        this.CSV_FILENAME = CSV_FILENAME;
    }

    public String getNUM_THREAD() {
        return NUM_THREAD;
    }

    public void setNUM_THREAD(String NUM_THREAD) {
        this.NUM_THREAD = NUM_THREAD;
    }

    public String getDBURL() {
        return DBURL;
    }

    public void setDBURL(String DBURL) {
        this.DBURL = DBURL;
    }

    public String getUSER() {
        return USER;
    }

    public void setUSER(String USER) {
        this.USER = USER;
    }

    public String getPASS() {
        return PASS;
    }

    public void setPASS(String PASS) {
        this.PASS = PASS;
    }

    public String getRECIEVER_EMAIL() {
        return RECIEVER_EMAIL;
    }

    public void setRECIEVER_EMAIL(String RECIEVER_EMAIL) {
        this.RECIEVER_EMAIL = RECIEVER_EMAIL;
    }
}
