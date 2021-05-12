/*
 *
 * Copyright 2018. Gatekeeper Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Karma configuration
var path = require('path');

var hasCoverage = global.process.argv.reduce(function (result, arg) {
    return arg.indexOf('coverage') !== -1 || result;
},false);

var include = [
    path.resolve('./app')
];
var webpackConfig = require('./webpack.config');

var webpack_module = {
    rules: webpackConfig.module.rules
};


if (hasCoverage) {
    webpack_module.postLoaders = [
        //{test: /\.js$/, loader: 'isparta', include: include, exclude: /\.spec\.js$/}
        {
            test: /\.js$/,
            include: include,
            loader: 'istanbul-instrumenter'
        }
    ];
}

module.exports = function (config) {

    config.set({
        // base path, that will be used to resolve files and exclude
        basePath: '',

        // testing framework to use (jasmine/mocha/qunit/...)
        frameworks: ['jasmine'],

        // list of files / patterns to load in the browser
        files: ['spec.js'],
        webpack: {
            devtool: 'inline-source-map',
            //plugins: webpackConfig.plugins,
            module: webpack_module,
            cache: true
        },
        webpackMiddleware: {
            stats: {
                chunkModules: false,
                colors: true
            }
        },
        // list of files / patterns to exclude
        exclude: [],


        //
        // For unit tests to run successfully, you must be on the finra.org domain
        //
        // Edit this file as Admin: C:\Windows\System32\drivers\etc\hosts (On Windows)
        // Add this entry to the end:
        //   127.0.0.1                     localhost       local.finra.org
        // Access your web server this way:
        //   http://local.finra.org
        //
        hostname: 'localhost',
        port: 9018,
        runnerPort: 9100,
        //proxies: {
        //    '/node_modules': '/base/node_modules',
        //    '/dist': '/base/dist'
        //},

        colors: true,
        // level of logging
        // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
        logLevel: config.ERROR,

        preprocessors: {
            'spec.js': ['webpack','sourcemap'],
            'app/app.js': ['coverage']
        },

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: false,
        // it is kinda slow with Jenkins
        browserNoActivityTimeout: 100000,


        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        /*
         browsers: ['PhantomJS','PhantomJS_custom'],
         customLaunchers: {
         'PhantomJS_custom': {
         base: 'PhantomJS',
         options: {
         windowName: 'my-window',
         settings: {
         webSecurityEnabled: false
         }
         },
         flags: ['--remote-debugger-port=9000']
         }
         },
         */
//        browsers: ['Chrome','IE'],
     //   browsers: ['Chrome'],
        browsers: ['PhantomJS'],
//        customLaunchers: {
//            'IE9': {
//                base: 'IE',
//                'x-ua-compatible': 'IE=EmulateIE9'
//            },
////            'IE10': {
////                base: 'IE',
////                'x-ua-compatible': 'IE=EmulateIE10'
////            }
//        },


        junitReporter: {
            outputDir: 'test/junit', // results will be saved as $outputDir/$browserName.xml
            suite: ''
        },

        coverageReporter: {
            dir: 'coverage/',
            subdir: '.',
            reporters: [
                {type: 'cobertura', file: 'cobertura.xml'},
                {type: 'text', file: 'text.txt'},
                {type: 'text-summary', file: 'text-summary.txt'},
                {type: 'html'},
                {type: 'clover'}
            ]
        },

        plugins: [
            'karma-webpack',
            'karma-jasmine',
            'karma-junit-reporter',
            'karma-chrome-launcher',
            'karma-phantomjs-launcher',
            'karma-htmlfile-reporter',
            'karma-coverage',
            'karma-spec-reporter',
            'karma-sourcemap-loader'
        ],
        reporters: ['progress', 'html', 'spec', 'coverage', 'junit'],

        htmlReporter: {
            outputFile: 'test/unit.html'
        },
// Continuous Integration mode
// if true, it capture browsers, run tests and exit
        singleRun: true
    });
};
