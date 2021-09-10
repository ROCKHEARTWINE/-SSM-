package com.company.proj.service;

import com.company.proj.dto.PageResult;
import com.company.proj.entity.TbOrder;


import java.util.List;

/**
 * 服务层接口
 * @author Administrator
 *
 */
public interface OrderService {

	/**
	 * 返回全部列表
	 * @return
	 */
	public List<TbOrder> findAll();
	
	
	/**
	 * 返回分页列表
	 * @return
	 */
	public PageResult findPage(int pageNum,int pageSize);
	
	
	/**
	 * 增加
	*/
	public void add(TbOrder order);
	
	
	/**
	 * 修改
	 */
	public void update(TbOrder order);
	

	/**
	 * 根据ID获取实体
	 * @param orderId
	 * @return
	 */
	public TbOrder findOne(Long orderId);
	
	
	/**
	 * 批量删除
	 * @param orderIds
	 */
	public void delete(Long [] orderIds);

	/**
	 * 分页
	 * @param order
	 * @param pageNum 当前页 码
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(TbOrder order, int pageNum,int pageSize);
	
}
