package com.company.proj.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.company.proj.dto.Result;
import com.company.proj.entity.TbUser;
import com.company.proj.service.UserService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户表controller
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;

    @RequestMapping("/sendCode")
    public Result sendCode(String phone){
        userService.createSmsCode(phone);
        try {
            return new Result(true,"验证码发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"验证码发送失败");
        }
    }

    @RequestMapping("/add")
    public Result add(@RequestBody TbUser user,String smscode){
        boolean checkSmsCode = userService.checkSmsCode(user.getPhone(), smscode);
        if (!checkSmsCode)
            return new Result(false,"验证码输入错误");
        try {
            userService.add(user);
            return new Result(true,"增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"增加失败");
        }

    }
}
