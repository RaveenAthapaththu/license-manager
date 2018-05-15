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

package org.wso2.internal.apps.license.manager.util.crawler;

import org.op4j.Op;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Crawling through all the folders inside a given folder.
 */
public class FolderCrawler implements Serializable {

    public List<File> find(String path) {

        List<File> files = new ArrayList<>();
        Stack<File> directories = new Stack<>();
        directories.add(new File(path));
        while (!directories.empty()) {
            File next = directories.pop();
            directories.addAll(Op.onArray(next.listFiles(File::isDirectory)).toList().get());
            files.addAll(Op.onArray(next.listFiles(
                    file -> file.getName().endsWith(".jar") || file.getName().endsWith(".mar"))).toList().get());
        }
        return files;
    }
}
