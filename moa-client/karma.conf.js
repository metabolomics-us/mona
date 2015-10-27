// Karma configuration
// Generated on Wed Jun 11 2014 20:13:48 GMT-0700 (PDT)

module.exports = function (config) {
    config.set({

        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath: '',


        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['jasmine'],


        // list of files / patterns to load in the browser
        files: [
            'app/bower_components/angular/angular.js',
            'app/bower_components/angular-route/angular-route.js',
            'app/bower_components/angular-cookies/angular-cookies.js',
            'app/bower_components/angular-sanitize/angular-sanitize.js',
            'app/bower_components/angular-dialog-service/dist/dialogs.js',
            'app/bower_components/angular-dialog-service/dist/dialogs-default-translations.js',
            'app/bower_components/angular-msp-parser/service.js',
            'app/bower_components/angular-mgf-parser/service.js',
            'app/bower_components/angular-massbank-parser/service.js',
            'app/bower_components/angular-cts-service/service.js',
            'app/bower_components/angular-masspec-plotter/angular-masspec-plotter.js',
            'app/bower_components/ngInfiniteScroll/build/ng-infinite-scroll.js',
            'app/bower_components/ng-file-upload/angular-file-upload.js',
            'app/bower_components/angular-bootstrap-affix/dist/angular-bootstrap-affix.js',
            'app/bower_components/angular-jquery/dist/angular-jquery.js',
            'app/bower_components/angular-translate/angular-translate.js',
            'app/bower_components/angular-resource/angular-resource.js',
            'app/bower_components/angular-mocks/angular-mocks.js',
            'app/bower_components/angular-bootstrap/ui-bootstrap.js',
            'app/bower_components/ng-tags-input/ng-tags-input.js',

            'app/scripts/**/*.js',
            'test/spec/**/*.js'
        ],


        // list of files to exclude
        exclude: [

        ],


        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {
          '**/scripts/services/persistence/*.js' : 'coverage'
        },


        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['progress', 'osx','coverage'],


        // web server port
        port: 9876,


        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_DEBUG,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,


        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        // browsers: ['Chrome'],
        browsers: ['PhantomJS'],


        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: false
    });
};
