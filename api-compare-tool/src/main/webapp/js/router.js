"use strict";
mainApp.config(["$routeProvider", function($routeProvider) {
    $routeProvider
        .when("/index", {
            templateUrl: "templates/index.html",
            controller: "IndexController"
        })
        .when("/tags", {
            templateUrl: "templates/tags.html",
            controller: "TagsController"
        })
        .when("/tasks", {
            templateUrl: "templates/tasks.html",
            controller: "TasksController"
        })
        .when("/errors", {
            templateUrl: "templates/errors.html",
            controller: "ErrorsController"
        })
        .otherwise({
            redirectTo: "/index"
        });
}]);
$(document).ready(function($) {});
