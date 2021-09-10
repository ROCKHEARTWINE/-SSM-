//广告控制层
app.controller("contentController", function ($scope, contentService) {
    $scope.contentList = {};//广告集合 {1:[{},{}],2:[]};
    $scope.findByCategoryId = function (categoryId) {
        contentService.findByCategoryId(categoryId).success(
            function (resp) {
                $scope.contentList[categoryId] = resp;
            }
        );
    }

    //连接搜索页和首页
    $scope.search = function () {
        location.href = "http://localhost:9006/search.html#?keywords=" + $scope.keywords;
    }
});