#!/bin/bash
. ./storm_env.sh

printFormat (){
	echo "Format:     ./squall_local.sh MODE CONFIG_PATH"
	echo "             MODE: SQL (default) or PLAN_RUNNER"
}

# Check the number of arguments
if [[ $# -lt 1 || $# -gt 2 ]]; then
	echo "ERROR:: Improper number of arguments!"
	printFormat
	exit 1
fi

# Reading input paramters
MODE=$1
if [[ "$MODE" != "PLAN_RUNNER" && "$MODE" != "SQL" ]]; then
	MODE=SQL	
else
   shift
fi

# Main class
if [ "$MODE" == "PLAN_RUNNER" ]; then
	MAIN_CLASS=ch.epfl.data.squall.main.Main
else
	MAIN_CLASS=ch.epfl.data.squall.api.sql.main.ParserMain
fi

# Set config file path and check if it exist
if [[ $# -lt 1 ]]; then
	echo "ERROR:: Missing configuration file path!"
	printFormat
	exit 1
fi
CONFIG_PATH=$1
if ! [ -f $CONFIG_PATH ]; then
	echo "ERROR:: File $CONFIG_PATH does not exist! Please specify a valid configuration file!"
	exit 1
fi

# Running
java -Xmx128m -cp ../squall-core/target/squall-0.2.0.jar:../squall-core/target/squall-dependencies-0.2.0.jar:\
../target/scala-2.11/squall-heron-assembly-0.1-SNAPSHOT-deps.jar:../target/scala-2.11/squall-heron_2.11-0.1-SNAPSHOT.jar:\
../squall-functional/target/squall-frontend-0.2.0.jar:./squall-functional/target/squall-frontend-dependencies-0.2.0.jar:\
../squall-functional/target/macros/squall-frontend-macros-0.2.0.jar:../squall-functional/target/macros/squall-frontend-macros-dependencies-0.2.0.jar $MAIN_CLASS $CONFIG_PATH


# This did not work (java.lang.ClassNotFoundException: backtype.storm.Config)
#heron submit local ../squall-core/target/squall-0.2.0.jar $MAIN_CLASS $CONFIG_PATH
