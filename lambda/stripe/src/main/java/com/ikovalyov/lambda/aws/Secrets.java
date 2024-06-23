package com.ikovalyov.lambda.aws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@ApplicationScoped
public class Secrets {
    public static final String STRIPE_API_KEY_SECRET_ID = "STRIPE_API_KEY_SECRET_ID";
    public static final String STRIPE_PUBLISHABLE_KEY_SECRET_ID = "STRIPE_PUBLISHABLE_KEY_SECRET_ID";
    public static final String STRIPE_STATIC_ROOT_SECRET_ID = "STRIPE_STATIC_ROOT_SECRET_ID";
    public static final String STRIPE_SERVICE_ROOT_URL_SECRET_ID = "STRIPE_SERVICE_ROOT_URL_SECRET_ID";

    @ConfigProperty(name = STRIPE_API_KEY_SECRET_ID)
    public String stripeApiKeySecretId;

    @ConfigProperty(name = STRIPE_PUBLISHABLE_KEY_SECRET_ID)
    public String stripePublishableKeySecretId;

    @ConfigProperty(name = STRIPE_STATIC_ROOT_SECRET_ID)
    public String stripeStaticRootKeySecretId;

    @ConfigProperty(name = STRIPE_SERVICE_ROOT_URL_SECRET_ID)
    public String stripeServiceRootUrlSecretId;

    @Inject
    SecretsManagerAsyncClient asyncClient;

    private String stripeApiKey = null;

    private String stripePublishableKey = null;

    private String stripeStaticRootPath = null;

    private String stripeServiceRootUrl = null;

    private void setStripeApiKey(String newKey) {
        stripeApiKey = newKey;
    }

    private void setStripePublishableKey(String newKey) {
        stripePublishableKey = newKey;
    }

    private void setStripeStaticRootPath(String newRootPath) {
        stripeStaticRootPath = newRootPath;
    }

    private void setStripeServiceRootUrl(String newStripeServiceRootUrl) {
        stripeServiceRootUrl = newStripeServiceRootUrl;
    }

    public CompletableFuture<String> getStripeApiKey() {
        var result = new CompletableFuture<String>();
        if (stripeApiKey == null) {
            return initSecretRequestFuture(result, stripeApiKeySecretId, this::setStripeApiKey);
        } else {
            result.complete(stripeApiKey);
            return result;
        }
    }

    public CompletableFuture<String> getStripePublishableKey() {
        var result = new CompletableFuture<String>();
        if (stripePublishableKey == null) {
            return initSecretRequestFuture(result, stripePublishableKeySecretId, this::setStripePublishableKey);
        } else {
            result.complete(stripePublishableKey);
        }
        return result;
    }

    public CompletableFuture<String> getStripeStaticRootPath() {
        var result = new CompletableFuture<String>();
        if (stripeStaticRootPath == null) {
            return initSecretRequestFuture(result, stripeStaticRootKeySecretId, this::setStripeStaticRootPath);
        } else {
            result.complete(stripeStaticRootPath);
        }
        return result;
    }

    public CompletableFuture<String> getStripeServiceRootUrl() {
        var result = new CompletableFuture<String>();
        if (stripeServiceRootUrl == null) {
            return initSecretRequestFuture(result, stripeServiceRootUrlSecretId, this::setStripeServiceRootUrl);
        } else {
            result.complete(stripeServiceRootUrl);
        }
        return result;
    }

    private CompletableFuture<String> initSecretRequestFuture(CompletableFuture<String> result, String awsSecretId, Consumer<String> consumer) {
        asyncClient.getSecretValue(GetSecretValueRequest.builder()
                        .secretId(awsSecretId)
                        .build())
                .whenComplete((ok, error) -> {
                    if (error != null) {
                        result.completeExceptionally(error);
                    } else {
                        consumer.accept(ok.secretString());
                        result.complete(ok.secretString());
                    }
                });
        return result;
    }
}
