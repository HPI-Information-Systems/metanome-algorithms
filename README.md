# Metanome Algorithm Repository

This repository contains several data profiling algorithms for the [Metanome platform](https://github.com/HPI-Information-Systems/Metanome). The algorithms have been implemented by students of the [information systems group](https://hpi.de/naumann) at the Hasso-Plattner-Institut ([HPI](http://www.hpi.de)) in the context of the [Metanome project](https://hpi.de/naumann/projects/data-profiling-and-analytics/metanome-data-profiling.html). All algorithms in the repository can be executed with the Metanome platform. Further, data profiling algorithms that have also been developed in the Metanome project but that are not (yet) compatible with the platform (because they are, for instance, distributed data profiling algorithms) are contained in the following repositories:

* https://github.com/srfc/raida
* https://github.com/alpreu/spin

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
.../metanome-algorithms$ MAVEN_OPTS="-Xmx1g -Xms20m -Xss10m" mvn clean install

```

Alternatively, you can open the algorithms project in your IDE of choice, specify `-Xmx1g -Xms20m -Xss10m` as build parameters, and run it as `mvn clean install`. 

The build creates one "fatjar" for each algorithm in the repository. After the build succeeded, run either the collect.bat (Windows) or collect.sh (Linux) script to copy all created algorithms into one folder named "_COLLECTION_". Now, you can choose the algorithms you need and copy them over into a Metanome deployment.

## Headless deployment

To run the Metanome algorithms without a full Metanome deployment, consider the [Metanome-cli](https://github.com/sekruse/metanome-cli) project. This project extends the Metanome framework with a command line interface, so you can configure end execute the jars from a shell. If you need to integrate Metanome algorithms into your own projects, the Metanome-cli implementation can serve as a reference on how to add the algorithms into other projects.

## Adding new algorithms

All algorithms in this repository are continuously maintained and upgraded to newer versions with every release of the Metanome framework. To add a new algorithm to the repository, the following steps should be followed:

1. Copy the algorithm maven project into a subdirectory of the algorithms repository.
1. Use the following pattern for the naming of your algorithm artifact:

    ```
      <groupId>de.metanome.algorithms.[algorithm-name-lowercase]</groupId>
      <artifactId>[algorithm-name]</artifactId>
      <packaging>jar</packaging>
      <name>[algorithm-name]</name>
    ```
1. Set the parent pom to the root pom using the root's current version:

    ```
      <parent>
        <groupId>de.metanome.algorithms</groupId>
        <artifactId>algorithms</artifactId>
        <version>1.1-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
      </parent>
    ```
1. Add the algorithm project as a module to the root pom of the reposotory.
1. Remove the version tags of your project and all dependencies to Metanome subprojects; these versions are inherited from the root pom.
1. Remove unnecessary repository information, e.g., all repositories that are defined in root/parent should not be duplicated.
1. Add a copy command for the jar file of the new algorithm to the collect.bat and collect.sh scripts.
