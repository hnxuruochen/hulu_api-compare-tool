"use strict";
mainApp.controller("TasksController", function($scope, $rootScope, $http, $location) {
    // Initialize.
    $scope.initializeSearchData = function() {
        $scope.searchData = {}
        $scope.searchData.preparingData = false;
        $scope.searchData.creator = "";
        $scope.searchData.tags = "";
        $scope.searchData.status = "";
        //$rootScope.searchResult = [];
    };
    $scope.initializeSearchData();
    $scope.search = function() {
        $scope.searchData.tags = "[" + $(".ui.dropdown.tags.multiple")
            .dropdown("get value") + "]";
        $scope.searchData.status = "[" + $(".ui.dropdown.status.multiple")
            .dropdown("get value") + "]";
        $scope.searchData.preparingData = true;
        $http.get("/api/tasks/search", {
                params: {
                    creator: $scope.searchData.creator,
                    tags: $scope.searchData.tags,
                    status: $scope.searchData.status
                }
            })
            .success(function(data) {
                $rootScope.searchResult = data.tasks;
            })
            .finally(function() {
                $scope.searchData.preparingData = false;
            });
    };
    $scope.newTask = function() {
        $location.path("/tasks/new");
    };
    $scope.setCreatorAsUser = function() {
        $scope.searchData.creator = $scope.userData.userAccount;
    }
    $scope.clearCreator = function() {
        $scope.searchData.creator = "";
    }
});
