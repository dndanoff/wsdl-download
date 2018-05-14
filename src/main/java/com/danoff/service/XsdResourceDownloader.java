/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.danoff.service;

import com.ibm.wsdl.util.xml.DOM2Writer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.extensions.schema.SchemaReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Denis
 */
public class XsdResourceDownloader {
    
    private static final String IMPORT_TAG = "import";
    private static final String INCLUDE_TAG = "include";
    private static final String SCHEMA_LOCATION = "schemaLocation";

    private final File outputFolder;
    private final Definition definition;
    private final Map<String, String> processedResources;
    
    public XsdResourceDownloader(File outputFolder, Definition definition){
        this.outputFolder = outputFolder;
        this.definition = definition;
        this.processedResources = new HashMap<>();
    }
    
    public void download() throws IOException{
        Types wsdlTypes = definition.getTypes();
        if (wsdlTypes != null) {
            List extensibilityElements = wsdlTypes.getExtensibilityElements();
            for (Iterator iter = extensibilityElements.iterator(); iter.hasNext();) {
                Object currentObject = iter.next();
                if (currentObject instanceof Schema) {
                    Schema schema = (Schema) currentObject;
                    processSchema(schema);
                }
            }
        }
    }
    
    private void processSchema(Schema schema) throws IOException {
        String fileName = getXsdFileName(schema.getDocumentBaseURI());
        processedResources.put(schema.getDocumentBaseURI(), fileName);
                
        if (schema.getIncludes() != null) {
            for (SchemaReference ref : (List<SchemaReference>) schema.getIncludes()) {
                Schema includedSchema = ref.getReferencedSchema();
                if (includedSchema != null || ! processedResources.containsKey(ref.getSchemaLocationURI())) {
                    String fileNameChild = getXsdFileName(ref.getSchemaLocationURI());
                    processedResources.put(ref.getSchemaLocationURI(), fileNameChild);
                    processSchema(includedSchema);
                }
            }
        }
        if (schema.getImports() != null && schema.getImports().values() != null) {
            for (List<SchemaImport> schemaImportList : (Collection<List<SchemaImport>>) schema.getImports().values()) {
                for (SchemaImport imp : schemaImportList) {
                    Schema importedSchema = imp.getReferencedSchema();
                    if (importedSchema != null || !processedResources.containsKey(imp.getSchemaLocationURI())) {
                        String fileNameChild = getXsdFileName(imp.getSchemaLocationURI());
                        processedResources.put(imp.getSchemaLocationURI(), fileNameChild);
                        processSchema(importedSchema);
                    }
                }
            }
        }
        if(!schema.getDocumentBaseURI().equals(definition.getDocumentBaseURI())){
            changeLocations(schema);
            writeToFile(schema,fileName);
        }
    }
        
    private String getXsdFileName(String schemaLocationUrl) {
        if(schemaLocationUrl.endsWith(".xsd")){
            String[] parts = schemaLocationUrl.split("/");
            return parts[parts.length-1];
        }
        return "UnknownSchema.xsd";
    }

    private void changeLocations(Schema schema) {
        NodeList nodeList = schema.getElement().getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            String tagName = nodeList.item(i).getLocalName();
            if (IMPORT_TAG.equals(tagName) || INCLUDE_TAG.equals(tagName)) {
                changeImportLocation(nodeList.item(i));
            }
        }
    }

    private void changeImportLocation(Node importNode) {
        NamedNodeMap nodeMap = importNode.getAttributes();
        for (int i = 0; i < nodeMap.getLength(); i++) {
            Node attribute = nodeMap.item(i);
            if (SCHEMA_LOCATION.equals(attribute.getNodeName())) {
                String newLocation = processedResources.get(attribute.getNodeValue());
                if (newLocation != null) {
                    attribute.setNodeValue(newLocation);
                }
            }
        }
    }
    
    private void writeToFile(Schema schema, String fileName) throws IOException {
        try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(outputFolder, fileName)), "UTF-8")) {
            DOM2Writer.serializeAsXML(schema.getElement(), definition.getNamespaces(), writer);
        }
    }
}
