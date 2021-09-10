//控制层
app.controller('indexController',function ($scope,$controller,indexService) {
    $controller('baseController',{$scope: $scope});//继承
    //搜索
    $scope.showName = function () {
        indexService.showName().success(
            function (resp) {
                $scope.loginName = resp.loginName;

            }
        );
    }

})