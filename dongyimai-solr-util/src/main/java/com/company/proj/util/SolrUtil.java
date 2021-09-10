package com.company.proj.util;

import com.alibaba.fastjson.JSON;
import com.company.proj.entity.TbItem;
import com.company.proj.entity.TbItemExample;
import com.company.proj.mapper.TbItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    public void importData(){
        //读取SKU
        TbItemExample itemExample = new TbItemExample();
        TbItemExample.Criteria criteria = itemExample.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(itemExample);
        for (TbItem item : itemList) {
            //TODO 注意清理数据，确保spec不为空{}
            //添加动态域
            Map specMap = JSON.parseObject(item.getSpec());
            item.setSpecMap(specMap);
            System.out.println(item);
        }
        //将sku写入solr,注意这里使用的是saveBeans，带s！！！
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }

    public void deleteData(){
        //从数据库读取SKU
        Query query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
