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

import org.testng.annotations.Test;
import org.wso2.internal.apps.license.manager.models.NewLicenseEntry;
import org.wso2.internal.apps.license.manager.util.EmailUtils;

import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;

/**
 * Test the email sending
 */
public class EmailTester {

    @Test
    public void testEmail() {

        List<NewLicenseEntry> components = new ArrayList<>();
        List<NewLicenseEntry> lib = new ArrayList<>();

        NewLicenseEntry newLicenseEntry1 =
                new NewLicenseEntry("xx-1.0.0", "xx", "1.0.0", "apache2");
        components.add(newLicenseEntry1);
        NewLicenseEntry newLicenseEntry2 =
                new NewLicenseEntry("yy-1.0.1", "yy", "1.0.1", "apache2");
        components.add(newLicenseEntry2);

        NewLicenseEntry newLicenseEntry3 =
                new NewLicenseEntry("xx-1.2.3", "xx", "1.2.3", "apache2");
        components.add(newLicenseEntry3);
        NewLicenseEntry newLicenseEntry4 =
                new NewLicenseEntry("xx-1.0.0", "xx", "1.0.0", "apache2");
        components.add(newLicenseEntry4);
        NewLicenseEntry newLicenseEntry5 =
                new NewLicenseEntry("yy-1.0.1", "yy", "1.0.1", "apache2");
        components.add(newLicenseEntry5);

        NewLicenseEntry newLicenseEntry6 =
                new NewLicenseEntry("xx-1.2.3", "xx", "1.2.3", "apache2");
        components.add(newLicenseEntry6);
        NewLicenseEntry newLicenseEntry7 =
                new NewLicenseEntry("xx-1.0.0", "xx", "1.0.0", "apache2");
        components.add(newLicenseEntry7);
        NewLicenseEntry newLicenseEntry8 =
                new NewLicenseEntry("yy-1.0.1", "yy", "1.0.1", "apache2");
        components.add(newLicenseEntry8);

        NewLicenseEntry newLicenseEntry9 =
                new NewLicenseEntry("xx-1.2.3", "xx", "1.2.3", "apache2");
        components.add(newLicenseEntry9);


        try {
            EmailUtils.sendEmail("Pamoda Wimalasiri",components,lib);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
