# edison-aws

Collection of independent libraries on top of [edison-microservice](https://github.com/otto-de/edison-microservice) to provide a faster setup of aws microservices.

> "I never did anything by accident, nor did any of my inventions come by accident; they came by work." - Thomas Edison

## Status

[![release](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-aws-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.otto.edison/edison-aws-core)
[![license](https://img.shields.io/github/license/otto-de/edison-aws.svg)](./LICENSE)

UNSTABLE - EARLY IN DEVELOPMENT

Have a look at the [release notes](CHANGELOG.md) for details about updates and changes.

## About

This project contains a number of independent libraries on top of [edison-microservice](https://github.com/otto-de/edison-microservice) to provide a faster setup of aws microservices.
The libraries are used in different projects at OTTO.
It's purpose is to provide a common implementation for cross-cutting requirements like:

* Get property files from S3 to load secret properties
* Reporting of metrics to cloudWatch

... plus all the features of [edison-microservice](https://github.com/otto-de/edison-microservice).

## Documentation

Edison Modules:
* [`edison-aws-core`](edison-aws-core/README.md): TODO.
* [`edison-aws-metrics`](edison-aws-metrics/README.md): TODO.
* [`edison-aws-config`](edison-aws-config/README.md): TODO.
* [`edison-aws-s3`](edison-aws-s3/README.md): TODO.

## Setup

Make sure you have Java 13 and gradle 6.x installed on your computer.

### Testing

In order to execute the tests, you need to have a running [Localstack](https://github.com/localstack/localstack)!
    
    gradle startLocalStack

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
