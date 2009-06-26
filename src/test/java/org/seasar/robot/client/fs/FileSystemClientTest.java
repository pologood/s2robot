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
package org.seasar.robot.client.fs;

import java.io.File;
import java.util.Set;

import org.seasar.extension.unit.S2TestCase;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.robot.Constants;
import org.seasar.robot.entity.ResponseData;

/**
 * @author shinsuke
 *
 */
public class FileSystemClientTest extends S2TestCase {
    public FileSystemClient fsClient;

    @Override
    protected String getRootDicon() throws Throwable {
        return "app.dicon";
    }

    public void test_doGet_dir() {
        File file = ResourceUtil.getResourceAsFile("test");
        try {
            fsClient.doGet("file://" + file.getAbsolutePath());
            fail();
        } catch (ChildUrlsException e) {
            Set<String> urlSet = e.getChildUrlList();
            for (String url : urlSet.toArray(new String[urlSet.size()])) {
                if (url.indexOf(".svn") < 0) {
                    assertTrue(url.endsWith("test/dir1")
                            || url.endsWith("test/dir2")
                            || url.endsWith("test/text1.txt")
                            || url.endsWith("test/text2.txt"));
                }
            }
        }

    }

    public void test_doGet_file() throws Exception {
        File file = ResourceUtil.getResourceAsFile("test/text1.txt");
        ResponseData responseData = fsClient.doGet("file://"
                + file.getAbsolutePath());
        assertEquals(200, responseData.getHttpStatusCode());
        assertEquals("UTF-8", responseData.getCharSet());
        assertEquals(6, responseData.getContentLength());
        assertNotNull(responseData.getLastModified());
        assertEquals(Constants.GET_METHOD, responseData.getMethod());
        assertEquals("text/plain", responseData.getMimeType());
        assertTrue(responseData.getUrl().endsWith("test/text1.txt"));
        String content = new String(InputStreamUtil.getBytes(responseData
                .getResponseBody()), "UTF-8");
        assertEquals("test1\n", content);
    }
}
