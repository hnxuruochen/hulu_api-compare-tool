"use strict";
var jq = angular.element;
var userData = {};

var mainApp = angular.module("mainApp", ["ngRoute"]);
var headerApp = angular.module("headerApp", []);
headerApp.controller("headerController", function($scope, $http) {
	$scope.userData = userData;
    $http.get("/api/user/info")
        .success(function(data) {
            userData.userName = data.first_name + " " + data.last_name;
            userData.userAccount = data.username;
        });
});
$(document).ready(function($) {
    angular.bootstrap(document.getElementById("mainApp"), ["mainApp"]);
    $(".ui.dropdown")
        .dropdown();
});
