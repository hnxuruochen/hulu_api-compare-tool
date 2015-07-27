"use strict";
mainApp.controller("TagsController", function($scope, $http) {
    $http.get("/api/tags")
        .success(function(data) {
            for (var i = 0; i < data.length; i++) {
                data[i].edit = false;
                data[i].oldName = data[i].name;
            }
            $scope.tags = data;
        });
    $scope.editName = function(event, t) {
        t.edit = true;
        t.oldName = t.name;
    };
    $scope.saveName = function(event, t) {
        t.edit = false;
        console.log(t.name);
    };
    $scope.resetName = function(event, t) {
        t.edit = false;
        t.name = t.oldName;
    };

}).directive("editFocus", function($timeout) {
    return function($scope, $element, $attr) {
        $scope.$watch($attr.editFocus, function(value) {
            $timeout(function() {
                value ? $element.focus() :
                    $element.blur();
            });
        });
    }
});
$(document).ready(function($) {});
