"use strict";
mainApp.controller("TasksIdController", function($scope, $routeParams, $http) {
    $scope.loadingData = true;

    $http.get("/api/tasks/" + $routeParams.id)
        .success(function(data) {
            $scope.task = data.task;
            $scope.loadingData = false;
        });
});
