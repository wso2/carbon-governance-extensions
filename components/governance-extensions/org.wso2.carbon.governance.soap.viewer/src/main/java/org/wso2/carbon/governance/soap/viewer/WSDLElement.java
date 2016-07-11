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

import java.util.ArrayList;
import java.util.List;

/**
 *  This class is a representation of WSDL elements. Each elements should have a name and a type. If the type is a
 *  complex type then there will be a list of sub elements.
 */
public class WSDLElement {

    private String name;

    private String type;

    private List<WSDLElement> subElements = new ArrayList<WSDLElement>();

    public WSDLElement(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<WSDLElement> getSubElements() {
        return subElements;
    }

    public void setSubElements(List<WSDLElement> subElements) {
        this.subElements = subElements;
    }
}
