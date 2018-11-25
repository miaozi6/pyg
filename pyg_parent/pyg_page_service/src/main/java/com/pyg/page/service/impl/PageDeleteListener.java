package com.pyg.page.service.impl;

import com.pyg.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
@Component
public class PageDeleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {

        try {
            ObjectMessage objectMessage= (ObjectMessage) message;

            Long []ids= (Long[]) objectMessage.getObject();

            for (Long id : ids) {
                itemPageService.deletePageById(id);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
