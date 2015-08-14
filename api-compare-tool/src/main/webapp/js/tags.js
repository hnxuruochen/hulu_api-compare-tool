"use strict";
mainApp.controller("TagsController", function($scope, $rootScope, $http) {
    // Initialize.
    $scope.preparingData = true;
    $scope.tagName = "";
    $scope.editStatus = "";
    $scope.currentTag = null;
    $scope.editBlock = false;
    $scope.removingTag = false;
    // Load data.
    $http.get("/api/tags")
        .success(function(data) {
            $scope.preparingData = false;
            $scope.tags = data;
        });
    // Open tag editor.
    $scope.openModal = function() {
        jq(".ui.modal")
            .modal({
                closable: false,
                onVisible: function() {
                    setTimeout(function() {
                        jq(".modal input").focus();
                    }, 1);
                }
            })
            .modal("show");
    }
    $scope.addTag = function() {
        $scope.removingTag = false;
        var tag = {}
        tag.id = null;
        $scope.tags.push(tag);
        $scope.tagName = "";
        $scope.currentTag = tag;
        $scope.openModal();
    }
    $scope.editTag = function(t) {
        $scope.removingTag = false;
        $scope.currentTag = t;
        $scope.tagName = t.name;
        $scope.openModal();
    };
    $scope.removeTag = function(t) {
        $scope.removingTag = true;
        $scope.currentTag = t;
        $scope.tagName = t.name;
        $scope.openModal();
    }
    $scope.saveTag = function() {
        $scope.editStatus = "Waiting please.";
        $scope.editBlock = true;
        var name = $scope.tagName;
        if ($scope.removingTag) {
            name = null;
        }
        $http.get("/api/tags/modify", {
                params: {
                    id: $scope.currentTag.id,
                    name: name
                }
            })
            .success(function(data) {
                $scope.editStatus = data.status.message;
                if (data.status.success) {
                    var p = $scope.tags.indexOf($scope.currentTag);
                    if ($scope.removingTag) {
                        $scope.tags.splice(p, 1);
                    } else {
                        $scope.tags[p] = data.tag;
                    }
                    jq(".ui.modal")
                        .modal("hide");
                    $scope.editStatus = "";
                }
            }).error(function() {
                $scope.editStatus = "Connection error.";
            }).finally(function() {
                $scope.editBlock = false;
                setTimeout(function() {
                    jq(".modal input").focus();
                }, 1);

            });
    };
    $scope.cancelTag = function() {
        if ($scope.currentTag.name == null) {
            $scope.tags.pop();
        }
        jq(".ui.modal")
            .modal("hide");
        $scope.editStatus = "";
    };
});
