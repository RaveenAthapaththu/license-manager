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

package org.wso2.internal.apps.license.manager.impl.folderCrawler;

import org.op4j.Op;
import org.wso2.internal.apps.license.manager.impl.filters.DirectoryFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
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
