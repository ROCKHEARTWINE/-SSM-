package com.company.proj.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/index")
public class IndexController {

    @RequestMapping("/showName")
    public Map showName(){
        //从安全框架读取当前登录用户
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("loginName",userName);
        return map;
    }
}
