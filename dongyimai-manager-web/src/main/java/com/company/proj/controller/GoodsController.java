package com.company.proj.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.company.proj.dto.PageResult;
import com.company.proj.dto.Result;

import com.company.proj.entity.TbGoods;
import com.company.proj.entity.TbItem;
import com.company.proj.service.GoodsService;
/*import com.company.proj.service.ItemPageService;*/
import com.company.proj.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    /*@Reference
    private ItemPageService itemPageService;*/

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("updateSolrQueue")
    private Destination updateSolrQueue;

    @Autowired
    @Qualifier("deleteSolrQueue")
    private Destination deleteSolrQueue;

    @Autowired
    @Qualifier("createPageQueue")
    private Destination createPageQueue;

    @Autowired
    @Qualifier("deletePageQueue")
    private Destination deletePageQueue;

    @Reference
    private ItemService itemService;


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

    /**
     * 增加
     *
     * @param goods
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbGoods goods) {
        try {
            goodsService.add(goods);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TbGoods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public TbGoods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            List<TbItem> itemList = itemService.selectByGoodsId(ids);
            String jsonString = JSON.toJSONString(itemList);
            goodsService.updateStatus(ids, status);
            //查询sku，转换成字符串后，发送给消息服务
            if ("1".equals(status)) {
                //更新索引
                jmsTemplate.send(updateSolrQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        //item列表
                        return session.createTextMessage(jsonString);
                    }
                });
                //--------------------------------------
                /*for (Long id : ids) {
                    itemPageService.createHtml(id);
                }*/
                jmsTemplate.send(createPageQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        //goodsId集合
                        return session.createObjectMessage(ids);
                    }
                });
            }else {
                //TbItem列表 删除索引
                jmsTemplate.send(deleteSolrQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(jsonString);
                    }
                });
                //goodsId
                jmsTemplate.send(deletePageQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage(ids);
                    }
                });
            }
            return new Result(true, "审批成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "审批失败");
        }
    }

    /*@RequestMapping("/createHtml")
    public boolean createHtml(Long goodsId) {
        return itemPageService.createHtml(goodsId);
    }*/

}
