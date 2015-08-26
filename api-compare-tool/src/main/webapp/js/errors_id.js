"use strict";
mainApp.controller("ErrorsIdController", function($scope, $routeParams, $http, $compile, $location) {
    $scope.loadingData = true;
    // Load error data.
    $http.get("/api/errors/" + $routeParams.id)
        .success(function(data) {
            if (!data.status.success) {
                $location.path("/404");
            } else {
                $scope.error = data.error;
                $scope.loadingData = false;
                $scope.renderErrorView();
            }
        });
    // Expand or collapse a block.
    $scope.toggleExpand = function(line) {
        $scope.lineStyle[line].expand = !$scope.lineStyle[line].expand;
    };
    // Check whether a line is a block start.
    $scope.isContentStart = function(line) {
        if ($scope.error.isXml) {
            if (line.endsWith("/>")) {
                return false;
            }
            if (!line.startsWith("<")) {
                return false;
            }
            var endTag = line.lastIndexOf("</");
            if (endTag != -1) {
                var quote = line.lastIndexOf("\"");
                if (quote < endTag) {
                    return false;
                }
            }
        } else {
            var c = line.charAt(line.length - 1);
            if ((c != '[') && (c != '{')) {
                return false;
            }
        }
        return true;
    };
    // Check whether a line is a block end.
    $scope.isContentEnd = function(line) {
        if ($scope.error.isXml) {
            if (line.startsWith("</")) {
                return true;
            }
        } else {
            var c = line.charAt(line.length - 1);
            // Skip ',' seperator.
            if (c == ",") {
                c = line.charAt(line.length - 2);
            }
            if ((c == ']') || (c == '}')) {
                return true;
            }
        }
        return false;

    };
    // Specify a color for each mark.
    $scope.getColor = function(c) {
        if (c == '*') {
            return " mixed";
        } else if (c == '+') {
            return " added";
        } else if (c == '-') {
            return " deleted";
        }
        return "";
    };
    $scope.renderErrorView = function() {
        if ($scope.error.output == null) {
            return;
        }
        // Initialize.
        var errorHtml = "";
        $scope.error.output = $scope.error.output.split("\n");
        var lines = $scope.error.output;
        $scope.lineStyle = [];
        // Add html one line by one line.
        for (var i = 0; i < lines.length; i++) {
            var style = {};
            style.color = $scope.getColor(lines[i].charAt(0));
            // Caculate margin.
            style.margin = lines[i].length - 1;
            lines[i] = lines[i].substring(1, lines[i].length).trim();
            style.margin -= lines[i].length;
            style.margin *= 5;
            // Spell line html.
            var blockStart = "";
            var blockEnd = "";
            var omit = "";
            var body = "";
            if ($scope.isContentEnd(lines[i])) {
                // End a sub content area.
                blockEnd = "</span>";
            }
            if ($scope.isContentStart(lines[i])) {
                // Show mixed content by default.
                style.expand = style.color == " mixed";
                // Add expand button.
                body = "<span ng-click=\"toggleExpand(" + i + ")\">";
                body = body + "<span><i class=\"minus small icon\" ng-show=\"lineStyle[" + i + "].expand\"></i></span>";
                body = body + "<span><i class=\"plus small icon\" ng-show=\"!lineStyle[" + i + "].expand\"></i></span>";
                body = body + "</span>";
                // Add omit block.
                omit = "<span ng-show=\"!lineStyle[" + i + "].expand\" style=\"margin-right: " + (-style.margin - 20) + "px;\">...</span>";
                // Start a sub content area.
                blockStart = "<span ng-show=\"lineStyle[" + i + "].expand\">";
                // Show omit when collapse the sub content.
            } else {
                // Add margin to align text.
                style.margin += 20;
            }
            // Line content.
            body = body + "<span>{{error.output[" + i + "]}}</span>" + omit;
            // Wrap div with color and margin.
            body = "<span class=\"error-line " + style.color + "\" style=\"margin-left: " + style.margin + "px;\">" + body + "</span>";
            // Add end and start tag, breakline.
            body = blockEnd + body + blockStart + "</br>";
            errorHtml = errorHtml + body;
            $scope.lineStyle.push(style);
        }
        // Compile html code.
        var ele = angular.element(document.getElementById("display-error"));
        ele.append(errorHtml);
        $compile(ele)($scope);
    };
});
