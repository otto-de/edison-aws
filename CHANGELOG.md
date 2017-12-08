# Release Notes

## 0.2.2
* Refactore aws-metrics: do not use local aws credentials provider  - use provider from aws-core

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
