var gulp = require('gulp'),
    bower = require('gulp-bower'),
    usemin = require('gulp-usemin'),
    jade = require('gulp-jade'),
    csso = require('gulp-csso'),
    uglify = require('gulp-uglify');

var uglifyEnabled = true;

var scalaJsType = uglifyEnabled ? 'opt' : 'fastopt';

var outputDir = './out';

gulp.task('copy', ['bower'], function() {
    gulp.src('./assets/favicon.ico')
        .pipe(gulp.dest(outputDir));
  
    return gulp.src(['./assets/bower/bootstrap/fonts/*'])
        .pipe(gulp.dest(outputDir + '/fonts/'));
});

gulp.task('images', function() {
    return gulp.src('./assets/img/**/*')
        .pipe(gulp.dest(outputDir + '/img/'));
});

gulp.task('stuff', ['images', 'copy']);

gulp.task('compile', ['bower', 'stuff'], function() {
    return gulp.src(['./assets/template/*.jade', '!./assets/template/_*.jade'])
        .pipe(jade({
            pretty: true
        }))
        .pipe(usemin({
            assetsDir: 'assets',
            css: [csso(), 'concat'],
            js: uglifyEnabled ? [uglify(), 'concat'] : ['concat']
        }))
        .pipe(gulp.dest(outputDir));
});

gulp.task('bower', function() {
    return bower()
        .pipe(gulp.dest('./assets/bower/'))
});

gulp.task('watch', ['compile'], function() {
    gulp.watch('../target/scala-2.11/webm-tv-frontend-' + scalaJsType + '.js', ['copy']);

    gulp.watch('bower.json', ['bower']);

    gulp.watch('assets/img/**/*', ['images']);

    gulp.watch(['assets/js/**/*', 'assets/template/**/*', 'assets/css/**/*'], ['compile']);
});

gulp.task('default', ['compile']);