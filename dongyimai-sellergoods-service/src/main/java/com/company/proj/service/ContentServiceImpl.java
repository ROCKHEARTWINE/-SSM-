package com.company.proj.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.company.proj.dto.PageResult;
import com.company.proj.entity.TbContent;
import com.company.proj.entity.TbContentExample;
import com.company.proj.mapper.TbContentMapper;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;

	@Resource
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//新增数据变动，清楚缓存影响
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbContent content){
		//从数据库读取旧分类
		Long oldCategoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		contentMapper.updateByPrimaryKey(content);
		//从redis中删除旧分类
		redisTemplate.boundHashOps("content").delete(oldCategoryId);
		//从redis中删除新分类
		Long newCategoryId = content.getCategoryId();
		redisTemplate.boundHashOps("content").delete(newCategoryId);
		//更新数据库
		contentMapper.updateByPrimaryKey(content);

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//读取完整对象
			TbContent tbContent = contentMapper.selectByPrimaryKey(id);
			//从redis中删除缓存
			redisTemplate.boundHashOps("content").delete(tbContent.getCategoryId());
			//删除数据库中数据
			contentMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		TbContentExample.Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {
		List<TbContent> contentList = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
		if (contentList == null){//没有缓存
			//根据广告分类ID查询广告列表
			TbContentExample contentExample = new TbContentExample();
			TbContentExample.Criteria criteria = contentExample.createCriteria();
			criteria.andCategoryIdEqualTo(categoryId);
			criteria.andStatusEqualTo("1");//开启状态
			contentExample.setOrderByClause("sort_order");//排序 根据排序列排序
			contentList = contentMapper.selectByExample(contentExample);
			//存入缓存
			redisTemplate.boundHashOps("content").put(categoryId,contentList);
		} else {
			System.out.println("使用缓存中的数据");
		}
		return contentList;
	}

	@Override
	public void updateStatus(Long[] ids, String status) {
		for (long id : ids) {
			TbContent tbContent = contentMapper.selectByPrimaryKey(id);
			redisTemplate.boundHashOps("content").delete(tbContent.getCategoryId());
			tbContent.setStatus(status);
			contentMapper.updateByPrimaryKey(tbContent);
		}
	}
}
