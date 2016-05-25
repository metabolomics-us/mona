/**
 * Created by sajjan on 1/8/15.
 */

exports.config = {
    seleniumAddress: 'http://localhost:4444/wd/hub',

    baseUrl: 'http://localhost:9090',

    capabilities: {
        'browserName': 'chrome'
    },

    specs: ['e2e/**/*.js'],

    framework: 'jasmine',

    jasmineNodeOpts: {
        showColors: true,
        isVerbose: true
    }
};