package com.pyg.shop.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbSeller;
import com.pyg.sellergoods.service.SellerService;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {


    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        TbSeller seller = sellerService.findOne(username);
        if (seller!=null&&"1".equals(seller.getStatus())){//必须由当前用户，而且状态必须为审核通过

            List<GrantedAuthority> authorities=new ArrayList<>();
            GrantedAuthority authority =new SimpleGrantedAuthority("ROLE_SELLER");
            authorities.add(authority);
            return new User(username,seller.getPassword(),authorities);
        }else {//认证管理器比对肯定失败，登陆失败
            return null;
        }

    }
}
