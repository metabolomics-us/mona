***ANGULARJS BEST PRACTICES***
    If you are new to AngularJS or developing features for Mona-client app, the resources below will
    speed you up on implementation practices and AngularJS.

    1. Code School - AngularJS intro course sponsored by Google
    2. John Papa Style Guide - https://github.com/johnpapa/angular-styleguide
    3. Immediately Invoked Function Expression - http://benalman.com/news/2010/11/immediately-invoked-function-expression/#iife

    **Implement ONE component per file to make code easy to maintain and read**
    
***DEPENDENCY INJECTION***
1. For minification-safe, we use ngAnnotate to auto inject dependencies, to do that
    a. prefix functions that require dependencies with /* @ngInject */
        e.g
        /* @ngInject */
        function AvengersController(storage, avengerService) {
            var vm = this;
            vm.heroSearch = '';
            vm.storeHero = storeHero;
        }
        
2. prefix route resolver with /* @ngInject */
    e.g
        // Using @ngInject annotations
        function config($routeProvider) {
            $routeProvider
                .when('/avengers', {
                    templateUrl: 'avengers.html',
                    controller: 'AvengersController',
                    controllerAs: 'vm',
                    resolve: { /* @ngInject */
                        moviesPrepService: function(movieService) {
                            return movieService.getMovies();
                        }
                    }
                });
        }

3. testing min-safe & dependency injection
     all files in DEV and PROD are annotated. If there are any errors, you will
     see it in the browser in dev env
    DEV
        files are configured to automate annotation when dependency is added.
    PROD
        removes, and add annotations to scripts.js, then uglify.
    
    
***COMPILING SOURCE CODE***
1. npm -install
    install all grunt dependencies to build the system using grunt

2. bower install
    install all the bower dependencies

3. grunt serve
    to start the client side application 

4. karma testing
    add desired test locations to karma.conf.js -> preprocessors: []
    open terminal and run 'karma start'
    code coverage can be viewed in /coverage
    npm install -g karma-cli  //if you have issues with karma start  
