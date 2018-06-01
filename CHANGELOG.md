# Release Notes

## 0.3.6-SNAPSHOT
* introduce prefetching s3 togglz state repository

## 0.3.5
* Use preview 9 of AWS SDK 2
* introduce cache-ttl for s3 togglz state repository

## 0.3.4
* [edison-aws-config] Add paging again, because `recursive` is only loading the properties recursive from the path and is not doing the paging.

## 0.3.3
* Added paging for getParametersByPath, because it normally returns a maximum of 10 parameters. Also added unit tests for ParamStorePropertySourcePostProcessor.

## 0.3.2
* [edison-aws-config] add property `edison.aws.config.paramstore.addWithLowestPrecedence` to configure the precedence for ssm properties.

## 0.3.1
* Add support for dimensions when writing metrics

## 0.3.0
* **Breaking change:**
  Remove AwsLdapConfiguration bean - 
  Set the new property `edison.ldap.encryptionType` in edison-microservice (1.2.8 or later) to `SSL` to get the same behavior 
* Remove dependency to edison-core

## 0.2.9
* Update aws metrics to aws sdk 2.0.0
* Configure allowed metrics with wildcards
* Use localstack in S3ServiceIntegrationTest

## 0.2.8
* Fix sending empty attribute values to dynamodb. Caused strange exceptions.

## 0.2.7
* Replace empty Strings with null for DynamoDB

## 0.2.6
* Make appending log messages in DynamoDB Job Repository more efficient

## 0.2.5
* Fix bug with enabled jobs in Job Meta Repository

## 0.2.4
* Fix naming issues in dynamo db module

## 0.2.3
* Use preview 7 of AWS SDK 2
* dynamodb job repositories for edison jobs

## 0.2.2
* Refactor aws-metrics: do not use local aws credentials provider  - use provider from aws-core

## 0.2.1
* Refactor S3Service.download(..) to not use StreamingResponseHandler to simplify testing  

## 0.1.0-SNAPSHOT

* Add edisons SSLLdapConnectionFactory config
* Migrated to AWS SDK 2.0.0-preview-4
* Renamed artifacts to edison-aws-*
* Unified properties: prefix is now edison.aws.*
* Using localstack instead of local installation of dynamo db
* Removed unused (and broken) DynamoJobRepository and DynamoTogglzRepository

## 0.0.1-SNAPSHOT

**Initial Release**
