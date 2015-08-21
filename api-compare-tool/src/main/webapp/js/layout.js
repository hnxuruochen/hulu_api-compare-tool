"use strict";
var mainApp = angular.module("mainApp", ["ngRoute"]);
mainApp.controller("layoutController", function($scope, $rootScope, $http) {
    // Initialize.
    $rootScope.types = ["Text", "Service"];
    $rootScope.status = ["No data", "Waiting", "Running", "Finished"];
    $rootScope.searchResult = [];
    // Load user info first.
    $.ajax({
        type: "GET",
        url: "/api/user/info",
        async: false, // sync request.
        success: function(data) {
            data = JSON.parse(data);
            $rootScope.userData = {};
            $rootScope.userData.userName = data.first_name + " " + data.last_name;
            $rootScope.userData.userAccount = data.username;
        }
    });
    $rootScope.setRootTagConfig = function(selector, id) {
        $rootScope.rootTagId = id;
        $rootScope.rootTagSelector = selector;
    }
    // Load tags.
    $rootScope.loadingTags = true;
    $rootScope.setRootTagConfig(null, 1);
    $http.get("/api/tags")
        .success(function(data) {
            $rootScope.tags = data.tags;
            $rootScope.loadingTags = false;
            // Set default tag for current page.
            setTimeout(function() {
                if ($rootScope.rootTagSelector != null) {
                    $($rootScope.rootTagSelector)
                        .dropdown("set selected", $rootScope.rootTagId);
                }
            });
            $rootScope.tagsIdMap = {};
            for (var i = 0; i < data.length; i++) {
                $rootScope.tagsIdMap[data[i].id] = data[i];
            }
        });
});
$(document).ready(function($) {});
