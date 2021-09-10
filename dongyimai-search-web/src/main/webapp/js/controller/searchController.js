app.controller("searchController", function ($scope,$location, searchService) {
    //组装搜索对象 增加价格域
    $scope.searchMap = {"keywords" : "","category" : "","brand" : "",
        "spec" : {},"price": "","currentPage" : 1,"pageSize" : 10,
        "sort": "", "sortField": ""};

    $scope.dataTab = {current : "1"};

    $scope.actions = {
        setCurrent:function (param) {
            $scope.dataTab.current = param;
        }
    }

    //读取列表数据数据绑定到表单中
    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (resp) {
            $scope.resultMap = resp;//response中多了一个categoryList
            $scope.list = resp.rows;
        })
    }

    //添加搜索项 增加价格判断
    $scope.addSearchItem = function (key,value) {
        if (key == "category" || key == "brand" || key == "price"){
            $scope.searchMap[key] = value;
        } else {
            $scope.searchMap.spec[key] = value;
        }
        console.log($scope.searchMap);
        //添加查询
        $scope.search();
    }

    //撤销搜索项 删除价格判断
    $scope.removeSearchItem = function (key) {
        if (key == "category" || key == "brand" || key == "price"){
            $scope.searchMap[key] = "";
        } else {
            delete $scope.searchMap.spec[key];
        }
        console.log($scope.searchMap);
        //添加查询
        $scope.search();
    }

    //构建分页标签 思路：计算两端的页码，在页面遍历出标签
    $scope.buildPageLabel = function () {
        $scope.pageLabel = [];//页码，最多存五个
        var totalPages = $scope.resultMap.totalPages;//设置最大页码
        var firstPage = 1;
        var lastPage = totalPages;
        //默认显示五页，如果查询多于五页则显示部分页面
        if (totalPages > 5){
            if ($scope.searchMap.currentPage < 3){ //前两页 显示页面/2取余
                lastPage = 5;//如果当前页显示前三页，则显示最大页码5
            } else if ($scope.searchMap.currentPage > lastPage-2){  //后两页 显示页面/2取余
                //如果是后三页，则显示最后五页
                firstPage = totalPages-4;
            } else {
                firstPage = $scope.searchMap.currentPage - 2;
                lastPage = $scope.searchMap.currentPage + 2;
            }
        }
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
        //计算上一页，下一页
        $scope.prePageNum = ($scope.searchMap.currentPage > 1)? ($scope.searchMap.currentPage - 1) : 1;
        $scope.postPageNum = ($scope.searchMap.currentPage < $scope.resultMap.totalPages) ? ($scope.searchMap.currentPage +1) : $scope.resultMap.totalPages;
    }

    //调用分页计算
    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (resp) {
            $scope.resultMap = resp;
            $scope.buildPageLabel();
        });
    }

    //提交分页查询
    $scope.queryByPage = function (currentPage) {
        //如果不再页面数字范围则不处理
        if (currentPage < 1 || currentPage > $scope.resultMap.totalPages) {
            return;
        }
        $scope.searchMap.currentPage = parseInt(currentPage);
        $scope.search();
    }

    //排序查询
    $scope.sortSearch = function (sortField,sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }

    //隐藏品牌列表
    $scope.keywordsIsBrand = function () {
        /*alert(1);*/
        //如果搜索关键字中包含品牌名称则返回true，否则返回false
        var brandList = $scope.resultMap.brandList;
        var keywords = $scope.searchMap.keywords;
        for (var i = 0; i < brandList.length; i++) {
            if (keywords.indexOf(brandList[i].text) >= 0){
                return true;
            }
        }
        return false;
    }

    //接受参数并查询 由搜索页跳转而来
    $scope.loadKeywords = function () {
        var keywords = $location.search()["keywords"];
        if (keywords != null){
            $scope.searchMap.keywords = keywords;
            $scope.search();
        }
    }
})