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

var HtmlReporter = require('protractor-html-screenshot-reporter');

exports.config = {
    seleniumServerJar: '../node_modules/gulp-protractor/node_modules/protractor/selenium/selenium-server-standalone-2.45.0.jar',
    seleniumPort: 4444,
    seleniumArgs: ['-browserTimeout=120'],
    chromeDriver: '../node_modules/gulp-protractor/node_modules/protractor/selenium/chromedriver',
    chromeOnly: false,


    /*
     This makes the tests run in series instead of parallel
     */
    maxSessions: 1,
    multiCapabilities: [
        {
            browserName: 'internet explorer',
            ensureCleanSession: 'true'
        }
        ,

        {
            browserName: 'firefox',
            version: '34'
        }
        ,

        {
            browserName: 'chrome',
            'chromeOptions': {
                args: [
                    '--test-type',
                    '--disable-cache'
                ]
            }
        }
    ],
    baseUrl: 'http://localhost:9000',

    // Selector for the element housing the angular app - this defaults to
    // body, but is necessary if ng-app is on a descendant of <body>.
    rootElement: 'html',

    // The timeout in milliseconds for each script run on the browser. This should
    // be longer than the maximum time your application needs to stabilize between
    // tasks.
    allScriptsTimeout: 500000,

    // How long to wait for a page to load.
    getPageTimeout: 500000,

    // A callback function called once protractor is ready and available, and
    // before the specs are executed.
    // If multiple capabilities are being run, this will run once per
    // capability.
    // You can specify a file containing code to run by setting onPrepare to
    // the filename string.
    onPrepare: function () {
        var jasmineReporters = require('jasmine-reporters');


        // returning the promise makes protractor wait for the reporter config before executing tests
        return browser.getProcessedConfig().then(function (config) {
            // you could use other properties here if you want, such as platform and version
            browser.driver.manage().window().maximize();
            var browserName = config.capabilities.browserName;
            var browserVersion = config.capabilities.version;
            var prefix = browserName + "_" + browserVersion;
            var junitReporter = new jasmineReporters.JUnitXmlReporter({
                consolidateAll: true,
                savePath: 'protractorxml',
                // this will produce distinct xml files for each capability
                filePrefix: prefix + '_',
                modifySuiteName: function (generatedSuiteName, suite) {
                    // this will produce distinct suite names for each capability,
                    // e.g. 'firefox.login tests' and 'chrome.login tests'
                    return generatedSuiteName + '_' + prefix;
                }
            });
            jasmine.getEnv().addReporter(junitReporter);
        });
    },
    // A callback function called once tests are finished.
    onComplete: function () {
        // At this point, tests will be done but global objects will still be
        // available.
    },

    // A callback function called once the tests have finished running and
    // the WebDriver instance has been shut down. It is passed the exit code
    // (0 if the tests passed or 1 if not). This is called once per capability.
    onCleanUp: function (exitCode) {
    },

    // The params object will be passed directly to the Protractor instance,
    // and can be accessed from your test as browser.params. It is an arbitrary
    // object and can contain anything you may need in your test.
    // This can be changed via the command line as:
    //   --params.login.user 'Joe'
    params: {
        login: {
            user: 'Jane',
            password: '1234'
        }
    },

    // ---------------------------------------------------------------------------
    // ----- The test framework --------------------------------------------------
    // ---------------------------------------------------------------------------

    // Test framework to use. This may be jasmine, cucumber, or mocha.
    //
    // Jasmine is fully supported as a test and assertion framework.
    // Mocha and Cucumber have limited beta support. You will need to include your
    // own assertion framework (such as Chai) if working with Mocha.
    framework: 'jasmine2',

    // Options to be passed to minijasminenode.
    //
    // See the full list at https://github.com/juliemr/minijasminenode/tree/jasmine1
    jasmineNodeOpts: {
        // If true, display spec names.
        //isVerbose: true,
        // If true, print colors to the terminal.
        showColors: true,
        // If true, include stack traces in failures.
        includeStackTrace: true,
        // Default time to wait in ms before a test fails.
        defaultTimeoutInterval: 500000
    }

    // Options to be passed to Mocha.
    //
    // See the full list at http://visionmedia.github.io/mocha/
//  mochaOpts: {
//    ui: 'bdd',
//    reporter: 'list'
//  },

    // Options to be passed to Cucumber.
//  cucumberOpts: {
//    // Require files before executing the features.
//    require: 'cucumber/stepDefinitions.js',
//    // Only execute the features or scenarios with tags matching @dev.
//    // This may be an array of strings to specify multiple tags to include.
//    tags: '@dev',
//    // How to format features (default: progress)
//    format: 'summary'
//  }
};
