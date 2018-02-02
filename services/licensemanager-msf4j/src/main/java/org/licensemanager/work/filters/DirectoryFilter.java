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
public class DirectoryFilter implements java.io.FileFilter {

    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        } else {
            return false;
        }
    }
}
