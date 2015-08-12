"use strict";
mainApp.controller("ErrorsIdController", function($scope, $routeParams, $http, $compile) {
    $scope.loadingData = true;

    $http.get("/api/errors/" + $routeParams.id)
        .success(function(data) {
            $scope.error = data.error;
            $scope.loadingData = false;
            $scope.renderErrorView();
        });

    $scope.toggleExpand = function(line) {
        $scope.lineStyle[line].expand = !$scope.lineStyle[line].expand;
    }
    $scope.isContentStart = function(line) {
        var c = line.charAt(line.length - 1);
        if ((c == '[') || (c == '{')) {
            return true;
        }
        return false;
    }
    $scope.isContentEnd = function(line) {
        var c = line.charAt(line.length - 1);
        // Skip ',' seperator.
        if (c == ",") {
            c = line.charAt(line.length - 2);
        }
        if ((c == ']') || (c == '}')) {
            return true;
        }
        return false;
    }
    $scope.getColor = function(c) {
        if (c == '*') {
            return " mixed";
        } else if (c == '+') {
            return " added";
        } else if (c == '-') {
            return " deleted";
        }
        return "";
    }
    $scope.renderErrorView = function() {
        var errorHtml = "";
        var lines = $scope.error.output.split("\n");
        $scope.lineStyle = [];
        for (var i = 0; i < lines.length; i++) {
            var line = lines[i];
            var style = {};
            style.color = $scope.getColor(line.charAt(0));
            // Caculate margin.
            style.margin = line.length - 1;
            line = line.substring(1, line.length).trim();
            style.margin -= line.length;
            style.margin *= 5;
            // Spell line html.
            var blockStart = "";
            var blockEnd = "";
            var body = "";
            var omit = "<span ng-show=\"!lineStyle[" + i + "].expand\" style=\"margin-right: " + (-style.margin - 20) + "px;\">";
            var br = "</br>";
            if ($scope.isContentEnd(line) && !$scope.isContentStart(lines[i - 1])) {
                // End a sub content area.
                blockEnd = "</span>";
            }
            if ($scope.isContentStart(line)) {
                if (!$scope.isContentEnd(lines[i + 1])) {
                    // Don't show same content by default.
                    style.expand = style.color == " mixed";
                    // Add expand button.
                    body = "<span ng-click=\"toggleExpand(" + i + ")\">";
                    body = body + "<span><i class=\"minus small icon\" ng-show=\"lineStyle[" + i + "].expand\"></i></span>";
                    body = body + "<span><i class=\"plus small icon\" ng-show=\"!lineStyle[" + i + "].expand\"></i></span>";
                    body = body + "</span>";
                    // Start a sub content area.
                    blockStart = "<span ng-show=\"lineStyle[" + i + "].expand\">";
                    // Show omit when collapse the sub content.
                    omit = omit + "...";
                } else {
                    br = "";
                }
            }
            if (body == "") {
                // Add same width as exp  and tag.
                style.margin = style.margin + 20;
            }
            omit = omit + "</span>";
            // Line content.
            body = body + "<span>" + line + "</span>" + omit;
            // Wrap div with color and margin.
            body = "<span class=\"error-line " + style.color + "\" style=\"margin-left: " + style.margin + "px;\">" + body + "</span>";
            // Add end and start tag, breakline.
            body = blockEnd + body + blockStart + br;
            errorHtml = errorHtml + body;
            $scope.lineStyle.push(style);
        }
        var ele = angular.element(document.getElementById("display-error"));
        ele.append(errorHtml);
        $compile(ele)($scope);
    };
});
