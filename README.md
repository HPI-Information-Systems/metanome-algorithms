# Metanome Algorithm Repository

This repository contains several data profiling algorithms for the [Metanome platform](https://github.com/HPI-Information-Systems/Metanome). The algorithms have been implemented by students of the [information systems group](https://hpi.de/naumann) at the Hasso-Plattner-Institut ([HPI](http://www.hpi.de)) in the context of the [Metanome project](https://hpi.de/naumann/projects/data-profiling-and-analytics/metanome-data-profiling.html).

## Installation

Before building the algorithms, the following prerequisites need to be installed:

* Java JDK 1.8 or later
* Maven 3.1.0
* Git

Because all profiling algorithms rely on the [Metanome platform](https://github.com/HPI-Information-Systems/Metanome), i.e., they use Metanome as a dependency, this project needs to be installed in the local maven repository first. So please visit the [GitHub-page](https://github.com/HPI-Information-Systems/Metanome), checkout the sources and build them with the following command:
```
.../metanome$ mvn install
```

Then, all algorithms can be built with this command:
```
.../metanomealgorithms$ MAVEN_OPTS="-Xmx1g -Xms20m -Xss10m" mvn clean install

```

Alternatively, you can open the algorithms project in your IDE of choice, specify `-Xmx1g -Xms20m -Xss10m` as build parameters, and run it as `mvn clean install`. 

The build creates one "fatjar" for each algorithm in the repository. After the build succeeded, run either the collect.bat (Windows) or collect.sh (Linux) script to copy all created algorithms into one folder named "_COLLECTION_". Now, you can choose the algorithms you need and copy them over into a Metanome deployment.

## Headless deployment

To run the Metanome algorithms without a full Metanome deployment, consider the [Metanome-cli](https://github.com/sekruse/metanome-cli) project. This project extends the Metanome framework with a command line interface, so you can configure end execute the jars from a shell. If you need to integrate Metanome algorithms into your own projects, the Metanome-cli implementation can serve as a reference on how to add the algorithms into other projects.


