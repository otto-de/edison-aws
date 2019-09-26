# Edison AWS Metrics

Write metrics to CloudWatch

## Usage

Enable by setting property:
`edison.aws.metrics.cloudwatch.enabled = true`

Set namespace:
`edison.aws.metrics.cloudwatch.namespace = someNamespace`

Set dimensions:
`edison.aws.metrics.cloudwatch.dimensions.someName = someValue`
`edison.aws.metrics.cloudwatch.dimensions.someOtherName = someOtherValue`

Set allowedmetrics:
`edison.aws.metrics.cloudwatch.allowedmetrics = someAllowedMetric`
`edison.aws.metrics.cloudwatch.allowedmetrics = someOtherAllowedMetric`