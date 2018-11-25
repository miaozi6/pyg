package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.order.service.OrderService;
import com.pyg.pay.service.WeixinPayService;
import com.pyg.pojo.TbPayLog;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.IdWorker;

import java.util.Map;

/**
 * @author huyy
 * @Title: PayController
 * @ProjectName pyg_parent
 * @Description: 支付的controller
 * @date 2018/11/2011:02
 */
@RestController
@RequestMapping("/pay")
public class PayController {

   @Autowired
   private IdWorker idWorker;

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private OrderService orderService;


    @RequestMapping("/createNative")
    public Map createNative(){
        //1. 获取订单号和总金额
       /* String out_trade_no = idWorker.nextId() +"";
        String total_fee = "1";*/
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        //从redis中获取父订单的总金额和订单号,调用服务层
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        String out_trade_no = payLog.getOutTradeNo();
        String total_fee = payLog.getTotalFee() + "";

        //2. 调用支付服务,获取预支付的url
        return weixinPayService.createNative(out_trade_no,total_fee);
    }


    @RequestMapping("/queryOrderStatus")
    public Result queryOrderStatus(String out_trade_no){
        Result result = null;
        //计数器
        int count = 0;

        while(true){
            Map<String,String> resultMap = weixinPayService.queryOrderStatus(out_trade_no);
            if(resultMap==null){//出错
                result=new  Result(false, "支付出错");
                break;
            }

            if("SUCCESS".equals(resultMap.get("trade_state"))){
                result=new  Result(true, "支付成功");

                //支付成功后,修改订单的支付状态为已支付,记录微信交易流水, 支付时间,删除redis中父订单
                orderService.updateOrderStatus(out_trade_no,resultMap.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
            if(count >= 10){
                result=new  Result(false, "TIME_OUT");
                break;
            }
        }

        return result;
    }
}
