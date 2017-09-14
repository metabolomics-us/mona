// Karma configuration
// Generated on Tue Dec 22 2015 08:07:34 GMT-0800 (Pacific Standard Time)

module.exports = function(config) {
    config.set({
        basePath: 'src/main/angularjs',
        frameworks: ['jasmine'],

        files: [
            // MoNA app dependencies
            'bower_components/jquery/dist/jquery.js',
            'bower_components/angular/angular.js',
            'bower_components/angular-route/angular-route.js',
            'bower_components/angular-resource/angular-resource.js',
            'bower_components/angular-cookies/angular-cookies.js',
            'bower_components/angular-sanitize/angular-sanitize.js',
            'bower_components/angular-animate/angular-animate.js',
            'bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
            'bower_components/angular-dialog-service/dist/dialogs.js',
            'bower_components/ng-tags-input/ng-tags-input.js',
            'bower_components/angular-cts-service/service.js',
            'bower_components/angular-msp-parser/service.js',
            'bower_components/angular-mgf-parser/service.js',
            'bower_components/angular-massbank-parser/service.js',
            'bower_components/angular-masspec-plotter/angular-masspec-plotter.js',
            'bower_components/ng-file-upload/angular-file-upload.js',
            'bower_components/paralleljs/lib/parallel.js',
            'bower_components/angular-filter/dist/angular-filter.js',
            'bower_components/AngularJS-Toaster/toaster.js',
            'bower_components/angular-nvd3/dist/angular-nvd3.js',
            'bower_components/angular-google-analytics/dist/angular-google-analytics.js',

            // test dependencies
            'bower_components/angular-mocks/angular-mocks.js',

            // source scripts
            'scripts/**/*.js',

            // html templates
            'views/**/*.html',

            // test scripts
            //'test/spec/controllers/**/*.js',
            //'test/spec/services/**/*.js',
            'test/spec/directives/**/*.js',
            //'test/spec/services/query/*.js',
            //'test/spec/controllers/query/*.js'
            //'test/spec/controllers/query/*.js'

            'test/spec/services/cookie/*.js',
            'test/spec/services/optimization/*.js',
            'test/spec/app-spec.js'
        ],

        // plugin to load our html templates as modules
        ngHtml2JsPreprocessor: {
            moduleName: 'templates'
        },

        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {
            '**/scripts/controllers/**/*.js': 'coverage',
            '**/scripts/services/**/*.js': 'coverage',
            '**/scripts/directives/**/*.js': 'coverage',
            'views/**/*.html': ['ng-html2js']
        },

        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['dots', 'junit', 'coverage'],

        junitReporter: {
            outputDir: 'coverage/junit',
		    outputFile: 'test-results.xml',
		    suite: ''
	    },

        // web server port
        port: 9876,

        // enable / disable colors in the output (reporters and logs)
        colors: false,

        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,
        browsers: ['Chrome'],
        singleRun: false,

        // Concurrency level
        // how many browser should be started simultanous
        concurrency: Infinity
    });
};
