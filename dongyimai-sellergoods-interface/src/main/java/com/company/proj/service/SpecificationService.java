package com.company.proj.service;

import com.company.proj.dto.PageResult;
import com.company.proj.entity.TbSpecification;


import java.util.List;
import java.util.Map;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface SpecificationService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbSpecification> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbSpecification specification);
	
	
	/**
	 * 修改
	 */
	public void update(TbSpecification specification);
	

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	public TbSpecification findOne(Long id);
	
	
	/**
	 * 批量删除
	 * @param ids
	 */
	public void delete(Long [] ids);

	/**
	 * 分页
	 * @param specification
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbSpecification specification, int pageNum,int pageSize);

	public List<Map> selectOptions();
	
}
