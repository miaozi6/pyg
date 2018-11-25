package com.pyg.pay.service;

import java.util.Map;

/**
 * @author huyy
 * @Title: WeixinPayService
 * @ProjectName pyg_parent
 * @Description: 微信支付的接口
 * @date 2018/11/2010:39
 */
public interface WeixinPayService {

    /**
     * 获取预支付url的接口方法
     * @param out_trade_no : 订单号
     * @param total_fee : 总金额
     * @return map: url
     */
    public Map createNative(String  out_trade_no,String total_fee);

    /**
     * 根据订单号,查询订单的支付状态
     * @param out_trade_no : 订单号
     * @return
     */
    Map<String,String> queryOrderStatus(String out_trade_no);
}
