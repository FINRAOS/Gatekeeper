<img src="/img/logo.png" alt="drawing" width="500px"/>

[![CircleCI](https://circleci.com/gh/FINRAOS/Gatekeeper/tree/master.svg?style=svg)](https://circleci.com/gh/FINRAOS/Gatekeeper/tree/master) [![Join the chat at https://gitter.im/FINRAOS/Gatekeeper](https://badges.gitter.im/FINRAOS/Gatekeeper.svg)](https://gitter.im/FINRAOS/Gatekeeper?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## What is Gatekeeper?
Gatekeeper is self-serviced web application allowing users to make requests for temporary access to EC2/RDS instances running in AWS and gain access instantly.

## How does Gatekeeper work?
### EC2
For EC2 Gatekeeper leverages AWS SSM (Amazon Simple System Manager) to create (and remove) temporary users on EC2 Instances 
### RDS
For RDS Gatekeeper leverages the AWS RDS API to be able to connect to supported RDS instances and generate users with generic sql queries specific to the engine of the RDS Instance. Currently gatekeeper supports mysql and postgres

## Gatekeeper at AWS Re:Invent 2017
See our blog post [here](https://aws.amazon.com/blogs/mt/finra-gatekeeper-amazon-ec2-access-management-system-using-amazon-ec2-systems-manager/) for more information

We also had a demo of the application in action at AWS Re:invent 2017, to see that you can find the following links below:

### Full Talk:
<a href="https://www.youtube.com/watch?feature=player_embedded&v=VJf1i_b-2Kc&t=1904" target="_blank"><img src="http://img.youtube.com/vi/VJf1i_b-2Kc/hqdefault.jpg" alt="Gatekeeper @ Re:Invent 2017" width="480" height="360" border="10" /></a> 

### Demo:
<a href="https://www.youtube.com/watch?feature=player_embedded&v=VJf1i_b-2Kc&t=44m21s" target="_blank"><img src="http://img.youtube.com/vi/VJf1i_b-2Kc/hqdefault.jpg" alt="Gatekeeper @ Re:Invent 2017" width="480" height="360" border="10" /></a> 

## Why Gatekeeper?
In a transient environment where application instances are constantly being torn down / spun up managing user access gets complicated. Gatekeeper resolves this by automating the creation of that user, and making sure that the user is only valid for a temporary time period. 

Since access is automated and on a temporary basis, using Gatekeeper can reduce the amount of permanent users that get set up on an instance, allowing your resources to be more secure.

Gatekeeper also stores and logs all of the access requests making user access fully auditable. 

## Local Setup

We are initally providing a local run of the gatekeeper application, in the coming months we will provide more details on getting the Gatekeeper Application deployed into AWS.

### Prerequisites

Gatekeeper requires the following tools to be installed and on your $PATH variable:

1. Java 8+
2. Maven 3+
3. NPM 3+
4. Docker

### Mac + Linux environments (1-Command Setup)
Currently we provide a shell script that builds all of the app code / containers that comprise Gatekeeper. Currently we only support this for Linux and Mac environments. All you have to do is provide the following environment variables

1. **AWS_DIRECTORY**: the location on your computer with your AWS Credentials usually it's <USER_HOME>/.aws
2. **http_proxy**: if behind a corporate proxy set the proxy to this variable. (defaults to blank if not provided)

Once you have these set, simply run **start.sh** and the script will build all the gatekeeper code/containers + bring up gatekeeper locally.

Hit gatekeeper at the following locations to see the different User/Roles in action:

https://localhost:443 => Regular User

https://localhost:444 => Ops/Support User

https://localhost:445 => Approver

### Windows 10 users

If you're a windows 10 user we don't have a script for you unfortunately, but that doesn't mean you can't run gatekeeper locally. 

Make sure you have the following environment variables set:

1. **AWS_DIRECTORY**: the location on your computer with your AWS Credentials usually it's <USER_HOME>/.aws
2. **http_proxy**: if behind a corporate proxy set the proxy to this variable. you need to set it to blank if not provided

Run the following commands:

```bash
#cd to containers folder and build the base containers
docker-compose build
#cd to demo-services and build the fake-account-service
mvn clean install
#cd to services directory and build the gatekeeper backend services
mvn clean install
#cd to the ui directory and build the gatekeeper UI code
npm run win-build
#cd to the main directory and build the gatekeeper containers
docker-compose -f local-docker-compose.yml build
#bring the gatekeeper up with the following command
docker-compose -f local-docker-compose.yml up
```
Hit gatekeeper at the following locations to see the different User/Roles in action:

https://localhost:443 => Regular User

https://localhost:444 => Ops/Support User

https://localhost:445 => Approver


## Setting your AWS environment up for Gatekeeper

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
| gatekeeper.aws.proxyHost | (Optional) The Proxy Host. If you are not behind a proxy you can ignore this | string
| gatekeeper.aws.proxyPort | (Optional) The Proxy Port. If you are not behind a proxy you can ignore this | integer
| gatekeeper.aws.roleToAssume | The AWS IAM role that Gatekeeper will assume to interact with AWS (e.g. Xacnt_APP_GATEKEEPER)   | string 

#### AWS ACCOUNTS
These configurations are used by Gatekeeper to manage the data coming from your account service provider.

|Property | Description | Type |
|---------|-------------|------|
| gatekeeper.account.serviceURL | The Endpoint gatekeeper calls to fetch the account data for all of your aws accounts | string
| gatekeeper.account.serviceURI | The URI where gatekeeper can call your account Info service. (Defaults to "accounts") | string
| gatekeeper.account.sdlcOverrides | A Map telling gatekeeper which account SDLC's to override. See the example below as to how this would look | Map<String, String>
| gatekeeper.account.sdlcGrouping | A Map allowing you to control the ordering in which accounts show up in the UI based on SDLC, of not set there will be no ordering by SDLC. See Example for how this looks | Map<String, String>
    
##### SDLC Overrides
You can override the SDLC of a given account by providing either it's name or AWS account id. This is useful if you want to have stricter or even looser approval requiremnts on a specific account in a given sdlc.

```dotenv
    gatekeeper.account.sdlcOverrides.poc=myacc1, 123456789
    gatekeeper.account.sdlcOverrides.test=myacc2
```    

##### SDLC Grouping
The dataset from the account service can get large over time, to control the sorting of these accounts in the UI you may (optionally) specify a grouping to group them up by SDLC. Gatekeeper will sort by the SDLC first and then the given Alias.
If you do not provide a grouping then the groupings will ultimately default to 1 for all accounts, effectively not sorting on the grouping but the alias alone

```dotenv
    gatekeeper.account.sdlcGrouping.dev=1
    gatekeeper.account.sdlcGrouping.qa=2
    gatekeeper.account.sdlcGrouping.prod=3
    gatekeeper.account.sdlcGrouping.poc=4
    gatekeeper.account.sdlcGrouping.test=5
```

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

#### JUSTIFICATION
| Property | Description | Type|
|----------|-------------|------|
| gatekeeper.explanationFieldRequired | Whether to require an explanation when approval is required. | String
| gatekeeper.ticketIdFieldRequired | Whether to require a ticket ID when approval is required. | String
| gatekeeper.ticketIdFieldMessage | Placeholder message in the ticket ID field. Automatically appended with " (Optional)" if gatekeeper.ticketIdFieldRequired is set to false. | String

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

#### AWS

### Configuring SSM Documents for Gatekeeper EC2

Gatekeeper for EC2 resources requires SSM documents to be staged to AWS before being able to generate a temporary user. There are 4 different documents that Gatekeeper requires you to provide in order to Create and Delete temporary users. These documents are:

1. Create document for Linux 
2. Delete document for Linux
3. Create document for Windows
4. Delete document for Windows

We provide examples of what we are currently using in our environment, you can find these documents located in the **aws/ssm** folder; You ultimately have control of the content in these documents should you have differing requirements in your environment. 

If you decide to write your own document, please keep in mind that gatekeeper will be passing on the following parameters to the documents. 

For create documents (linux):
1. **userName** - Gatekeeper provides The Username for the temporary user. Currently gatekeeper provides this to the SSM document in the form of **gk-$username**
2. **publicKey** - Gatekeeper generates the public key for the user and provides it to the SSM document
3. **executionTimeout** - Gatekeeper provides The maximum time to wait for the script to succeed

For windows gatekeeper functions differently as we do not want to send the user password directly to SSM (these parameters get logged to the AWS console, so the password would be exposed which is insecure), instead we rely on SSM to generate the user password on the Windows box and have the box send the username and password directly to the requestor(s) 

For create documents (windows):
1. **opsEmail** - Gatekeeper provides the email of the operations team so that they are notified if any issue occurs 
2. **teamEmail** - Gatekeeper provides the team email to notify the team that supports gatekeeper if it encounters any issues
3. **userId** - Gatekeeper provides the User Id being added
4. **userName** - Gatekeeper provides the User Name being added
5. **account** - Gatekeeper provides the account the windows instance exists in
6. **mailFrom** - Gatekeeper provides the sender of the email (who users of gatekeeper see the email is from)
7. **smtpServer** - **YOU MUST PROVIDE THIS IN THE TEMPLATE** the SMTP server that will be responsible for relaying the mail
8. **hours** - Gatekeeper provides the amount of time the request is for
9. **accessRequest** - Gatekeeper provides the access request ID 
10. **executionTimeout** - Gatekeeper provides The maximum time to wait for the script to succeed


For delete documents:
1. **userName** - Gatekeeper provides the Username for the temporary user that should be removed.
2. **executionTimeout** - Gatekeeper provides the maximum time to wait for the script to succeed.

For convenience, we have provided SSM scripts with the script part left blank if you need to create your own script to set up a user / remove them. You can learn more about SSM [here](https://docs.aws.amazon.com/systems-manager/latest/userguide/create-ssm-doc.html)

The following are configuration properties which tell gatekeeper which documents to run for which action:

| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.ssmGrantRetryCount | ( Linux + Windows )The amount of times to retry any instances where creation failed (default is 3 tries) | integer
| gatekeeper.ssm.linux.create.documentName | For Linux: The name of the SSM document Gatekeeper will call to create a user | string
| gatekeeper.ssm.linux.create.timeout | For Linux: The Amount of time Gatekeeper should wait for the ssm create call to complete | integer
| gatekeeper.ssm.linux.create.waitInterval | For Linux: The Interval that Gatekeeper polls the SSM create for completion | integer
| gatekeeper.ssm.linux.delete.documentName | For Linux: The name of the SSM document Gatekeeper will call to delete a user | string
| gatekeeper.ssm.linux.delete.timeout | For Linux: The Amount of time Gatekeeper should wait for the ssm delete call to complete | integer 
| gatekeeper.ssm.linux.delete.waitInterval | For Linux: The Interval that Gatekeeper polls the SSM delete for completion | integer
| gatekeeper.ssm.windows.create.documentName | For Windows: The name of the SSM document Gatekeeper will call to create a user | string
| gatekeeper.ssm.windows.create.timeout | For Windows: The Amount of time Gatekeeper should wait for the ssm create call to complete | integer
| gatekeeper.ssm.windows.create.waitInterval | For Windows: The Interval that Gatekeeper polls the SSM create for completion | integer
| gatekeeper.ssm.windows.delete.documentName | For Windows: The name of the SSM document Gatekeeper will call to delete a user
| gatekeeper.ssm.windows.delete.timeout | For Windows: The Amount of time Gatekeeper should wait for the ssm delete call to complete | integer 
| gatekeeper.ssm.windows.delete.waitInterval | The Interval that Gatekeeper polls the SSM delete for completion | integer

### RDS
These configurations are specific to Gatekeeper RDS

| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.db.schema | The schema in which gatekeeper operates its tables  | string
| gatekeeper.requiredSecurityGroups | A comma separated list of the Security Group(s) in which Gatekeeper RDS requires for connectivity | string 
| gatekeeper.rds.postgresMinServerVersion | The minimum postgrs server to use | string 
| gatekeeper.rds.ssl | Whether Gatekeeeper-RDS should use SSL or not to connect | boolean
| gatekeeper.rds.connectTimeout | The timeout (in milliseconds) to wait for a connection | Integer
| gatekeeper.rds.sslMode | The mode of SSL verification to use while Gatekeeper RDS connects to an instance | string
| gatekeeper.rds.sslCert | The SSL Cert to use for gatekeeper to connect (We provide the RDS cert) | string
| gatekeeper.rds.user | The gatekeeper user to log into the RDS instance. | string
| gatekeeper.rds.password | The password for the gatekeeper user | string

| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.auth.approverGroup | The LDAP group containing Gatekeeper Approvers for RDS | string
| gatekeeper.auth.dbaGroupsPattern | A regular expression to extract group(s) of DBAS from ldap groups, requires one capture | string
| gatekeeper.auth.opsGroupsPattern | A regular expression to extract group(s) of Ops Members from ldap groups. requires one capture | string
| gatekeeper.auth.devGroupsPattern | A regular expression to extract group(s) of Dev Members from ldap groups. requires one capture | string

### SNS
Gatekeeper supports the publishing of Approval/Expiration events to an SNS topic, this can be useful if you have other applications that need to react to an event from the gatekeeper service. 

Here's an example of how we are leveraging this feature from our AWS RE:Inforce 2019 session:

<a href="https://www.youtube.com/watch?feature=player_embedded&v=d1V0RNJOFeE" target="_blank"><img src="http://img.youtube.com/vi/d1V0RNJOFeE/hqdefault.jpg" alt="Gatekeeper @ RE:INFORCE 2019" width="480" height="360" border="10" /></a>  

| Property | Description | Type | 
|----------|-------------|------|
| gatekeeper.sns.topicARN | (required) The ARN of the SNS topic that you want gatekeeper to publish updates to. | string
| gatekeeper.sns.retryCount | The number of times gatekeeper will re-try publishing to the given SNS topic  (Defaults to 5) | number
| gatekeeper.sns.retryIntervalMillis | The time in milliseconds to wait before trying again (Defaults to 1000) | number
| gatekeeper.sns.retryIntervalMultiplier | The multiplier in which gatekeeper will apply on subsequent retries (if this is set to 2 then gatekeeper will multiply the retry interval by 2 every time it fails up until the maximum retry count is reached) (Defaults to 1) | number


#### APPROVALS

Like the EC2 service, the RDS approval criteria needs to be set, however this configuration is handled at a more granular level.

Here is an example from [config/rds_service_config.env](config/rds_service_config.env) 

```dotenv
gatekeeper.approvalThreshold.dev.readonly.dev=120
gatekeeper.approvalThreshold.dev.readonly.qa=100
gatekeeper.approvalThreshold.dev.readonly.prod=-1

gatekeeper.approvalThreshold.dev.readonly_confidential.dev=50
gatekeeper.approvalThreshold.dev.readonly_confidential.qa=20
gatekeeper.approvalThreshold.dev.readonly_confidential.prod=-1

gatekeeper.approvalThreshold.dev.datafix.dev=180
gatekeeper.approvalThreshold.dev.datafix.qa=180
gatekeeper.approvalThreshold.dev.datafix.prod=-1

gatekeeper.approvalThreshold.dev.dba.dev=180
gatekeeper.approvalThreshold.dev.dba.qa=2
gatekeeper.approvalThreshold.dev.dba.prod=-1

gatekeeper.approvalThreshold.dev.dba_confidential.dev=-1
gatekeeper.approvalThreshold.dev.dba_confidential.qa=-1
gatekeeper.approvalThreshold.dev.dba_confidential.prod=-1

gatekeeper.approvalThreshold.ops.readonly.dev=-1
gatekeeper.approvalThreshold.ops.readonly.qa=-1
gatekeeper.approvalThreshold.ops.readonly.prod=180

gatekeeper.approvalThreshold.ops.readonly_confidential.dev=75
gatekeeper.approvalThreshold.ops.readonly_confidential.qa=50
gatekeeper.approvalThreshold.ops.readonly_confidential.prod=-1

gatekeeper.approvalThreshold.ops.datafix.dev=-1
gatekeeper.approvalThreshold.ops.datafix.qa=-1
gatekeeper.approvalThreshold.ops.datafix.prod=1

gatekeeper.approvalThreshold.ops.dba.dev=-1
gatekeeper.approvalThreshold.ops.dba.qa=-1
gatekeeper.approvalThreshold.ops.dba.prod=-1

gatekeeper.approvalThreshold.ops.dba_confidential.dev=-1
gatekeeper.approvalThreshold.ops.dba_confidential.qa=-1
gatekeeper.approvalThreshold.ops.dba_confidential.prod=-1

gatekeeper.approvalThreshold.dba.readonly.dev=180
gatekeeper.approvalThreshold.dba.readonly.qa=180
gatekeeper.approvalThreshold.dba.readonly.prod=180

gatekeeper.approvalThreshold.dba.readonly_confidential.dev=100
gatekeeper.approvalThreshold.dba.readonly_confidential.qa=125
gatekeeper.approvalThreshold.dba.readonly_confidential.prod=1

gatekeeper.approvalThreshold.dba.datafix.dev=180
gatekeeper.approvalThreshold.dba.datafix.qa=180
gatekeeper.approvalThreshold.dba.datafix.prod=180

gatekeeper.approvalThreshold.dba.dba.dev=180
gatekeeper.approvalThreshold.dba.dba.qa=180
gatekeeper.approvalThreshold.dba.dba.prod=-1

gatekeeper.approvalThreshold.dba.dba_confidential.dev=180
gatekeeper.approvalThreshold.dba.dba_confidential.qa=180
gatekeeper.approvalThreshold.dba.dba_confidential.prod=-1
```
Here's an explanation of what this configuration translates to in Gatekeeper:

1. For Dev Role 
    - Requesting readonly
        - < 120 days for dev environment
        - < 180 days for qa environment
        - approval is **always** required for prod
    - Requesting readonly_confidential
        - <= 50 days for dev environment
        - <= 20 days for qa environment
        - approval is **always** required for prod
    - Requesting datafix
        - <= 180 days for dev environment
        - <= 180 days for qa environment
        - approval is **always** required for prod
    - Requesting dba
        - <= 180 days for dev environment
        - <= 2 days for qa environment
        - approval is **always** required for prod
    - Requesting dba_confidential
        - approval is **always** required for dev
        - approval is **always** required for qa
        - approval is **always** required for prod
2. For Ops Role
    - Requesting readonly
        - approval is **always** required for dev
        - approval is **always** required for qa
        - <= 180 days for prod
    - Requesting readonly_confidential
        - <= 75 days for dev environment
        - <= 50 days for qa environment
        - approval is **always** required for prod
    - Requesting datafix
        - approval is **always** required for dev
        - approval is **always** required for qa
        - <= 1 days for prod
    - Requesting dba
        - approval is **always** required for dev
        - approval is **always** required for qa
        - approval is **always** required for prod
    - Requesting dba_confidential
        - approval is **always** required for dev
        - approval is **always** required for qa
        - approval is **always** required for prod
3. For DBA Role
    - Requesting readonly
        - <= 180 days for dev environment
        - <= 180 days for qa environment
        - <= 180 days for prod environment
    - Requesting readonly_confidential
        - <= 100 days for dev environment
        - <= 125 days for qa environment
        - approval is **always** required for prod        
    - Requesting datafix
        - <= 180 days for dev environment
        - <= 180 days for qa environment
        - <= 180 days for prod environment
    - Requesting dba
        - <= 180 days for dev environment
        - <= 180 days for qa environment
        - approval is **always** required for prod
    - Requesting dba_confidential
        - <= 180 days for dev environment
        - <= 180 days for qa environment
        - approval is **always** required for prod

You can see the format for these properties below:

| Property | Description | Type |
|----------|-------------|------|
| gatekeeper.approvalThreshold.dev.readonly.<environment>| The threshold in which devs can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.dev.readonly_confidential.<environment>| The threshold in which devs can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.dev.datafix.<environment>| The threshold in which devs can request datafix access before requiring approval | integer
| gatekeeper.approvalThreshold.dev.dba.<environment>| The threshold in which devs can request dba access before requiring approval | integer
| gatekeeper.approvalThreshold.dev.dba_confidential.<environment>| The threshold in which devs can request dba access before requiring approval | integer
| gatekeeper.approvalThreshold.ops.readonly.<environment>| The threshold in which ops can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.ops.readonly_confidential.<environment>| The threshold in which ops can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.ops.datafix.<environment>| The threshold in which ops can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.ops.dba.<environment>| The threshold in which ops can request dba access before requiring approval  | integer
| gatekeeper.approvalThreshold.ops.dba_confidential.<environment>| The threshold in which ops can request dba access before requiring approval  | integer
| gatekeeper.approvalThreshold.dba.readonly.<environment>| The threshold in which dbas can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.dba.readonly_confidential.<environment>| The threshold in which dbas can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.dba.datafix.<environment>| The threshold in which dbas can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.dba.dba.<environment>| The threshold in which dbas can request readonly access before requiring approval | integer
| gatekeeper.approvalThreshold.dba.dba_confidential.<environment>| The threshold in which dbas can request readonly access before requiring approval | integer
| gatekeeper.overridePolicy.maxDays | The maximum amount of time a user can request temporary access for | integer
| gatekeeper.overridePolicy.overrides.<user_role>.<db_role>.<environment> | for a given user role, db role and environment, this Overrides the maximum amount of days a user can request temporary access for | integer
