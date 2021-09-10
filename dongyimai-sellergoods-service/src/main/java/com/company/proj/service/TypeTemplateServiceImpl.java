package com.company.proj.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.company.proj.dto.PageResult;
import com.company.proj.entity.TbSpecificationOption;
import com.company.proj.entity.TbSpecificationOptionExample;
import com.company.proj.entity.TbTypeTemplate;
import com.company.proj.entity.TbTypeTemplateExample;
import com.company.proj.mapper.TbSpecificationOptionMapper;
import com.company.proj.mapper.TbTypeTemplateMapper;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service(timeout = 60000)
public class TypeTemplateServiceImpl implements TypeTemplateService {

	@Autowired
	private TbTypeTemplateMapper typeTemplateMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;

	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbTypeTemplate> findAll() {
		List<TbTypeTemplate> typeTemplateList = typeTemplateMapper.selectByExample(null);
		for (TbTypeTemplate typeTemplate : typeTemplateList) {
			//缓存品牌
			List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
			redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandList);
			//缓存规格
			List<Map> specList = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
			//遍历规格
			for (Map spec : specList) {
				//根据规格id获得规格列表，保存到spec的options属性中
				TbSpecificationOptionExample example = new TbSpecificationOptionExample();
				TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
				criteria.andSpecIdEqualTo(Long.parseLong(spec.get("id").toString()));
				List<TbSpecificationOption> optionList = specificationOptionMapper.selectByExample(example);
				spec.put("options",optionList);
			}
			redisTemplate.boundHashOps("specList").put(typeTemplate.getId(),specList);
		}
		System.out.println("cache brand and spec finish!");
		return typeTemplateList;
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbTypeTemplate> page=   (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbTypeTemplate typeTemplate) {
		typeTemplateMapper.insert(typeTemplate);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbTypeTemplate typeTemplate){
		typeTemplateMapper.updateByPrimaryKey(typeTemplate);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbTypeTemplate findOne(Long id){
		return typeTemplateMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			typeTemplateMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbTypeTemplateExample example=new TbTypeTemplateExample();
		TbTypeTemplateExample.Criteria criteria = example.createCriteria();
		
		if(typeTemplate!=null){			
						if(typeTemplate.getName()!=null && typeTemplate.getName().length()>0){
				criteria.andNameLike("%"+typeTemplate.getName()+"%");
			}			if(typeTemplate.getSpecIds()!=null && typeTemplate.getSpecIds().length()>0){
				criteria.andSpecIdsLike("%"+typeTemplate.getSpecIds()+"%");
			}			if(typeTemplate.getBrandIds()!=null && typeTemplate.getBrandIds().length()>0){
				criteria.andBrandIdsLike("%"+typeTemplate.getBrandIds()+"%");
			}			if(typeTemplate.getCustomAttributeItems()!=null && typeTemplate.getCustomAttributeItems().length()>0){
				criteria.andCustomAttributeItemsLike("%"+typeTemplate.getCustomAttributeItems()+"%");
			}	
		}
		
		Page<TbTypeTemplate> page= (Page<TbTypeTemplate>)typeTemplateMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}


	/**
	 * 查询分类集合
	 * @return
	 */
	@Override
	public List<Map> selectOptionList() {
		return typeTemplateMapper.selectOptionList();
	}

	/**
	 * 查询规格数据
	 * @param id
	 * @return
	 */
	@Override
	public List<Map> findSpecList(Long id) {
		//根据模板id获取完整模板的对象
		TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey(id);
		//获取规格id集合，并转换成数组[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
		List<Map> list = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
		//循环规格id
		for (Map map : list) {
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(Long.parseLong(map.get("id")+""));
			List<TbSpecificationOption> options = specificationOptionMapper.selectByExample(example);
			//将规格选项和规格存入同一个map
			map.put("options",options);
		}

		//返回规格集合
		return list;
	}
}
