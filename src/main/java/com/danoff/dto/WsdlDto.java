/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danoff.dto;

import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author Denis
 */
public class WsdlDto {

    @Parameter(required = false)
    private String serviceName;

    @Parameter(required = true)
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "WsdlDto{" + "serviceName=" + serviceName + ", url=" + url + '}';
    }

}
