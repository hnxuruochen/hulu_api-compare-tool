"use strict";
mainApp.controller("TagsController", function($scope, $http) {
    $scope.userData = userData;
    // Initialize.
    $scope.preparingData = true;
    $scope.tagName = "";
    $scope.editStatus = "";
    $scope.currentTag = null;
    $scope.editBlock = false;
    $scope.removingTag = false;

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
        tag.name = null;
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
        var newTag = $scope.tagName;
        if ($scope.removingTag) {
            newTag = null;
        }
        $http.get("/api/tags/modify", {
                params: {
                    "oldTag": $scope.currentTag.name,
                    "newTag": newTag
                }
            })
            .success(function(data) {
                $scope.editStatus = data.status.message;
                $scope.editBlock = false;
                setTimeout(function() {
                    jq(".modal input").focus();
                }, 1);
                if (data.status.success) {
                    var p = $scope.tags.indexOf($scope.currentTag);
                    if ($scope.removingTag) {
                        $scope.tags.splice(p, 1);
                    } else {
                        $scope.tags[p] = data.tag;
                        $scope.editStatus = "";
                    }
                    jq(".ui.modal")
                        .modal("hide");
                }
            });
    };
    $scope.cancelTag = function() {
        if ($scope.currentTag.name == null) {
            $scope.tags.pop();
        }
        jq(".ui.modal")
            .modal("hide");
    };
});
$(document).ready(function($) {});
