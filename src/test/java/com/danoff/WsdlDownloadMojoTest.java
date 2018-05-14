package com.danoff;

import static org.junit.Assert.*;

import org.junit.Test;
import java.io.File;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

public class WsdlDownloadMojoTest extends AbstractMojoTestCase{

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * @throws Exception if any
     */
    @Test
    public void testSomething()
            throws Exception {
        File pom = getTestFile("src/test/resources/project-to-test/pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());

        WsdlDownloadMojo myMojo = (WsdlDownloadMojo) lookupMojo("wsdl-download", pom);
        System.out.println(myMojo);
        assertNotNull(myMojo);
        myMojo.execute();
    }
}
