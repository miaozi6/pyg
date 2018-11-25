package com.pyg.manage.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    //获取登录用户名
    @RequestMapping("loginName")
    public Map loginName(){
        //跟security索要用户名
        Map map=new HashMap();
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName",userName);
        return map;

    }
}
