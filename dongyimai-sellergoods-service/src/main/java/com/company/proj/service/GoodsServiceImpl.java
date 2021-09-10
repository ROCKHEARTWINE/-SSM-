package com.company.proj.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.company.proj.dto.PageResult;
import com.company.proj.entity.*;
import com.company.proj.mapper.*;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    /*@Autowired
    private SolrTemplate solrTemplate;*/

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加 商品及商品详情
     */
    @Override
    public void add(TbGoods goods) {
        goods.setAuditStatus("0");
        //保存tb_goods
        goodsMapper.insert(goods);
        //读取主键id，写入从表 //保存tb_goods_desc
        TbGoodsDesc goodsDesc = goods.getGoodsDesc();
        goodsDesc.setGoodsId(goods.getId());
        goodsDescMapper.insert(goodsDesc);
        saveItemList(goods);//插入商品SKU列表数据
    }

    private void saveItemList(TbGoods goods){
        //缺SKU保存
        //保存tb_item
        if ("1".equals(goods.getIsEnableSpec())) {
            for (TbItem item : goods.getItemList()) {
                //标题
                String title = goods.getGoodsName();
                Map<String, Object> specMap = JSON.parseObject(item.getSpec());
                for (String key : specMap.keySet()) {
                    title += " " + specMap.get(key);
                }
                item.setTitle(title);

                setItemValus(goods, item);
                itemMapper.insert(item);
            }
        } else {
            TbItem item = new TbItem();
            item.setTitle(goods.getGoodsName());//商品KPU+规格描述串作为SKU名称
            item.setPrice(goods.getPrice());//价格
            item.setStatus("1");//状态
            item.setIsDefault("1");//是否默认
            item.setNum(99999);//库存数量
            item.setSpec("{}");
            setItemValus(goods, item);
            itemMapper.insert(item);
        }
    }


    private void setItemValus(TbGoods goods, TbItem item) {
        item.setGoodsId(goods.getId());//商品SKU编号
        item.setSellerId(goods.getSellerId());//商家编号
        item.setCategoryid(goods.getCategory3Id());//商品分类编号（3级）
        item.setCreateTime(new Date());//创建日期
        item.setUpdateTime(new Date());//修改日期

        //品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getBrandId());
        item.setBrand(brand.getName());
        //分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id());
        item.setCategory(itemCat.getName());

        //商家名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getSellerId());
        item.setSeller(seller.getNickName());

        //图片地址（取spu的第一个图片） 没图片报错
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList.size() > 0) {
            item.setImage((String) imageList.get(0).get("url"));
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(TbGoods goods) {
        goods.setAuditStatus("0");//设置未申请状态；经过修改的需要重新设置
        goodsMapper.updateByPrimaryKey(goods);//保存商品表
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());//保存商品扩展列表
        //删除原有SKU列表数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getId());
        itemMapper.deleteByExample(example);
        //添加新的SKU列表数据
        saveItemList(goods);//插入商品SKU列表数据
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbGoods findOne(Long id) {
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        tbGoods.setGoodsDesc(goodsDescMapper.selectByPrimaryKey(id));
        //查询sku商品列表
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);//查询条件：商品ID
        List<TbItem> itemList = itemMapper.selectByExample(example);
        tbGoods.setItemList(itemList);
        return tbGoods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            goodsMapper.deleteByPrimaryKey(id);
        }
    }


    /**
     * 分页条件查询
     * @param goods
     * @param pageNum 当前页 码
     * @param pageSize 每页记录数
     * @return
     */
    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        TbGoodsExample.Criteria criteria = example.createCriteria();
        if (goods != null){
            if (goods.getSellerId() !=null && goods.getSellerId().length()>0){
                //模糊匹配改为等值比较
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 服务层实现类(上下架)
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            //根据商品ID获取商品信息
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            //修改商品状态
            goods.setAuditStatus(status);
            //更新商品数据到数据库
            goodsMapper.updateByPrimaryKey(goods);

            /*if ("1".equals(status)){
                //审核通过
                TbItemExample example = new TbItemExample();
                TbItemExample.Criteria criteria = example.createCriteria();
                criteria.andGoodsIdEqualTo(id);
                //更新solr动态域
                List<TbItem> itemList = itemMapper.selectByExample(example);
                for (TbItem item : itemList) {
                    Map map = JSON.parseObject(item.getSpec(), Map.class);
                    item.setSpecMap(map);
                }
                solrTemplate.saveBeans(itemList);
                solrTemplate.commit();
            } else {
                Query query = new SimpleQuery("*:*");
                Criteria criteria = new Criteria("item_goodsid").is(id);
                query.addCriteria(criteria);
                solrTemplate.delete(query);
                solrTemplate.commit();
            }*/
        }
    }
}
