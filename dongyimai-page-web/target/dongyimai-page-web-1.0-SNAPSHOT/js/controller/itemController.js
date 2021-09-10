app.controller("itemController", function ($scope) {
    $scope.addNum = function (x) {
        $scope.num = $scope.num + parseInt(x);
        if ($scope.num < 1) $scope.num = 1;
    }

    $scope.specificationItems = {};//{"网络":"移动4G","机身内存":"32G"}
//用户选择规格
    $scope.selectSpecification = function (name, value) {
        $scope.specificationItems[name] = value;
    }
//判断规格选项是否被选中
    $scope.isSelected = function (name, value) {
        return ($scope.specificationItems[name] == value) ? "selected" : "";
    }

    $scope.loadSKU = function () {
        $scope.sku = skuList[0];
        $scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));//深层拷贝
    }

    //对象比较，如果map1的每一个值都和map2相同，并且map2的每一个值都和map1相同，则两个对象相同
    $scope.matchObject = function (map1, map2) {
        //假设
        //map1:{key1:val1,key2:val2,key3:val3}
        //map2:{key1:val1,key4:val4,key3:val3}
        for (var k in map1) {
            if (map1[k] != map2[k]) {
                return false;
            }
        }
        for (var k in map2) {
            if (map1[k] != map2[k]) {
                return false;
            }
        }
        return true;
    }

    //在SKU列表中查询当前用户选择的SKU
    $scope.searchSKU = function () {
        for (var i = 0; i < skuList.length; i++) {
            if ($scope.matchObject(skuList[i].spec, $scope.specificationItems)) {
                $scope.sku = skuList[i];
            }
        }
    }

    //在用户选择规格后触发读取方法
    //用户选择规格
    $scope.selectSpecification = function (name, value) {
        $scope.specificationItems[name] = value;
        $scope.searchSKU();
    }

    //添加购物车
    $scope.addCart = function () {
        alert("你购买的是：" + JSON.stringify($scope.sku) + ",数量是：" + $scope.num)
    }
})