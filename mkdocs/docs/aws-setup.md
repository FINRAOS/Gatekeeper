## Setting your AWS environment

We currently only have a local instance of gatekeeper running, if you wanted to use it with your AWS Environment some setup needs to be done.

1. Ensure that your EC2 instances all install (and run) the AWS SSM agent
    - without the SSM agent running gatekeeper cannot create/delete temp users
2. Stage SSM docs to create/delete users for Linux/Windows (more details below)
3. Set up IAM roles for gatekeeper
    - an IAM role that the gatekeeper app runs with
        - this account needs to be able to assume to the cross-account gatekeeper role
    - a "cross-account" IAM role that gatekeeper assumes to perform AWS tasks
        - this needs to allow the gatekeeper role to assume this IAM role
        - this needs to be set up on every account Gatekeeper will support 
        - below is the priveliges the cross account IAM role for gatekeeper should have:
```json
{
            "Version": "2012-10-17",
            "Statement": [
                {
                    "Sid": "Update",
                    "Effect": "Allow",
                    "Action": [
                        "ec2:Describe*",
                        "rds:Describe*",
                        "rds:ListTagsForResource",
                        "ssm:Describe*",
                        "ssm:Get*",
                        "ssm:List*",
                        "ssm:CancelCommand"
                    ],
                    "Resource": [
                        "*"
                    ]
                },
                {
                    "Effect": "Allow",
                    "Action": [
                        "ssm:SendCommand"
                    ],
                    "Resource": [
                        "arn:aws:ssm:*:*:document/GK*",
                        "arn:aws:ec2:*:*:*"
                    ]
                }
            ]
        }
```
4. for RDS databases a gatekeeper account needs to be set up on every RDS instance you intend to use Gatekeeper with
    - for postgres please see [aws/rds/postgres_setup.sql](aws/rds/postgres_setup.sql)
    - for mysql please see [aws/rds/mysql_setup.sql](aws/rds/mysql_setup.sql)      

## Configuring

Gatekeeper requires some configuration for it to run in your environment, see below for all of the supported configuration parameters

### Global

These parameters are used by both the EC2 and RDS Gatekeeeper services 

#### Authorization
Currently Gatekeeper only supports authorization through LDAP, the application expects authentication to be done via SSO, and looks for the username in a header.

|Property                                               | Description                                                             | Type                                                          |
|-------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------|
| gatekeeper.auth.userIdHeader                           | The header in which gatekeeper looks to extract the authenticated user | string                                                        | 
| gatekeeper.auth.ldap.isActiveDirectory                 | Whether your LDAP server is Microsoft Active Directory or not (Nested groups are not supported with Non-Active Directory LDAP servers) | boolean
| gatekeeper.auth.ldap.objectClass                       | The Object class to look for users with (ex. posixAccount, person, user) | string 
| gatekeeper.auth.ldap.usersBase                         | The base in which the Users are stored on the LDAP Server (e.g. ou=Users,dc=example,dc=org) | string
| gatekeeper.auth.ldap.userDn                            | The DN for the user that gatekeeper connects as to query ldap (e.g. cn=admin,dc=example,dc=org ) | string
| gatekeeper.auth.ldap.userPassword                      | The password to the ldap user | string
| gatekeeper.auth.ldap.server                            | The domain of the LDAP server that gatekeeper should connect to | string
| gatekeeper.auth.ldap.base                              | The base that LDAP calls will be made off of (e.g. dc=example,dc=org) | string    
| gatekeeper.auth.ldap.usersCnAttribute                  | The cn | string
| gatekeeper.auth.ldap.usersIdAttribute                  | The uid | string
| gatekeeper.auth.ldap.usersNameAttribute                | The name | string
| gatekeeper.auth.ldap.usersEmailAttribute               | The email | string
| gatekeeper.auth.ldap.usersDnAttribute                  | The dn | string
| gatekeeper.auth.ldap.pattern                           | A regular expression that is used to extract group names from the LDAP results. The regular expression must have exactly one capture ( e.g developer_([A-Za-z0-9]+)_dev) pattern | string
| gatekeeper.auth.ldap.groupsBase                        | The base where your groups are stored on your organization's LDAP server (e.g. ou=groups) | string 

#### AWS
|Property | Description | Type |
|---------|-------------|------|
| gatekeeper.accountInfoEndpoint | The Endpoint gatekeeper calls to fetch the account data for all of your aws accounts | string
| gatekeeper.accountInfoUri | The URI where gatekeeper can call your account Info service. (Defaults to "accounts") | string
| gatekeeper.aws.proxyHost | (Optional) The Proxy Host. If you are not behind a proxy you can ignore this | string
| gatekeeper.aws.proxyPort | (Optional) The Proxy Port. If you are not behind a proxy you can ignore this | integer
| gatekeeper.aws.roleToAssume | The AWS IAM role that Gatekeeper will assume to interact with AWS (e.g. Xacnt_APP_GATEKEEPER)   | string 

#### EMAIL
Gatekeeper primarily communicates out temporary credentials via email, these are the properties gatekeeper requires for email

| Property | Description | Type|
|----------|-------------|------|
| gatekeeper.email.host | The host of your mail server | string
| gatekeeper.email.port | The port that it runs on | integer
| gatekeeper.email.from | The name of the Sender for Gatekeeper emails. | string
| gatekeeper.email.team | The team that will be maintaining gatekeeper on your environment. | string 
| gatekeeper.email.approverEmails | The email address for the group who will be handling approvals | string
| gatekeeper.email.opsEmails | The email address for gatekeeper to reach out to the Ops team | string

#### DATABASE
| Property | Description | Type|
|----------|-------------|------|
| gatekeeper.db.url | The Postgres Database URL to connect to | String
| gatekeeper.db.port | The Port of the database | integer
| gatekeeper.db.database | The name of the database | string
| gatekeeper.db.ssl | Whether to enable SSL or not | boolean
| gatekeeper.db.sslMode | The SSL mode to use | string
| gatekeeper.db.sslCert | The SSL certificate location to use ( we provide the RDS cert in the container ) | string
| gatekeeper.db.user | The DB user to log in as | string
| gatekeeper.db.password | The DB user password | string

### EC2
These configurations are specific to Gatekeeper EC2

#### DATABASE
| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.db.schema | The schema in which gatekeeper operates its tables | gatekeeper_ec2

#### AUTHORIZATION
| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.auth.approverGroup | The LDAP group containing the gatekeeper approvers | string
| gatekeeper.auth.supportGroup | The LDAP group containing the gatekeeper support staff | string

#### APPROVALS

You will set these once for each environment you have in your company

Here's an example (taken from [config/ec2_service_config.env](config/ec2_service_config.env))

```dotenv
gatekeeper.approvalThreshold.dev.dev=45
gatekeeper.approvalThreshold.dev.qa=44
gatekeeper.approvalThreshold.dev.prod=-1
gatekeeper.approvalThreshold.dev.test=4

gatekeeper.approvalThreshold.support.dev=42
gatekeeper.approvalThreshold.support.qa=41
gatekeeper.approvalThreshold.support.prod=2
gatekeeper.approvalThreshold.support.test=5
```

This tells Gatekeeper the following:
1. For a developer role the maximum hours the user can request is:
    - <= 45 for dev environment
    - <= 44 for qa environment
    - approval is **always** required for prod
    - <= 4 for test
2. For a support role the maximum hours the user can request without requiring approval is:
   - <= 42 hours for dev environment
   - <= 41 for qa environment
   - <= 2 for prod environment
   - <= 5 for test environment


| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.approvalThreshold.dev.<environment> | The approval threshold for a given environment for the dev role | integer
| gatekeeper.approvalThreshold.support.<environment> | The approval threshold for a given environment for the support role | integer