package com.company.app.config;

import fi.csc.chipster.proxy.ConnectionManager;
import fi.csc.chipster.proxy.HttpProxyServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 *
 */
@Configuration
public class ProxyServletConfig implements EnvironmentAware {


    @Override
    public void setEnvironment(Environment environment) {

    }

//    @Bean
//    public ServletRegistrationBean proxyServlet() {
//        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new HttpProxyServlet(new ConnectionManager()), "/go");
//
//        // proxyTo
//        servletRegistrationBean.addInitParameter("proxyTo", "http://localhost:8080/");
//
//        return servletRegistrationBean;
//    }
//
//
//
//    @Bean
//    public ServletRegistrationBean servletRegistrationBean(){
////        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new org.mitre.dsmiley.httpproxy.ProxyServlet(), propertyResolver.getProperty("servlet_url"));
////        servletRegistrationBean.addInitParameter("targetUri", propertyResolver.getProperty("target_url"));
//
//        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(new org.mitre.dsmiley.httpproxy.ProxyServlet(), "/proxy2");
//        servletRegistrationBean.addInitParameter("targetUri", "http://localhost:8080");
////
////        servletRegistrationBean.addInitParameter(ProxyServlet.P_LOG, propertyResolver.getProperty("logging_enabled", "true"));
//        return servletRegistrationBean;
//    }

}
