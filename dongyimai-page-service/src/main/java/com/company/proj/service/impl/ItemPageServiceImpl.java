package com.company.proj.service.impl;

import com.company.proj.entity.TbGoods;
import com.company.proj.entity.TbGoodsDesc;
import com.company.proj.entity.TbItem;
import com.company.proj.entity.TbItemExample;
import com.company.proj.mapper.TbGoodsDescMapper;
import com.company.proj.mapper.TbGoodsMapper;
import com.company.proj.mapper.TbItemCatMapper;
import com.company.proj.mapper.TbItemMapper;
import com.company.proj.service.ItemPageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*@Service*/
//TODO 这里删除了dubbo的service，添加了spring的service
@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Value("${freemarker.path}")
    private String path;

    @Resource
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean createHtml(Long goodsId) {
        Configuration configuration = freeMarkerConfig.getConfiguration();
        Template template = null;
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        try {
            template = configuration.getTemplate("item.ftl");
            //读取商品信息
            Map dataModule =  new HashMap<>();
            //1.加载商品表数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            //2.加载商品扩展表数据
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            goods.setGoodsDesc(goodsDesc);

            dataModule.put("goods",goods);

            //3.商品分类
            {
                String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
                String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
                String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
                dataModule.put("itemCat1", itemCat1);
                dataModule.put("itemCat2", itemCat2);
                dataModule.put("itemCat3", itemCat3);
            }

            //4.SKU列表
            {
                TbItemExample tbItemExample = new TbItemExample();
                TbItemExample.Criteria criteria = tbItemExample.createCriteria();
                //criteria.andStatusEqualTo("1");
                criteria.andGoodsIdEqualTo(goodsId);
                tbItemExample.setOrderByClause("is_default desc");
                List<TbItem> tbItems = itemMapper.selectByExample(tbItemExample);
                dataModule.put("itemList", tbItems);
            }

            //使用FileOutputStream 和 OutputStreamWriter解决中文乱码问题
            fileOutputStream = new FileOutputStream(new File(path + goodsId + ".html"));
            outputStreamWriter = new OutputStreamWriter(fileOutputStream,"utf-8");
            template.process(dataModule,outputStreamWriter);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                outputStreamWriter.flush();
                outputStreamWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public boolean deleteHtml(Long goodsId) {
        File file = new File(path + goodsId + ".html");
        return file.delete();
    }
}
