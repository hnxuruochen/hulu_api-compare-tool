"use strict";
mainApp.controller("TagsController", function($scope, $http) {
    // Initialize.
    $scope.tagName = "";
    $scope.editStatus = "";
    $scope.currentIndex = null;
    $scope.editBlock = false;
    $scope.removingTag = false;
    $scope.input = $(".modal input");
    // Open tag editor.
    $scope.openModal = function() {
        $(".ui.modal.tags")
            .modal({
                closable: false,
                onVisible: function() {
                    setTimeout(function() {
                        $scope.input.focus();
                    }, 0);
                }
            })
            .modal("show");
    };
    $scope.closeModal = function() {
        $(".ui.modal.tags")
            .modal("hide");
        $scope.editStatus = "";
    };
    $scope.addTag = function() {
        $scope.removingTag = false;
        var tag = {}
        tag.id = null;
        $scope.tags.push(tag);
        $scope.tagName = "";
        $scope.currentIndex = $scope.tags.indexOf(tag);
        $scope.openModal();
    }
    $scope.editTag = function(t) {
        $scope.removingTag = false;
        $scope.currentIndex = $scope.tags.indexOf(t);
        $scope.tagName = t.name;
        $scope.openModal();
    };
    $scope.removeTag = function(t) {
        $scope.removingTag = true;
        $scope.currentIndex = $scope.tags.indexOf(t);
        $scope.tagName = t.name;
        $scope.openModal();
    }
    $scope.saveTag = function() {
        $scope.editStatus = "Waiting please.";
        $scope.editBlock = true;
        if ($scope.removingTag) {
            // Delete.
            $http.get("/api/tags/delete", {
                    params: {
                        id: $scope.tags[$scope.currentIndex].id,
                    }
                })
                .success(function(data) {
                    $scope.editStatus = data.message;
                    if (data.success) {
                        $scope.tags.splice($scope.currentIndex, 1);
                        $scope.closeModal();
                    }
                })
                .error(function() {})
                .finally(function() {
                    $scope.editBlock = false;
                });
        } else if ($scope.tags[$scope.currentIndex].id != null) {
            // Modify.
            $http.get("/api/tags/update", {
                    params: {
                        id: $scope.tags[$scope.currentIndex].id,
                        name: $scope.tagName
                    }
                })
                .success(function(data) {
                    $scope.editStatus = data.status.message;
                    if (data.status.success) {
                        $scope.tags[$scope.currentIndex] = data.tag;
                        $scope.closeModal();
                    }
                })
                .error(function() {})
                .finally(function() {
                    $scope.editBlock = false;
                    setTimeout(function() {
                        $scope.input.focus();
                    });
                });
        } else {
            // New.
            $http.get("/api/tags/new", {
                    params: {
                        name: $scope.tagName
                    }
                })
                .success(function(data) {
                    $scope.editStatus = data.status.message;
                    if (data.status.success) {
                        $scope.tags[$scope.currentIndex] = data.tag;
                        $scope.closeModal();
                    }
                })
                .error(function() {})
                .finally(function() {
                    $scope.editBlock = false;
                    setTimeout(function() {
                        $scope.input.focus();
                    });
                });
        }
    };
    $scope.cancelTag = function() {
        if ($scope.tags[$scope.currentIndex].id == null) {
            $scope.tags.pop();
        }
        $scope.closeModal();
    };
});
