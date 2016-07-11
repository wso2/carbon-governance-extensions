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
 * Message parts are used to define parameter name and type of a WSDL message.
 */
public class MessagePart {

    private String name;
    private String type;

    private List<WSDLElement> subElements;

    public MessagePart(String name, String type) {
        this.name = name;
        this.type = type;
        this.subElements = new ArrayList<>();
    }

    public MessagePart(String name, String type, List<WSDLElement> subElms) {
        this.name = name;
        this.type = type;
        this.subElements = subElms;
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

    public List<WSDLElement> getSubElements() {
        return subElements;
    }

}
