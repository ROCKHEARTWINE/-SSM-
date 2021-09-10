package com.company.proj.test;

import com.company.proj.util.SolrUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:applicationContext-*.xml")
//classpath*:applicationContext-*.xml 在所有依赖包中找名字是applicationContext-开头的xml文件
public class SolrTest {
    @Resource
    private SolrUtil solrUtil;

    @Test
    public void testImport(){
        solrUtil.importData();
    }

    @Test
    public void testDelete(){
        solrUtil.deleteData();
    }
}
