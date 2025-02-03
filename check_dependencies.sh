#!/bin/bash

# Check if sbt is installed
if ! command -v sbt &> /dev/null
then
    echo "sbt could not be found, installing..."
    # Install sbt
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x99E82A75642AC823" | sudo apt-key add
    sudo apt-get update
    sudo apt-get install sbt
else
    echo "sbt is already installed"
fi

# Check if GraphViz is installed
if ! command -v dot &> /dev/null
then
    echo "GraphViz could not be found, installing..."
    sudo apt-get install graphviz
else
    echo "GraphViz is already installed"
fi

# Check if Scala is installed
if ! command -v scala &> /dev/null
then
    echo "Scala could not be found, installing..."
    sudo apt-get install scala
else
    echo "Scala is already installed"
fi

echo "All dependencies are installed"