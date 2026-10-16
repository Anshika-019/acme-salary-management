package com.acme.salary.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The Angular app handles routes like /insights in the browser. When someone opens or refreshes
 * such a URL directly, the server must return index.html instead of a 404.
 */
@Controller
public class SpaForwardingController {

    @GetMapping({"/employees", "/insights"})
    public String forwardToApp() {
        return "forward:/index.html";
    }
}
