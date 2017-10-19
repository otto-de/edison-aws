# edison-aws

Collection of independent libraries on top of [edison-microservice](https://github.com/otto-de/edison-microservice) to provide a faster setup of aws microservices.

> "I never did anything by accident, nor did any of my inventions come by accident; they came by work." - Thomas Edison

## Status

[![release](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-core)
[![license](https://img.shields.io/github/license/otto-de/edison-aws.svg)](./LICENSE)

Have a look at the [release notes](CHANGELOG.md) for details about updates and changes.

## About

This project contains a number of independent libraries on top of [edison-microservice](https://github.com/otto-de/edison-microservice) to provide a faster setup of aws microservices.
The libraries are used in different projects at OTTO.
It's purpose is to provide a common implementation for cross-cutting requirements like:

* Support for Dynamodb-based repositories in case you do not like Spring Data
* An optional Dynamodb-based implementation of a JobRepository
* An optional Dynamodb-based implementation of a TogglzRepository
* Get property files from s3 to load secret properties
* Reporting of metrics to cloudWatch

... plus all the features of [edison-microservice](https://github.com/otto-de/edison-microservice).

## Documentation

Edison Modules:
* [`edison-dynamodb`](edison-dynamodb/README.md): TODO.
* [`edison-metrics-cloudwatch`](edison-metrics-cloudwatch/README.md): TODO.
* [`edison-s3-properties`](edison-s3-properties/README.md): TODO.

## Setup

Make sure you have Java 1.8 and gradle 4.x installed on your computer.

### Testing

Test and create coverage report

    gradle check

### Dependency Update

Determine possible dependency updates

    gradle dependencyUpdates -Drevision=release

### Publishing

Publish new releases

    gradle uploadArchives


## Contributing

Have a look at our [contribution guidelines](CONTRIBUTING.md).
