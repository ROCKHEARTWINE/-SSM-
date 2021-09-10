package com.company.proj.service.impl;

import com.company.proj.entity.TbSeller;
import com.company.proj.service.SellerService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;

public class UserDetailServiceImpl implements UserDetailsService {
    private SellerService sellerService;

    //添加setter手动注入

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //创建权限集合
        ArrayList<SimpleGrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //查询登陆用户信息
        TbSeller seller = sellerService.findOne(username);
        if ("1".equals(seller.getStatus())){
            return new User(username,seller.getPassword(),list);
        }
        return null;
    }
}
