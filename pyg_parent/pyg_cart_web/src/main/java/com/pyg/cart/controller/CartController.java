package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.cart.service.CartService;
import com.pyg.pojogroup.Cart;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.CookieUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;

    //    request可以直接注入
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;



    //获取购物车列表
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();


        String cookieValue = CookieUtil.getCookieValue(request, "cartList", "utf-8");
//        如果列表为空，下边转换会报错。所以需要这部
        if (StringUtils.isEmpty(cookieValue)) {
            cookieValue = "[]";
        }
//        获取到的cookvalue值是一个字符串，要转变为列表
        List<Cart> cartList_cookie = JSON.parseArray(cookieValue, Cart.class);

        if ("anonymousUser".equals(userName)) {
//            如果没登录
//            获取购物车列表

            System.out.println("从cookie中获取购物车列表");
            return cartList_cookie;
        }else {
//            查询redis购物车列表
            System.out.println("从redis中获取购物车列表");
            List<Cart> cartList_redis= cartService.findCartListFromRedis(userName);

//            这块代码是必经之路，合并购物车
                if(cartList_cookie.size()>0){
//                    1.合并后的两个购物车，将合并后的结果给我返回
                    cartList_redis=cartService.mergeCartList(cartList_cookie,cartList_redis);

//                    2.合并后将redis写回到redis
                cartService.saveCartListToRedis(cartList_redis,userName);

//                3.清空cookie购物车
                    CookieUtil.deleteCookie(request,response,"cartList");
                }


                return cartList_redis;

            }
        }



    //    添加商品到购物车
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins ="http://localhost:9105")
    public Result addGoodsToCartList (Long itemId, Integer num){

        try {
            String userName = SecurityContextHolder.getContext().getAuthentication().getName();

//            代码复用
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if ("anonymousUser".equals(userName)) {
//            如果没登录


                //        1.获取购物车列表


//        2.添加商品到购物车列表


//        3.将添加的购物车列表写到客户端浏览器上面
                String jsonString = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request, response, "cartList", jsonString, 3600 * 24, "utf-8");

            }else{

//                操作redis
                //        1.获取购物车列表

//                       2.添加商品到购物车列表

//                3.将添加后的购物车列表写到redis中
                cartService.saveCartListToRedis(cartList,userName);
            }

            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }


    }


}
