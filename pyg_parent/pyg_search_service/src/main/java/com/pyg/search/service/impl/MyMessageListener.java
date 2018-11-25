package com.pyg.search.service.impl;


import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class MyMessageListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        try {

            //1.获取消息内容
            TextMessage textMessage= (TextMessage) message;
            String text = textMessage.getText();

            //2.调用业务方法
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
            itemSearchService.importItemDate(itemList);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
