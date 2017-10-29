# Edison Param Store Properties

Use SSM Parameter Store to load properties or secrets

## Usage

Enable by setting property:
`edison.aws.paramstore-properties.enabled = true`

Set path to load properties from:
`edison.aws.paramstore-properties.path = /path/to/properties`

Make sure to have the correct policy to edit ssm parameter store properties.
[paramstore docs](http://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-paramstore.html)

## Put Parameters AWS CLI
 
```
aws ssm put-parameter --name "/path/to/properties/some.secret.application.property" --value "secret" --type SecureString
aws ssm get-parameters-by-path --path "/path/to/properties"
```
