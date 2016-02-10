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

	service postgresql start && \
		psql -h localhost -U mona -d mona < mona_db_structure.sql && \
		psql -h localhost -U mona -d mona < mona_admin_account.sql
else
	service postgresql start
fi


# Fix server url in MoNA client
echo "Fixing server url in MoNA web-app..."

if [ -z "$SITE_URL" ]; then
	sed -i "s/mona.fiehnlab.ucdavis.edu/localhost:$SITE_PORT/" /opt/mona/scripts/*.scripts.js
else
	sed -i "s/mona.fiehnlab.ucdavis.edu/$SITE_URL:$SITE_PORT/" /opt/mona/scripts/*.scripts.js
fi


# MoNA client
echo "Starting NGINX..."
service nginx start


# MoNA server
if [ ! -d /data/mona ]; then
	echo "Creating MoNA storage directory..."
	mkdir -p /data/mona
fi

echo "Starting MoNA server..."

cd /opt/jetty
java -XX:MaxPermSize=1024m -server -Xmx32328m -jar start.jar "jetty.home=/opt/jetty"