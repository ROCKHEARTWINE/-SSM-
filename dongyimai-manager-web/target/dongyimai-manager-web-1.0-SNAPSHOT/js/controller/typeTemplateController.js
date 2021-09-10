//控制层
app.controller('typeTemplateController', function ($scope, $controller, typeTemplateService,specificationService,brandService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        typeTemplateService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        typeTemplateService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体
    $scope.findOne = function (id) {
        typeTemplateService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                $scope.entity.brandIds = JSON.parse($scope.entity.brandIds);
                $scope.entity.specIds = JSON.parse($scope.entity.specIds);
                $scope.entity.customAttributeItems = JSON.parse($scope.entity.customAttributeItems);
            }
        );
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = typeTemplateService.update($scope.entity); //修改
        } else {
            serviceObject = typeTemplateService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        typeTemplateService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        typeTemplateService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //品牌列表
    $scope.brandList = {data:[]};
    $scope.selectBrandList = function () {
        brandService.selectOptions().success(function (resp) {
            $scope.brandList = {data: resp};
        })
    }

    //规格列表
    $scope.specList = {data:[]};
    $scope.selectSpecList = function () {
        specificationService.selectOptions().success(function (resp) {
            $scope.specList = {data: resp};
        })
    }

    //自定义属性
    $scope.addTabRow = function () {
        if ($scope.entity.customAttributeItems == undefined){
            $scope.entity.customAttributeItems = [];
        }
        //追加空置对象
        $scope.entity.customAttributeItems.push({});
    }
    $scope.delTabRow = function (index) {
        $scope.entity.customAttributeItems.splice(index,1);
    }

    //优化显示
    $scope.jsonToString = function (jsonStr,key) {
        var array = JSON.parse(jsonStr);
        var str = "";
        for (var i = 0; i < array.length; i++) {
            str += array[i][key] + " ";

        }
        return str;
    }
});	