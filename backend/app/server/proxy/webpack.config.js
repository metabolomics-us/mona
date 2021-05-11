let webpack = require('webpack');

module.exports = {
    mode: 'development',
    entry: "./src/main/app/main.ts",
    output: {
        filename: "bundle.js",
        path: __dirname + "/src/main/app/dist"
    },
    resolve: {
        extensions: ["", ".webpack.js", ".web.js", ".ts", ".tsx", ".js"],
    },

    module: {
        rules: [
            { test : /\.tsx?$/, loader: "ts-loader" , },
            {
                test: /\.css$/i,
                use: ["style-loader", "css-loader",],
            },
            { test: /\.(scss)$/,
                use: [{
                    loader: 'style-loader', // inject CSS to page
                }, {
                    loader: 'css-loader', // translates CSS into CommonJS modules
                }, {
                    loader: 'postcss-loader', // Run post css actions
                    options: {
                        plugins: function () { // post css plugins, can be exported to postcss.config.js
                            return [
                                require('precss'),
                                require('autoprefixer')
                            ];
                        }
                    }
                }, {
                    loader: 'sass-loader' // compiles Sass to CSS
                }]},
            {
                test: /\.(woff|woff2|eot|ttf|otf|svg)$/,
                loader: "file-loader",
                options: {
                    outputPath: "../fonts",
                }
            }
        ]
    },
    devtool: "source-map",
    devServer: {
        contentBase: './src/main/app',
    },
    plugins: [
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery",
            jquery: "jquery",
            "window.jquery": "jquery",
            "window.jQuery": "jquery"
        })
    ],

};
