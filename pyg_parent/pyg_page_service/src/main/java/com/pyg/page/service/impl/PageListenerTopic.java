package com.pyg.page.service.impl;

import com.pyg.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class PageListenerTopic implements MessageListener {
   @Autowired
   private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message)  {

        try {
            //生产静态页面的消费者
            ObjectMessage objectMessage= (ObjectMessage) message;
            Long goodsId= (Long) objectMessage.getObject();


            //调用业务方法生成静态页面
            itemPageService.genItemPage(goodsId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
