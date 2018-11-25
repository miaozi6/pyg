package com.pyg.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pyg.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import utils.HttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author huyy
 * @Title: WeixinPayServiceImpl
 * @ProjectName pyg_parent
 * @Description: 微信支付的服务实现
 * @date 2018/11/2010:43
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    //公众账号id
    @Value("${appid}")
    private String appid;

    //商户号
    @Value("${partner}")
    private String partner;

    //秘钥
    @Value("${partnerkey}")
    private String partnerkey;

    /**
     * 获取预支付url的接口方法
     *
     * @param out_trade_no : 订单号
     * @param total_fee    : 总金额
     * @return map: url
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {

        try {
            //1. 准备参数
            Map param = new HashMap();
            param.put("appid",appid);
            param.put("mch_id",partner);
            param.put("nonce_str",WXPayUtil.generateNonceStr());
            param.put("body","品优购双十二打折商品");
            param.put("out_trade_no",out_trade_no);
            param.put("total_fee",total_fee);
            param.put("spbill_create_ip","192.168.127.129");
            param.put("notify_url","https://www.jd.com");
            param.put("trade_type","NATIVE");

//            String xmlParam = WXPayUtil.mapToXml(param);
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求参数:" + xmlParam);

            //2. 发送请求
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //3. 获取响应结果
            String content = httpClient.getContent();
            System.out.println("响应的结果:" + content);
            Map<String, String> result = WXPayUtil.xmlToMap(content);

            //4. 封装返回数据
            Map returnMap = new HashMap();
            returnMap.put("out_trade_no",out_trade_no);
            returnMap.put("total_fee",total_fee);
            returnMap.put("code_url",result.get("code_url"));
            return returnMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据订单号,查询订单的支付状态
     *
     * @param out_trade_no : 订单号
     * @return
     */
    @Override
    public Map<String, String> queryOrderStatus(String out_trade_no) {
        try {
            //1.准备请求参数
            Map param = new HashMap();
            //公众账号
            param.put("appid",appid);
            //商户号
            param.put("mch_id",partner);
            //随机字符串
            param.put("nonce_str",WXPayUtil.generateNonceStr());
            //商户订单号
            param.put("out_trade_no",out_trade_no);

            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求参数:" + xmlParam);


            //2. 发送请求
            //2. 发送请求
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            HttpClient httpClient = new HttpClient(url);
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();

            //3.获取响应结果
            String content = httpClient.getContent();
            System.out.println("响应的结果:" + content);
            Map<String, String> result = WXPayUtil.xmlToMap(content);

            //4. 返回结果
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
