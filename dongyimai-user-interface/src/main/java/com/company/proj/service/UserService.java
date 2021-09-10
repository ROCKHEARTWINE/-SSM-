package com.company.proj.service;

import com.company.proj.entity.TbUser;

/**
 * 用户服务层接口
 */
public interface UserService {
    /**
     * 发送验证码
     *
     * @param phone
     */
    public void createSmsCode(String phone);

    /**
     * 校验验证码
     *
     * @param phone
     * @param code
     * @return
     */
    public boolean checkSmsCode(String phone, String code);



    /**
     * 增加
     */
    public void add(TbUser user);

}
