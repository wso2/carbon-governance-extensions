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

import org.wso2.carbon.registry.core.service.RegistryService;

public class ServiceHolder {

    /**
     * Registry service which is used to get registry data.
     */
    private static RegistryService registryService;


    /**
     * Method to get registry service.
     *
     * @return  registry service.
     */
    public static RegistryService getRegistryService() {
        return registryService;
    }

    /**
     * Method to set registry RegistryService.
     *
     * @param   service registry service.
     */
    public static void setRegistryService(RegistryService service) {
        registryService = service;
    }

    /**
     * Method to unset registry RegistryService.
     */
    public static void unsetRegistryService() {
        registryService = null;
    }

}
