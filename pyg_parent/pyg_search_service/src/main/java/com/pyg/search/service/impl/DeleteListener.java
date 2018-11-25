package com.pyg.search.service.impl;

import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class DeleteListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        try {

            //1.读取ids
            ObjectMessage objectMessage= (ObjectMessage) message;

            Long [] ids = (Long[]) objectMessage.getObject();

            //2.调用服务。删除solr索引库
            itemSearchService.deleteItem(ids);

        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
}
