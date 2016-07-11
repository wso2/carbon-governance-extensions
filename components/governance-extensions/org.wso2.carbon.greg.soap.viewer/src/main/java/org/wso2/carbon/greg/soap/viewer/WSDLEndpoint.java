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

package org.wso2.carbon.greg.soap.viewer;

/**
 * This class represents the endpoints of a WSDL file.
 */
public class WSDLEndpoint {

    private String address;

    private String binding;

    private MessageGroup messages;

    private int endpointIndex;

    private String name;


    /**
     * This class represents endpoint of a wsdl file
     * @param address Endpoint address
     * @param binding Binding of the endpoint.
     * @param messageGroup messageGroup object related to this endpoint
     * @param endpointIndex Index of the endpoint for the UI
     * @param name Name of the endpoint.
     */
    public WSDLEndpoint(String address, String binding, MessageGroup messageGroup, int endpointIndex, String name) {
        this.address = address;
        this.binding = binding;
        this.messages = messageGroup;
        this.endpointIndex = endpointIndex;
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    public MessageGroup getMessages() {
        return messages;
    }

    public void setMessages(MessageGroup messages) {
        this.messages = messages;
    }

    public int getEndpointIndex() {
        return endpointIndex;
    }

    public void setEndpointIndex(int endpointIndex) {
        this.endpointIndex = endpointIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
