FROM ubuntu
MAINTAINER Gert Wohlgemuth "wohlgemuth@ucdavis.edu"

#nginx - runs on port 80
EXPOSE 80

#update apt-get
RUN apt-get update -y

RUN apt-get -qq update
RUN apt-get -qq upgrade
RUN apt-get -qq install bash

# Install awstats

RUN apt-get install awstats vim -y
RUN apt-get install -y libnet-ip-perl
RUN apt-get install -y libgeo-ipfree-perl

#install nginx
RUN apt-get install -y nginx
RUN rm /etc/nginx/sites-enabled/default

#configure nginx
ADD nginx/logrotate /etc/logrotate.d/nginx
RUN chmod 644 /etc/logrotate.d/nginx
ADD nginx/nginx.conf /etc/nginx/nginx.conf
ADD nginx/cache.conf /etc/nginx/sites-enabled/cache.conf

#awstats configuration
ADD nginx/awtstats.conf /etc/awstats/awstats.client.conf

RUN   echo "\ndaemon off;" >> /etc/nginx/nginx.conf

ADD start.sh /opt/start.sh
RUN chmod +x /opt/start.sh

RUN mkdir /data
ADD www/* /data/

# Run nginx
CMD ["/opt/start.sh"]
