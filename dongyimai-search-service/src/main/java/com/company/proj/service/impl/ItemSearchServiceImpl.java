package com.company.proj.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.company.proj.ItemSearchService;
import com.company.proj.entity.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//注意这里是com.alibaba.dubbo.config.annotation.Service;
@Service(timeout = 60000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        Map<String, Object> map = new HashMap<String, Object>();
        //设置高亮
        map.putAll(searchList(searchMap));
        //读取分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);
        //查询品牌和规格列表
        String categoryName = (String) searchMap.get("category");
        if (!"".equals(categoryName)){
            map.putAll(searchBrandAndSpecList(categoryName));
        } else if (categoryList.size()>0){
            map.putAll(searchBrandAndSpecList((String) categoryList.get(0)));
        }
        /*Query query = new SimpleQuery("*:*");
        //是否获取到关键词
        System.out.println("keywords:"+searchMap.get("keywords"));
        //创建查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);
        map.put("rows",page.getContent());*/

        return map;
    }

    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        //高亮查询对象
        HighlightQuery query = new SimpleHighlightQuery();
        //设置高亮域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //设置高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        query.setHighlightOptions(highlightOptions);

        //多关键字，清除中间的空格
        String keywords = (String) searchMap.get("keywords");
        keywords = keywords.replace(" ", "");

        //查询对象添加查询条件
        Criteria criteria = new Criteria("item_keywords").is(keywords);
        query.addCriteria(criteria);

        //过滤分类
        if (!"".equals(searchMap.get("category"))){
            Criteria criteriaFilter = new Criteria("item_category").is(searchMap.get("category"));
            SimpleFilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
            query.addFilterQuery(filterQuery);
        }
        //过滤品牌
        if (!"".equals(searchMap.get("brand"))){
            Criteria criteriaFilter = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
            query.addFilterQuery(filterQuery);
        }
        //过滤规格
        if (searchMap.get("spec") != null){
            Map<String,String> specMap = JSON.parseObject(searchMap.get("spec")+"",Map.class) ;
            for (String key : specMap.keySet()) {
                Criteria criteriaFilter = new Criteria("item_spec_" + key).is(specMap.get(key));
                System.out.println("item_spec_"+key+" : "+specMap.get(key));
                FilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
                query.addFilterQuery(filterQuery);
            }
        }
        //过滤价格
        if (!"".equals(searchMap.get("price"))){
            String[] prices = ((String) searchMap.get("price")).split("-");
            //判空
            if (!prices[0].equals("0")){
                Criteria criteriaFilter = new Criteria("item_price").greaterThanEqual(prices[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
                query.addFilterQuery(filterQuery);
            }
            if (!prices[1].equals("*")){
                Criteria criteriaFilter = new Criteria("item_price").lessThanEqual(prices[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(criteriaFilter);
                query.addFilterQuery(filterQuery);
            }
        }

        //TODO:处理分页-----------------------------------------------------
        Integer currentPage = (Integer) searchMap.get("currentPage");
        if (currentPage == null){
            currentPage = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize ==null){
            pageSize = 10;
        }
        query.setOffset((currentPage-1)*pageSize);//设置偏移量
        query.setRows(pageSize);//设置页容量
        //TODO-----------------------------------------------------

        //排序
        String sortValue = (String) searchMap.get("sort");//ASC | DESC
        String sortField = (String) searchMap.get("sortField");//排序字段
        if ("ASC".equals(sortValue)){
            Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
            query.addSort(sort);
        } else if ("DESC".equals(sortValue)){
            Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
            query.addSort(sort);
        } else {
            //不做排序处理
        }

        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        for (HighlightEntry<TbItem> highlightEntry : page.getHighlighted()) {
            //循环高亮入口集合
            TbItem entity = highlightEntry.getEntity();
            //高亮数量大于0 并且 片段内容大于0
            //获取高亮列表（高亮字段的个数） h.getHighlights()
            //获取每个片段可能存储多值 getSnipplets()
            //获取要高亮的内容 getSnipplets.get(0);
            if (highlightEntry.getHighlights().size() > 0
                    && highlightEntry.getHighlights().get(0).getSnipplets().size() > 0) {
                entity.setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
            }
        }
        //TODO:返回分页信息-----------------------------------------------------
        map.put("rows", page.getContent());
        map.put("totalPages",page.getTotalPages());//返回总页数
        map.put("total",page.getTotalElements());//返回总记录数
        //TODO-----------------------------------------------------
        return map;
    }

    /**
     * 根据分类查询品牌规格列表
     * @param category
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId!=null){
            //根据模板id查询品牌列表
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList",brandList);
            //根据模板Id查询规格列表
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);
        }
        return map;
    }

    /**
     * 查询分类列表
     */
    private List searchCategoryList(Map search) {
        List list = new ArrayList();
        Query query = new SimpleQuery("*:*");
        //keywords 页面文本域的名称
        //item_keywords solr中带域
        Criteria criteria = new Criteria("item_keywords").is(search.get("keywords"));
        query.addCriteria(criteria);
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            //将分组结果的名称封装到返回值中
            list.add(entry.getGroupValue());
        }
        //设置分组选项
        return list;
    }
}

