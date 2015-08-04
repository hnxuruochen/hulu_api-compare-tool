"use strict";
mainApp.config(function($routeProvider, $locationProvider) {
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
        .when("/tasks/:id", {
            templateUrl: "templates/tasks_id.html",
            controller: "TasksIdController"
        })
        .when("/errors", {
            templateUrl: "templates/errors.html",
            controller: "ErrorsController"
        })
        .when("/", {
            redirectTo: "/index"
        })
        .otherwise({
            templateUrl: "templates/404.html"
        });
    $locationProvider.html5Mode(true);
});
