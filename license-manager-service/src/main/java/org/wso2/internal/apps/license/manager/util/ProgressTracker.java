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
package org.wso2.internal.apps.license.manager.util;

import org.wso2.internal.apps.license.manager.model.TaskProgress;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Track the progress of long running tasks.
 */
public class ProgressTracker {

    private static final ReadWriteLock progressTrackerLock = new ReentrantReadWriteLock();
    // Stores the TaskProgress object mapped to the username.
    private static Map<String, TaskProgress> taskProgressMap = new HashMap<>();

    /**
     * Create a new task and return it.
     *
     * @param username The email of the user who requested the task
     * @return a new TaskProgress object.
     */
    public static TaskProgress createNewTaskProgress(String username) {

        progressTrackerLock.writeLock().lock();
        try {
            TaskProgress taskProgress = new TaskProgress(username, UUID.randomUUID().toString(), Constants.RUNNING);
            taskProgressMap.put(username, taskProgress);
            return taskProgress;
        } finally {
            progressTrackerLock.writeLock().unlock();
        }
    }

    /**
     * Get an existing task. If such a task does not exist null will returned.
     *
     * @param username The email of the user who requested the task
     * @return The task progress for the specified task
     */
    public static TaskProgress getTaskProgress(String username) {

        TaskProgress taskProgress;
        progressTrackerLock.readLock().lock();
        try {
            taskProgress = taskProgressMap.get(username);
        } finally {
            progressTrackerLock.readLock().unlock();
        }
        return taskProgress;
    }

    /**
     * Delete a task progress being tracked.
     *
     * @param username The email of the user who started the task
     */
    public static void deleteTaskProgress(String username) {

        progressTrackerLock.writeLock().lock();
        try {
            taskProgressMap.remove(username);
        } finally {
            progressTrackerLock.writeLock().unlock();
        }
    }
}
