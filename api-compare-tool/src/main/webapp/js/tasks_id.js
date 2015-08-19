"use strict";
mainApp.controller("TasksIdController", function($scope, $routeParams, $http, $location) {
    // Initialize task view with task object.
    $scope.typeSelector = $("#type-selector");
    $scope.serviceInfo = $("#service-info");
    $scope.textInfo = $("#text-info");
    $scope.toggleTypeView = function() {
        $scope.serviceInfo.toggleClass("hide");
        $scope.textInfo.toggleClass("hide");
    };
    $scope.initializeTask = function() {
        if ($scope.task.type == 1) {
            $scope.task.address1 = $scope.task.param1;
            $scope.task.address2 = $scope.task.param2;
            $scope.textInfo.addClass("hide");
        } else {
            $scope.task.text1 = $scope.task.param1;
            $scope.task.text2 = $scope.task.param2;
            $scope.serviceInfo.addClass("hide");
        }
        $(".ui.dropdown.errors-limit")
            .dropdown("set selected", $scope.task.errorsLimit);
        // When tags haven't been loaded.
        $scope.setRootTagConfig(".ui.dropdown.tags.tasks-id", $scope.task.tagId);
        // When tags already have been loaded.
        setTimeout(function() {
            $(".ui.dropdown.tags")
                .dropdown("set selected", $scope.task.tagId);
        });
    };
    // Load task from backend api.
    $scope.loadingData = false;
    $scope.loadTask = function() {
        $scope.loadingData = true;
        $http.get("/api/tasks/" + $routeParams.id)
            .success(function(data) {
                $scope.task = data.task;
                $scope.loadingData = false;
                $scope.initializeTask();
            });
    };
    // Create a new task object.
    $scope.newTask = function() {
        $scope.task = {};
        $scope.task.tagId = 1;
        $scope.task.param1 = "";
        $scope.task.param2 = "";
        $scope.task.requests = "";
        $scope.task.type = 0;
        $scope.task.errorsLimit = 1;
        $scope.initializeTask();
    };
    // Fetch task object for current page.
    $scope.fetchTask = function() {
        if ($routeParams.id == undefined) {
            $scope.editingTask = true;
            $scope.newTask();
        } else {
            $scope.editingTask = false;
            $scope.loadTask();
        }
    }
    $scope.fetchTask();
    // Restart task.
    $scope.restartTask = function() {
        $scope.savingTask = true;
        $http.get("/api/tasks/restart", {
                params: {
                    id: $scope.task.id
                }
            })
            .success(function() {
                $scope.loadTask();
            }).finally(function() {
                $scope.savingTask = false;
            });
    };
    $scope.editTask = function() {
        $scope.editingTask = true;
    };
    // Save current task.
    $scope.savingTask = false;
    $scope.saveTask = function() {
        // Map view value to task object.
        $scope.task.errorsLimit = $(".ui.dropdown.errors-limit")
            .dropdown("get value");
        $scope.task.tagId = $(".ui.dropdown.tags")
            .dropdown("get value");
        if ($scope.task.type == 1) {
            $scope.task.param1 = $scope.task.address1;
            $scope.task.param2 = $scope.task.address2;
        } else {
            $scope.task.param1 = $scope.task.text1;
            $scope.task.param2 = $scope.task.text2;
        }
        var params = {
            tagId: $scope.task.tagId,
            errorsLimit: $scope.task.errorsLimit,
            type: $scope.task.type,
            param1: $scope.task.param1,
            param2: $scope.task.param2,
            requests: $scope.task.requests
        };
        $scope.savingTask = true;
        // If new task.
        if ($scope.task.id == undefined) {
            $http.post("/api/tasks/new", params)
                .success(function(data) {
                    $location.path("/tasks/" + data.message);
                })
                .error(function() {})
                .finally(function() {
                    $scope.savingTask = false;
                });
        } else {
            // If update task.            
            params.id = $scope.task.id;
            $http.post("/api/tasks/update", params)
                .success(function(data) {
                    $scope.editingTask = false;
                })
                .error(function() {})
                .finally(function() {
                    $scope.savingTask = false;
                });
        }
    };

    $scope.typeSelector
        .checkbox({
            onChange: function() {
                $scope.task.type = 1 - $scope.task.type;
                $scope.toggleTypeView();
            }
        });
});
