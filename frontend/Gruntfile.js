// Generated on 2014-05-28 using generator-angular 0.8.0


'use strict';

// # Globbing
// for performance reasons we're only matching one level down:
// 'test/spec/{,*/}*.js'
// use this if you want to recursively match all subfolders:
// 'test/spec/**/*.js'



module.exports = function (grunt) {

    // Load grunt tasks automatically
    require('load-grunt-tasks')(grunt);

    // for grunt:serve to rewrite URLS and use HTML5 form
    var serveStatic = require('serve-static');
    var pushState = require('connect-pushstate');
    var modRewrite = require('connect-modrewrite');

    // Time how long tasks take. Can help when optimizing build times
    require('time-grunt')(grunt);


    // Define the configuration for all the tasks
    grunt.initConfig({

        // Project settings
        yeoman: {
            // configurable paths
            app: require('./bower.json').appPath || 'app',
            dist: 'dist'
        },

        // Watches files for changes and runs tasks based on the changed files
        watch: {
            bower: {
                files: ['bower.json'],
                tasks: ['bowerInstall']
            },
            js: {
                files: ['<%= yeoman.app %>/scripts/{,*/}*.js'],
                //tasks: ['newer:jshint:all'],
                options: {
                    livereload: true
                }
            },
            jsTest: {
                files: ['test/spec/{,*/}*.js'],
                //tasks: ['newer:jshint:test', 'karma']
                tasks: ['karma']
            },
            compass: {
                files: ['<%= yeoman.app %>/styles/{,*/}*.{scss,sass}'],
                tasks: ['compass:server', 'autoprefixer']
            },
            gruntfile: {
                files: ['Gruntfile.js']
            },
            livereload: {
                options: {
                    livereload: '<%= connect.options.livereload %>'
                },
                files: [
                    '<%= yeoman.app %>/{,*/}*.html',
                    '.tmp/styles/{,*/}*.css',
                    '<%= yeoman.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
                ]
            }
        },

        // The actual grunt server settings
        connect: {
            options: {
                port: 9090,
                // Change this to '0.0.0.0' to access the server from outside.
                hostname: '0.0.0.0',
                livereload: 35729,
                middleware: function(connect,options) {
                    return [
                       // modRewrite(['^(//bower_components+)$ /index.html [L]']),
                        pushState(),
                        serveStatic('.tmp'),
                        //connect().use('/bower_components',serveStatic('/bower_components')),
                        serveStatic(options.base[1])];
                }

            },
            livereload: {
                options: {
                    open: true,
                    base: [
                        '.tmp',
                        '<%= yeoman.app %>'
                    ]
                    //middleware: function(connect,options) {
                    //    return [
                    //        modRewrite(['^(//w+)$ /index.html [L]']),
                    //        pushState(),
                    //        serveStatic('.tmp'),
                    //        connect().use('bower_components',serveStatic('app/bower_components')),
                    //        serveStatic(options.base[1])];
                    //}
                }
            },
            test: {
                options: {
                    port: 9001,
                    base: [
                        '.tmp',
                        'test',
                        '<%= yeoman.app %>'
                    ]
                }
            },
            dist: {
                options: {
                    base: '<%= yeoman.dist %>'
                }
            }
        },

        // Make sure code styles are up to par and there are no obvious mistakes
        //jshint: {
        //    options: {
        //        jshintrc: '.jshintrc',
        //        reporter: require('jshint-stylish')
        //    },
        //    all: [
        //        'Gruntfile.js',
        //        '<%= yeoman.app %>/scripts/{,*/}*.js'
        //    ],
        //    test: {
        //        options: {
        //            jshintrc: 'test/.jshintrc'
        //        },
        //        src: ['test/spec/{,*/}*.js']
        //    }
        //},

        // Empties folders to start fresh
        clean: {
            dist: {
                files: [{
                    dot: true,
                    src: [
                        '.tmp',
                        '<%= yeoman.dist %>/*',
                        '!<%= yeoman.dist %>/.git*'
                    ]
                }]
            },
            server: '.tmp'
        },

        // Add vendor prefixed styles
        autoprefixer: {
            options: {
                browsers: ['last 1 version']
            },
            dist: {
                files: [{
                    expand: true,
                    cwd: '.tmp/styles/',
                    src: '{,*/}*.css',
                    dest: '.tmp/styles/'
                }]
            }
        },

        wiredep: {
          app: {
            src:['<%= yeoman.app %>/index.html'],
            ignorePath: /\.\.\//
          },
          test: {
            devDependencies: true,
            src: 'karma.conf.js',
            ignorePath: /\.\.\//,
            fileTypes: {
              js: {
                block: /(([\s\t]*)\/\/\s*bower:*(\S*))(\n|\r)*?(\/\/\s*endbower)/gi,
                detect: {
                  js: /'(.*\.js)'/gi
                },
                replace: {
                  js:'\'{{filePath}}\','
                }
              }
            }
          }
        },

        // Automatically inject Bower components into the app
        bowerInstall: {
            app: {
                src: ['<%= yeoman.app %>/index.html'],
                ignorePath: '<%= yeoman.app %>/',
                exclude: ['<%= yeoman.app %>/bower_components/bootstrap-sass-official/assets/javascripts/bootstrap/']
            },
            sass: {
                src: ['<%= yeoman.app %>/styles/{,*/}*.{scss,sass}'],
                ignorePath: '<%= yeoman.app %>/bower_components/'
            }
        },

        // Compiles Sass to CSS and generates necessary files if requested
        compass: {
            options: {
                sassDir: '<%= yeoman.app %>/styles',
                cssDir: '.tmp/styles',
                generatedImagesDir: '.tmp/images/generated',
                imagesDir: '<%= yeoman.app %>/images',
                javascriptsDir: '<%= yeoman.app %>/scripts',
                fontsDir: '<%= yeoman.app %>/styles/fonts',
                importPath: '<%= yeoman.app %>/bower_components',
                httpImagesPath: '/images',
                httpGeneratedImagesPath: '/images/generated',
                httpFontsPath: '/styles/fonts',
                relativeAssets: false,
                assetCacheBuster: false,
                raw: 'Sass::Script::Number.precision = 10\n'
            },
            dist: {
                options: {
                    generatedImagesDir: '<%= yeoman.dist %>/images/generated'
                }
            },
            server: {
                options: {
                    debugInfo: true
                }
            }
        },

        // Renames files for browser caching purposes
        rev: {
            dist: {
                files: {
                    src: [
                        '<%= yeoman.dist %>/scripts/{,*/}*.js',
                        '<%= yeoman.dist %>/styles/{,*/}*.css',
                        // Temporary measure until image references are removed from angular controller
                        //'<%= yeoman.dist %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}',
                        '<%= yeoman.dist %>/styles/fonts/*'
                    ]
                }
            }
        },

        // Reads HTML for usemin blocks to enable smart builds that automatically
        // concat, minify and revision files. Creates configurations in memory so
        // additional tasks can operate on them
        useminPrepare: {
            html: '<%= yeoman.app %>/index.html',
            options: {
                dest: '<%= yeoman.dist %>',
                flow: {
                    html: {
                        steps: {
                            js: ['concat'],
                            css: ['cssmin']
                        },
                        post: {}
                    }
                }
            }
        },

        // Performs rewrites based on rev and the useminPrepare configuration
        usemin: {
            html: ['<%= yeoman.dist %>/{,*/}*.html'],
            css: ['<%= yeoman.dist %>/styles/{,*/}*.css'],
            options: {
                assetsDirs: ['<%= yeoman.dist %>']
            }
        },

        // The following *-min tasks produce minified files in the dist folder
        // cssmin: {
        //     options: {
        //         relativeTo: '<%= yeoman.dist %>'
        //     }
        // },

        // Changes the icon font path in the scss
        replace: {
            bower_css: {
                src: ['<%= yeoman.dist %>/styles/{,*/}*.css'],
                overwrite: true,
                replacements: [
                    {
                        from: '/bower_components/bootstrap/dist/fonts',
                        to: '/fonts'
                    },
                    {
                        from: '/bower_components/bootstrap-sass-official/assets/fonts/bootstrap',
                        to: '/fonts'
                    }
                ]
            }
        },

        imagemin: {
            dist: {
                files: [{
                    expand: true,
                    cwd: '<%= yeoman.app %>/images',
                    src: '{,*/}*.{png,jpg,jpeg,gif}',
                    dest: '<%= yeoman.dist %>/images'
                }]
            }
        },

        svgmin: {
            dist: {
                files: [{
                    expand: true,
                    cwd: '<%= yeoman.app %>/images',
                    src: '{,*/}*.svg',
                    dest: '<%= yeoman.dist %>/images'
                }]
            }
        },

        htmlmin: {
            dist: {
                options: {
                    collapseWhitespace: true,
                    collapseBooleanAttributes: true,
                    removeCommentsFromCDATA: true,
                    removeOptionalTags: true
                },
                files: [{
                    expand: true,
                    cwd: '<%= yeoman.dist %>',
                    src: ['*.html', 'views/{,*/}*.html'],
                    dest: '<%= yeoman.dist %>'
                }]
            }
        },

        ngAnnotate: {
            dist: {
                options: {
                    singleQuotes: true
                },
                files: {
                    '<%= yeoman.dist %>/scripts/scripts.js': [
                        '<%= yeoman.dist %>/scripts/scripts.js'
                    ]
                }
                //files: [{
                //    expand: true,
                //    cwd: '.tmp/concat/scripts',
                //    src: 'scripts.js',
                //    dest: '.tmp/concat/scripts'
                //}]
            }
        },

        // Replace Google CDN references
        cdnify: {
            dist: {
                html: ['<%= yeoman.dist %>/*.html']
            }
        },

        // Copies remaining files to places other tasks can use
        copy: {
            dist: {
                files: [
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= yeoman.app %>',
                        dest: '<%= yeoman.dist %>',
                        src: [
                            '*.{ico,png,txt}',
                            '.htaccess',
                            '*.html',
                            'views/**/*.html',
                            'images/{,*/}*.{webp}',
                            'fonts/*'
                        ]
                    },
                    {
                        expand: true,
                        cwd: '.tmp/images',
                        dest: '<%= yeoman.dist %>/images',
                        src: ['generated/*']
                    },

                    // scripts
                    // remove if using ngmin and uglify
                    {
                        expand: true,
                        cwd: '.tmp/concat/scripts',
                        dest: '<%= yeoman.dist %>/scripts',
                        src: ['*.js']
                    },

                    // fonts
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= yeoman.app %>/bower_components/bootstrap',
                        src: ['fonts/*.*'],
                        dest: '<%= yeoman.dist %>'
                    },
                    {
                        expand: true,
                        dot: true,
                        cwd: '<%= yeoman.app %>/bower_components/components-font-awesome',
                        src: ['fonts/*.*'],
                        dest: '<%= yeoman.dist %>'
                    }
                ]
            },
            styles: {
                expand: true,
                cwd: '<%= yeoman.app %>/styles',
                dest: '.tmp/styles/',
                src: '{,*/}*.css'
            }
        },

        // Run some tasks in parallel to speed up the build process
        concurrent: {
            server: [
                'compass:server'
            ],
            test: [
                'compass'
            ],
            dist: [
                'compass:dist',
                'imagemin',
                'svgmin'
            ]
        },

        // By default, your `index.html`'s <!-- Usemin block --> will take care of
        // minification. These next options are pre-configured if you do not wish
        // to use the Usemin blocks.
        //cssmin: {
        //    dist: {
        //        files: {
        //            '<%= yeoman.dist %>/styles/main.css': [
        //                '.tmp/styles/{,*/}*.css',
        //                '<%= yeoman.app %>/styles/{,*/}*.css'
        //            ]
        //        }
        //    }
        //},

        // minifies our scripts in the distribution folder
        uglify: {
            dist: {
                files: {
                    '<%= yeoman.dist %>/scripts/scripts.js': ['<%= yeoman.dist %>/scripts/scripts.js'],
                    '<%= yeoman.dist %>/scripts/vendor.js': ['<%= yeoman.dist %>/scripts/vendor.js']
                }
            }
        },
        //concat: {
        //    dist: {}
        //},

        // Test settings
        karma: {
            unit: {
                configFile: 'karma.conf.js',
                singleRun: true
            }
        },

        compress: {
            mona: {
                options: {
                    archive: 'mona-client.zip',
                    mode: 'zip'
                },
                files: [
                    {
                        src: '**/*', cwd: 'dist', expand: true
                    }
                ]
            }
        }
    });

    grunt.loadNpmTasks('grunt-contrib-compress');
    grunt.loadNpmTasks('grunt-angular-templates');
    grunt.loadNpmTasks('grunt-wiredep');
    grunt.loadNpmTasks('grunt-ng-annotate');
    /**
     * which server do we want to use for our application
     */
    grunt.registerTask('setServer', function (target) {

        if (target === 'dist') {
            grunt.file.copy('serverDeploy.js', 'app/scripts/server.js');
        }
        else if (target === 'local') {
            grunt.file.copy('local.js', 'app/scripts/server.js');
        }
        else {
            //
        }

    });

    grunt.registerTask('serve', function (target) {
        if (target === 'dist') {
            return grunt.task.run(['build', 'connect:dist:keepalive']);
        }

        grunt.task.run([
            'setServer:local',
            'clean:server',
            'bowerInstall',
            'concurrent:server',
            'autoprefixer',
            'connect:livereload',
            'watch'
        ]);
    });

    grunt.registerTask('server', function (target) {
        grunt.log.warn('The `server` task has been deprecated. Use `grunt serve` to start a server.');
        grunt.task.run(['serve:' + target]);
    });

    grunt.registerTask('test', [
        'clean:server',
        'concurrent:test',
        'autoprefixer',
        'connect:test',
        'karma'
    ]);

    grunt.registerTask('build', [
        'clean:dist',
        'bowerInstall',
        'useminPrepare',
        'concurrent:dist',
        'autoprefixer',
        'concat',
        'ngAnnotate:dist',
        'copy:dist',
        'cdnify',
        'cssmin',
        //'uglify',
        'rev',
        'usemin',
        'htmlmin',
        'replace' // updates the css icon font path
    ]);

    grunt.registerTask('default', [
        //'newer:jshint',
        'test',
        'build'
    ]);

    /**
     * distribution task
     */
    grunt.registerTask('dist', [
        'setServer:dist',
        'build',
        'compress'
    ])
};
