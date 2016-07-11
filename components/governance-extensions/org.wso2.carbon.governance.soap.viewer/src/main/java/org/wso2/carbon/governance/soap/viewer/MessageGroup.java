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

/**
 * This class holds the input, output and fault messages.
 */
public class MessageGroup {

    private InputMessage input;
    private OutputMessage output;
    private FaultMessage fault;


    public MessageGroup(InputMessage input,OutputMessage output, FaultMessage fault ){
        this.input = input;
        this.output = output;
        this.fault = fault;
    }
    public InputMessage getInput() {
        return input;
    }

    public void setInput(InputMessage input) {
        this.input = input;
    }

    public OutputMessage getOutput() {
        return output;
    }

    public void setOutput(OutputMessage output) {
        this.output = output;
    }

    public FaultMessage getFault() {
        return fault;
    }

    public void setFault(FaultMessage fault) {
        this.fault = fault;
    }
}
