var isProd = process.env.NODE_ENV.trim() === 'production';

var webpack = require('webpack');
var TerserPlugin = require("terser-webpack-plugin");
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

];
if (isProd) {
    plugins.splice(3, 0, new HtmlWebpackPlugin({
        hash: true,
        filename: './index.html',//relative to root of the application
        template: './index-build.html'
    }));
}


module.exports = {
    mode: isProd ? 'production' : 'development',
    //for local dev against a backend
    devServer: {
        proxy: {
            '/api/gatekeeper-ec2': {
                target: 'http://localhost:8080',
                headers: {'user': 'sm'}
            },
            '/api/gatekeeper-rds': {
                target: 'http://localhost:8088',
                headers: {'user': 'sm'}
            }
        },
    },
    context: path.resolve(__dirname, 'app'),
    entry: {
        vendor: vendor,
        bundle: isProd ? [ '/app.js'] : ['webpack/hot/dev-server', './app.js']
    },
    output: {
        path: path.join(__dirname, isProd ? '/dist' : '/app'),
        filename: isProd ? '[name].[fullhash].js' : '[name].js'
    },
    optimization: {
        minimize: isProd,
        minimizer: [new TerserPlugin({
            extractComments: false,
            terserOptions: {
                mangle: false
            }
        })],
    },
    performance: {
        hints: false
    },
    plugins: plugins,
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /(node_modules|bower_components)/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: ['@babel/preset-env']
                    }
                }
            },
            {
                test: /\.(woff|woff2|ttf|eot)$/,
                use: 'url-loader',
            },
            {
                test: /\.svg$/,
                use: 'svgo-loader',
                type: 'asset'
            },
            {
                test: /\.tpl\.html$/,
                use: 'raw-loader'
            },
            {
                test: /\.png$/,
                use: [{
                    loader: 'url-loader',
                    options: {
                        limit:100000,
                        mimetype: 'image / png'
                    }
                }]
            },
            {
                test: /\.jpg$/,
                use: [{loader: 'file-loader'}]
            },
            {
                test: /\.ico$/,
                use: [{loader: 'file-loader'}]
            },
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.less$/,
                use: ['style-loader', 'css-loader', 'less-loader']
            }
        ]
    }
};

if(!isProd){
    module.exports.devtool = 'source-map';
}
