package com.pyg.cart.service;

import com.pyg.pojogroup.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    public List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId,Integer num);

//    从redis中获取购物车列表
    List<Cart> findCartListFromRedis(String username);

//    将购物车列表存放到redis中
    void saveCartListToRedis(List<Cart> cartList, String userName);

//    合并后的购物车
    List<Cart> mergeCartList(List<Cart> cartList_cookie, List<Cart> cartList_redis);

//
}
