"use strict";
var jq = angular.element;

var mainApp = angular.module("mainApp", ["ngRoute"]);
mainApp.controller("headerController", function($scope, $rootScope, $http) {
	// Initialize.
    $rootScope.types = ["Text", "Service"];
    $rootScope.status = ["Waiting", "Running", "Finished"];
    $rootScope.searchResult = [];
	// Load user info.
    $http.get("/api/user/info")
        .success(function(data) {
        	$rootScope.userData = {};
            $rootScope.userData.userName = data.first_name + " " + data.last_name;
            $rootScope.userData.userAccount = data.username;
        });
});
$(document).ready(function($) {
});
