# Serverless Stripe payment gateway

The goal of this project is to create simple easy-to-deploy stripe payment gateway using quarkus and AWS.

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

It uses AWS CDK to deploy application. You can read more about it here https://aws.amazon.com/cdk/ .

Static files (`index.js` and `logo.png`) should be uploaded to a S3 bucket and be publicly available.

Project is run using native executable.

## Requirements

It depends on some secrets in AWS Secrets Manager https://aws.amazon.com/secrets-manager/

Before deploying application please create such secrets:

| Secret name                   | Description                                            |
|-------------------------------|--------------------------------------------------------|
| prod/stripe/api-key           | The Api key of your stripe application                 |
| prod/stripe/publishable-key   | The Publishable key of your stripe application         |
| prod/stripe/static-root       | The root url of location where static files are stored |
| prod/stripe/service-root-url  | The root url of the service deployment                 |

## Build + Deploy

You should have initialised CDK environment in your console and available docker to build a native executable. 

Build and deployment is executed as a single operation. Run this command in the `app` folder

```shell
cdk deploy
```

## Related Guides

- AWS Lambda HTTP ([guide](https://quarkus.io/guides/aws-lambda-http)): Allow applications written for a servlet
  container to run in AWS Lambda
