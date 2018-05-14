/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danoff.service;

import com.danoff.dto.WsdlDto;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

/**
 *
 * @author Denis
 */
public class WsdlResourceDownloader {

    private final WSDLWriter wsdlWriter;
    private final WSDLReader reader;
    
    private final File baseFolder;
    private final WsdlDto wsdl;

    private final Map<String, String> processedResources = new HashMap<String, String>();

    public WsdlResourceDownloader(File baseFolder, WsdlDto wsdl) throws WSDLException {
        this.baseFolder = baseFolder;
        this.wsdl = wsdl;
        
        this.wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
        this.reader = WSDLFactory.newInstance().newWSDLReader();
        this.reader.setFeature("javax.wsdl.importDocuments", true);
    }

    public void download() {
        try {
            Definition wsdlDefinition = readWsdl(wsdl.getUrl());
            File outputDir = createServiceFolder(wsdlDefinition, wsdl);
            
            downloadResource(wsdlDefinition, outputDir);
        } catch (Exception e) {
            throw new RuntimeException("WSDL writing failed!", e);
        }
    }

    private Definition readWsdl(String url) throws WSDLException {
        Definition wsdlDefinition = reader.readWSDL(url);
        return wsdlDefinition;
    }

    private File createServiceFolder(Definition wsdlDefinition, WsdlDto wsdl) throws IOException {
        String serviceName = wsdl.getServiceName() != null
                    ? wsdl.getServiceName() : extractServiceName(wsdlDefinition);
        
        File serviceFolder = new File(baseFolder, serviceName);
        serviceFolder.mkdirs();
        return serviceFolder;
    }

    private void downloadResource(Definition definition, File outputDir) throws Exception {
        String serviceName = extractServiceName(definition);
        String resourceTypeExtension = extractResourceTypeExtension(definition);
        processedResources.put(definition.getDocumentBaseURI(), serviceName + resourceTypeExtension);

        List<Import> imports = ((Map<String, List<Import>>) definition.getImports())
                                                                        .values().stream()
                                                                        .flatMap(l -> l.stream())
                                                                        .collect(Collectors.toList());

        XsdResourceDownloader schemaDownloader = new XsdResourceDownloader(outputDir, definition);
        schemaDownloader.download();
        
        for (Import resource : imports) {
            String resourceLocation = resource.getDefinition().getDocumentBaseURI();
            if (!processedResources.containsKey(resourceLocation)) {
                downloadResource(resource.getDefinition(), outputDir);
            }

            resource.setLocationURI((String) processedResources.get(resourceLocation));
        }

        writeToFile(outputDir, serviceName+resourceTypeExtension, wsdlWriter, definition);
    }
    
    private String extractServiceName(Definition wsdlDefinition) {
        if (wsdlDefinition.getServices().isEmpty()) {
            String fileUrl = wsdlDefinition.getDocumentBaseURI();
            if(fileUrl.endsWith("?wsdl") || fileUrl.endsWith("?WSDL")){
                String[] parts = fileUrl.split("/");
                return parts[parts.length-1].replace("?WSDL", "").replace("?wsdl", "");
            }else if(fileUrl.contains(".wsdl")){
                String[] parts = fileUrl.split("/");
                return parts[parts.length-1].replace(".wsdl", "");
            }else{
                return "UnknownService";
            }
        }

        return ((Service) wsdlDefinition.getServices().values().iterator().next()).getQName().getLocalPart();
    }
    
    private String extractResourceTypeExtension(Definition resource) {
        return resource.getDocumentBaseURI().contains("xsd") ? ".xsd" : ".wsdl";
    }

    private void writeToFile(File serviceFolder, String fileName, WSDLWriter wsdlWriter, Definition definition) throws IOException, WSDLException {
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(new File(serviceFolder, fileName)), "UTF-8")) {
            wsdlWriter.writeWSDL(definition, out);
        }
    }
}
