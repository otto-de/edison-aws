# Edison AWS Metrics

Write metrics to CloudWatch

## Usage

Enable by setting property:
`edison.aws.metrics.cloudwatch.enabled = true`

Set namespace:
`edison.aws.metrics.cloudwatch.namespace = someNamespace`

Set dimensions:
`edison.aws.metrics.cloudwatch.dimensions.name = someDimension`
`edison.aws.metrics.cloudwatch.dimensions.value = someDimensionValue`
`edison.aws.metrics.cloudwatch.dimensions.name = someOtherDimension`
`edison.aws.metrics.cloudwatch.dimensions.value = someOtherDimensionValue`

Set allowedMetrics:
`edison.aws.metrics.cloudwatch.allowedMetrics = someAllowedMetric`
`edison.aws.metrics.cloudwatch.allowedMetrics = someOtherAllowedMetric`