var app = angular.module('eclipse-cs', ['ngRoute', 'ui.bootstrap']);

/**
 * Configure angular routing for all subpages.
 */
app.config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {

    $locationProvider.hashPrefix('!');

    $routeProvider.when('/', {
        templateUrl: 'partials/index.html'
    }).when('/releasenotes', {
        templateUrl: 'partials/releasenotes.html'
    }).when('/install', {
        templateUrl: 'partials/install.html'
    }).when('/project-setup', {
        templateUrl: 'partials/project-setup.html'
    }).when('/custom-config', {
        templateUrl: 'partials/custom-config.html'
    }).when('/filesets', {
        templateUrl: 'partials/filesets.html'
    }).when('/filters', {
        templateUrl: 'partials/filters.html'
    }).when('/configtypes', {
        templateUrl: 'partials/configtypes.html'
    }).when('/properties', {
        templateUrl: 'partials/properties.html'
    }).when('/preferences', {
        templateUrl: 'partials/preferences.html'
    }).when('/extensions', {
        templateUrl: 'partials/extensions.html'
    }).when('/custom-checks', {
        templateUrl: 'partials/custom-checks.html'
    }).when('/custom-filters', {
        templateUrl: 'partials/custom-filters.html'
    }).when('/builtin-config', {
        templateUrl: 'partials/builtin-config.html'
    }).when('/faq', {
        templateUrl: 'partials/faq.html'
    }).otherwise({ redirectTo: '/' });

}]);

/**
 * Custom directive for the flattr button, since the standard flattr button code did not quite work with Angular routing.
 */
app.directive('flattrButton', function() {
    return {
        restrict: 'A',
        controller: function() {
            var s = document.createElement('script'), t = document.getElementsByTagName('script')[0];
            s.type = 'text/javascript';
            s.async = true;
            s.src = 'http://api.flattr.com/js/0.6/load.js?mode=auto';
            t.parentNode.insertBefore(s, t);
        }
    };
});

/**
 * Custom directive for Google Ad integration.
 */
app.directive('googleAd', ['$rootScope', '$location', '$timeout', '$window', function($rootScope, $location, $timeout, $window) {
    return {
        restrict: 'A',
        replace: true,
        templateUrl: 'partials/google-ad.html',
        controller: function() {

            if ($location.host() == 'localhost' || $location.host() == '127.0.0.1') {
                console.log('We\'re on localhost, skipping ad push');
                return;
            }

            $rootScope.showAd = true;

            $timeout(function() {
                (adsbygoogle = window.adsbygoogle || []).push({});
            }, 500);

            var w = angular.element($window);
            w.bind('resize', function() {
                //
            });
        }
    };
}]);

/**
 * Controller for the release notes page.
 */
app.controller('ReleaseNotesCtrl', ['$scope', '$location', '$http', function ($scope, $location, $http) {

    $http.get('releases.json').success(function(data, status, headers, config) {
        $scope.releases = data;
    });

    /** Set all releases to expanded. **/
    $scope.expandAll = function() {
        angular.forEach($scope.releases, function(release) {
            release.open = true;
        });
    };

    var init = function () {

       var expandAll = $location.search().expandAll;
       if (expandAll == true) {
            $scope.expandAll();
       }
    };

    init();
}]);

/**
 * Controller for the screenshots carousel.
 */
app.controller('ScreenshotsCtrl', function ($scope) {

    $scope.slides = [
        {
            image: 'images/screenshots/eclipsecs0000.png',
            text: 'Checkstyle violations annotated in the Java editor'
        },
        {
            image: 'images/screenshots/eclipsecs0013.png',
            text: 'Checkstyle violations chart with drilldown capability'
        },
        {
            image: 'images/screenshots/eclipsecs0011.png',
            text: 'Checkstyle violations view, group violations by type'
        },
        {
            image: 'images/screenshots/eclipsecs0012.png',
            text: 'Drill down into violation categories'
        },
        {
            image: 'images/screenshots/eclipsecs0001.png',
            text: 'Checkstyle Project configuration (simple)'
        },
        {
            image: 'images/screenshots/eclipsecs0006.png',
            text: 'Checkstyle workspace preferences and setup of global check configurations'
        },
        {
            image: 'images/screenshots/eclipsecs0008.png',
            text: 'Checkstyle configuration editor, assemble your own Checkstyle setup'
        }
    ];
});
