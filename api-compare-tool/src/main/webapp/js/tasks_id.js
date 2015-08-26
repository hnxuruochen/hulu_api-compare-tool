"use strict";
mainApp.controller("TasksIdController", function($scope, $routeParams, $http, $location) {
    // Initialize task view with task object.
    $scope.errorsLimitDropdown = $(".ui.dropdown.errors-limit");
    $scope.tagsDropdown = $(".ui.dropdown.tags.tasks-id");
    $scope.initializeTask = function() {
        if ($scope.task.type == 1) {
            $scope.task.address1 = $scope.task.param1;
            $scope.task.address2 = $scope.task.param2;
            $("#type-selector").checkbox("set checked");
        } else {
            $scope.task.text1 = $scope.task.param1;
            $scope.task.text2 = $scope.task.param2;
        }
        $scope.errorsLimitDropdown
            .dropdown("set selected", $scope.task.errorsLimit);
        // When tags haven't been loaded.
        $scope.setRootTagConfig(".ui.dropdown.tags.tasks-id", $scope.task.tagId);
        // When tags already have been loaded.
        setTimeout(function() {
            $scope.tagsDropdown
                .dropdown("set selected", $scope.task.tagId);
        });
    };
    // Load task from backend api.
    $scope.loadingData = false;
    $scope.loadTask = function() {
        $scope.loadingData = true;
        $http.get("/api/tasks/" + $routeParams.id)
            .success(function(data) {
                if (!data.status.success) {
                    $location.path("/404");
                } else {
                    $scope.task = data.task;
                    $scope.loadingData = false;
                    $scope.initializeTask();
                }
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
        $scope.task.useFile = false;
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
        $scope.task.errorsLimit = $scope.errorsLimitDropdown
            .dropdown("get value");
        $scope.task.tagId = $scope.tagsDropdown
            .dropdown("get value");
        if ($scope.task.type == 1) {
            if ($scope.task.address1.endsWith("/")) {
                $scope.task.address1 = $scope.task.address1.substring(0, $scope.task.address1.length - 1);
            }
            if ($scope.task.address2.endsWith("/")) {
                $scope.task.address2 = $scope.task.address2.substring(0, $scope.task.address2.length - 1);
            }
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
            useFile: $scope.task.useFile,
            fileId: $scope.task.fileId == "" ? null : $scope.task.fileId,
            requests: $scope.task.requests,
            isXml: $scope.task.isXml
        };
        $scope.savingTask = true;
        // If new task.
        if ($scope.task.id == undefined) {
            $http.post("/api/tasks/new", params)
                .success(function(data) {
                    if (data.success) {
                        $location.path("/tasks/" + data.message);
                    }
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
                    if (data.success) {
                        $scope.editingTask = false;
                        $scope.loadTask();
                    }
                })
                .error(function() {})
                .finally(function() {
                    $scope.savingTask = false;
                });
        }
    };
    $scope.scriptUsage = "Usage: upload.sh <task id> <file path>";
    $scope.getUploadScript = function() {
        window.location.href = "/upload.sh";
    };
});
