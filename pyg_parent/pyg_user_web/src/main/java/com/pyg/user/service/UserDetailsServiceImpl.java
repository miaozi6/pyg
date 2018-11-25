package com.pyg.user.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/*
*@Description 自定义授权类
*@Param
*@return
**/
@Component
public class UserDetailsServiceImpl implements UserDetailsService{


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        List<GrantedAuthority> authorities=new ArrayList<>();//权限列表
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_SELLER");//商家的权限独享
        GrantedAuthority authority2 = new SimpleGrantedAuthority("ROLE_ADMIN");//商家的权限独享
        authorities.add(authority);
        authorities.add(authority2);
        return new User(username,"",authorities);
    }
}
