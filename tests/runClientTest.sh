#!/bin/bash

## ===========================
## JXIO Verification Client Test
## ===========================
## This script compiles and runs the JXIO verification test of the session client.

echo -e "\n******************* JXIO Verification Client Test *******************"

# Get Running Directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $DIR

# Exporting LD library
export LD_LIBRARY_PATH=$DIR
echo -e "\nLD library is: $LD_LIBRARY_PATH\n"

# Checks to see if JAVA path is valid
if [ ! -e ${JAVA_HOME} ]; then
        echo -e "\nError: JAVA not found!\n"
        exit 1
fi

# Remove cores
rm -rf /.autodirect/mtrswgwork/UDA/core_files_TEMP/*

# Check arguments
if [ -z $1 ]; then
	 echo -e "\nError: Missig first parameter. Should be a hostname or IP.\n"
fi
if [ -z $2 ]; then
	 echo -e "\nError: Missig second parameter. Should be a port.\n"
fi
if [ -z $3 ]; then
	 echo -e "\nError: Missig third parameter. Should be a a test number.\n"
fi
# Get machine IP
IP=$1
# Configure Port
PORT=$2
# Get Test Number
TEST_NUMBER=$3

# Compile
echo -e "\nCompiling JAVA files....\n"
javac -cp "../bin/jxio.jar:../lib/commons-logging.jar" ./com/mellanox/jxio/tests/*.java
if [[ $? != 0 ]] ; then
    exit 1
fi

# Run the tests
export LD_LIBRARY_PATH=$DIR
echo -e "\nRunning client test....\n"
java -Dlog4j.configuration=com/mellanox/jxio/tests/log4j.properties.jxiotest -cp "../bin/jxio.jar:../lib/commons-logging.jar:../lib/log4j-1.2.15.jar:." com.mellanox.jxio.tests.TestClient $IP $PORT $TEST_NUMBER