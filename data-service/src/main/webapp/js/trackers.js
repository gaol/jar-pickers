(function() {
  var app = angular.module("trackerApp",[]);

  app.controller("dateController", function(){
    this.year = new Date().getFullYear();
  });
 
  app.directive("products", ["$http", function($http) {
     return {
       
     }
  }]);

})();
