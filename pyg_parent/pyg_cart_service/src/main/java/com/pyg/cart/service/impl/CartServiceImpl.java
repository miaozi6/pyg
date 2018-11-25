package com.pyg.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.cart.service.CartService;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbOrder;
import com.pyg.pojo.TbOrderItem;
import com.pyg.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品SKU ID查询SKU商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item==null){
            System.out.println("商品不存在");
        }if (!"1".equals(item.getStatus())){
            System.out.println("商品状态失效");
        }
        //2.获取商家ID
        String sellerId = item.getSellerId();
        String sellerName = item.getSeller();

        //3.根据商家ID判断购物车列表中是否存在该商家的购物车

       Cart cart= searchCartBySellerId(cartList,sellerId);

        //4.如果购物车列表中不存在该商家的购物车
        if(cart==null){
            //4.1 新建购物车对象
            cart=new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            List<TbOrderItem> orderItemList=new ArrayList<>();
           TbOrderItem orderItem= createOrderItem(item,num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
            //4.2 将新建的购物车对象添加到购物车列表
        }else{
            //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
          TbOrderItem orderItem= searchOrderByItemId(cart.getOrderItemList(),itemId);
            if(orderItem==null){
                //5.1. 如果没有，新增购物车明细
                orderItem= createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
            }else{
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));

                //商品数量小于0
                if(orderItem.getNum()<=0){
                    cart.getOrderItemList().remove(orderItem);
                }
//                购物明细为0
                if(cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }



        }




        return cartList;
    }


    //    根据item对象创建orderItem对象
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setNum(num);
        orderItem.setItemId(item.getId());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPicPath(item.getImage());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setPrice(item.getPrice());
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
        return orderItem;
    }

    // 查询购物车明细列表中是否存在该商品
    private TbOrderItem searchOrderByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if(orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return null;
    }

    //    根据商家id，查询购物车列表，该商家的购物车是否存在
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    @Autowired
    private RedisTemplate redisTemplate;
//    从redis中获取
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        Object o = redisTemplate.boundHashOps("cartList").get(username);
        if(o==null){
            return new ArrayList<>();
        }
        return (List<Cart>) o;
    }

    @Override
    public void saveCartListToRedis(List<Cart> cartList, String userName) {
        redisTemplate.boundHashOps("cartList").put(userName,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList_cookie, List<Cart> cartList_redis) {

        for (Cart cart : cartList_cookie) {
            for(TbOrderItem tbOrderItem : cart.getOrderItemList()){
              cartList_redis=  addGoodsToCartList(cartList_redis,tbOrderItem.getItemId(),tbOrderItem.getNum());
            }
        }
        return cartList_redis;

    }


}
