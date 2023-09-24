package com.atguigu.gulimall.gulimallsearch.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {

        @GetMapping({"/", "/index.html"})
        public String index() {
                return "index";
        }

        @GetMapping("/hello")
        @ResponseBody
        public String hello() {
                return "hello";
        }

}
