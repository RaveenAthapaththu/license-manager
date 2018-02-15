package org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.mail.create;

import java.sql.ResultSet;

public class MailBody {
    public static String createEmailBody(ResultSet results){
        StringBuilder email = new StringBuilder();
        int count = 1;
        try{
            email.append("<html><body>");
            email.append("Hi,<br/>The Group ID and Artifact ID columns of the following rows in LM_LIBRARY table of LicenceManager database need to be Updated.<br/></br/>");
            email.append("<table style='border:2px solid black'>");
            email.append("<tr bgcolor=\"#F84B23\">");
            email.append("<td></td>");
            email.append("<td>Library ID</td>");
            email.append("<td>Library Name</td>");
            email.append("<td>Library Version</td>");
            email.append("<td>Library Type</td>");
            email.append("<td>Library FileName</td>"+"</tr>");


            while (results.next()) {
                email.append("<tr bgcolor=\"#F8EE23\">");
                email.append("<td>");
                email.append(count++);
                email.append("</td>");

                email.append("<td>");
                email.append(results.getString("LIB_ID"));
                email.append("</td>");

                email.append("<td>");
                email.append(results.getString("LIB_NAME"));
                email.append("</td>");

                email.append("<td>");
                email.append(results.getString("LIB_VERSION"));
                email.append("</td>");

                email.append("<td>");
                email.append(results.getString("LIB_TYPE"));
                email.append("</td>");

                email.append("<td>");
                email.append(results.getString("LIB_FILE_NAME"));
                email.append("</td>");


                email.append("<tr>");
            }

            email.append("</table>");
            email.append("<br/>Thanks!<br/>Jayathma Chathurangani<br/>Intern - Software Engineer<br/>WSO2");
            email.append("</body></html>");
            return email.toString();
        }catch (Exception e){
            System.out.println("Error in MailBody Creation: "+e);
        }
        return "<html><body>Error</body></html>";
    }
}
