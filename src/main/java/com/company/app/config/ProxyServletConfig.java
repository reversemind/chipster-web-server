package com.company.app.config;

import fi.csc.chipster.proxy.ConnectionManager;
import fi.csc.chipster.proxy.HttpProxyServlet;
import fi.csc.chipster.proxy.ProxyServer;
import fi.csc.chipster.proxy.WebSocketProxyServlet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
@Slf4j
@Configuration
public class ProxyServletConfig implements EnvironmentAware, ServletContextInitializer, EmbeddedServletContainerCustomizer {


    @Override
    public void setEnvironment(Environment environment) {

    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        try {
            registerServlet(servletContext);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    @Bean
    public ProxyServer proxyServer() throws URISyntaxException {
        ProxyServer proxy = new ProxyServer();
        return proxy;
    }

    private void registerServlet(ServletContext servletContext) throws URISyntaxException {
        log.debug("register a bunch of servlets");

        ProxyServer proxyServer = proxyServer();

        // WS
        ServletRegistration.Dynamic serviceServletWs = servletContext.addServlet("servletWs", new WebSocketProxyServlet(proxyServer.getConnectionManager()));

        serviceServletWs.addMapping("/websockify/*");
        serviceServletWs.setAsyncSupported(true);
        serviceServletWs.setLoadOnStartup(2);

        serviceServletWs.setInitParameter("prefix", "/" + "websockify");
        serviceServletWs.setInitParameter("proxyTo", "ws://ux64:6080/websockify");


        ServletRegistration.Dynamic serviceServletProxy1 = servletContext.addServlet("serviceServletProxy1", new HttpProxyServlet(proxyServer.getConnectionManager()));

        serviceServletProxy1.addMapping("/core/input/*");
        serviceServletProxy1.setAsyncSupported(true);
        serviceServletProxy1.setLoadOnStartup(2);

        serviceServletProxy1.setInitParameter("prefix", "/" + "core/input");
        serviceServletProxy1.setInitParameter("proxyTo", "http://ux64:6080/core/input");



        ServletRegistration.Dynamic serviceServletProxy2 = servletContext.addServlet("serviceServletProxy2", new HttpProxyServlet(proxyServer.getConnectionManager()));

        serviceServletProxy2.addMapping("/core/input/*");
        serviceServletProxy2.setAsyncSupported(true);
        serviceServletProxy2.setLoadOnStartup(2);

        serviceServletProxy2.setInitParameter("prefix", "/" + "core/input");
        serviceServletProxy2.setInitParameter("proxyTo", "http://ux64:6080/core/input");


        ServletRegistration.Dynamic serviceServletProxy3 = servletContext.addServlet("serviceServletProxy3", new HttpProxyServlet(proxyServer.getConnectionManager()));

        serviceServletProxy3.addMapping("/app/*");
        serviceServletProxy3.setAsyncSupported(true);
        serviceServletProxy3.setLoadOnStartup(2);

        serviceServletProxy3.setInitParameter("prefix", "/" + "app");
        serviceServletProxy3.setInitParameter("proxyTo", "http://ux64:6080/app");



        ServletRegistration.Dynamic serviceServletProxy4 = servletContext.addServlet("serviceServletProxy4", new HttpProxyServlet(proxyServer.getConnectionManager()));

        serviceServletProxy4.addMapping("/vendor/*");
        serviceServletProxy4.setAsyncSupported(true);
        serviceServletProxy4.setLoadOnStartup(2);

        serviceServletProxy4.setInitParameter("prefix", "/" + "vendor");
        serviceServletProxy4.setInitParameter("proxyTo", "http://ux64:6080/vendor");


        ServletRegistration.Dynamic serviceServletProxy5 = servletContext.addServlet("serviceServletProxy5", new HttpProxyServlet(proxyServer.getConnectionManager()));

        serviceServletProxy5.addMapping("/core/*");
        serviceServletProxy5.setAsyncSupported(true);
        serviceServletProxy5.setLoadOnStartup(2);

        serviceServletProxy5.setInitParameter("prefix", "/" + "core");
        serviceServletProxy5.setInitParameter("proxyTo", "http://ux64:6080/core");

    }

    @Override
    public void customize(ConfigurableEmbeddedServletContainer container) {

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


//    @Bean
//    public JettyEmbeddedServletContainerFactory jettyEmbeddedServletContainerFactory() {
//        JettyEmbeddedServletContainerFactory jettyContainer =
//                new JettyEmbeddedServletContainerFactory();
//
//        jettyContainer.setPort(8080);
//        jettyContainer.setContextPath("/");
////        jettyContainer.getEmbeddedServletContainer();
//        return jettyContainer;
//    }

}
