/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.robot.transformer.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.seasar.robot.RobotSystemException;
import org.seasar.robot.entity.ResponseData;
import org.seasar.robot.entity.ResultData;
import org.seasar.robot.util.StreamUtil;

/**
 * @author shinsuke
 * 
 */
public class FileTransformer extends HtmlTransformer {
    /**
     * A path to store downloaded files. The default path is a current
     * directory.
     */
    public String path;

    /**
     * A string to replace ?.
     */
    public String questionStr = "_QUEST_";

    /**
     * A string to replace :.
     */
    public String colonStr = "_CLN_";

    /**
     * A string to replace ;.
     */
    public String semicolonStr = "_SCLN_";

    /**
     * A string to replace &.
     */
    public String ampersandStr = "_AMP_";

    public int maxDuplicatedPath = 100;

    /**
     * A directory to store downloaded files.
     */
    protected File baseDir;

    protected File createFile(String path) {
        String[] paths = path.split("/");
        File targetFile = baseDir;
        for (int i = 0; i < paths.length - 1; i++) {
            File file = new File(targetFile, paths[i]);
            if (file.exists()) {
                if (!file.isDirectory()) {
                    for (int j = 0; j < maxDuplicatedPath; j++) {
                        file = new File(targetFile, paths[i] + "_" + j);
                        if (file.exists()) {
                            if (file.isDirectory()) {
                                break;
                            }
                        } else {
                            file.mkdirs();
                            break;
                        }
                    }
                }
            } else {
                file.mkdirs();
            }
            targetFile = file;
        }

        File file = new File(targetFile, paths[paths.length - 1]);
        if (file.exists()) {
            for (int i = 0; i < maxDuplicatedPath; i++) {
                file = new File(targetFile, paths[paths.length - 1] + "_" + i);
                if (!file.exists()) {
                    targetFile = file;
                    break;
                }
            }
        } else {
            targetFile = file;
        }
        return targetFile;
    }

    @Override
    public void storeData(ResponseData responseData, ResultData resultData) {
        resultData.setTransformerName(getName());

        initBaseDir();

        String url = responseData.getUrl();
        String path = getFilePath(url);

        synchronized (this) {

            File file = createFile(path);

            InputStream is = responseData.getResponseBody();
            OutputStream os = null;
            try {
                os = new FileOutputStream(file);
                StreamUtil.drain(is, os);
            } catch (IOException e) {
                throw new RobotSystemException("Could not store "
                        + file.getAbsolutePath(), e);
            } finally {
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
            }
        }
        resultData.setData(path);

    }

    private void initBaseDir() {
        if (baseDir == null) {
            if (path == null) {
                // current directory
                baseDir = new File(".");
            } else {
                baseDir = new File(path);
                if (!baseDir.isDirectory()) {
                    if (!baseDir.mkdirs()) {
                        throw new RobotSystemException("Could not create "
                                + baseDir.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Generate a path from a url.
     * 
     * @param url
     * @return
     */
    protected String getFilePath(String url) {
        return url.replaceAll("/+", "/")//
                .replaceAll("\\./", "")//
                .replaceAll("\\.\\./", "")//
                .replaceAll("/$", "/index.html")//
                .replaceAll("\\?", questionStr)//
                .replaceFirst(":", colonStr)//
                .replaceAll(";", semicolonStr)//
                .replaceAll("&", ampersandStr)//
        ;
    }
}