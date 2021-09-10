package com.company.proj.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.company.proj.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component
public class CreatePageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] ids = (Long[]) objectMessage.getObject();
            for (Long id : ids) {
                itemPageService.createHtml(id);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
