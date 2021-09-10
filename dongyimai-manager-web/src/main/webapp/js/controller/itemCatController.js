 //商品类目控制层 
app.controller('itemCatController' ,function($scope,$controller,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			//修改这里
			if ($scope.entity_2 != null){//entity不为空代表是第三级
				$scope.entity.parentId = $scope.entity_2.id;
			} else if ($scope.entity_1 !=null){////entity_1不为空代表他是第二级
				$scope.entity.parentId = $scope.entity_1.id;
			} else { //代表他是第一级
				$scope.entity.parentId = 0;
			}
			serviceObject=itemCatService.add( $scope.entity);//增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.findByParentId($scope.entity.parentId);//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					if ($scope.entity_2 != null) {//第三级
						$scope.findByParentId( $scope.entity_2.id);
					} else if ($scope.entity_1 != null) {//第二级
						$scope.findByParentId( $scope.entity_1.id);
					} else {////第一级
						$scope.findByParentId(0);
					}
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//根据父id 查询列表
	$scope.findByParentId = function (parentId) {
		itemCatService.findByParentId(parentId).success(function (resp) {
			$scope.list = resp;
		})
	}

	//面包屑导航
	$scope.grade = 1;//定义分类层级，商品分类一共三级，默认第一级
	$scope.setGrade = function (g) {
		$scope.grade = Number(g);
	}
	//层级目录显示
	//TODO 根据层级判断，如果是第一级，则无需面包屑;如果是第二级，则需要显示第一级的面包屑;如果是第三级，则前两级都要显示
	$scope.selectList = function (parentEntity) {
		if ($scope.grade == 1){
			$scope.entity_1 = null;
			$scope.entity_2 = null;
		} else if ($scope.grade == 2){
			$scope.entity_1 = parentEntity;
			$scope.entity_2 = null;
		} else {
			//TODO *因为页面没有刷新，所以entity_1无需再次设置
			$scope.entity_2 = parentEntity;
		}
		$scope.findByParentId(parentEntity.id);
	}

	//查询列表
	$scope.typeTemplateList={data:[]};//模板列表
	//读取模板列表
	$scope.findtypeTemplateList = function () {
		typeTemplateService.selectOptionList().success(function (resp) {
			$scope.typeTemplateList={data:resp};
		})
	}
});	