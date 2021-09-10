package com.company.proj.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.company.proj.dto.PageResult;
import com.company.proj.entity.TbSpecification;
import com.company.proj.entity.TbSpecificationExample;
import com.company.proj.entity.TbSpecificationOption;
import com.company.proj.entity.TbSpecificationOptionExample;
import com.company.proj.mapper.TbSpecificationMapper;

import com.company.proj.mapper.TbSpecificationOptionMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

	@Autowired
	private TbSpecificationMapper specificationMapper;

	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=   (Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSpecification specification) {
		//添加主表数据
		//TODO 通过在主表mapper中添加useGeneratedKeys="true" keyProperty="id" ，返回主键到specification.id
		specificationMapper.insert(specification);
		Long id = specification.getId();
		//循环添加从表数据
		for (TbSpecificationOption option : specification.getSpecOptionList()) {
			option.setSpecId(id);//设置外键
			specificationOptionMapper.insert(option);
		}
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbSpecification specification){
		//删除主表id关联的从表数据
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(specification.getId());
		specificationOptionMapper.deleteByExample(example);

		//更新主表数据
		specificationMapper.updateByPrimaryKey(specification);
		//更新从表，添加新的关联外键
		Long id = specification.getId();
		//循环添加从表数据
		for (TbSpecificationOption option : specification.getSpecOptionList()) {
			option.setSpecId(id);//外键id
			specificationOptionMapper.insert(option);
		}

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSpecification findOne(Long id){
		//由id获得规格
		TbSpecification specification = specificationMapper.selectByPrimaryKey(id);
		//查规格选项
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andSpecIdEqualTo(specification.getId());
		List<TbSpecificationOption> optionList = specificationOptionMapper.selectByExample(example);
		specification.setSpecOptionList(optionList);
		return specification;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		//循环主表id
		for(Long id:ids){
			//删除从表数据
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
			criteria.andSpecIdEqualTo(id);
			specificationOptionMapper.deleteByExample(example);
			//删除主表数据
			specificationMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		TbSpecificationExample.Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<Map> selectOptions() {
		return specificationMapper.selectOptions();
	}
}
