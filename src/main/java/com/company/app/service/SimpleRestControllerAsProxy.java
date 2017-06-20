package com.company.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
@Slf4j
@Controller
@RequestMapping(value = "/fake/{valueOne}")
public class SimpleRestControllerAsProxy {

    @RequestMapping(value = "/{valueTwo}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public void console(
            @PathVariable String valueOne,
            @PathVariable String valueTwo,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        response.sendRedirect("/extra/proxy");
    }

}
