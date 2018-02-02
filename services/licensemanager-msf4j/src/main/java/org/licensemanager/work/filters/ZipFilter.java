/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.licensemanager.work.filters;

import java.io.File;

/**
 * 
 * @author pubudu
 */
public class ZipFilter implements java.io.FileFilter {

    public boolean accept(File file) {
        if (file.getName().endsWith(".jar") || file.getName().endsWith(".mar")) {
            return true;
        }
        return false;
    }
}
