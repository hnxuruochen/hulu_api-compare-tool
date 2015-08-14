"use strict";
mainApp.controller("TasksIdController", function($scope, $routeParams, $http) {
	$scope.restartingTask = false;
    $scope.loadTask = function() {
    	$scope.loadingData = true;    	
        $http.get("/api/tasks/" + $routeParams.id)
            .success(function(data) {
                $scope.task = data.task;
                $scope.loadingData = false;
            });
    };
    $scope.loadTask();
    $scope.restartTask = function() {
        $scope.restartingTask = true;
        $http.get("/api/tasks/restart", {
                params: {
                    id: $scope.task.id
                }
            })
            .success(function() {
                $scope.loadTask();
            }).finally(function() {
                $scope.restartingTask = false;
            });
    };
});
