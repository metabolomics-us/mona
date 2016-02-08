#!/bin/bash

# RabbitMQ server
if [ ! -d /data/rabbitmq ]; then
	echo "Initializing RabbitMQ storage..."

	mkdir -p /data/rabbitmq
	chown -R rabbitmq:rabbitmq /data/rabbitmq/
fi 

echo "Starting RabbitMQ..."
/usr/sbin/rabbitmq-server > /data/rabbitmq/rabbitmq.log &


# PostgreSQL server
# If database data does not exist in the shared volume, copy the base database
if [ ! -f /data/postgresql/PG_VERSION ]; then
	echo "Initializing database..."

	mkdir -p /data/postgresql
	cp -r /var/lib/postgresql/9.5/main/* /data/postgresql/

	chmod 700 /data/postgresql/
	chown -R postgres:postgres /data/postgresql/
fi

service postgresql start


# MoNA client
echo "Starting NGINX..."
service nginx start

# MoNA server
echo "Starting MoNA server..."
cd /opt/jetty

java -XX:MaxPermSize=1024m -server -Xmx32328m -jar start.jar "jetty.home=/opt/jetty"