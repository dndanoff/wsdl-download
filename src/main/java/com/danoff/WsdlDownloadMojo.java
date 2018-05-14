package com.danoff;

import com.danoff.dto.WsdlDto;
import com.danoff.service.WsdlResourceDownloader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import javax.wsdl.WSDLException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "wsdl-download", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, requiresProject = true)
public class WsdlDownloadMojo extends AbstractMojo {

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.basedir}/src/main/resources/wsdl", required = false)
    private File outputDirectory;
    @Parameter(required = true)
    private WsdlDto wsdl;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            createOutputFolders();
            WsdlResourceDownloader wsdlResourceDownloader = new WsdlResourceDownloader(outputDirectory, wsdl);
            wsdlResourceDownloader.download();
        } catch (IOException | WSDLException ex) {
            getLog().error(ex);
            throw new MojoFailureException(ex.getMessage(), ex);
        } 
    }

    private void createOutputFolders() throws IOException {
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        Files.walk(outputDirectory.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Override
    public String toString() {
        return "WsdlDownloadMojo{" + "outputDirectory=" + outputDirectory + ", wsdl=" + wsdl + '}';
    }
    
    
}
