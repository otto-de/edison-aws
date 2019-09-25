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

Set allowedMetrics:
`edison.aws.metrics.cloudwatch.allowedMetrics = someAllowedMetric`
`edison.aws.metrics.cloudwatch.allowedMetrics = someOtherAllowedMetric`