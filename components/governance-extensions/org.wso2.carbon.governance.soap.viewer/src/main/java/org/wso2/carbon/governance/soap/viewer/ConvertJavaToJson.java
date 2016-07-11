/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.governance.soap.viewer;

import com.google.gson.Gson;

import java.util.List;

/**
 * The purpose of this class is to convert java objects to Json arrays.
 */
public class ConvertJavaToJson {

    private static Gson gson = new Gson();

    public ConvertJavaToJson() {

    }

    /**
     * This methods converts list of services objects to s json array.
     *
     * @param services Services list
     * @return Json string of list of services
     */
    public static String getServiceListJsonString(List<WSDLService> services) {
        return gson.toJson(services);

    }
}
