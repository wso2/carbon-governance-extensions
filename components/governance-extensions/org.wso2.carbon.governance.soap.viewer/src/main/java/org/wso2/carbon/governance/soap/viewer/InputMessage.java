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
 * This class is an implementation of Message class. This class represents the input messages in a wsdl file.
 */
public class InputMessage implements Message{

    private List<MessagePart> messageParts;

    public InputMessage() {

    }

    public InputMessage(List<MessagePart> messageParts) {
        this.messageParts = messageParts;
    }


    public List<MessagePart> getMessageParts() {
        return messageParts;
    }

    public void setMessageParts(List<MessagePart> messageParts) {
        this.messageParts = messageParts;
    }
}