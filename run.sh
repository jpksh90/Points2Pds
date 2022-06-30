#Benchmark running script for PDS Pointer Analysis

PAPDS_ROOT=$PWD
VERSION=1.0
JAR_PATH="target/pointerPDS.jar"
MODE="non"

echo "Executable $JAR_PATH"

#Set the timeout duration for the analysis
TIMEOUT=7h

if [ $# -gt 1 ]; then
    echo "Usage: ./run.sh [debug mode]"
    exit 1
fi

if [ $# -eq 1 ]; then
    if [ $1 == "debug" ]; then
        MODE="debug"
    else
        echo "Usage: ./run.sh [debug mode]"
        exit 1
    fi
fi


#SETUP the graph environment
BENCHMARK=("avrora.properties")

#BENCHMARK=("Lindenmayer.properties")
for app in ${BENCHMARK[@]}
do
    echo "=================================================="
    echo "Analysing benchmark $(basename $app .properties)"
    if [ $MODE != "debug" ]; then
        timeout $TIMEOUT java -classpath $PAPDS_ROOT -Xmx8g -jar $PAPDS_ROOT/$JAR_PATH -p properites/$app -a pds
    else
        timeout $TIMEOUT java -classpath $PAPDS_ROOT -Xmx8g -jar $PAPDS_ROOT/$JAR_PATH -p properites/$app -a pds -d
    fi

    if [ $? -eq 124 ]; then
        echo "TIMEDOUT"
    fi
    echo "=================================================="
done
