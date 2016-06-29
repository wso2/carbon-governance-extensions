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

import com.google.gson.Gson;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


/**
 * This class is responsible of testing the services array of json string returns from getListOfServices method in
 * WSDLVisualizer class
 */
public class WSDLVisualizerServicesTest {

    private static WSDLVisualizer visualizer;

    @BeforeClass
    public static void setUp() throws Exception {
        visualizer = new WSDLVisualizer();
        visualizer.init("src/test/resources/StockQuote.wsdl");
    }

    /**
     * Test all the properties of the services json string returns from getListOfServices method in WSDLVisualizer class
     * @throws Exception
     */
    @Test
    public void testGetListOfServices() throws Exception {
        String wsdlServicesString = visualizer.getListOfServices();
        Gson gson = new Gson();
        WSDLService[] services = gson.fromJson(wsdlServicesString, WSDLService[].class);
        WSDLService wsdlService = services[0];
        assertEquals("StockQuote", wsdlService.getName());
        List<WSDLOperation> wsdlOperations = wsdlService.getOperations();
        assertEquals(1, wsdlOperations.size());
        WSDLOperation operation = wsdlOperations.get(0);
        assertEquals("GetQuote", operation.getName());
        List<WSDLEndpoint> endpoints = operation.getWSDLEndpoints();
        assertEquals(4, endpoints.size());

        int endpointFound = 0;
        for (WSDLEndpoint endpoint : endpoints) {
            if (endpoint.getBinding().equals("StockQuoteHttpPost")) {
                endpointFound = endpointFound + 1;
                assertEquals("http://www.webservicex.net/stockquote.asmx",endpoint.getAddress());

                 //test message types
                MessageGroup messageGroup = endpoint.getMessages();
                InputMessage input = messageGroup.getInput();
                assertEquals("symbol", input.getMessageParts().get(0).getName());
                assertEquals("string", input.getMessageParts().get(0).getType());

                OutputMessage output = messageGroup.getOutput();
                assertEquals("Body", output.getMessageParts().get(0).getName());
                assertEquals("string", output.getMessageParts().get(0).getType());

                FaultMessage fault = messageGroup.getFault();
                assertEquals("undefined", fault.getMessageParts().get(0).getName());
                assertEquals("undefined", fault.getMessageParts().get(0).getType());

            }else if (endpoint.getBinding().equals("StockQuoteSoap")) {
                endpointFound = endpointFound + 1;
                assertEquals("http://www.webservicex.net/stockquote.asmx",endpoint.getAddress());

                //test message types
                MessageGroup messageGroup = endpoint.getMessages();
                InputMessage input = messageGroup.getInput();
                assertEquals("parameters", input.getMessageParts().get(0).getName());
                assertEquals("GetQuote", input.getMessageParts().get(0).getType());
                assertFalse(input.getMessageParts().get(0).getSubElements().isEmpty());

                OutputMessage output = messageGroup.getOutput();
                assertEquals("parameters", output.getMessageParts().get(0).getName());
                assertEquals("GetQuoteResponse", output.getMessageParts().get(0).getType());
                assertFalse(output.getMessageParts().get(0).getSubElements().isEmpty());


                FaultMessage fault = messageGroup.getFault();
                assertEquals("undefined", fault.getMessageParts().get(0).getName());
                assertEquals("undefined", fault.getMessageParts().get(0).getType());

            }else if (endpoint.getBinding().equals("StockQuoteHttpGet")) {
                endpointFound = endpointFound + 1;
                assertEquals("http://www.webservicex.net/stockquote.asmx",endpoint.getAddress());

                //test message types
                MessageGroup messageGroup = endpoint.getMessages();
                InputMessage input = messageGroup.getInput();
                assertEquals("symbol", input.getMessageParts().get(0).getName());
                assertEquals("string", input.getMessageParts().get(0).getType());

                OutputMessage output = messageGroup.getOutput();
                assertEquals("Body", output.getMessageParts().get(0).getName());
                assertEquals("string", output.getMessageParts().get(0).getType());

                FaultMessage fault = messageGroup.getFault();
                assertEquals("undefined", fault.getMessageParts().get(0).getName());
                assertEquals("undefined", fault.getMessageParts().get(0).getType());

            }else if (endpoint.getBinding().equals("StockQuoteSoap12")) {
                endpointFound = endpointFound + 1;
                assertEquals("http://www.webservicex.net/stockquote.asmx",endpoint.getAddress());

                //test message types
                MessageGroup messageGroup = endpoint.getMessages();
                InputMessage input = messageGroup.getInput();
                assertEquals("parameters", input.getMessageParts().get(0).getName());
                assertEquals("GetQuote", input.getMessageParts().get(0).getType());
                assertFalse(input.getMessageParts().get(0).getSubElements().isEmpty());

                OutputMessage output = messageGroup.getOutput();
                assertEquals("parameters", output.getMessageParts().get(0).getName());
                assertEquals("GetQuoteResponse", output.getMessageParts().get(0).getType());
                assertFalse(output.getMessageParts().get(0).getSubElements().isEmpty());


                FaultMessage fault = messageGroup.getFault();
                assertEquals("undefined", fault.getMessageParts().get(0).getName());
                assertEquals("undefined", fault.getMessageParts().get(0).getType());
            }
        }

        assertEquals(4, endpointFound);
    }
}
