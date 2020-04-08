# SGoV
This repository contains a web service for validating Semantic Government Vocabulary (SGoV). This includes checking consistency and compliance of
glossaries and models according to predefined rules. These rules check:
- glossaries - e.g. "each glossary concept at least one skos:prefLabel"
- models - e.g. OntoUML relationships like "each Role concept must (transitively) inherit from a Kind concept"
- interplay between glossaries and models - e.g. "each glossary concept should be used in the model"

The project consists of the following modules:
- validator - the actual validation logic. It consists of SHACL rules and a simple wrapper to evaluate them over a Jena model.
- server - web service for workspace validation. It connects to a remote SPARQL endpoint and validates vocabulary data in a
 workspaces by loading them first in memory and running the validator.

## Proposing validation changes
The best way to propose validation rule changes is to:

1. create a new issue with the tag 'validation'. Describe (i) why a you request to change validation (to add a new rule/change an existing one),
(ii) describe use-case/example. The issue is given a number <ISSUE>
2. create a new branch of the form '<ISSUE>-<short-description>'
3. implement the changes and create a pull request. If the pull request passes all automatic checks, ask one of the maintainers to approve.
4. Once approved, merge PR into master

## Building
You can specify host/port of the validated RDF4J repository in `server/src/main/resources/application.yml`

    gradle bootRun

## Creating an executable JAR
Create the JAR using

    gradle bootJar

Now you can run the server like

    java -jar server/build/libs/server-1.0.jar
