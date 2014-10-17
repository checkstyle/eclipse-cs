var app = angular.module('eclipse-cs', ['ngRoute', 'ui.bootstrap']);


app.config(['$routeProvider', function ($routeProvider) {

    $routeProvider.when('/', {
        templateUrl: '/partials/home.html'
    }).when('/faq', {
        templateUrl: '/partials/faq.html'
    }).otherwise({ redirectTo: '/' });

}]);


app.controller('ScreenshotsCtrl', function ($scope) {

    $scope.slides = [
        {
            image: '/images/screenshots/eclipsecs0000.png',
            text: 'Checkstyle violations annotated in the Java editor'
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
            image: '/images/screenshots/eclipsecs0013.png',
            text: 'Checkstyle violations chart with drilldown capability'
        }
    ];
});