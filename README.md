# SGoV
This repository contains web services for SGoV workspace tools:
- user authentication
- workspace validation

## Workspace Validation
There is a web service for validating Semantic Government Vocabulary (SGoV). It includes checking consistency and compliance of glossaries and models according to predefined rules, as defined in 
https://github.com/opendata-mvcr/sgov-validator.

The project consists of the following modules:
- validator - the actual validation logic. It consists of SHACL rules and a simple wrapper to evaluate them over a Jena model.
- server - web service for workspace validation. It connects to a remote SPARQL endpoint and validates vocabulary data in a
 workspaces by loading them first in memory and running the validator.

## Proposing validation changes
The best way to propose validation rule changes is to:

1. create a new issue using template 'Validation Change Request' template. Describe (i) why you request to change validation (to add a new rule/change an existing one),
(ii) describe use-case/example. The issue is given a number <ISSUE>
2. create a new branch of the form '<ISSUE>-<short-description>'
3. implement the changes and create a pull request. If the pull request passes all automatic checks, ask one of the maintainers to approve.
4. Once approved, merge PR into master

## Building
You need to have GitHub token in order to build this project (as there is a GitHub Packages dependency). Check build.gradle for details. 
Once You generate PAT on GitHub, You can specify it using `gpr.user` and `gpr.key` in your `~/.gradle/gradle.properties`.
You can specify host/port of the validated RDF4J repository in `server/src/main/resources/application.yml`

    gradle bootRun

## Creating an executable JAR
Create the JAR using

    gradle bootJar

Now you can run the server like

    java -jar server/build/libs/sgov-server.jar

## IDE configuration

### Intellij Idea

In settings, configure Gradle to use JDK 11.

To manage source code it is recommended to install plugins:
 - [Lombok](https://plugins.jetbrains.com/plugin/6317-lombok) 
 - [CheckStyle](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea)
 
Static code analysis rules are defined in file `./config/checkstyle/checkstyle.xml`. In order to set up this checkstyle
in Intellij Idea IDE following steps are recommended:
1) Install checkstyle plugin CheckStyle-IDEA
2) Import the file into project checkstyle scheme using 
`Settings/Editor/Code Style/Java/Scheme/Import Scheme/Checkstyle configuration`.
3) Configure the plugin by adding `checkstyle.xml` into `Settings/Tools/Checkstyle/Configuration file`
 
-----
Tento repozitář vznikl v rámci projektu OPZ č. [CZ.03.4.74/0.0/0.0/15_025/0013983](https://esf2014.esfcr.cz/PublicPortal/Views/Projekty/Public/ProjektDetailPublicPage.aspx?action=get&datovySkladId=F5E162B2-15EC-4BBE-9ABD-066388F3D412).
![Evropská unie - Evropský sociální fond - Operační program Zaměstnanost](https://data.gov.cz/images/ozp_logo_cz.jpg)
