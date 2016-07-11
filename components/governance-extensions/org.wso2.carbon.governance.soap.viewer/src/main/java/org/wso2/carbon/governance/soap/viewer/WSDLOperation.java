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

import java.util.List;

/**
 * This class is a representation of operations in a WSDL file.
 */
public class WSDLOperation implements Comparable<WSDLOperation> {

    private String name;

    private List<WSDLEndpoint> WSDLEndpoints;

    private int operationIndex;

    public WSDLOperation(String name, List<WSDLEndpoint> endpoints, int operationIndex) {
        this.name = name;
        this.setWSDLEndpoints(endpoints);
        this.setOperationIndex(operationIndex);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WSDLEndpoint> getWSDLEndpoints() {
        return WSDLEndpoints;
    }

    public void setWSDLEndpoints(List<WSDLEndpoint> WSDLEndpoints) {
        this.WSDLEndpoints = WSDLEndpoints;
    }

    public int getOperationIndex() {
        return operationIndex;
    }

    public void setOperationIndex(int operationIndex) {
        this.operationIndex = operationIndex;
    }

    @Override
    public int compareTo(WSDLOperation o) {
        if (this.operationIndex > o.operationIndex)
            return 1;
        else {
            return -1;
        }

    }

}
