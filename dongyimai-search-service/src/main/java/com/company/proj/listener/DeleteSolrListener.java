package com.company.proj.listener;

import com.alibaba.fastjson.JSON;
import com.company.proj.entity.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

@Component
public class DeleteSolrListener implements MessageListener {
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            List<TbItem> itemList = JSON.parseArray(textMessage.getText(), TbItem.class);
            for (TbItem item : itemList) {
                Query query = new SimpleQuery("*:*");
                Criteria criteria = new Criteria("id").is(item.getId());
                query.addCriteria(criteria);
                solrTemplate.delete(query);
                solrTemplate.commit();
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
