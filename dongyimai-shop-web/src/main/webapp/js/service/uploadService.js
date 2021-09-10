app.service("uploadService", function ($http) {
    this.uploadFile = function () {
        var formData = new FormData();
        //和controller保持public Result upload(MultipartFile file)
        formData.append("file", file.files[0]);
        return $http({
            method: 'POST',
            url: "../upload_file.do",
            data: formData,
            headers: {'Content-Type': undefined},
            transformRequest: angular.identity
        });
    }
});