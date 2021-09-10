var app = angular.module("dongyimai", []);
//angular为了防止页面注入攻击，会屏蔽html
//需要时用$sce的trustHtml方法跳过检查
//编写过滤器
app.filter('to_trusted',['$sce',function ($sce) {
    return function (data) {
        return $sce.trustAsHtml(data);
    }
}])