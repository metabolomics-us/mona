let webpack = require('webpack');

module.exports = {
    mode: 'development',
    entry: "./src/main/app/main.ts",
    output: {
        filename: "bundle.js",
        path: __dirname + "/src/main/app/dist"
    },
    resolve: {
        extensions: ["", ".webpack.js", ".web.js", ".ts", ".tsx", ".js"]
    },
    module: {
        rules: [
            { test : /\.tsx?$/, loader: "ts-loader" , }
        ]
    },
    devtool: "source-map",
    devServer: {
        contentBase: './src/main/app',
    },
    plugins: [
        new webpack.ProvidePlugin({
            $: "jquery",
            jQuery: "jquery"
        })
    ]
};
