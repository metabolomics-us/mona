// Karma configuration
// Generated on Tue Dec 22 2015 08:07:34 GMT-0800 (Pacific Standard Time)

module.exports = function(config) {
    config.set({

        basePath: '',
        frameworks: ['jasmine'],

        files: [
            'app/bower_components/angular/angular.js',
            'app/bower_components/angular-jquery/dist/angular-jquery.js',
            'app/bower_components/jquery/dist/jquery.js',

            // moaClientApp modules
            'app/bower_components/angular-mocks/angular-mocks.js',
            'app/bower_components/angular-route/angular-route.js',
            'app/bower_components/angular-resource/angular-resource.js',
            'app/bower_components/angular-cookies/angular-cookies.js',
            'app/bower_components/angular-animate/angular-animate.js',
            'app/bower_components/angular-sanitize/angular-sanitize.js',
            'app/bower_components/angular-bootstrap/ui-bootstrap.js',
            'app/bower_components/angular-filter/dist/angular-filter.js',
            'app/bower_components/angular-dialog-service/dist/dialogs.js',
            'app/bower_components/angular-dialog-service/dist/dialogs-default-translations.js',
            'app/bower_components/ng-tags-input/ng-tags-input.js',
            'app/bower_components/angular-msp-parser/service.js',
            'app/bower_components/angular-mgf-parser/service.js',
            'app/bower_components/angular-massbank-parser/service.js',
            'app/bower_components/angular-cts-service/service.js',
            'app/bower_components/ng-file-upload/angular-file-upload.js',
            'app/bower_components/angular-masspec-plotter/angular-masspec-plotter.js',
            'app/bower_components/ngInfiniteScroll/build/ng-infinite-scroll.js',
            'app/bower_components/angular-bootstrap-affix/dist/angular-bootstrap-affix.js',
            'app/bower_components/angular-translate/angular-translate.js',

            // load html templates
            'app/index.html',
            'app/views/*html',

            // source and test files
            'app/scripts/**/*.js',
            'test/spec/controllers/**/*.js'


        ],
        exclude: ['app/scripts/theme.js'],


        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {
            '**/scripts/controllers/**/*.js': 'coverage',
            '**/scripts/directives/**/*.js': 'coverage'
        },


        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['progress', 'coverage'],


        // web server port
        port: 9876,


        // enable / disable colors in the output (reporters and logs)
        colors: true,


        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,


        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,
        browsers: ['PhantomJS'],
        singleRun: false,

        // Concurrency level
        // how many browser should be started simultanous
        concurrency: Infinity
    });
}
