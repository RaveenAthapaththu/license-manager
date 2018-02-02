/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.licensemanager.work.main;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 *
 * @author pubudu
 */
public class Unzip {

    public static void unzip(String infile,String outFolder) {
        Enumeration entries;
        ZipFile zipFile;

        try {
            zipFile = new ZipFile(infile);
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File f=new File(outFolder+File.separator+entry.getName());
                if (!entry.isDirectory()){
                    f.getParentFile().mkdirs();
                    copyInputStream(zipFile.getInputStream(entry),new BufferedOutputStream(new FileOutputStream(f.getAbsolutePath())));
                }
            }
            zipFile.close();
        } catch (IOException ioe) {            
            ioe.printStackTrace();
            return;
        }catch (Exception ioe) {
            ioe.printStackTrace();
            return;
        }
    }

    public static final void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }
}
