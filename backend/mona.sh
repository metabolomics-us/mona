#!/bin/bash

function show_help() {
	echo "Invalid option."
	echo ""
	echo "Use:"
	echo "	mona-stack.sh <start | stop <stop options> | test <test options>>"
	echo ""
	echo "stop options:"
	echo "	-k | --keep-data		Keeps the data volume containers shutting down only the service containers."
	echo "	-d | --delete-data		Shuts down all the containers, including the data volume containers."
	echo ""
	echo "test options:"
	echo "	-u | --up				Starts the test environment."
	echo "	-d | --down				Stops the test environment."
	exit 1
}

if [[ $# -gt 0 ]]; then
	key="$1"
	action="$2"
	
	case $key in
		start)
		DOCKER_FILE="dc-up.yml"
		action="up"
		;;
		stop)
		case $action in
			-k|--keep-data)
			DOCKER_FILE="dc-down.yml"
			action="down"
			;;
			-d|--delete-data)
			DOCKER_FILE="dc-up.yml"
			action="down"
			;;
			*)
			show_help
			;;
		esac
		;;
		test)
		DOCKER_FILE="dc-test.yml"
		case $action in
			-u|--up)
			action="up"
			;;
			-d|--down)
			action="down"
			;;
			*)
			show_help
			exit 1
			;;
		esac
		;;
		*)
		show_help
		;;
	esac

	COMMAND="docker-compose -f $DOCKER_FILE $action"
	
	echo $COMMAND
	$($COMMAND)
else
	show_help
	exit 1
fi
