/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danoff.service;

import static org.junit.Assert.*;

import com.danoff.dto.WsdlDto;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import javax.wsdl.WSDLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Denis
 */
public class WsdlResourceDownloaderTest {
    
    private File baseDir;
    private WsdlDto wsdl;
    
    @Before
    public void setUp() throws Exception {
        baseDir = new File("./src/test/resources/wsdl");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        
        wsdl = new WsdlDto();
        wsdl.setUrl("http://ukdcwl12bpmadfdev01.vistajet.local:7005/FlightOperations-PreFlightApprovalModel-context-root/ExternalPfaAppModuleService?wsdl");
    }

    @After
    public void tearDown() throws Exception {
        Files.walk(baseDir.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
    
    @Test
    public void test() throws IOException, WSDLException {
        WsdlResourceDownloader down = new WsdlResourceDownloader(baseDir, wsdl);
        down.download();
    }
}
