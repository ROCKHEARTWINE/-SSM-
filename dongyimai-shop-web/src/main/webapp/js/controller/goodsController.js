 //控制层 
app.controller('goodsController' ,function($scope,$controller, $location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承

    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}

	$scope.searchEntity={};//定义搜索对象
	
	//分页
	$scope.findPage=function(page,rows){
		//注意这里调用的是 goodsService.search
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		//获取url参数
		var id = $location.search()["id"];//goods_edit.html#?id={{entity.id}}
		if (id == null) {//如果是新增则id不存在，直接结束
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//向富文本编辑器添加商品介绍
				editor.html($scope.entity.goodsDesc.introduction);
				//显示图片列表
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
				//显示扩展属性
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//规格
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
				//SKU 商品详情
				for (var i = 0; i < $scope.entity.itemList.length; i++) {
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);

				}
			}
		);				
	}

	$scope.entity = {goodsDesc: {introduction: ""}};
	//保存 
	$scope.save=function(){
		//从富文本编辑器取值，并赋值给实体类属性
		$scope.entity.goodsDesc.introduction = editor.html();

		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				//成功失败都打印提示信息
				alert(response.message);
				if(response.success){
					//$scope.reloadList();//重新加载
					location.href = "goods.html";
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	/*$scope.searchEntity={};//定义搜索对象*/
	
	//搜索
	$scope.search = function(page,rows){
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}



	//读取一级分类
	$scope.selectItemCat1List = function () {
		itemCatService.findByParentId(0).success(function (resp) {
			$scope.itemCat1List = resp;
		});
	}

	//读取二级分类
	$scope.$watch('entity.category1Id',function (newValue,oldValue) {
		if (newValue){
			itemCatService.findByParentId(newValue).success(function (resp) {
				$scope.itemCat2List = resp;
			});
		}
	})

	//读取三级分类
	$scope.$watch('entity.category2Id',function (newValue,oldValue) {
		if (newValue){
			itemCatService.findByParentId(newValue).success(function (resp) {
				$scope.itemCat3List = resp;
			});
		}
	})

	//读取模版
	$scope.$watch('entity.category3Id',function (newValue,oldValue) {
		//判断三级分类被选中，再去获得更新模板id
		if (newValue){
			itemCatService.findOne(newValue).success(function (resp) {
				//更新模板ID
				$scope.entity.typeTemplateId = resp.typeId;
			})
		}
	})

	//模板ID选择后  更新品牌列表
	$scope.$watch('entity.typeTemplateId',function (newValue,oldValue) {
		if (newValue){
			typeTemplateService.findOne(newValue).success(function (resp) {
				$scope.typeTemplate = resp;//获取类型模板
				//品牌列表
				$scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
				//判断id是否存在解决代码冲突 数据清空问题
				if ($location.search()['id']==null){
					//扩展列表
					$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
				}
			});

			//新增内容
			typeTemplateService.findSpecList(newValue).success(function (resp) {
				$scope.specList = resp;
			});
		}
	})

	//定义页面实体结构
	$scope.entity = {goodsDesc: {itemImages: [], specificationItems: []}};
	//选择规格选项
	$scope.updateSpecAttribute = function ($event, specName, optionName) {
		var specObject = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, "attributeName", specName);
		if (specObject == null) {
			//{
			//	"attributeName": "网络",
			//	"attributeValue": ["移动4G"]
			//}
			$scope.entity.goodsDesc.specificationItems.push({
				"attributeName": specName,
				"attributeValue": [optionName]
			})
		} else {
			//如果已经存在，则包含两种情况，正选，反选
			if ($event.target.checked) {
				specObject.attributeValue.push(optionName);
			} else {
				//移除规格选项
				var idx = specObject.attributeValue.indexOf(optionName);
				specObject.attributeValue.splice(idx, 1);
				//如果规格选项全部移除，则移除规格
				if (specObject.attributeValue.length == 0) {
					var idx2 = $scope.entity.goodsDesc.specificationItems.indexOf(specObject);
					$scope.entity.goodsDesc.specificationItems.splice(idx2, 1);
				}
			}
		}
	}

	$scope.uploadFile = function () {
		uploadService.uploadFile().success(function (response) {
			if (response.success) {//如果上传成功，取出url
				$scope.image_entity.url = response.message;//设置文件地址
			} else {
				alert(response.message);
			}
		}).error(function () {
			alert("上传发生错误");
		});
	};

	$scope.entity = {goodsDesc: {itemImages: []}};//定义页面实体结构
	//添加图片列表
	$scope.add_image_entity = function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
	//从列表删除
	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}

	//{{entity.goodsDesc.specificationItems}}
	//创建SKU列表
	$scope.createItemList = function () {
		$scope.entity.itemList = [{"spec": {}, "price": 0, "num": 0, "status": "0", "isDefault": "0"}]
		var specList = $scope.entity.goodsDesc.specificationItems;
		for (var i = 0; i < specList.length; i++) {
			$scope.entity.itemList = $scope.addColumn($scope.entity.itemList, specList[i].attributeName, specList[i].attributeValue);
		}
	}

	$scope.addColumn = function (itemList, attributeName, attributeValue) {
		var newList = [];
		for (var i = 0; i < itemList.length; i++) {
			var oldItem = itemList[i];
			for (var j = 0; j < attributeValue.length; j++) {
				//深层拷贝:把json对象转换成json字符串，再转换回json对象，目的是创造新的内存空间 值不变
				var newItem = JSON.parse(JSON.stringify(oldItem));//obj->str->obj
				newItem.spec[attributeName] = attributeValue[j];
				newList.push(newItem);
			}
		}
		return newList;
	}

	//规格初始化
	$scope.entity = {isEnableSpec: 0, goodsDesc: {itemImages: [], customAttributeItems: [], specificationItems: []}};
	//规格清理数据
	$scope.$watch("entity.isEnableSpec", function (newValue, oldVaue) {
		if (newValue == 0) {
			$scope.entity.goodsDesc.specificationItems = [];
			$scope.entity.itemList = [];
		}
	})

	//商品状态
	$scope.status = ['未审核','已审核'];

	//商品分类
	$scope.itemCatList={};
	//加载商品分类列表
	$scope.findItemCatList = function () {
		itemCatService.findAll().success(function (resp) {
			for (var i = 0; i < resp.length; i++) {
				$scope.itemCatList[resp[i].id] = resp[i].name;

			}
		});
	}

	//根据规格名称和选项名称返回是否被勾选
	$scope.checkAttributeValue = function (specName,optionName) {
		var items = $scope.entity.goodsDesc.specificationItems;
		var object = $scope.searchObjectByKey(items,'attributeName',specName);
		if (object ==null){
			return false;
		} else {
			return (object.attributeValue.indexOf(optionName) >= 0);
		}
	}


});	