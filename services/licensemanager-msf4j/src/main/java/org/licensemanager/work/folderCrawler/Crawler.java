package org.licensemanager.work.folderCrawler;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import org.licensemanager.work.filters.DirectoryFilter;
import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.op4j.Op;

/**
 * 
 * @author pubudu
 */
public class Crawler implements Serializable {

    public List<File> find(String path, FileFilter filter) {
        List<File> files;
        Stack<File> directories = new Stack<File>();
        DirectoryFilter dirFilter = new DirectoryFilter();
        files = new ArrayList<File>();
        directories.add(new File(path));
        while (!directories.empty()) {
            File next = directories.pop();
            directories.addAll(Op.onArray(next.listFiles(dirFilter)).toList().get());
            files.addAll(Op.onArray(next.listFiles(filter)).toList().get());
        }
        return files;
    }
}
