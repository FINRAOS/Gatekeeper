var isProd = process.env.NODE_ENV === 'production';
var webpack = require('webpack');
var WebpackDevServer = require("webpack-dev-server");
var path = require('path');
var vendor = [
    'angular',
    'angular-animate',
    'angular-aria',
    'angular-material',
    'angular-material-data-table',
    'angular-mocks',
    'angular-ui-router',
    'angular-resource',
    'moment'
];

var plugins = [
    new webpack.optimize.DedupePlugin(),
    new webpack.optimize.OccurenceOrderPlugin(),
    new webpack.optimize.CommonsChunkPlugin('common', 'common.js')
];
if (isProd) {
    plugins.splice(2, 0, new webpack.optimize.UglifyJsPlugin({
        mangle: false,
        sourceMap: false,
        compress: {
            warnings: false
        }
    }));
}


module.exports = {
    //for local dev against a backend
    devServer: {
        proxy: [
            //{
            //    path:"/api/register/*",
            //    target: "https://petstore-register.pet.finra.org",
            //    rewrite: function(req){
            //        var apiRegex = /^\/api\/\w+\/(.*)/;
            //        var match = apiRegex.exec(req.url);
            //        console.info(req.url);
            //        req.url = "/"+match[1];
            //        console.info(req.url);
            //    },
            //    headers: { "samaccountname": "tst_pet_gatekeeper" }
            //},
            {
                path:"/api/gatekeeper-ec2/*",
                target: "http://localhost:8080",
                rewrite: function(path){
                    var apiRegex = /^\/api\/[\w-]+\/(.*)/;
                    var match = apiRegex.exec(path.url);
                    path.url = "/"+match[1];
                    console.info(path.url);
                },
                headers: { "samaccountname": "meles" }
            }
            ,{
                path:"/api/gatekeeper-rds/*",
                target: "http://localhost:8088",
                rewrite: function(path){
                    var apiRegex = /^\/api\/[\w-]+\/(.*)/;
                    var match = apiRegex.exec(path.url);
                    path.url = "/"+match[1];
                    console.info(path.url);
                },
                headers: { "samaccountname": "meles" }
            }
        ]
    },
    context: path.resolve(__dirname, 'app'),
    entry: {
        vendor: vendor,
        bundle: isProd ? ['babel-polyfill', './app.js'] : ['babel-polyfill', 'webpack/hot/dev-server', './app.js']
    },
    devtool: isProd ? '' : 'source-map',
    output: {
        path: isProd ? './dist' : './app',
        filename: '[name].js'
    },
    plugins: plugins,
    module: {
        loaders: [
            {test: /\.js$/, loader: 'babel!imports?angular', include: /app|src|test/},
            {test: /\.woff$/, loader: 'url-loader?limit=10000&mimetype=application/font-woff'},
            {test: /\.woff2$/, loader: 'url-loader?limit=10000&mimetype=application/font-woff'},
            {test: /\.ttf$/, loader: 'url-loader?limit=10000&mimetype=application/octet-stream'},
            {test: /\.eot$/, loader: 'file-loader'},
            {test: /\.svg$/, loader: 'url-loader?limit=10000&mimetype=image/svg+xml'},
            {test: /\.tpl\.html$/, loader: 'raw'},
            {test: /\.png$/, loader: 'url-loader?limit=100000&mimetype=image/png'},
            {test: /\.jpg$/, loader: 'file-loader'},
            {test: /\.ico$/, loader: 'file-loader'},
            {test: /\.css$/, loader: 'style!css'},
            {test: /\.less$/, loader: 'style!css!less'}
        ]
    }
};
