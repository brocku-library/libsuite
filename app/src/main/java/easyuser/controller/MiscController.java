package easyuser.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MiscController {
    /*
     * Global error page. Contains a link to search page.
     */
    @GetMapping("/error")
    public String error() {
        return "error";
    }
}
