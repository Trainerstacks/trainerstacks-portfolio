package com.trainerstacks.portfolio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final RuntimeInfo runtime;

    @Value("${trainer.email}")
    private String email;

    @Value("${trainer.phone}")
    private String phone;

    @Value("${trainer.linkedin}")
    private String linkedin;

    @Value("${trainer.github}")
    private String github;

    public HomeController(RuntimeInfo runtime) {
        this.runtime = runtime;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("podName", runtime.podName());
        model.addAttribute("nodeName", runtime.nodeName());
        model.addAttribute("version", runtime.version());
        model.addAttribute("email", email);
        model.addAttribute("phone", phone);
        model.addAttribute("linkedin", linkedin);
        model.addAttribute("github", github);
        return "index";
    }
}
