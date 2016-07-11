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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * This test class test the complex types patterns.
 */
public class WSDLVisualizerComplexTypesTest {

    private static WSDLVisualizer visualizer;

    @BeforeClass
    public static void setUp() throws Exception {
        visualizer = new WSDLVisualizer();
        visualizer.init("src/test/resources/StockQuote.wsdl");
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test the complex types which start with element tag.
     * @throws Exception
     */
    @Test
    public void testElement() throws Exception {
        HashMap<String, List<WSDLElement>> allTypes = visualizer.getAllTypes();
        /*
            <s:element name="GetQuote">
                <s:complexType>
                    <s:sequence>
                        <s:element maxOccurs="1" minOccurs="0" name="symbol" type="s:string"/>
                    </s:sequence>
                </s:complexType>
            </s:element>
         */
        List<WSDLElement> listOfElements = allTypes.get("{http://www.webserviceX.NET/}GetQuote");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        boolean elementFound = false;
            for (WSDLElement wsdlElm : listOfElements) {
                if(wsdlElm.getName().equals("symbol") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}string") && wsdlElm
                        .getSubElements().isEmpty()){
                    elementFound = true;
                }
            }
        assertTrue(elementFound);

        /*
            <s:element name="age">
              <s:simpleType>
                <s:restriction base="s:integer">
                  <s:minInclusive value="0"/>
                  <s:maxInclusive value="100"/>
                </s:restriction>
              </s:simpleType>
            </s:element>
         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}age");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            if(wsdlElm.getName().equals("restriction_base") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}integer") && wsdlElm
                    .getSubElements().isEmpty()){
                elementFound = true;
            }
        }
        assertTrue(elementFound);
    }

    /**
     * Test the complex types in a schema which start with complexType tag.
     * @throws Exception
     */
    @Test
    public void testComplexType() throws Exception {
        HashMap<String, List<WSDLElement>> allTypes = visualizer.getAllTypes();
        /*
            <s:complexType name="ordertype">
                <s:group ref="custGroup"/>
                <s:attribute name="status" type="s:string"/>
            </s:complexType>
         */
        List<WSDLElement> listOfElements = allTypes.get("{http://www.webserviceX.NET/}ordertype");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
        }

        /*
          <s:complexType name="complexAll">
                <s:all>
                    <s:element name="firstname" type="s:string"/>
                    <s:element name="lastname" type="s:string"/>
                </s:all>
            </s:complexType>
         */

        listOfElements = allTypes.get("{http://www.webserviceX.NET/}complexAll");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        boolean elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            if(wsdlElm.getName().equals("firstname") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}string") && wsdlElm
                    .getSubElements().isEmpty()){
               elementFound = true;
            }
        }
        assertTrue(elementFound);

        /*
            <s:complexType name="complexChoice">
                <s:choice>
                    <s:element name="firstname" type="s:string"/>
                    <s:element name="lastname" type="s:string"/>
                </s:choice>
            </s:complexType>
         */

        listOfElements = allTypes.get("{http://www.webserviceX.NET/}complexChoice");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            if(wsdlElm.getName().equals("firstname") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}string") && wsdlElm
                    .getSubElements().isEmpty()){
                elementFound = true;
            }
        }
        assertTrue(elementFound);
    }

    /**
     * Test the group element in different locations.
     * @throws Exception
     */
    @Test
    public void testGroupType() throws Exception {
        HashMap<String, List<WSDLElement>> allTypes = visualizer.getAllTypes();
        //Element order refers a complex type. That complex type has a group reference.
        /*
             <s:element name="order" type="ordertype"/>

            <s:complexType name="ordertype">
                <s:group ref="custGroup"/>
                <s:attribute name="status" type="s:string"/>
            </s:complexType>
         */
        List<WSDLElement> listOfElements = allTypes.get("{http://www.webserviceX.NET/}order");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        assertNotNull(listOfElements.get(0).getSubElements());
        boolean elementFound = false;
        List<WSDLElement> subElms = listOfElements.get(0).getSubElements();
        for (WSDLElement elm : subElms) {
            if (elm.getType().equals("{http://www.webserviceX.NET/}custGroup") && !elm.getSubElements().isEmpty()) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        //Group with all element
        /*
            e.g.

            <s:group name="groupAll">
                <s:all>
                    <s:element name="customer" type="s:string"/>
                    <s:element name="orderdetails" type="s:string"/>
                    <s:element name="billto" type="s:string"/>
                    <s:element name="shipto" type="s:string"/>
                </s:all>
            </s:group>

         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}groupAll");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("customer") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}string")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        //Group with choice element
        /*
            e.g.
                <s:group name="groupChoice1">
                <s:choice>
                    <s:sequence>
                        <s:element name="customer" type="s:string"/>
                        <s:element name="orderdetails" type="s:string"/>
                        <s:element name="billto" type="s:string"/>
                        <s:element name="shipto" type="s:string"/>
                    </s:sequence>
                </s:choice>
            </s:group>
         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}groupChoice1");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("customer") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}string")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        //Group with choice element. Under choice element there is a reference for another group element
        /*
            e.g.
                 <s:group name="groupChoice2">
                    <s:choice>
                        <s:group ref="custGroup"/>

                    </s:choice>
                </s:group>

         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}groupChoice2");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("ref") && wsdlElm.getType().equals("{http://www.webserviceX.NET/}custGroup")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);
    }

    /**
     * Test the element type choice.
     * @throws Exception
     */
    @Test
    public void testChoiceType() throws Exception {
        HashMap<String, List<WSDLElement>> allTypes = visualizer.getAllTypes();
        //test list
        /*
            e.g.
                <s:element name="personChoiceSequence">
                    <s:complexType>
                        <s:choice>
                            <s:sequence>
                                <s:element name="employee" type="employee"/>
                                <s:element name="member" type="member"/>
                            </s:sequence>
                        </s:choice>
                    </s:complexType>
                </s:element>
         */
        List<WSDLElement> listOfElements = allTypes.get("{http://www.webserviceX.NET/}personChoiceSequence");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        boolean elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getType().equals("{http://www.webserviceX.NET/}member")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);


        /*
            e.g.
                <s:element name="personChoiceElement">
                    <s:complexType>
                        <s:choice>
                            <s:choice>
                                <s:sequence>
                                    <s:element name="employee" type="employee"/>
                                    <s:element name="member" type="member"/>
                                </s:sequence>
                            </s:choice>
                        </s:choice>
                    </s:complexType>
                </s:element>
         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}personChoiceElement");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getType().equals("{http://www.webserviceX.NET/}member")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        /*
            e.g.
                 <s:element name="personChoiceGroup">
                    <s:complexType>
                        <s:choice>
                            <s:group ref="custGroup"/>
                        </s:choice>
                    </s:complexType>
                </s:element>
         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}personChoiceGroup");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getType().equals("{http://www.webserviceX.NET/}custGroup")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);
    }

    /**
     * Test the complexContent elements.
     * @throws Exception
     */
    @Test
    public void testComplexContent() throws Exception {
        HashMap<String, List<WSDLElement>> allTypes = visualizer.getAllTypes();
        /*
            <s:complexType name="fullpersoninfo2">
                <s:complexContent>
                    <s:extension base="personinfo">
                        <s:sequence>
                            <s:element name="address" type="s:string"/>
                            <s:element name="city" type="s:string"/>
                            <s:element name="country" type="s:string"/>
                        </s:sequence>
                    </s:extension>
                </s:complexContent>
            </s:complexType>
         */
        List<WSDLElement> listOfElements = allTypes.get("{http://www.webserviceX.NET/}fullpersoninfo2");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        boolean elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("address") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}string")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

         /*
          <s:complexType name="fullpersoninfo1">
                <s:complexContent>
                    <s:extension base="personinfo">
                        <s:group ref="custGroup"/>
                    </s:extension>
                </s:complexContent>
            </s:complexType>
          */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}fullpersoninfo1");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("ref") && wsdlElm.getType().equals("{http://www.webserviceX.NET/}custGroup") && !wsdlElm
                    .getSubElements().isEmpty()) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        /*
            <s:complexType name="fullpersoninfo3">
                <s:complexContent>
                    <s:extension base="personinfo">
                        <s:all>
                            <s:element name="address" type="s:string"/>
                            <s:element name="city" type="s:string"/>
                            <s:element name="country" type="s:string"/>
                        </s:all>
                    </s:extension>
                </s:complexContent>
            </s:complexType>
         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}fullpersoninfo3");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("address") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}string")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        /*
             <s:complexType name="fullpersoninfo4">
                <s:complexContent>
                    <s:extension base="personinfo">
                        <s:choice>
                            <s:sequence>
                            <s:element name="address" type="s:string"/>
                            <s:element name="city" type="s:string"/>
                            <s:element name="country" type="s:string"/>
                            </s:sequence>
                        </s:choice>
                    </s:extension>
                </s:complexContent>
            </s:complexType>

         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}fullpersoninfo4");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("address") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}string")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        /*
            <s:complexType name="fullpersoninfo5">
                <s:complexContent>
                    <s:extension base="personinfo">
                        <s:attributeGroup ref="personattr"/>
                    </s:extension>
                </s:complexContent>
            </s:complexType>
         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}fullpersoninfo5");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("ref") && wsdlElm.getType().equals("{http://www.webserviceX.NET/}personattr") && !wsdlElm
                    .getSubElements().isEmpty()) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        /*
         <s:complexType name="Norwegian_customer1">
                <s:complexContent>
                    <s:restriction base="customer">
                        <s:sequence>
                            <s:element name="firstname" type="s:string"/>
                            <s:element name="lastname" type="s:string"/>
                            <s:element name="country" type="s:string" fixed="Norway"/>
                        </s:sequence>
                    </s:restriction>
                </s:complexContent>
            </s:complexType>
         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}Norwegian_customer1");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("firstname") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}string")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        /*
         <s:complexType name="Norwegian_customer2">
                <s:complexContent>
                    <s:restriction base="customer">
                        <s:group ref="custGroup"/>
                    </s:restriction>
                </s:complexContent>
            </s:complexType>
         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}Norwegian_customer2");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("ref") && wsdlElm.getType().equals("{http://www.webserviceX.NET/}custGroup") && !wsdlElm
                    .getSubElements().isEmpty()) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        /*
         <s:complexType name="Norwegian_customer3">
                <s:complexContent>
                    <s:restriction base="customer">
                        <s:all>
                            <s:element name="firstname" type="s:string"/>
                            <s:element name="lastname" type="s:string"/>
                            <s:element name="country" type="s:string" fixed="Norway"/>
                        </s:all>
                    </s:restriction>
                </s:complexContent>
            </s:complexType>

         */

        listOfElements = allTypes.get("{http://www.webserviceX.NET/}Norwegian_customer3");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("firstname") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}string")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        /*
        <s:complexType name="Norwegian_customer4">
                <s:complexContent>
                    <s:restriction base="customer">
                        <s:choice>
                            <s:sequence>
                                <s:element name="firstname" type="s:string"/>
                                <s:element name="lastname" type="s:string"/>
                                <s:element name="country" type="s:string" fixed="Norway"/>
                            </s:sequence>
                        </s:choice>
                    </s:restriction>
                </s:complexContent>
            </s:complexType>
         */

        listOfElements = allTypes.get("{http://www.webserviceX.NET/}Norwegian_customer4");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("firstname") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}string")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        /*
            <s:complexType name="Norwegian_customer5">
                <s:complexContent>
                    <s:restriction base="customer">
                        <s:attributeGroup ref="personattr"/>
                    </s:restriction>
                </s:complexContent>
            </s:complexType>
            <!-- -->
         */

        listOfElements = allTypes.get("{http://www.webserviceX.NET/}Norwegian_customer5");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("ref") && wsdlElm.getType().equals("{http://www.webserviceX.NET/}personattr") && !wsdlElm
                    .getSubElements().isEmpty()) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        /*
            <s:complexType name="Norwegian_customer6">
                <s:complexContent>
                    <s:restriction base="customer">
                        <s:attribute name="code">
                            <s:simpleType>
                                <s:restriction base="s:string">
                                    <s:pattern value="[A-Z][A-Z]"/>
                                </s:restriction>
                            </s:simpleType>
                        </s:attribute>
                    </s:restriction>
                </s:complexContent>
            </s:complexType>
         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}Norwegian_customer6");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("attribute") && wsdlElm.getType().equals("code") && wsdlElm
                    .getSubElements().get(0).getType().equals("{http://www.w3.org/2001/XMLSchema}string")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

    }

    /**
     * Test the simpleContent element type.
     * @throws Exception
     */
    @Test
    public void testSimpleContent() throws Exception {
        HashMap<String, List<WSDLElement>> allTypes = visualizer.getAllTypes();
       /*
        <s:element name="shoesize2">
                <s:complexType>
                    <s:simpleContent>
                        <s:restriction base="s:string">
                            <s:simpleType>
                                <s:restriction base="s:string">
                                    <s:pattern value="[A-Z][A-Z]"/>
                                </s:restriction>
                            </s:simpleType>
                        </s:restriction>
                    </s:simpleContent>
                </s:complexType>
            </s:element>
        */
        List<WSDLElement> listOfElements = allTypes.get("{http://www.webserviceX.NET/}shoesize2");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        assertEquals(2, listOfElements.size());

    }

    @Test
    public void testSequence() throws Exception {
        HashMap<String, List<WSDLElement>> allTypes = visualizer.getAllTypes();
       /*
         <s:element name="pets1">
                <s:complexType>
                    <s:sequence minOccurs="0" maxOccurs="unbounded">
                        <s:choice>
                            <s:element name="dog" type="s:string"/>
                            <s:element name="cat" type="s:string"/>
                        </s:choice>
                    </s:sequence>
                </s:complexType>
            </s:element>
        */
        List<WSDLElement> listOfElements = allTypes.get("{http://www.webserviceX.NET/}pets1");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        assertEquals(2, listOfElements.size());

        /*
            <s:element name="pets2">
                <s:complexType>
                    <s:sequence minOccurs="0" maxOccurs="unbounded">
                        <s:sequence>
                            <s:element name="dog" type="s:string"/>
                            <s:element name="cat" type="s:string"/>
                        </s:sequence>
                    </s:sequence>
                </s:complexType>
            </s:element>
         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}pets2");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        assertEquals(2, listOfElements.size());

        /*
            <s:element name="pets3">
                <s:complexType>
                    <s:sequence minOccurs="0" maxOccurs="unbounded">
                        <s:group ref="groupChoice1"/>
                </s:complexType>
            </s:element>
         */

        listOfElements = allTypes.get("{http://www.webserviceX.NET/}pets3");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        boolean elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("ref") && wsdlElm.getType().equals("{http://www.webserviceX.NET/}groupChoice1") && !wsdlElm
                    .getSubElements().isEmpty()) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

    }

    /**
     * Test the elements starts with element tag which are inside other elements like complexTypes, sequence etc.
     * @throws Exception
     */
    @Test
    public void testInnerElementsWithSubElements() throws Exception{
        /*
         <s:element name="testInnerElementsWithSubElements">
            <s:complexType>
                <s:sequence>
                    <s:element minOccurs="0" name="input">
                        <s:complexType>
                            <s:sequence>
                                <s:element name="firstName" type="s:string"/>
                                <s:element name="lastName" type="s:string"/>
                            </s:sequence>
                        </s:complexType>
                    </s:element>
                    <s:element minOccurs="0" name="output">
                        <s:complexType>
                            <s:sequence>
                                <s:element maxOccurs="unbounded" minOccurs="0" name="itemList">
                                    <s:complexType>
                                        <s:sequence>
                                            <s:element minOccurs="0" name="id" type="s:string"/>
                                            <s:element minOccurs="0" name="name" type="s:string"/>
                                        </s:sequence>
                                    </s:complexType>
                                </s:element>
                            </s:sequence>
                        </s:complexType>
                    </s:element>
                </s:sequence>
            </s:complexType>
         </s:element>
         */
        HashMap<String, List<WSDLElement>> allTypes = visualizer.getAllTypes();
        List<WSDLElement> listOfElements = allTypes.get("{http://www.webserviceX.NET/}testInnerElementsWithSubElements");
        assertFalse(listOfElements.isEmpty());
        boolean elementFound =  false;
        for (WSDLElement wsdlElm : listOfElements) {
            if(wsdlElm.getName().equals("element_type") && wsdlElm.getType().equals("input") && wsdlElm
                    .getSubElements() != null){
                  elementFound = true;
                for(WSDLElement subElms : wsdlElm.getSubElements()){
                    assertEquals(subElms.getType(), "{http://www.w3.org/2001/XMLSchema}string");
                }
            }
        }
        assertTrue(elementFound);

    }

    /**
     * Test SimpleType elements
     * @throws Exception
     */
    @Test
    public void testGetElementsOfSimpleType() throws Exception {
        HashMap<String, List<WSDLElement>> allTypes = visualizer.getAllTypes();
        //test list
        /*
            e.g.
                <s:simpleType name="valuelist">
                    <s:list itemType="s:integer"/>
                </s:simpleType>
         */
        List<WSDLElement> listOfElements = allTypes.get("{http://www.webserviceX.NET/}valuelist");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
        }
        //test union
        /*
            e.g.
                  <s:element name="jeans_size">
                    <s:simpleType>
                        <s:union memberTypes="sizebyno sizebystring"/>
                    </s:simpleType>
                </s:element>
         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}jeans_size");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
        }
    }


    /**
     * Test elements of Attribute element.
     * @throws Exception
     */
    @Test
    public void testGetElementsOfAttribute() throws Exception {
        HashMap<String, List<WSDLElement>> allTypes = visualizer.getAllTypes();
        /*
            e.g.
                 <s:attribute name="code">
                    <s:simpleType>
                        <s:restriction base="s:string">
                            <s:pattern value="[A-Z][A-Z]"/>
                        </s:restriction>
                    </s:simpleType>
                </s:attribute>

                <s:complexType name="someComplexType">
                    <s:attribute ref="code"/>
                </s:complexType>
         */
        List<WSDLElement> listOfElements = allTypes.get("{http://www.webserviceX.NET/}code");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
        }

        listOfElements = allTypes.get("{http://www.webserviceX.NET/}someComplexType");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        boolean elementFound = false;

        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("ref") && wsdlElm.getType().equals("{http://www.webserviceX.NET/}code") && !wsdlElm
                    .getSubElements().isEmpty()) {
                elementFound = true;
            }

        }
        assertTrue(elementFound);

        /*
            e.g.
                 <s:element name="shoesize">
                   <s:complexType>
                        <s:simpleContent>
                            <s:extension base="s:integer">
                                <s:attribute name="country" type="s:string"/>
                            </s:extension>
                        </s:simpleContent>
                    </s:complexType>
                </s:element>
         */
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}shoesize");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        elementFound = false;
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
            if (wsdlElm.getName().equals("country") && wsdlElm.getType().equals("{http://www.w3.org/2001/XMLSchema}string")) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);
    }


    /**
     * Test the attributeGroup element.
     * @throws Exception
     */
    @Test
    public void testGetElementsOfAttrGroup() throws Exception {
        HashMap<String, List<WSDLElement>> allTypes = visualizer.getAllTypes();
        /*
            e.g.
            <s:attributeGroup name="personattr">
                <s:attribute name="attr1" type="string"/>
                <s:attribute name="attr2" type="integer"/>
            </s:attributeGroup>
         */
        List<WSDLElement> listOfElements = allTypes.get("{http://www.webserviceX.NET/}personattr");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        for (WSDLElement wsdlElm : listOfElements) {
            assertNotNull(wsdlElm.getType());
        }

        /*
             <s:complexType name="person">
                <s:attributeGroup ref="personattr"/>
            </s:complexType>
         */
        boolean elementFound = false;
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}person");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        for (WSDLElement wsdlElm : listOfElements) {
            if (wsdlElm.getName().equals("ref") && wsdlElm.getType().equals("{http://www.webserviceX.NET/}personattr") && !wsdlElm
                    .getSubElements().isEmpty()) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);

        /*
            <s:attributeGroup name="personInfo">
                <s:attributeGroup ref="personattr"/>
                <s:attribute name="attr1" type="string"/>
                <s:attribute name="attr2" type="integer"/>
            </s:attributeGroup>
         */
        elementFound = false;
        listOfElements = allTypes.get("{http://www.webserviceX.NET/}personInfo");
        assertNotNull(listOfElements);
        assertFalse(listOfElements.isEmpty());
        for (WSDLElement wsdlElm : listOfElements) {
            if (wsdlElm.getName().equals("ref") && wsdlElm.getType().equals("{http://www.webserviceX.NET/}personattr") && !wsdlElm
                    .getSubElements().isEmpty()) {
                elementFound = true;
            }
        }
        assertTrue(elementFound);
    }

    @AfterClass
    public static void clean() throws Exception {
        if (visualizer != null) {
            visualizer = null;
        }
    }

}