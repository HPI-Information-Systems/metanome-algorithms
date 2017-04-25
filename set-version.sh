#!/bin/bash

if [ $# -eq 0 ]; then
	echo "This script changes the Metanome version and sets the very same version for the here contained algorithms."
	echo "Usage: $0 <Metanome version>"
	exit 1
fi

# Change the metanome.version variable for Metanome dependencies.
basedir="$(cd "$(dirname "$0")"; pwd)"
sed -e "s/<metanome.version>.*<\/metanome.version>/<metanome.version>$1<\/metanome.version>/" "$basedir/pom.xml" > "$basedir/pom.xml.tmp"
mv "$basedir/pom.xml.tmp" "$basedir/pom.xml"

# Update the version of the algorithms.
mvn versions:set "-DnewVersion=$1" -DgenerateBackupPoms=false
