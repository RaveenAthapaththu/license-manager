package org.wso2.internalapps.dependencymanager.Database;

string LM_DB_LIBRARY_DROP_DOWN = "SELECT DISTINCT LIB_NAME
                                   FROM LM_LIBRARY
                                   WHERE LIB_NAME <> ''
                                   ORDER BY LIB_NAME";

string LM_DB_PRODUCT_DROP_DOWN = "SELECT DISTINCT(PRODUCT_NAME)
                                    FROM LM_PRODUCT
                                    ORDER BY PRODUCT_NAME";

string LM_DB_COMPONENT_DROP_DOWN = "SELECT DISTINCT(COMP_NAME)
                                    FROM LM_COMPONENT
                                    ORDER BY COMP_NAME";

string LM_DB_LIBRARY_VERSION_DROP_DOWN = "SELECT DISTINCT LIB_VERSION
                                            FROM LM_LIBRARY
                                            WHERE LIB_NAME = ? AND LIB_VERSION <> ''";

string LM_DB_PRODUCT_VERSION_DROP_DOWN = "SELECT DISTINCT PRODUCT_VERSION
                                            FROM LM_PRODUCT
                                            WHERE PRODUCT_NAME = ? AND PRODUCT_VERSION <> ''";

string LM_DB_COMPONENT_VERSION_DROP_DOWN = "SELECT DISTINCT COMP_VERSION
                                            FROM LM_COMPONENT
                                            WHERE COMP_NAME = ? AND COMP_VERSION <> ''";

string LM_DB_LIBRARY_ARTIFACT_GROUP = "SELECT LIB_ARTIFACT_ID, LIB_GROUP_ID, LIB_TYPE
                                            FROM LM_LIBRARY
                                            WHERE LIB_NAME = ? AND LIB_VERSION = ?";

string LM_DB_PRODUCT_LIBRARIES = "SELECT DISTINCT lb.LIB_ID, lb.LIB_NAME, lb.LIB_VERSION, lb.LIB_TYPE, lb.LIB_ARTIFACT_ID, lb.LIB_GROUP_ID
                                FROM LM_LIBRARY as lb
                                INNER JOIN LM_LIBRARY_PRODUCT as lp ON lp.LIB_ID = lb.LIB_ID
                                INNER JOIN LM_PRODUCT as pr ON lp.PRODUCT_ID = pr.PRODUCT_ID
                                WHERE pr.PRODUCT_NAME = ? AND pr.PRODUCT_VERSION = ?";

string LM_DB_PROD_LIBRARIES = "SELECT DISTINCT lb.LIB_ID, lb.LIB_NAME, lb.LIB_VERSION, lb.LIB_TYPE, lb.LIB_ARTIFACT_ID, lb.LIB_GROUP_ID, lb.LIB_DATE, lb.LIB_LATEST_VERSION,lb.LIB_LATEST_DATE
                                FROM LM_LIBRARY as lb
                                INNER JOIN LM_LIBRARY_PRODUCT as lp ON lp.LIB_ID = lb.LIB_ID
                                INNER JOIN LM_PRODUCT as pr ON lp.PRODUCT_ID = pr.PRODUCT_ID
                                WHERE pr.PRODUCT_NAME = ? AND pr.PRODUCT_VERSION = ?";

string LM_DB_PROD_LIBRARIES_OUTDATED = "SELECT DISTINCT lb.LIB_ID, lb.LIB_NAME, lb.LIB_VERSION, lb.LIB_TYPE, lb.LIB_ARTIFACT_ID, lb.LIB_GROUP_ID, lb.LIB_DATE, lb.LIB_LATEST_VERSION,lb.LIB_LATEST_DATE
                                FROM LM_LIBRARY as lb
                                INNER JOIN LM_LIBRARY_PRODUCT as lp ON lp.LIB_ID = lb.LIB_ID
                                INNER JOIN LM_PRODUCT as pr ON lp.PRODUCT_ID = pr.PRODUCT_ID
                                WHERE pr.PRODUCT_NAME = ? AND pr.PRODUCT_VERSION = ? lb.LIB_DATE < ?";

string LM_DB_PROD_LIBRARIES_DATE_UNKNOWN = "SELECT DISTINCT lb.LIB_ID, lb.LIB_NAME, lb.LIB_VERSION, lb.LIB_TYPE, lb.LIB_ARTIFACT_ID, lb.LIB_GROUP_ID, lb.LIB_DATE, lb.LIB_LATEST_VERSION,lb.LIB_LATEST_DATE
                                FROM LM_LIBRARY as lb
                                INNER JOIN LM_LIBRARY_PRODUCT as lp ON lp.LIB_ID = lb.LIB_ID
                                INNER JOIN LM_PRODUCT as pr ON lp.PRODUCT_ID = pr.PRODUCT_ID
                                WHERE pr.PRODUCT_NAME = ? AND pr.PRODUCT_VERSION = ? lb.LIB_DATE is null";

string LM_DB_COMPONENT_LIBRARIES_DETAIL = "SELECT DISTINCT lb.LIB_ID, lb.LIB_NAME, lb.LIB_VERSION, lb.LIB_TYPE, lb.LIB_ARTIFACT_ID, lb.LIB_GROUP_ID
                                FROM LM_LIBRARY as lb
                                INNER JOIN LM_COMPONENT_LIBRARY as lp ON lp.LIB_ID = lb.LIB_ID
                                INNER JOIN LM_COMPONENT as cm ON lp.COMP_KEY = cm.COMP_KEY
                                WHERE cm.COMP_NAME = ? AND cm.COMP_VERSION = ?";

string LM_DB_COMP_LIBRARIES_DETAIL = "SELECT DISTINCT lb.LIB_ID, lb.LIB_NAME, lb.LIB_VERSION, lb.LIB_TYPE, lb.LIB_ARTIFACT_ID, lb.LIB_GROUP_ID, lb.LIB_DATE, lb.LIB_LATEST_VERSION,lb.LIB_LATEST_DATE
                                FROM LM_LIBRARY as lb
                                INNER JOIN LM_COMPONENT_LIBRARY as lp ON lp.LIB_ID = lb.LIB_ID
                                INNER JOIN LM_COMPONENT as cm ON lp.COMP_KEY = cm.COMP_KEY
                                WHERE cm.COMP_NAME = ? AND cm.COMP_VERSION = ?";

string LM_DB_COMP_LIBRARIES_OUTDATED = "SELECT DISTINCT lb.LIB_ID, lb.LIB_NAME, lb.LIB_VERSION, lb.LIB_TYPE, lb.LIB_ARTIFACT_ID, lb.LIB_GROUP_ID, lb.LIB_DATE, lb.LIB_LATEST_VERSION,lb.LIB_LATEST_DATE
                                FROM LM_LIBRARY as lb
                                INNER JOIN LM_COMPONENT_LIBRARY as lp ON lp.LIB_ID = lb.LIB_ID
                                INNER JOIN LM_COMPONENT as cm ON lp.COMP_KEY = cm.COMP_KEY
                                WHERE cm.COMP_NAME = ? AND cm.COMP_VERSION = ? AND lb.LIB_DATE < ?";

string LM_DB_COMP_LIBRARIES_DATE_UNKNOWN = "SELECT DISTINCT lb.LIB_ID, lb.LIB_NAME, lb.LIB_VERSION, lb.LIB_TYPE, lb.LIB_ARTIFACT_ID, lb.LIB_GROUP_ID, lb.LIB_DATE, lb.LIB_LATEST_VERSION,lb.LIB_LATEST_DATE
                                FROM LM_LIBRARY as lb
                                INNER JOIN LM_COMPONENT_LIBRARY as lp ON lp.LIB_ID = lb.LIB_ID
                                INNER JOIN LM_COMPONENT as cm ON lp.COMP_KEY = cm.COMP_KEY
                                WHERE cm.COMP_NAME = ? AND cm.COMP_VERSION = ? AND lb.LIB_DATE is null";

string LM_DB_LIBRARY_PRODUCTS="SELECT DISTINCT pr.PRODUCT_ID, pr.PRODUCT_NAME, pr.PRODUCT_VERSION
                                FROM LM_PRODUCT as pr
                                INNER JOIN LM_LIBRARY_PRODUCT as lp ON lp.PRODUCT_ID = pr.PRODUCT_ID
                                INNER JOIN LM_LIBRARY as lb ON lp.LIB_ID = lb.LIB_ID
                                WHERE lb.LIB_NAME = ? AND lb.LIB_VERSION = ?";

string LM_DB_LIBRARY_COMPONENTS="SELECT DISTINCT cm.COMP_ID, cm.COMP_NAME, cm.COMP_TYPE, cm.COMP_VERSION
                                FROM LM_COMPONENT as cm
                                INNER JOIN LM_COMPONENT_LIBRARY as lp ON lp.COMP_KEY = cm.COMP_KEY
                                INNER JOIN LM_LIBRARY as lb ON lp.LIB_ID = lb.LIB_ID
                                WHERE lb.LIB_NAME = ? AND lb.LIB_VERSION = ?";

string LM_DB_COMPONENT_PRODUCTS = "SELECT DISTINCT pr.PRODUCT_ID, pr.PRODUCT_NAME, pr.PRODUCT_VERSION
                                    FROM LM_PRODUCT as pr
                                    INNER JOIN LM_COMPONENT_PRODUCT as cp ON cp.PRODUCT_ID = pr.PRODUCT_ID
                                    INNER JOIN LM_COMPONENT as cm ON cm.COMP_KEY = cp.COMP_KEY
                                    WHERE cm.COMP_NAME = ? AND cm.COMP_VERSION = ?";

string LM_DB_COMPONENT_DETAILS = "SELECT COMP_TYPE
                                    FROM LM_COMPONENT
                                    WHERE COMP_NAME = ? AND COMP_VERSION = ?";

string LM_DB_PRODUCT_AREAS = "SELECT *
                                FROM LM_PRODUCT_AREA
                                ORDER BY LM_PRODUCT_AREA_NAME";

string LM_DB_PRODUCTS = "SELECT DISTINCT PRODUCT_NAME
                                FROM LM_PRODUCT
                                WHERE PRODUCT_AREA_ID = (SELECT LM_PRODUCT_AREA_ID FROM LM_PRODUCT_AREA WHERE LM_PRODUCT_AREA_NAME=?)
                                ORDER BY PRODUCT_NAME";

string LM_DB_PRODUCTS = "SELECT DISTINCT PRODUCT_NAME
                                FROM LM_PRODUCT
                                WHERE PRODUCT_AREA_ID = (SELECT LM_PRODUCT_AREA_ID FROM LM_PRODUCT_AREA WHERE LM_PRODUCT_AREA_NAME=?)
                                ORDER BY PRODUCT_NAME";




