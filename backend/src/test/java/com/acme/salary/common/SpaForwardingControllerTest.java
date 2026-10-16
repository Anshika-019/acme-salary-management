package com.acme.salary.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

@WebMvcTest(SpaForwardingController.class)
class SpaForwardingControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void deepLinksServeTheAngularApp() throws Exception {
        mvc.perform(get("/insights")).andExpect(forwardedUrl("/index.html"));
        mvc.perform(get("/employees")).andExpect(forwardedUrl("/index.html"));
    }
}
