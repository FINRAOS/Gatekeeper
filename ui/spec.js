require('angular');
require('angular-mocks');
require('babel-polyfill');

var testsContext = require.context('./test/unit/specs', true, /\.spec\.js$/);
testsContext.keys().forEach(testsContext);
