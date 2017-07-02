package com.company.app.controller;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.UUID;

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
        requestClient.addHeader("Escape-Value", "__value:0:1:15:7");

        HttpResponse responseClient = client.execute(requestClient);
        responseClient.addHeader("Escape-Value2", "_2_value:0:1:15:7");


        String token = UUID.randomUUID().toString();

        HttpSession session = request.getSession();
        System.out.println("session:" + session);
        System.out.println("session:" + session.getId());
        session.setAttribute("TOKEN", token);

        System.out.println("token:" + token);

//        responseClient.getHeaders()
//        responseClient.setHeader("Cookie", "ttt=VVV");

        System.out.println("headers:" + Arrays.asList(responseClient.getAllHeaders()));
        System.out.println("\n\n");

        //
        Header[] headers = responseClient.getAllHeaders();
        for(Header header: headers){
            response.addHeader(header.getName(), header.getValue());
        }

        responseClient.getEntity().writeTo(response.getOutputStream());
    }

}
