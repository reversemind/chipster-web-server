package com.company.app.controller;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
@RestController
public class GCOntroller {

    @RequestMapping(value = "/go", method = RequestMethod.GET)
    public void greeting(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String url = "http://ux64:6080/vnc.html?host=ux64&port=6080";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet requestClient = new HttpGet(url);

        HttpResponse responseClient = client.execute(requestClient);
        responseClient.getEntity().writeTo(response.getOutputStream());
    }

}
