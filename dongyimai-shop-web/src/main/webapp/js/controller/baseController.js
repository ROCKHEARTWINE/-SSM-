app.controller("baseController", function ($scope) {
    //分页处理
    $scope.paginationConf = {
        currentPage: 1,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30],
        onChange: function () {
            $scope.reloadList();
        }
    };
    //定义要删除的id数组
    $scope.selectIds = [];
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectIds.push(id);
        } else {
            //获取id所在位置
            var idx = $scope.selectIds.indexOf(id);
            //移除改位置上的元素
            $scope.selectIds.splice(idx, 1);
        }
    }
    $scope.reloadList = function () {
        $scope.findPage($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }
    
    //根据属性名和对应的值在数组中查找完整对象
    $scope.searchObjectByKey = function (list,key,val) {
        for (var i = 0; i < list.length; i++) {
            //list[i][key]==val的意义：list[i][attributeName]="网络"
            if (list[i][key] == val){
                return list[i];
            }
        }
        return null;
    }
})