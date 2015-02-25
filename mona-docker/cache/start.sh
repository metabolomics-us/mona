#!/bin/sh
#generate our upstream file

echo "
        upstream backends {
                server gose.fiehnlab.ucdavis.edu:10000;
        }

" > /etc/nginx/conf.d/upstream.conf

#start nginx

nginx
