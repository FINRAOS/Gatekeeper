var isProd = process.env.NODE_ENV.trim() === 'production';

var webpack = require('webpack');
var WebpackDevServer = require("webpack-dev-server");
var HtmlWebpackPlugin = require("html-webpack-plugin");
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
    new webpack.optimize.CommonsChunkPlugin('common', isProd ? '[name].[hash].js' : '[name].js')
];
if (isProd) {
    plugins.splice(2, 0, new webpack.optimize.UglifyJsPlugin({
        mangle: false,
        sourceMap: false,
        compress: {
            warnings: false
        }
    }));
    plugins.splice(3, 0, new HtmlWebpackPlugin({
        hash: true,
        filename: './index.html',//relative to root of the application
        template: './index.html'
    }))
}


module.exports = {
    //for local dev against a backend
    devServer: {
        proxy: [
            {
                path:"/api/gatekeeper-ec2/*",
                headers: { "account": "sm" }
            }
            ,{
                path:"/api/gatekeeper-rds/*",
                headers: { "account": "sm" }
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
        filename: isProd ? '[name].[hash].js' : '[name].js'
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
