var app = angular.module('eclipse-cs', ['ngRoute', 'ui.bootstrap']);

/**
 * Configure angular routing for all subpages.
 */
app.config(['$routeProvider', '$locationProvider', function ($routeProvider, $locationProvider) {

    $locationProvider.hashPrefix('!');

    $routeProvider.when('/', {
        templateUrl: '/partials/index.html'
    }).when('/releasenotes', {
        templateUrl: '/partials/releasenotes.html'
    }).when('/install', {
        templateUrl: '/partials/basic/install.html'
    }).when('/project-setup', {
        templateUrl: '/partials/basic/project-setup.html'
    }).when('/custom-config', {
        templateUrl: '/partials/basic/custom-config.html'
    }).when('/filesets', {
        templateUrl: '/partials/advanced/filesets.html'
    }).when('/filters', {
        templateUrl: '/partials/advanced/filters.html'
    }).when('/configtypes', {
        templateUrl: '/partials/advanced/configtypes.html'
    }).when('/properties', {
        templateUrl: '/partials/advanced/properties.html'
    }).when('/preferences', {
        templateUrl: '/partials/advanced/preferences.html'
    }).when('/extensions', {
        templateUrl: '/partials/extensions/extensions.html'
    }).when('/custom-checks', {
        templateUrl: '/partials/extensions/custom-checks.html'
    }).when('/custom-filters', {
        templateUrl: '/partials/extensions/custom-filters.html'
    }).when('/builtin-config', {
        templateUrl: '/partials/extensions/builtin-config.html'
    }).when('/faq', {
        templateUrl: '/partials/faq.html'
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
app.directive('googleAd', ['$location', '$timeout', function($location, $timeout) {
    return {
        restrict: 'A',
        templateUrl: '/partials/google-ad.html',
        controller: function() {

            if ($location.host() == 'localhost') {
                console.log('We\'re on localhost, skipping ad push');
                return;
            }

            $timeout(function() {
                (adsbygoogle = window.adsbygoogle || []).push({});
            }, 100);
        }
    };
}]);

/**
 * Controller for the release notes page.
 */
app.controller('ReleaseNotesCtrl', ['$scope', '$location', function ($scope, $location) {

    $scope.releases = [
      {
        version: "Release 5.8.0",
        template: "/partials/releases/5.8.0/release_notes.html",
        open: true
      },
      {
        version: "Release 5.7.0",
        template: "/partials/releases/5.7.0/release_notes.html",
        open: true
      },
      {
        version: "Release 5.6.1",
        template: "/partials/releases/5.6.1/release_notes.html",
        open: true
      },
      {
        version: "Release 5.6.0",
        template: "/partials/releases/5.6.0/release_notes.html",
        open: true
      },
      {
        version: "Release 5.5.0",
        template: "/partials/releases/5.5.0/release_notes.html"
      },
      {
        version: "Release 5.4.1",
        template: "/partials/releases/5.4.1/release_notes.html"
      },
      {
        version: "Release 5.4.0",
        template: "/partials/releases/5.4.0/release_notes.html"
      },
      {
        version: "Release 5.3.0",
        template: "/partials/releases/5.3.0/release_notes.html"
      },
      {
        version: "Release 5.2.0",
        template: "/partials/releases/5.2.0/release_notes.html"
      },
      {
        version: "Release 5.1.1",
        template: "/partials/releases/5.1.1/release_notes.html"
      },
      {
        version: "Release 5.1.0",
        template: "/partials/releases/5.1.0/release_notes.html"
      },
      {
        version: "Release 5.0.3",
        template: "/partials/releases/5.0.3/release_notes.html"
      },
      {
        version: "Release 5.0.2",
        template: "/partials/releases/5.0.2/release_notes.html"
      },
      {
        version: "Release 5.0.1",
        template: "/partials/releases/5.0.1/release_notes.html"
      },
      {
        version: "Release 5.0.0-final",
        template: "/partials/releases/5.0.0final/release_notes.html"
      },
      {
        version: "Release 5.0.0-beta4",
        template: "/partials/releases/5.0.0beta4/release_notes.html"
      },
      {
        version: "Release 5.0.0-beta3",
        template: "/partials/releases/5.0.0beta3/release_notes.html"
      },
      {
        version: "Release 5.0.0-beta2",
        template: "/partials/releases/5.0.0beta2/release_notes.html"
      },
      {
        version: "Release 5.0.0-beta1",
        template: "/partials/releases/5.0.0beta1/release_notes.html"
      },
      {
        version: "Release 4.4.2",
        template: "/partials/releases/4.4.2/release_notes.html"
      },
      {
        version: "Release 4.4.1",
        template: "/partials/releases/4.4.1/release_notes.html"
      },
      {
        version: "Release 4.4.0",
        template: "/partials/releases/4.4.0/release_notes.html"
      },
      {
        version: "Release 4.3.3",
        template: "/partials/releases/4.3.3/release_notes.html"
      },
      {
        version: "Release 4.3.2",
        template: "/partials/releases/4.3.2/release_notes.html"
      },
      {
        version: "Release 4.3.1",
        template: "/partials/releases/4.3.1/release_notes.html"
      },
      {
        version: "Release 4.3.0",
        template: "/partials/releases/4.3.0/release_notes.html"
      },
      {
        version: "Release 4.2.1",
        template: "/partials/releases/4.2.1/release_notes.html"
      },
      {
        version: "Release 4.2.0",
        template: "/partials/releases/4.2.0/release_notes.html"
      },
      {
        version: "Older releases",
        template: "/partials/releases/release_notes_older.html"
      }
    ];

    /** Set all releases to expanded. **/
    $scope.expandAll = function() {
        angular.forEach($scope.releases, function(release) {
            release.open = true;
        });
    };


    var init = function () {

       var expandAll = $location.search().expandAll;

       console.log(expandAll);

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
            image: '/images/screenshots/eclipsecs0000.png',
            text: 'Checkstyle violations annotated in the Java editor'
        },
        {
            image: '/images/screenshots/eclipsecs0013.png',
            text: 'Checkstyle violations chart with drilldown capability'
        },
        {
            image: '/images/screenshots/eclipsecs0011.png',
            text: 'Checkstyle violations view, group violations by type'
        },
        {
            image: '/images/screenshots/eclipsecs0012.png',
            text: 'Drill down into violation categories'
        },
        {
            image: '/images/screenshots/eclipsecs0001.png',
            text: 'Checkstyle Project configuration (simple)'
        },
        {
            image: '/images/screenshots/eclipsecs0006.png',
            text: 'Checkstyle workspace preferences and setup of global check configurations'
        },
        {
            image: '/images/screenshots/eclipsecs0008.png',
            text: 'Checkstyle configuration editor, assemble your own Checkstyle setup'
        }
    ];
});
