package com.myorg;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration;
import software.amazon.awscdk.services.apigatewayv2.*;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.FunctionProps;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.secretsmanager.ISecret;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.ikovalyov.lambda.aws.Secrets.*;

public class AppStack extends Stack {
    public static final String AWS_SM_STRIPE_API_KEY_NAME = "stripe.api.key.secret-name";
    public static final String AWS_SM_STRIPE_API_KEY_SECRET_ID = "stripe.api.key.secret-id";
    public static final String AWS_SM_STRIPE_PUBLISHABLE_KEY_NAME = "stripe.publishable.key.secret-name";
    public static final String AWS_SM_STRIPE_PUBLISHABLE_KEY_SECRET_ID = "stripe.publishable.key.secret-id";
    public static final String AWS_SM_STRIPE_STATIC_ROOT_KEY_NAME = "stripe.static.root.secret-name";
    public static final String AWS_SM_STRIPE_STATIC_ROOT_KEY_SECRET_ID = "stripe.static.root.secret-id";
    public static final String AWS_SM_STRIPE_SERVICE_ROOT_URL_SECRET_KEY_NAME = "stripe.service.root.url.secret-name";
    public static final String AWS_SM_STRIPE_SERVICE_ROOT_URL_KEY_SECRET_ID = "stripe.service.root.url.secret-id";

    public AppStack(final Construct scope, final String id) throws ConfigurationException {
        this(scope, id, null);
    }

    public AppStack(final Construct scope, final String id, final StackProps props) throws ConfigurationException {
        super(scope, id, props);

        Configurations configs = new Configurations();
        Configuration config = configs.properties(new File("application.properties"));
        ISecret stripeSecretKey = Secret.fromSecretNameV2(this, config.getString(AWS_SM_STRIPE_API_KEY_SECRET_ID), config.getString(AWS_SM_STRIPE_API_KEY_NAME));
        ISecret stripePublishableKey = Secret.fromSecretNameV2(this, config.getString(AWS_SM_STRIPE_PUBLISHABLE_KEY_SECRET_ID), config.getString(AWS_SM_STRIPE_PUBLISHABLE_KEY_NAME));
        ISecret stripeStaticRootKey = Secret.fromSecretNameV2(this, config.getString(AWS_SM_STRIPE_STATIC_ROOT_KEY_SECRET_ID), config.getString(AWS_SM_STRIPE_STATIC_ROOT_KEY_NAME));
        ISecret stripeServiceRootUrlKey = Secret.fromSecretNameV2(this, config.getString(AWS_SM_STRIPE_SERVICE_ROOT_URL_KEY_SECRET_ID), config.getString(AWS_SM_STRIPE_SERVICE_ROOT_URL_SECRET_KEY_NAME));

        // The code that defines your stack goes here
        Function handler = new Function(this, "Lambda", FunctionProps.builder()
                .description("Quarkus graalvm-based stripe payment")
                .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
                .runtime(Runtime.PROVIDED_AL2)
                .environment(Map.of(
                        STRIPE_API_KEY_SECRET_ID, stripeSecretKey.getSecretArn(),
                        STRIPE_PUBLISHABLE_KEY_SECRET_ID, stripePublishableKey.getSecretArn(),
                        STRIPE_STATIC_ROOT_SECRET_ID, stripeStaticRootKey.getSecretArn(),
                        STRIPE_SERVICE_ROOT_URL_SECRET_ID, stripeServiceRootUrlKey.getSecretArn()

                ))
                .code(Code.fromAsset("../lambda/stripe/build/function.zip"))
                .build());

        stripeSecretKey.grantRead(handler);
        stripePublishableKey.grantRead(handler);
        stripeStaticRootKey.grantRead(handler);
        stripeServiceRootUrlKey.grantRead(handler);

        HttpApi httpApi = new HttpApi(this, "MyApi", HttpApiProps.builder()
                .apiName("My API")
                .corsPreflight(CorsPreflightOptions.builder()
                        .allowMethods(
                                List.of(CorsHttpMethod.GET)
                        )
                        .allowOrigins(
                                List.of("*")
                        )
                        .build())
                .build()
        );
        HttpLambdaIntegration templateLambdaIntegration = new HttpLambdaIntegration("TemplateIntegration", handler);

        // Create a resource and method for the API
        httpApi.addRoutes(AddRoutesOptions.builder()
                        .path("/stripe/payment-description")
                        .methods(List.of(HttpMethod.GET))
                        .integration(templateLambdaIntegration)
                        .build()
        );
        httpApi.addRoutes(AddRoutesOptions.builder()
                        .path("/hello")
                        .methods(List.of(
                                HttpMethod.GET
                        ))
                        .integration(templateLambdaIntegration)
                .build()
        );
        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/stripe")
                .methods(List.of(
                        HttpMethod.GET
                ))
                .integration(templateLambdaIntegration)
                .build()
        );
        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/stripe/return")
                .methods(List.of(
                        HttpMethod.GET
                ))
                .integration(templateLambdaIntegration)
                .build()
        );
        httpApi.addRoutes(AddRoutesOptions.builder()
                .path("/stripe/create-checkout-session")
                .methods(List.of(
                        HttpMethod.POST
                ))
                .integration(templateLambdaIntegration)
                .build()
        );

        // Output the API endpoint URL
        CfnOutput cfnOutput = new CfnOutput(this, "ApiEndpoint", CfnOutputProps.builder()
                .value(httpApi.getApiEndpoint())
                .build()
        );

        // example resource
        // final Queue queue = Queue.Builder.create(this, "AppQueue")
        //         .visibilityTimeout(Duration.seconds(300))
        //         .build();
    }
}
