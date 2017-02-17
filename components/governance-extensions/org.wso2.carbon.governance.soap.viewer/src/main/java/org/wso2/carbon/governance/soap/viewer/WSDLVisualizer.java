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

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.ow2.easywsdl.schema.api.All;
import org.ow2.easywsdl.schema.api.Attribute;
import org.ow2.easywsdl.schema.api.Choice;
import org.ow2.easywsdl.schema.api.ComplexContent;
import org.ow2.easywsdl.schema.api.ComplexType;
import org.ow2.easywsdl.schema.api.Element;
import org.ow2.easywsdl.schema.api.Group;
import org.ow2.easywsdl.schema.api.Restriction;
import org.ow2.easywsdl.schema.api.Schema;
import org.ow2.easywsdl.schema.api.SchemaException;
import org.ow2.easywsdl.schema.api.Sequence;
import org.ow2.easywsdl.schema.api.SimpleContent;
import org.ow2.easywsdl.schema.api.SimpleType;
import org.ow2.easywsdl.schema.api.Type;
import org.ow2.easywsdl.schema.impl.AttributeImpl;
import org.ow2.easywsdl.schema.impl.ChoiceImpl;
import org.ow2.easywsdl.schema.impl.ComplexContentImpl;
import org.ow2.easywsdl.schema.impl.ComplexTypeImpl;
import org.ow2.easywsdl.schema.impl.ElementImpl;
import org.ow2.easywsdl.schema.impl.SchemaImpl;
import org.ow2.easywsdl.schema.impl.SequenceImpl;
import org.ow2.easywsdl.schema.impl.SimpleContentImpl;
import org.ow2.easywsdl.schema.impl.SimpleTypeImpl;
import org.ow2.easywsdl.schema.org.w3._2001.xmlschema.Annotated;
import org.ow2.easywsdl.schema.org.w3._2001.xmlschema.AttributeGroupRef;
import org.ow2.easywsdl.schema.org.w3._2001.xmlschema.ComplexRestrictionType;
import org.ow2.easywsdl.schema.org.w3._2001.xmlschema.ExplicitGroup;
import org.ow2.easywsdl.schema.org.w3._2001.xmlschema.ExtensionType;
import org.ow2.easywsdl.schema.org.w3._2001.xmlschema.GroupRef;
import org.ow2.easywsdl.schema.org.w3._2001.xmlschema.LocalElement;
import org.ow2.easywsdl.schema.org.w3._2001.xmlschema.LocalSimpleType;
import org.ow2.easywsdl.schema.org.w3._2001.xmlschema.NamedAttributeGroup;
import org.ow2.easywsdl.schema.org.w3._2001.xmlschema.NamedGroup;
import org.ow2.easywsdl.schema.org.w3._2001.xmlschema.SimpleRestrictionType;
import org.ow2.easywsdl.schema.org.w3._2001.xmlschema.Union;
import org.ow2.easywsdl.wsdl.WSDLFactory;
import org.ow2.easywsdl.wsdl.api.Binding;
import org.ow2.easywsdl.wsdl.api.Description;
import org.ow2.easywsdl.wsdl.api.Endpoint;
import org.ow2.easywsdl.wsdl.api.Fault;
import org.ow2.easywsdl.wsdl.api.Input;
import org.ow2.easywsdl.wsdl.api.Operation;
import org.ow2.easywsdl.wsdl.api.Output;
import org.ow2.easywsdl.wsdl.api.Part;
import org.ow2.easywsdl.wsdl.api.Service;
import org.ow2.easywsdl.wsdl.api.Types;
import org.ow2.easywsdl.wsdl.api.WSDLReader;
import org.ow2.easywsdl.wsdl.impl.wsdl11.MessageImpl;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.resource.beans.ContentDownloadBean;
import org.wso2.carbon.registry.resource.services.utils.ContentUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * This class is responsible of parsing a WSDL file and generate complex types tree and create list of services in
 * the WSDL along with its sub elements such as operations, endpoints, parameters etc.
 *
 * @scr.component name="org.wso2.carbon.greg.soap.viewer" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class WSDLVisualizer {

    private static final Logger logger = Logger.getLogger(WSDLVisualizer.class);
    private Description desc;

    /*
    This is a list to keep the message elements in a WSDL file.
     */
    private List<MessageImpl> messages;
    /*
    This hashmap keeps a list of subElements of a WSDL element along with their name. The key of the hashmap is the
    element name with its namespace.
     */
    private HashMap<String, List<WSDLElement>> allTypes = new HashMap<>();
    /*
    This hasmap is used to keep track of the processed elements when digging down to subelements of a particular
    element. This is used to avoid circular dependencies of an element.
     */
    private HashMap<String, String> typesStack = new HashMap<>();

    public WSDLVisualizer() {
    }

    public WSDLVisualizer(String pathToWSDL,int tenantId) {

        String outdir = null;
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            //Get the location of the temp direcoty where wsdl file is saved.
            outdir = writeResourceToFile(pathToWSDL,tenantId);
            //Get the name of the WSDL file
            String[] list = pathToWSDL.split("/");
            String fileName = list[list.length - 1];
            //Create the full path of the wsdl file
            String fullPath = outdir + "/" + fileName;
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            URL url = (new File(fullPath)).toURI().toURL();
            this.desc = reader.read(url);
            //Get the message element list of the wsdl files
            this.messages = ((org.ow2.easywsdl.wsdl.impl.wsdl11.DescriptionImpl) desc).getMessages();
            //Create a map of complex types in the schema of the WSDL file
            createMapOfComplexTypes();

        } catch (URISyntaxException e) {
            logger.error("Error occurred while retrieving wsdl file", e);
        } catch (MalformedURLException e) {
            logger.error("URL provided is incorrect", e);
        } catch (IOException e) {
            logger.error("Error occurred while reading the wsdl file " + pathToWSDL, e);
        } catch (SchemaException e) {
            logger.error("Error occurred while resolving schema types ", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
            //Delete the temp folder created
            if (outdir != null) {
                FileUtils.deleteQuietly(new File(outdir));
            }
        }
    }

    /**
     * Get the temp location of output directory where wsdl files is saved with its dependencies
     *
     * @param path Registry path the the WSDL file
     * @return Output directory path
     */
    private String writeResourceToFile(String path, int tenantId) {
        String outDir = null;
        try {
            UserRegistry registry = ServiceHolder.getRegistryService().getSystemRegistry(tenantId);
            ContentDownloadBean zipContentBean = ContentUtil.getContentWithDependencies(path, registry);
            InputStream zipContentStream = zipContentBean.getContent().getInputStream();
            ZipInputStream stream = new ZipInputStream(zipContentStream);
            byte[] buffer = new byte[2048];
            String uniqueID = UUID.randomUUID().toString();
            outDir = CarbonUtils.getCarbonHome() + File.separator + "tmp" + File.separator + uniqueID;
            (new File(outDir)).mkdir();
            ZipEntry entry;
            while ((entry = stream.getNextEntry()) != null) {
                String s = String.format("Entry: %s len %d added %TD", outDir + File.separator + entry.getName(),
                        entry.getSize(), new Date(entry.getTime()));

                String outPath = outDir + File.separator + entry.getName();
                (new File(outPath)).getParentFile().mkdirs();
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(outPath);
                    int len = 0;
                    while ((len = stream.read(buffer)) > 0) {
                        output.write(buffer, 0, len);
                    }
                } finally {
                    // Close the output file
                    if (output != null) {
                        output.close();
                    }
                }
            }

        } catch (FileNotFoundException e) {
            logger.error("temporary output directory cannot be found ", e);
        } catch (IOException e) {
            logger.error("Error occurred while writing the WSDL content to the temporary file", e);
        } catch (RegistryException e) {
            logger.error("Error occurred while getting registry from service holder", e);
        } catch (Exception e) {
            logger.error("Error occurred while getting registry from service holder", e);
        }
        return outDir;
    }

    /**
     * This method is used for unit tests where it will take a wsdl file saved in a local file structure and
     * process.
     *
     * @param pathToWSDL Path to the wsdl file
     */
    public void init(String pathToWSDL) {
        try {
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            URL url = (new File(pathToWSDL)).toURI().toURL();
            this.desc = reader.read(url);
            this.messages = ((org.ow2.easywsdl.wsdl.impl.wsdl11.DescriptionImpl) desc).getMessages();
            createMapOfComplexTypes();

        } catch (URISyntaxException e) {
            logger.error("Error occurred while retrieving wsdl file", e);
        } catch (MalformedURLException e) {
            logger.error("URL provided is incorrect", e);
        } catch (IOException e) {
            logger.error("Error occurred while reading the wsdl file " + pathToWSDL, e);
        } catch (SchemaException e) {
            logger.error("Error occurred while resolving schema types ", e);
        }
    }

    /**
     * Bundle activation method
     */
    protected void activate(ComponentContext context) {
        try {
            logger.info("Registry WSDL Visualizer bundle is activated");
        } catch (Throwable e) {
            logger.error("Failed to activate Registry APP bundle", e);
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceHolder.unsetRegistryService();
    }

    /**
     * Service object contains all the details related to a particular service. This method returns such a list of
     * service objects.
     *
     * @return List of services
     */
    public String getListOfServices() {
        List<WSDLService> wsdlServices = new ArrayList<>();
        if (desc != null) {
            List<Service> services = desc.getServices();
            for (Service service : services) {
                WSDLService wsdlService = new WSDLService();
                //Set the service name
                wsdlService.setName(service.getQName().getLocalPart());

                HashMap<String, WSDLOperation> operationMap = new HashMap<>();
                //Get all the endpoints of each service
                List<Endpoint> endpoints = service.getEndpoints();
                int operationIndex = -1;
                int endpointIndex = -1;
                for (Endpoint endpoint : endpoints) {
                    Binding binding = endpoint.getBinding();
                    endpointIndex = endpointIndex + 1;
                    //Get list of operations for each binding
                    List<Operation> operations = binding.getInterface().getOperations();
                    for (Operation operation : operations) {
                        String operationName = operation.getQName().getLocalPart();
                        List<WSDLEndpoint> currentWSDLEndpoints = new ArrayList<>();
                        int currentOperationIndex;
                        /*
                        Operations are listed under each binding. By below logic we categorize endpoints under each
                        unique operation.
                         */
                        //See whether it is an already listed operation. If so get it from the map and update endpoints.
                        if (operationMap.containsKey(operationName)) {
                            currentWSDLEndpoints = operationMap.get(operationName).getWSDLEndpoints();
                            currentOperationIndex = operationMap.get(operationName).getOperationIndex();
                        } else {
                            operationIndex = operationIndex + 1;
                            currentOperationIndex = operationIndex;
                        }
                        Input input = operation.getInput();

                        InputMessage inputMessage;
                        //handle if input is not defined
                        if (input != null) {
                            if (input.getParts().isEmpty()) {
                                //when message parts are empty, give operation name as the input name
                                inputMessage = new InputMessage(getMessageParts(operationName));
                            } else {
                                inputMessage = new InputMessage(getMessageParts(input.getMessageName().toString()));
                            }
                        } else {
                            List<MessagePart> messageParts = new ArrayList<>();
                            inputMessage = new InputMessage(messageParts);
                        }
                        Output output = operation.getOutput();
                        OutputMessage outputMessage;
                        //handle if output is not defined
                        if (output != null) {
                            outputMessage = new OutputMessage(getMessageParts(output.getMessageName().toString()));
                        } else {
                            List<MessagePart> messageParts = new ArrayList<>();
                            outputMessage = new OutputMessage(messageParts);
                        }
                        //handle if fault message is not defined

                        FaultMessage faultMessage;
                        if ((operation.getFaults() == null) || operation.getFaults().isEmpty()) {
                            List<MessagePart> messageParts = new ArrayList<>();
                            faultMessage = new FaultMessage(messageParts);
                        } else {
                            Fault fault = operation.getFaults().get(0);
                            faultMessage = new FaultMessage(getMessageParts(fault.getMessageName().toString()));
                        }

                        MessageGroup message = new MessageGroup(inputMessage, outputMessage, faultMessage);
                        // Set this endpoint to operation
                        WSDLEndpoint wsdlEndpoint = new WSDLEndpoint(endpoint.getAddress(), endpoint.getName(),
                                message, endpointIndex, operationName);
                        currentWSDLEndpoints.add(wsdlEndpoint);
                        operationMap.put(operationName, new WSDLOperation(operationName, currentWSDLEndpoints,
                                currentOperationIndex));


                    }
                }

                List<WSDLOperation> operations = new ArrayList<>(operationMap.values());
                Collections.sort(operations);
                wsdlService.setOperations(operations);
                wsdlServices.add(wsdlService);
            }
        }
        return ConvertJavaToJson.getServiceListJsonString(wsdlServices);

    }

    /**
     * For each input/output/fault there is a message element. Each message element can have message parts elements
     * which describes input/output/fault name and type. This method returns such a list of message part when name
     * of the message element is given.
     *
     * @param messageName Name of the message element
     * @return List of message part elements
     */
    public List<MessagePart> getMessageParts(String messageName) {
        List<MessagePart> messageParts = new ArrayList<>();
        for (MessageImpl message : messages) {
            if (message.getQName().toString().equals(messageName)) {
                List<Part> parts = message.getParts();
                for (Part part : parts) {
                    String name = part.getPartQName().getLocalPart();
                    String type = Constants.UNDEFINED_TYPE;
                    String typeLocalPart = Constants.UNDEFINED_TYPE;

                    Type typeObj = part.getType();
                    if (typeObj != null) {
                        type = typeObj.getQName().toString();
                        //get the type name without the namespace
                        typeLocalPart = typeObj.getQName().getLocalPart();
                    } else {
                        Element elemObj = part.getElement();
                        if (elemObj != null) {
                            type = elemObj.getQName().toString();
                            typeLocalPart = elemObj.getQName().getLocalPart();
                        }
                    }
                    List<WSDLElement> subElms = getAllTypes().get(type);
                    MessagePart messagePart;
                    if (subElms != null) {
                        messagePart = new MessagePart(name, typeLocalPart, removeNamespaces(subElms));
                    } else {
                        messagePart = new MessagePart(name, typeLocalPart);
                    }
                    messageParts.add(messagePart);
                }
                break;
            }
        }
        if (messageParts.isEmpty()) {
            MessagePart messagePart = new MessagePart(messageName, Constants.EMPTY_TYPE);
            messageParts.add(messagePart);
        }
        return messageParts;
    }

    /**
     * This method is used to remove the namespaces of the element types.
     *
     * @param wsdlElements A list of WSDL elements.
     * @return Namespace removed list of WSDL elements.
     */
    public List<WSDLElement> removeNamespaces(List<WSDLElement> wsdlElements) {
        List<WSDLElement> elementsWithoutNameSpaces = new ArrayList<>();
        for (WSDLElement elm : wsdlElements) {
            String type = getLocalName(elm.getType());
            elm.setType(type);
            List<WSDLElement> subElements = removeNamespaces(elm.getSubElements());
            elm.setSubElements(subElements);
            elementsWithoutNameSpaces.add(elm);
        }
        return elementsWithoutNameSpaces;
    }

    /**
     * This method create a map of all the complex types in this method. In this method inner complex types are not
     * navigated to primitive types.   Handled types are, elements, complexTypes, simpleTypes, attributes,
     * attributeGroup
     */
    public void createMapOfComplexTypes() throws SchemaException {
        Types types = desc.getTypes();

        if (types != null) {
            List<Schema> schemas = types.getSchemas();

            for (Schema schema : schemas) {

                /*
                e.g.
                     <s:element name="GetQuoteResponse">
                        <s:complexType>
                          <s:sequence>
                            <s:element maxOccurs="1" minOccurs="0" name="GetQuoteResult" type="s:string"/>
                          </s:sequence>
                        </s:complexType>
                     </s:element>
                 */
                if ((schema.getElements() != null) && !(schema.getElements().isEmpty())) {
                    getAllTypes().putAll(getElementTypes(schema.getElements(), schema.getTargetNamespace()));
                }

                 /*
                 e.g.
                    <complexType name="ArrayOf_tns1_Review">
                        <complexContent>
                         <restriction base="soapenc:Array">
                          <attribute ref="soapenc:arrayType" wsdl:arrayType="tns1:Review[]"/>
                         </restriction>
                        </complexContent>
                    </complexType>
                  */
                if ((schema.getTypes() != null) && !(schema.getTypes().isEmpty())) {
                    getAllTypes().putAll(getSchemaTypes(schema.getTypes(), schema.getTargetNamespace()));
                }


                /*
                    e.g.
                     <s:attribute name="code">
                        <s:simpleType>
                            <s:restriction base="s:string">
                                <s:pattern value="[A-Z][A-Z]"/>
                            </s:restriction>
                        </s:simpleType>
                    </s:attribute>
                 */
                if ((schema.getAttributes() != null) && !(schema.getAttributes().isEmpty())) {
                    getAllTypes().putAll(getAttributeTypes(schema.getAttributes(), schema.getTargetNamespace()));
                }

                /*
                    e.g.
                        <xs:attributeGroup name="personattr">
                          <xs:attribute name="attr1" type="string"/>
                          <xs:attribute name="attr2" type="integer"/>
                        </xs:attributeGroup>
                 */
                if (((SchemaImpl) schema).getModel().getSimpleTypeOrComplexTypeOrGroup() != null) {
                    List<Annotated> listOfAnnoteted = ((SchemaImpl) schema).getModel()
                            .getSimpleTypeOrComplexTypeOrGroup();
                    for (Annotated annotated : listOfAnnoteted) {
                        if (annotated.getClass().toString().equals("class org.ow2.easywsdl.schema.org.w3._2001"
                                                                   + ".xmlschema"
                                                                   + ".NamedAttributeGroup")) {
                            List<WSDLElement> wsdlElements = getElementsOfAttrGroup((NamedAttributeGroup) annotated,
                                    schema.getTargetNamespace());
                            getAllTypes().put(getTypeWithNamespace(schema.getTargetNamespace(), new QName("", (
                                            (NamedAttributeGroup) annotated).getName())),
                                    wsdlElements);

                        } else if (annotated.getClass().toString().equals("class org.ow2.easywsdl.schema.org.w3._2001"
                                                                          + ".xmlschema"
                                                                          + ".NamedGroup")) {
                            List<WSDLElement> newElmList = new ArrayList<>();
                            newElmList = getElementsOfParticles(((NamedGroup) annotated).getParticle(), newElmList,
                                    schema.getTargetNamespace());
                            getAllTypes().put(getTypeWithNamespace(schema.getTargetNamespace(), new QName("", (
                                    (NamedGroup) annotated).getName())), newElmList);

                        }
                    }
                }


            }

            //Dig down the elements of complex types until a primitive is found
            linkElementsOfComplexTypes();
        }

    }


    /**
     * This methods will dig down the complex types to primitive types
     */
    public void linkElementsOfComplexTypes() {
        Iterator iterator = getAllTypes().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<WSDLElement>> entry = (Map.Entry<String, List<WSDLElement>>) iterator.next();
            //typesStack is used to avoid unlimited recursive calls

            if (!(isPrimitive(getLocalName(entry.getKey())))) {
                typesStack = new HashMap<>();
                typesStack.put(entry.getKey(), entry.getKey());
                List<WSDLElement> elements = entry.getValue();
                getRecursiveTypes(elements);
            } else {
                iterator.remove();
            }

        }
    }

    /**
     * This method is used to dig down complex types to primitive types. This method is called recursively until a
     * primitive types is found.
     *
     * @param elements List of complex types without sub elements
     * @return A list of complex types with their sub elements
     */
    public List<WSDLElement> getRecursiveTypes(List<WSDLElement> elements) {

        for (WSDLElement element : elements) {

            if (!isPrimitive(getLocalName(element.getType()))) {
                if (typesStack.get(element.getType()) == null) {
                    typesStack.put(element.getType(), element.getType());
                    List<WSDLElement> wsdlElms = getAllTypes().get(element.getType());
                    if (wsdlElms != null) {
                        element.setSubElements(wsdlElms);
                        getRecursiveTypes(wsdlElms);
                    }
                }
            }
        }

        return elements;
    }


    /**
     * This method will return the name of an element type without the namespace.
     *
     * @param nameWithNameSpace Element type with its namespace
     * @return Element type name without namespace.
     */
    public String getLocalName(String nameWithNameSpace) {
        String[] nameSpaceSplit = nameWithNameSpace.split("}");
        return nameSpaceSplit[nameSpaceSplit.length - 1];
    }

    /**
     * This method returns a list of sub elements of AttributeGroup element when Attribute group is given
     *
     * @param attrGroup       AttributeGroup element
     * @param targetNamespace TargetNamespace of the element
     * @return List of sub elements
     */
    public List<WSDLElement> getElementsOfAttrGroup(NamedAttributeGroup attrGroup, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();

        if (attrGroup.getAttributeOrAttributeGroup() != null) {
            for (Annotated innerAttr : attrGroup.getAttributeOrAttributeGroup()) {
                if (innerAttr.getClass().toString().equals("class org.ow2.easywsdl.schema.org.w3._2001"
                                                           + ".xmlschema.Attribute")) {
                    wsdlElements.addAll(getElementsOfAttribute((org.ow2.easywsdl.schema.org.w3._2001
                            .xmlschema.Attribute) innerAttr, targetNamespace));
                } else if (innerAttr.getClass().toString().equals("class org.ow2.easywsdl.schema.org.w3._2001"
                                                                  + ".xmlschema.AttributeGroupRef")) {
                    wsdlElements.addAll(getElementsAttrGroupRef((AttributeGroupRef) innerAttr, targetNamespace));
                }

            }
        }
        return wsdlElements;
    }

    /**
     * This method returns subElements of attribute elements of attributeGroup elements
     *
     * @param annotations     List of annotated types
     * @param targetNamespace TargetNamespace of the element
     * @return List of sub elements of attribute to attributeGroup type
     */
    public List<WSDLElement> getElementsOfAttrOrAtrrGroup(List<Annotated> annotations, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();
        for (Annotated annotated : annotations) {
            if (annotated.getClass().toString().equals("class org.ow2.easywsdl.schema.org.w3._2001.xmlschema"
                                                       + ".Attribute")) {
                if (((org.ow2.easywsdl.schema.org.w3._2001
                        .xmlschema.Attribute) annotated).getName() != null && ((org.ow2.easywsdl.schema.org.w3._2001
                        .xmlschema.Attribute) annotated).getType() == null) {
                    WSDLElement wsdlElement = new WSDLElement(Constants.ATTRIBUTE_TYPE, ((org.ow2.easywsdl.schema.org
                            .w3._2001.xmlschema.Attribute) annotated).getName());
                    wsdlElement.setSubElements(getElementsOfAttribute(((org.ow2.easywsdl.schema.org.w3._2001.xmlschema
                            .Attribute) annotated), targetNamespace));
                    wsdlElements.add(wsdlElement);
                } else {
                    wsdlElements.addAll(getElementsOfAttribute(((org.ow2.easywsdl.schema.org.w3._2001.xmlschema
                            .Attribute) annotated), targetNamespace));
                }
            } else if (annotated.getClass().toString().equals("class org.ow2.easywsdl.schema.org.w3._2001.xmlschema"
                                                              + ".NamedAttributeGroup")) {
                if (((NamedAttributeGroup) annotated).getName() != null) {
                    WSDLElement wsdlElement = new WSDLElement(Constants.ATTRIBUTE_GROUP_TYPE, ((NamedAttributeGroup)
                            annotated).getName());
                    wsdlElement.setSubElements(getElementsOfAttrGroup((NamedAttributeGroup) annotated,
                            targetNamespace));
                    wsdlElements.add(wsdlElement);
                } else {
                    wsdlElements.addAll(getElementsOfAttrGroup((NamedAttributeGroup) annotated, targetNamespace));
                }

            } else if (annotated.getClass().toString().equals("class org.ow2.easywsdl.schema.org.w3._2001.xmlschema"
                                                              + ".AttributeGroupRef")) {
                wsdlElements.addAll(getElementsAttrGroupRef((AttributeGroupRef) annotated, targetNamespace));

            }
        }
        return wsdlElements;
    }

    /**
     * This method returns Attribute reference of attribute group
     * <p/>
     * e.g.  <s:attributeGroup ref="personattr"/>
     *
     * @param attributeGroupRef AttributeGroupRef object
     * @param targetNamespace   TargetNamespace of the element
     * @return A list contains a WSDLElement which has attributeGroup reference
     */
    public List<WSDLElement> getElementsAttrGroupRef(AttributeGroupRef attributeGroupRef, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();
        WSDLElement wsdlElement = new WSDLElement(Constants.REFERENCE_TYPE, getTypeWithNamespace(targetNamespace,
                attributeGroupRef.getRef()));
        wsdlElements.add(wsdlElement);
        return wsdlElements;
    }

    /**
     * This method create a map of list of sub elements against element name
     *
     * @param schemaElements  List of elements
     * @param targetNamespace TargetNamespace of the element
     * @return Map of list of sub elements against element name
     */
    public HashMap<String, List<WSDLElement>> getElementTypes(List<Element> schemaElements, String targetNamespace) {
        HashMap<String, List<WSDLElement>> elementTypeToElements = new HashMap<>();
        for (Element elm : schemaElements) {
            ElementImpl elmImpl = (ElementImpl) elm;
            elementTypeToElements.put(getTypeWithNamespace(targetNamespace, elmImpl.getQName()), getElementsOfElement
                    (elm, targetNamespace));

        }
        return elementTypeToElements;
    }


    /**
     * Schemas can have types which start with elements. e.g.
     * <s:element name="GetCityForecastByZIP">
     * <s:complexType>
     * <s:sequence>
     * <s:element maxOccurs="1" minOccurs="0" name="ZIP" type="s:string"/>
     * </s:sequence>
     * </s:complexType>
     * </s:element>
     * <p/>
     * This method returns sub elements of those types.
     *
     * @param elm             Element object
     * @param targetNamespace TargetNamespace of the element
     * @return A list of sub-elements of element type.
     */
    public List<WSDLElement> getElementsOfElement(Element elm, String targetNamespace) {
        List<WSDLElement> listOfWSDLElements = new ArrayList<>();
        ElementImpl elmImpl = (ElementImpl) elm;
        if (elmImpl.getType() != null) {
            if (elmImpl.getType().getClass().toString().equals("class org.ow2.easywsdl.schema.impl"
                                                               + ".ComplexTypeImpl")) {

                ComplexType complexType = (ComplexType) elmImpl.getType();
                /*
                e.g.
                <s:element name="GetQuote">
                    <s:complexType>
                      <s:sequence>
                        <s:element maxOccurs="1" minOccurs="0" name="symbol" type="s:string"/>
                      </s:sequence>
                    </s:complexType>
                </s:element>
                 */

                if (complexType != null) {
                    if(elmImpl.getType().getQName() != null){
                        WSDLElement tempElm = new WSDLElement(Constants.COMPLEX_TYPE, getTypeWithNamespace
                                (targetNamespace, elmImpl.getType().getQName()));
                        tempElm.setSubElements(getElementsOfComplexType(complexType, targetNamespace));
                        listOfWSDLElements.add(tempElm);
                    }else {
                        listOfWSDLElements = getElementsOfComplexType(complexType, targetNamespace);
                    }
                }
            } else if (elmImpl.getType().getClass().toString().equals("class org.ow2.easywsdl.schema.impl"
                                                                      + ".SimpleTypeImpl")) {
                SimpleType simpleType = (SimpleType) elmImpl.getType();
                if (simpleType != null) {
                    if (simpleType.getQName() != null) {
                        if (isPrimitive(simpleType.getQName().getLocalPart())) {
                            WSDLElement wsdlElement = new WSDLElement(elmImpl.getQName().getLocalPart(),
                                    getTypeWithNamespace(targetNamespace, simpleType.getQName()));
                            listOfWSDLElements.add(wsdlElement);
                        } else {
                            WSDLElement wsdlElement = new WSDLElement("simple_type", getTypeWithNamespace
                                    (targetNamespace, simpleType.getQName()));
                            listOfWSDLElements.add(wsdlElement);
                        }
                    } else {
                        listOfWSDLElements = getElementsOfSimpleType(simpleType, targetNamespace);
                    }
                }
            }
        } else {
                /*
                    e.g.
                     <s:element name="order" type="ordertype"/>
                 */
            List<WSDLElement> wsdlElements = new ArrayList<>();
            if (elmImpl.getModel().getType() != null) {
                wsdlElements.add(new WSDLElement(Constants.COMPLEX_TYPE, getTypeWithNamespace(targetNamespace, elmImpl
                        .getModel()
                        .getType())));
                listOfWSDLElements = wsdlElements;
            }
        }
        return listOfWSDLElements;
    }


    /**
     * There are two types of types elements. Those are simpleTypes and complexType. This method create a map of complex
     * types and simple types in a schema
     *
     * @param schemaTypes     List of type elements
     * @param targetNamespace TargetNamespace of the element
     * @return Map of list of sub elements against the type name
     */
    public HashMap<String, List<WSDLElement>> getSchemaTypes(List<Type> schemaTypes, String targetNamespace) {
        HashMap<String, List<WSDLElement>> schemaTypeToElements = new HashMap<>();
        for (Type type : schemaTypes) {
            if (type.getClass().toString().equals("class org.ow2.easywsdl.schema.impl.ComplexTypeImpl")) {
                ComplexType complexType = (ComplexTypeImpl) type;
                List<WSDLElement> wsdlElements = getElementsOfComplexType(complexType, targetNamespace);

                schemaTypeToElements.put(getTypeWithNamespace(targetNamespace, complexType.getQName()), wsdlElements);
            } else if (type.getClass().toString().equals("class org.ow2.easywsdl.schema.impl.SimpleTypeImpl")) {
                //Handle SimpleTypes
                List<WSDLElement> wsdlElements = getElementsOfSimpleType((SimpleTypeImpl) type, targetNamespace);
                schemaTypeToElements.put(getTypeWithNamespace(targetNamespace, type.getQName()), wsdlElements);

            }
        }
        return schemaTypeToElements;
    }

    /**
     * This method adds the target namespace if the QName of an element does not have a namespace.
     *
     * @param targetNamespace Target namespace of the schema.
     * @param queueName       QName of an element.
     * @return Element type with the namespace.
     */
    private String getTypeWithNamespace(String targetNamespace, QName queueName) {
        String typeName;
        if (queueName.getNamespaceURI().equals("")) {
            typeName = "{" + targetNamespace + "}" + queueName.toString();
        } else {
            typeName = queueName.toString();
        }
        return typeName;
    }


    /**
     * This method returns a Map of list sub elements for each Attribute type in the schema
     * <p/>
     * e.g.
     * <s:attribute name="code">
     * <s:simpleType>
     * <s:restriction base="s:string">
     * <s:pattern value="[A-Z][A-Z]"/>
     * </s:restriction>
     * </s:simpleType>
     * </s:attribute>
     *
     * @param attributes      List of attributes elements
     * @param targetNamespace TargetNamespace of the element
     * @return Map of list of sub elements against attribute name
     */
    public HashMap<String, List<WSDLElement>> getAttributeTypes(List<Attribute> attributes, String targetNamespace) {
        HashMap<String, List<WSDLElement>> attributeToElements = new HashMap<>();
        for (Attribute attr : attributes) {
            attributeToElements.put(getTypeWithNamespace(targetNamespace, new QName("", attr.getName())),
                    getElementsOfAttribute
                            (attr,
                                    targetNamespace));
        }
        return attributeToElements;
    }


    /**
     * Dig down to sub elements of a complex type
     *
     * @param complexType     Complex type object
     * @param targetNamespace TargetNamespace of the element
     * @return List of sub elements of complex type
     */
    public List<WSDLElement> getElementsOfComplexType(ComplexType complexType, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();
        if (complexType.getSequence() != null) {
            wsdlElements = getElementsOfSequence(complexType.getSequence(), targetNamespace);
        } else if (complexType.getAll() != null) {
            wsdlElements = getElementsOfAll(complexType.getAll(), targetNamespace);
        } else if (complexType.getChoice() != null) {
            wsdlElements = getElementsOfChoice(complexType.getChoice(), targetNamespace);
        } else if (((ComplexTypeImpl) complexType).getModel().getGroup() != null) {
            WSDLElement wsdlElement = new WSDLElement(Constants.REFERENCE_TYPE, getTypeWithNamespace(targetNamespace, (
                    (ComplexTypeImpl) complexType)
                    .getModel().getGroup().getRef()));
            wsdlElements.add(wsdlElement);
        }

        if (complexType.getComplexContent() != null) {
            wsdlElements = getElementsOfComplexContent(complexType.getComplexContent(), targetNamespace);
        } else if (complexType.getSimpleContent() != null) {
            wsdlElements = getElementsOfSimpleContent(complexType.getSimpleContent(), targetNamespace);
        }

        if (((ComplexTypeImpl) complexType).getModel().getAttributeOrAttributeGroup() != null) {
            List<Annotated> annotations = ((ComplexTypeImpl) complexType).getModel().getAttributeOrAttributeGroup();
            wsdlElements.addAll(getElementsOfAttrOrAtrrGroup(annotations, targetNamespace));
        }

        return wsdlElements;
    }

    /**
     * This method returns sub elements of simple types
     *
     * @param simpleType      SimpleType element
     * @param targetNamespace TargetNamespace of the element
     * @return List of sub elements of the given simple type
     */
    public List<WSDLElement> getElementsOfSimpleType(SimpleType simpleType, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();
        if (simpleType.getRestriction() != null) {

                    /*  e.g.
                        <xs:element name="initials">
                          <xs:simpleType>
                            <xs:restriction base="xs:string">
                              <xs:pattern value="[a-zA-Z][a-zA-Z][a-zA-Z]"/>
                            </xs:restriction>
                          </xs:simpleType>
                        </xs:element>
                     */
            Restriction restrictionType = simpleType.getRestriction();
            WSDLElement wsdlElm = new WSDLElement(Constants.RESTRICTION_BASE_TYPE, getTypeWithNamespace
                    (targetNamespace, restrictionType.getBase()));

            wsdlElements.add(wsdlElm);
        } else if (((SimpleTypeImpl) simpleType).getModel().getList() != null) {
            /*
                e.g.
                <xs:simpleType name="valuelist">
                  <xs:list itemType="xs:string"/>
                </xs:simpleType>
             */
            SimpleTypeImpl simpleTypeImpl = (SimpleTypeImpl) simpleType;
            org.ow2.easywsdl.schema.org.w3._2001.xmlschema.List list = simpleTypeImpl.getModel().getList();
            if (list.getItemType() != null) {
                WSDLElement wsdlElm = new WSDLElement("list_type", getTypeWithNamespace(targetNamespace, list
                        .getItemType()));
                wsdlElements.add(wsdlElm);
            } else if (list.getSimpleType() != null) {
                wsdlElements = getElementsOfLocalSimpleType(list.getSimpleType(), targetNamespace);

            }
        } else if (((SimpleTypeImpl) simpleType).getModel().getUnion() != null) {
            /*
                e.g.
                 <s:element name="jeans_size">
                    <s:simpleType>
                        <s:union memberTypes="sizebyno sizebystring"/>
                    </s:simpleType>
                 </s:element>
             */
            Union union = ((SimpleTypeImpl) simpleType).getModel().getUnion();
            List<QName> simpleTypeNames = union.getMemberTypes();
            for (QName simpleTypeName : simpleTypeNames) {
                WSDLElement wsdlElement = new WSDLElement("union_param", getTypeWithNamespace(targetNamespace,
                        simpleTypeName));
                wsdlElements.add(wsdlElement);
            }
        }
        return wsdlElements;
    }

    /**
     * This method returns a list of sub elements when a local simple type is given
     *
     * @param localSimpleType LocalSimpleType element
     * @param targetNamespace TargetNamespace of the element
     * @return A list of sub elements
     */
    public List<WSDLElement> getElementsOfLocalSimpleType(LocalSimpleType localSimpleType, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();
        if (localSimpleType.getRestriction() != null) {

                    /*  e.g.
                        <xs:element name="initials">
                          <xs:simpleType>
                            <xs:restriction base="xs:string">
                              <xs:pattern value="[a-zA-Z][a-zA-Z][a-zA-Z]"/>
                            </xs:restriction>
                          </xs:simpleType>
                        </xs:element>
                     */
            org.ow2.easywsdl.schema.org.w3._2001.xmlschema.Restriction restrictionType = localSimpleType
                    .getRestriction();
            WSDLElement wsdlElm = new WSDLElement(Constants.RESTRICTION_BASE_TYPE, getTypeWithNamespace
                    (targetNamespace, restrictionType.getBase()));

            wsdlElements.add(wsdlElm);
        } else if (localSimpleType.getList() != null) {
            /*
                e.g.
                <xs:simpleType name="valuelist">
                  <xs:list itemType="xs:string"/>
                </xs:simpleType>
             */

            org.ow2.easywsdl.schema.org.w3._2001.xmlschema.List list = localSimpleType.getList();
            if (list.getItemType() != null) {
                WSDLElement wsdlElm = new WSDLElement("list_type", getTypeWithNamespace(targetNamespace, list
                        .getItemType()));
                wsdlElements.add(wsdlElm);
            } else if (list.getSimpleType() != null) {
                wsdlElements = getElementsOfLocalSimpleType(list.getSimpleType(), targetNamespace);

            }
        } else if (localSimpleType.getUnion() != null) {
            Union union = localSimpleType.getUnion();
            List<QName> simpleTypeNames = union.getMemberTypes();
            for (QName simpleTypeName : simpleTypeNames) {
                WSDLElement wsdlElement = new WSDLElement("union_param", getTypeWithNamespace(targetNamespace,
                        simpleTypeName));
                wsdlElements.add(wsdlElement);
            }
        }
        return wsdlElements;
    }

    /**
     * This method returns a list of sub elements when an attribute element is given
     *
     * @param attribute       Attribute element
     * @param targetNamespace TargetNamespace of the element
     * @return A list of sub elements
     */
    public List<WSDLElement> getElementsOfAttribute(Attribute attribute, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();
        AttributeImpl attrImpl = (AttributeImpl) attribute;
        if (attrImpl.getModel().getSimpleType() != null) {
            wsdlElements = getElementsOfLocalSimpleType((attrImpl.getModel().getSimpleType()), targetNamespace);
        }
        return wsdlElements;
    }

    /**
     * This method returns a list of sub elements when an attribute element is given
     *
     * @param attribute       Attribute element
     * @param targetNamespace TargetNamespace of the element
     * @return A list of sub elements
     */
    public List<WSDLElement> getElementsOfAttribute(org.ow2.easywsdl.schema.org.w3._2001.xmlschema.Attribute
                                                            attribute, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();

        if (attribute.getSimpleType() != null) {
            wsdlElements = getElementsOfLocalSimpleType(attribute.getSimpleType(), targetNamespace);
        } else if (attribute.getType() != null) {
            String attributeName = Constants.UNDEFINED_TYPE;
            if (attribute.getName() != null) {
                attributeName = attribute.getName();
            }
            WSDLElement wsdlElement = new WSDLElement(attributeName, getTypeWithNamespace(targetNamespace, attribute
                    .getType()));
            wsdlElements.add(wsdlElement);
        } else if (attribute.getRef() != null) {
            WSDLElement wsdlElement = new WSDLElement(Constants.REFERENCE_TYPE, getTypeWithNamespace(targetNamespace,
                    attribute.getRef()));
            wsdlElements.add(wsdlElement);
        }
        return wsdlElements;
    }

    /**
     * This method returns a list of sub elements when a sequence is given
     *
     * @param sequence        Sequence element
     * @param targetNamespace TargetNamespace of the element
     * @return A list of sub elements
     */
    public List<WSDLElement> getElementsOfSequence(Sequence sequence, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();
        if (sequence.getElements() != null && !(sequence.getElements().isEmpty())) {
            List<Element> elements = sequence.getElements();
            for (Element elm : elements) {
                WSDLElement wsdlElm = getElementsOfInnerElement(elm, targetNamespace);
                wsdlElements.add(wsdlElm);
            }
        } else if (((SequenceImpl) sequence).getModel() != null) {
            if (((SequenceImpl) sequence).getModel().getParticle() != null) {
                List<WSDLElement> emptyElementList = new ArrayList<>();
                wsdlElements = getElementsOfParticles(((SequenceImpl) sequence).getModel().getParticle(),
                        emptyElementList, targetNamespace);
            }
        }
        return wsdlElements;
    }

    /**
     * This method returns a list of sub elements when a choice element is given
     *
     * @param choice          Choice element
     * @param targetNamespace TargetNamespace of the element
     * @return A list of sub elements of choice element
     */
    public List<WSDLElement> getElementsOfChoice(Choice choice, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();
        ExplicitGroup choiceModel = ((ChoiceImpl) choice).getModel();
        if ((choice.getElements() != null) && !(choice.getElements().isEmpty())) {
            List<Element> elements = choice.getElements();
            for (Element elm : elements) {
                WSDLElement wsdlElm = getElementsOfInnerElement(elm, targetNamespace);
                wsdlElements.add(wsdlElm);
            }
        } else if (choiceModel != null) {
            if (choiceModel.getParticle() != null) {
                List<Object> particles = choiceModel.getParticle();
                List<WSDLElement> newWSDLElmList = new ArrayList<>();
                wsdlElements.addAll(getElementsOfParticles(particles, newWSDLElmList, targetNamespace));
            }
        }
        return wsdlElements;
    }

    /**
     * This class will return a list of sub elements when there is a list of particle elements
     *
     * @param particles        List of particle elements
     * @param wsdlElementsList An empty list of WSDLElement.
     * @param targetNamespace  TargetNamespace of the element
     * @return A list of WSDLElement.
     */
    public List<WSDLElement> getElementsOfParticles(List<Object> particles, List<WSDLElement> wsdlElementsList,
                                                    String targetNamespace) {
        for (Object obj : particles) {
            if (obj.getClass().toString().equals("class javax.xml.bind.JAXBElement")) {
                JAXBElement particle = (JAXBElement) obj;
                if (particle.getValue() != null) {
                    if (particle.getValue().getClass().toString().equals("class org.ow2.easywsdl.schema.org.w3._2001"
                                                                         + ".xmlschema.ExplicitGroup")) {
                        getElementsOfParticles(((ExplicitGroup) particle.getValue()).getParticle(), wsdlElementsList,
                                targetNamespace);
                    } else if (particle.getValue().getClass().toString().equals
                            ("class org.ow2.easywsdl.schema.org.w3._2001.xmlschema.LocalElement") &&
                            ((LocalElement) particle.getValue()).getType() != null) {
                        WSDLElement wsdlElement = new WSDLElement(((LocalElement) particle.getValue()).getName(),
                                                                  getTypeWithNamespace(targetNamespace,
                                                                  ((LocalElement) particle.getValue()).getType()));
                        wsdlElementsList.add(wsdlElement);

                    } else if (particle.getValue().getClass().toString().equals
                            ("class org.ow2.easywsdl.schema.org.w3._2001.xmlschema.GroupRef") &&
                            ((GroupRef) particle.getValue()).getRef() != null) {
                        WSDLElement wsdlElement = new WSDLElement(Constants.REFERENCE_TYPE,
                                                                  getTypeWithNamespace(targetNamespace,
                                                                  ((GroupRef) particle.getValue()).getRef()));
                        wsdlElementsList.add(wsdlElement);
                    } else if (particle.getValue().getClass().toString().equals("class org.ow2.easywsdl.schema.org.w3"
                                                                                + "._2001.xmlschema.All")) {
                        getElementsOfParticles(((org.ow2.easywsdl.schema.org.w3._2001.xmlschema.All) particle
                                .getValue()).getParticle(), wsdlElementsList, targetNamespace);

                    }
                }
            }
        }
        return wsdlElementsList;
    }

    /**
     * This method returns a list of sub elements when a group element is given
     *
     * @param group           Group element
     * @param targetNamespace TargetNamespace of the element
     * @return A list of sub elements of group element
     */
    public List<WSDLElement> getElementsOfGroup(Group group, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();
        if ((group.getElements() != null) && !(group.getElements().isEmpty())) {
            List<Element> elements = group.getElements();
            for (Element elm : elements) {
                WSDLElement wsdlElm = getElementsOfInnerElement(elm, targetNamespace);
                wsdlElements.add(wsdlElm);
            }
        }
        return wsdlElements;
    }

    /**
     * This method returns a list of sub elements when an all element is given
     *
     * @param all             All element
     * @param targetNamespace TargetNamespace of the element
     * @return A list of sub elements of "all" element
     */
    public List<WSDLElement> getElementsOfAll(All all, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();
        if ((all.getElements() != null) && !(all.getElements().isEmpty())) {
            List<Element> elements = all.getElements();
            for (Element elm : elements) {

                WSDLElement wsdlElm = getElementsOfInnerElement(elm, targetNamespace);
                wsdlElements.add(wsdlElm);
            }
        }
        return wsdlElements;
    }

    /**
     * This method returns a list of sub elements when complexContent element is given. ComplexContent comes under
     * complex elements.
     *
     * @param complexContent  ComplexContent element
     * @param targetNamespace TargetNamespace of the element
     * @return A list of sub elements of ComplexContent element
     */
    public List<WSDLElement> getElementsOfComplexContent(ComplexContent complexContent, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();
        if (((ComplexContentImpl) complexContent).getModel().getRestriction() != null) {
            ComplexRestrictionType restrictionType = ((ComplexContentImpl) complexContent).getModel().getRestriction();

            WSDLElement wsdlElm = new WSDLElement(Constants.RESTRICTION_BASE_TYPE, getTypeWithNamespace
                    (targetNamespace, restrictionType.getBase()));
            if (restrictionType.getSequence() != null) {
                List<WSDLElement> emptyElementList = new ArrayList<>();
                wsdlElements = getElementsOfParticles(restrictionType.getSequence().getParticle(), emptyElementList,
                        targetNamespace);

            } else if (restrictionType.getAll() != null) {
                List<WSDLElement> emptyElementList = new ArrayList<>();
                wsdlElements = getElementsOfParticles(restrictionType.getAll().getParticle(), emptyElementList,
                        targetNamespace);
            } else if (restrictionType.getChoice() != null) {
                List<WSDLElement> emptyElementList = new ArrayList<>();
                wsdlElements = getElementsOfParticles(restrictionType.getChoice().getParticle(), emptyElementList,
                        targetNamespace);
            } else if (restrictionType.getGroup() != null) {
                WSDLElement wsdlElement = new WSDLElement(Constants.REFERENCE_TYPE, getTypeWithNamespace
                        (targetNamespace, restrictionType.getGroup().getRef()));
                wsdlElements.add(wsdlElement);
            }
            if (restrictionType.getAttributeOrAttributeGroup() != null) {
                List<Annotated> annotations = restrictionType.getAttributeOrAttributeGroup();
                wsdlElements.addAll(getElementsOfAttrOrAtrrGroup(annotations, targetNamespace));
            }
            wsdlElements.add(wsdlElm);
        } else if (((ComplexContentImpl) complexContent).getModel().getExtension() != null) {
            ExtensionType extensionType = ((ComplexContentImpl)
                    complexContent).getModel().getExtension();
            wsdlElements = getElementsOfExtension(extensionType, targetNamespace);

        }
        return wsdlElements;
    }

    /**
     * This method returns a list of sub elements when simpleContent element is given. simpleContent comes under
     * complex elements.
     *
     * @param simpleContent   SimpleContent element
     * @param targetNamespace TargetNamespace of the element
     * @return A list of sub elements of simpleContent element
     */
    public List<WSDLElement> getElementsOfSimpleContent(SimpleContent simpleContent, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();
        if (((SimpleContentImpl) simpleContent).getModel().getRestriction() != null) {
            SimpleRestrictionType restrictionType = ((SimpleContentImpl) simpleContent).getModel().getRestriction();
            /*
                e.g.
                <xs:element name="age">
                  <xs:simpleType>
                    <xs:restriction base="xs:integer">
                      <xs:minInclusive value="0"/>
                      <xs:maxInclusive value="100"/>
                    </xs:restriction>
                  </xs:simpleType>
                </xs:element>
             */
            WSDLElement wsdlElm = new WSDLElement(Constants.RESTRICTION_BASE_TYPE, getTypeWithNamespace
                    (targetNamespace, restrictionType.getBase()));
            if (restrictionType.getSimpleType() != null) {
                wsdlElements = getElementsOfLocalSimpleType(restrictionType.getSimpleType(), targetNamespace);

            }
            if (restrictionType.getAttributeOrAttributeGroup() != null) {
                List<Annotated> annotations = restrictionType.getAttributeOrAttributeGroup();
                wsdlElements.addAll(getElementsOfAttrOrAtrrGroup(annotations, targetNamespace));
            }
            wsdlElements.add(wsdlElm);
        } else if (((SimpleContentImpl) simpleContent).getModel().getExtension() != null) {
            /*
                e.g.
                <xs:simpleContent>
                <xs:extension base="size">
                  <xs:attribute name="sex">
                    <xs:simpleType>
                      <xs:restriction base="xs:string">
                        <xs:enumeration value="male" />
                        <xs:enumeration value="female" />
                      </xs:restriction>
                    </xs:simpleType>
                  </xs:attribute>
                </xs:extension>
              </xs:simpleContent>
             */
            ExtensionType extensionType = ((SimpleContentImpl)
                    simpleContent).getModel().getExtension();
            wsdlElements = getElementsOfExtension(extensionType, targetNamespace);
        }
        return wsdlElements;
    }


    /**
     * The extension element extends an existing simpleType or complexType element. This method
     * returns a list of sub elements for a given extension.
     *
     * @param extensionType   ExtensionType element
     * @param targetNamespace TargetNamespace of the element
     * @return A list of sub elements of extension element
     */
    public List<WSDLElement> getElementsOfExtension(ExtensionType extensionType, String targetNamespace) {
        List<WSDLElement> wsdlElements = new ArrayList<>();

        WSDLElement wsdlElm = new WSDLElement(Constants.RESTRICTION_BASE_TYPE, getTypeWithNamespace(targetNamespace,
                extensionType.getBase()));
        if (extensionType.getSequence() != null) {
            List<WSDLElement> newWsdlElementsList = new ArrayList<>();
            wsdlElements = getElementsOfParticles(extensionType.getSequence().getParticle(), newWsdlElementsList,
                    targetNamespace);
        } else if (extensionType.getAll() != null) {
            List<WSDLElement> newWsdlElementsList = new ArrayList<>();
            wsdlElements = getElementsOfParticles(extensionType.getAll().getParticle(), newWsdlElementsList,
                    targetNamespace);
        } else if (extensionType.getChoice() != null) {
            List<WSDLElement> newWsdlElementsList = new ArrayList<>();
            wsdlElements = getElementsOfParticles(extensionType.getChoice().getParticle(), newWsdlElementsList,
                    targetNamespace);
        } else if (extensionType.getGroup() != null) {
            WSDLElement wsdlElement = new WSDLElement(Constants.REFERENCE_TYPE, getTypeWithNamespace(targetNamespace,
                    extensionType.getGroup().getRef()));
            wsdlElements.add(wsdlElement);
        }

        if (extensionType.getAttributeOrAttributeGroup() != null) {
            List<Annotated> annotations = extensionType.getAttributeOrAttributeGroup();
            wsdlElements.addAll(getElementsOfAttrOrAtrrGroup(annotations, targetNamespace));

        }
        wsdlElements.add(wsdlElm);
        return wsdlElements;
    }

    /**
     * Element can be a primary element type in a schema.
     * e.g. <xs:element name="name" type="xs:string">
     * It defines the name of the element and type. This method returns WSDLElement object which has the name and
     * then type of the element type.
     *
     * @param elm             Element object
     * @param targetNamespace TargetNamespace of the element
     * @return WSDLElement created from element type.
     */
    public WSDLElement getElementsOfInnerElement(Element elm, String targetNamespace) {
        ElementImpl element = (ElementImpl) elm;
        List<WSDLElement> subElms = null;
        String type = Constants.UNDEFINED_TYPE;
        String paramName = element.getModel().getName();
        if (element.getRef() != null) {
            paramName = Constants.REFERENCE_TYPE;
            type = getTypeWithNamespace(targetNamespace, element.getRef());
        } else {
            if (paramName == null) {
                paramName = Constants.UNDEFINED_TYPE;
                QName typeQName = element.getModel().getType();
                if (typeQName != null) {
                    type = getTypeWithNamespace(targetNamespace, element.getModel().getType());
                }
            } else {
                QName typeQName = element.getModel().getType();
                if (typeQName != null) {
                    type = getTypeWithNamespace(targetNamespace, element.getModel().getType());
                } else {
                    type = paramName;
                    paramName = "element_type";
                    subElms = getElementsOfElement(elm, targetNamespace);
                }
            }
        }

        WSDLElement wsdlElement = new WSDLElement(paramName, type);
        if (subElms != null) {
            wsdlElement.setSubElements(subElms);
        }
        return wsdlElement;
    }


    /**
     * This method is to check whether a type is primitive or not
     *
     * @param type Type of element
     * @return Whether the type is primitive or not
     */
    public boolean isPrimitive(String type) {
        boolean isPrimitive = false;
        if (type.equals(Constants.STRING) ||
            type.equals(Constants.INT) ||
            type.equals(Constants.BOOLEAN) ||
            type.equals(Constants.DECIMAL) ||
            type.equals(Constants.FLOAT) ||
            type.equals(Constants.DOUBLE) ||
            type.equals(Constants.DURATION) ||
            type.equals(Constants.DATE_TIME) ||
            type.equals(Constants.TIME) ||
            type.equals(Constants.DATE) ||
            type.equals(Constants.G_YEAR_MONTH) ||
            type.equals(Constants.G_YEAR) ||
            type.equals(Constants.G_MONTH_DAY) ||
            type.equals(Constants.G_DAY) ||
            type.equals(Constants.G_MONTH) ||
            type.equals(Constants.HEX_BINARY) ||
            type.equals(Constants.BASE_64_BINARY) ||
            type.equals(Constants.ANY_URL) ||
            type.equals(Constants.Q_NAME) ||
            type.equals(Constants.TYPE_NOTATION)) {

            isPrimitive = true;

        }
        return isPrimitive;
    }

    public HashMap<String, List<WSDLElement>> getAllTypes() {
        return allTypes;
    }
}
