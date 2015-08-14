"use strict";
mainApp.controller("TasksController", function($scope, $rootScope, $http) {
    // Initialize.
    $scope.initializeSearchData = function() {
        $scope.searchData = {}
        $scope.searchData.preparingTags = true;
        $scope.searchData.preparingData = false;
        $scope.searchData.creator = "";
        $scope.searchData.tags = [];
        $scope.searchData.status = [];
        //$rootScope.searchResult = [];
    };
    $scope.initializeCreateData = function() {
        $scope.createData = {}
        $scope.createData.editBlock = false;
        $scope.createData.editStatus = "";
        $(".ui.dropdown.tag-id")
            .dropdown("set selected", "1");
        $(".ui.dropdown.errors-limit")
            .dropdown("set selected", "1");
        $scope.createData.type = false;
        $scope.createData.text1 = "";
        $scope.createData.text2 = "";
        $scope.createData.address1 = "";
        $scope.createData.address2 = "";
        $scope.createData.requests = "";
    };
    $scope.initializeSearchData();
    // Load tags data.
    $http.get("/api/tags")
        .success(function(data) {
            $scope.tags = data;
            $scope.tagsIdMap = {};
            for (var i = 0; i < $scope.tags.length; i++) {
                $scope.tagsIdMap[$scope.tags[i].id] = $scope.tags[i];
            }
            $scope.searchData.preparingTags = false;
        });
    $scope.search = function() {
        $scope.searchData.preparingData = true;
        $http.get("/api/tasks/search", {
                params: {
                    creator: $scope.searchData.creator,
                    tags: JSON.stringify($scope.searchData.tags),
                    status: JSON.stringify($scope.searchData.status)
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
        $scope.initializeCreateData();
        jq(".ui.modal.tasks")
            .modal({
                closable: false
            })
            .modal("show");
    };
    $scope.createTask = function() {
        var p1 = $scope.createData.text1;
        var p2 = $scope.createData.text2;
        $scope.createData.errorsLimit = $(".ui.dropdown.errors-limit")
            .dropdown("get value");
        $scope.createData.tagId = $(".ui.dropdown.tag-id")
            .dropdown("get value");
        if ($scope.createData.type) {
            p1 = $scope.createData.address1;
            p2 = $scope.createData.address2;
        }
        $scope.createData.editBlock = true;
        $http.post("/api/tasks/new", {
                tagId: $scope.createData.tagId,
                errorsLimit: $scope.createData.errorsLimit,
                type: $scope.createData.type ? 1 : 0,
                param1: p1,
                param2: p2,
                requests: $scope.createData.requests
            })
            .success(function(data) {
                $scope.searchResult.unshift(data.task);
                $scope.cancelTask();
            })
            .error(function() {
                $scope.createData.editStatus = "Connection failed."
            })
            .finally(function() {
                $scope.createData.editBlock = false;
            });
    };
    $scope.cancelTask = function() {
        jq(".ui.modal.tasks")
            .modal("hide");
    };
});
