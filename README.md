# edison-aws

Collection of independent libraries on top of [edison-microservice](https://github.com/otto-de/edison-microservice) to provide a faster setup of aws microservices.

> "I never did anything by accident, nor did any of my inventions come by accident; they came by work." - Thomas Edison


## Status

[![Next Selected Stories](https://badge.waffle.io/otto-de/edison-aws.svg?label=Ready&title=Selected)](http://waffle.io/otto-de/edison-aws)
[![Active Stories](https://badge.waffle.io/otto-de/edison-aws.svg?label=In%20Progress&title=Doing)](http://waffle.io/otto-de/edison-aws)

[![build](https://travis-ci.org/otto-de/edison-aws.svg)](https://travis-ci.org/otto-de/edison-aws) 
[![codecov](https://codecov.io/gh/otto-de/edison-aws/branch/master/graph/badge.svg)](https://codecov.io/gh/otto-de/edison-aws)
[![dependencies](https://www.versioneye.com/user/projects/58b16b4a7b9e15004a98c400/badge.svg?style=flat)](https://www.versioneye.com/user/projects/58b16b4a7b9e15004a98c400)
[![release](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-core)
[![license](https://img.shields.io/github/license/otto-de/edison-aws.svg)](./LICENSE)

Have a look at the [release notes](CHANGELOG.md) for details about updates and changes.


## About

This project contains a number of independent libraries on top of Spring Boot to provide a faster setup of jvm microservices.
The libraries are used in different projects at OTTO.
It's purpose is to provide a common implementation for cross-cutting requirements like:

* ...
* ...

... plus all the features of [edison-microservice](https://github.com/otto-de/edison-microservice).


## Documentation

Edison Modules:
* [`edison-core`](edison-core/README.md): Main library of Edison microservices.
* [`edison-cache`](edison-cache/README.md): Optional support for Caffeine caches in Edison.
* [`edison-jobs`](edison-jobs/README.md): Optional module providing a simple job library.
* [`edison-mongo`](edison-mongo/README.md): Auto-configuration for MongoDB repositories plus implementation of MongoJobRepository and
 Togglz StateRepository.
* [`edison-dynamodb`](edison-dynamodb/README.md): Auto-configuration for DynamoDB repositories plus implementation of DynamoJobRepository and
 Togglz StateRepository.
* [`edison-togglz`](edison-togglz/README.md): Optional support for feature toggles for Edison microservices based on [Togglz](https://www.togglz.org/).
* `edison-testsupport`: Test support for feature toggles plus utilities.

Examples:
* [`example-status`](examples/example-status): Service only relying on `edison-core` to show the usage of health and status features. 
* [`example-metrics`](examples/example-metrics): Service that is using edison-core metrics.
* [`example-jobs`](examples/example-jobs): Edison service using edison-jobs to run background tasks. 
* [`example-togglz`](examples/example-togglz): Example using `edison-togglz´ to implement feature toggles.
* [`example-togglz-mongo`](examples/example-togglz-mongo): Same `edison-toggz`, but with a MongoDB configuration to auto-configure persistence of 
feature toggles.


## Setup

Make sure you have Java 1.8 and gradle 3.x installed on your computer.

### Testing

Test and create coverage report

    gradle check

### Dependency Update

Determine possible dependency updates

    gradle dependencyUpdates -Drevision=release

### Publishing

Publish new releases

    gradle uploadArchives


## Examples

There are a few examples that may help you to start your first microservice based
on Edison and Spring Boot. Because Spring Boot itself has some complexity, it is
recommended to first read it's documentation before starting with Edison.

The examples can be started with gradle:

    gradle examples:example-status:bootRun
    gradle examples:example-metrics:bootRun
    gradle examples:example-jobs:bootRun
    gradle examples:example-togglz:bootRun
    gradle examples:example-togglz-mongo:bootRun

Open in your browser [http://localhost:8080/](http://localhost:8080/)

*Note:* Every example is configured to use port 8080, so make sure to run only one example at a time or to reconfigure
the ports.


## Contributing

Have a look at our [contribution guidelines](CONTRIBUTING.md).
