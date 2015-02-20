#!/bin/sh
#generate our upstream file

echo "
        upstream backends {
                server trashcan.fiehnlab.ucdavis.edu:8080;
        }

" > /etc/nginx/conf.d/upstream.conf

#start nginx

nginx
