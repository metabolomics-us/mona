// Generated on 2014-05-28 using generator-angular 0.8.0


'use strict';

module.exports = function (grunt) {

    require('load-grunt-tasks')(grunt);
    require('time-grunt')(grunt);

    // for grunt:serve to rewrite URLS and use HTML5 mode
    var serveStatic = require('serve-static');
    var pushState = require('connect-pushstate');

    // Define the configuration for all the tasks
    grunt.initConfig({

        yeoman: {
            app: 'src/main/angularjs',
            dist: 'target/public'
        },

        // watcher for development
        watch: {
            bower: {
                files: ['bower.json']
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
                    'target/.tmpGrunt/styles/{,*/}*.css',
                    '<%= yeoman.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
                ]
            }
        },

        // grunt server for development purpose
        connect: {
            options: {
                port: 9090,
                // Change this to '0.0.0.0' to access the server from outside.
                hostname: '0.0.0.0',
                livereload: 35729,
                middleware: function (connect, options) {
                    var resources = [
                        pushState()
                    ];

                    for (var i = 0; i < options.base.length; i++)
                        resources.push(serveStatic(options.base[i]));

                    return resources;
                }
            },
            livereload: {
                options: {
                    open: true,
                    base: [
                        'target/.tmpGrunt',
                        '<%= yeoman.app %>'
                    ]

                }
            },
            test: {
                options: {
                    port: 9001,
                    base: [
                        'target/.tmpGrunt',
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

        clean: {
            dist: {
                files: [{
                    dot: true,
                    src: [
                        'target/.tmpGrunt',
                        '<%= yeoman.dist %>/*',
                        '!<%= yeoman.dist %>/.git*'
                    ]
                }]
            },
            server: 'target/.tmpGrunt'
        },

        // Add vendor prefixed styles
        autoprefixer: {
            options: {
                browsers: ['last 1 version']
            },
            dist: {
                files: [{
                    expand: true,
                    cwd: 'target/.tmpGrunt/styles/',
                    src: '{,*/}*.css',
                    dest: 'target/.tmpGrunt/styles/'
                }]
            }
        },


        // Compiles Sass to CSS and generates necessary files if requested
        compass: {
            options: {
                sassDir: '<%= yeoman.app %>/styles',
                cssDir: 'target/.tmpGrunt/styles',
                generatedImagesDir: 'target/.tmpGrunt/images/generated',
                imagesDir: '<%= yeoman.app %>/images',
                javascriptsDir: '<%= yeoman.app %>/scripts',
                fontsDir: '<%= yeoman.app %>/styles/fonts',
                importPath: 'src/main/angularjs/bower_components',
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

        // concatinate js and css files from index.html
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
            },
            dev: {

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
                        cwd: 'target/.tmpGrunt/images',
                        dest: '<%= yeoman.dist %>/images',
                        src: ['generated/*']
                    },


                    // remove if using ngmin and uglify for js files
                    {
                        expand: true,
                        cwd: 'target/.tmpGrunt/concat/scripts',
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
                dest: 'target/.tmpGrunt/styles/',
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


        // minifies our scripts in the distribution folder
        uglify: {
            dist: {
                files: {
                    '<%= yeoman.dist %>/scripts/scripts.js': ['<%= yeoman.dist %>/scripts/scripts.js'],
                    '<%= yeoman.dist %>/scripts/vendor.js': ['<%= yeoman.dist %>/scripts/vendor.js']
                }
            }
        },


        // Test settings
        karma: {
            unit: {
                configFile: 'karma.conf.js',
                singleRun: true
            }
        }
    });

    grunt.loadNpmTasks('grunt-angular-templates');
    grunt.loadNpmTasks('grunt-ng-annotate');
    /**
     * which server do we want to use for our application
     */
    grunt.registerTask('setServer', function (target) {

        if (target === 'dist') {
            grunt.file.copy('serverDeploy.js', 'src/main/angularjs/scripts/server.js');
        }
        else if (target === 'local') {
            grunt.file.copy('local.js', 'src/main/angularjs/scripts/server.js');
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
        'useminPrepare',
        'concurrent:dist',
        'autoprefixer',
        'concat',
        'ngAnnotate:dist',
        'copy:dist',
        'cdnify',
        'cssmin',
        'uglify:dist',
        'rev',
        'usemin',
        'htmlmin',
        'replace' // updates the css icon font path
    ]);

    grunt.registerTask('default', [
        'test',
        'build'
    ]);

    /**
     * distribution task
     */
    grunt.registerTask('dist', [
        'setServer:dist',
        'build'
    ])
};
