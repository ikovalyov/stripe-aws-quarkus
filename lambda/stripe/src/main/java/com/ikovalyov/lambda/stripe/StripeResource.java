package com.ikovalyov.lambda.stripe;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.ikovalyov.lambda.aws.Secrets;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Path("/stripe")
public class StripeResource {
    @Inject
    public Secrets secrets;

    private final Logger logger = LoggerFactory.getLogger(StripeResource.class.getName());

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String helloGet(@Context APIGatewayV2HTTPEvent event) throws ExecutionException, InterruptedException {
        String paymentAmount = event.getQueryStringParameters().get("paymentAmount");
        String paymentDescription = event.getQueryStringParameters().get("paymentDescription");
        CompletableFuture<String> publishableKeyFuture = secrets.getStripePublishableKey();
        CompletableFuture<String> stripeStaticFilesRootPathFuture = secrets.getStripeStaticRootPath();
        String publishableKey = publishableKeyFuture.get();
        String stripeStaticFilesRootPath = stripeStaticFilesRootPathFuture.get();
        return String.format("""
                <html>
                    <head>
                        <script src="https://js.stripe.com/v3/"></script>
                        <script>
                            var paymentDescription="%s";
                            var paymentAmount="%s";
                            var publishableKey="%s";
                        </script>
                        <script src="%s/index.js"></script>
                    </head>
                    <body>
                        <div id="checkout">
                        <!-- Checkout will insert the payment form here -->
                        </div>
                    </body>
                </html>
                """, paymentDescription, paymentAmount, publishableKey, stripeStaticFilesRootPath)  ;
    }

    @GET()
    @Path("payment-description")
    @Produces(MediaType.TEXT_HTML)
    public String paymentDescriptionAction() throws ExecutionException, InterruptedException {
        String stripeStaticFilesRootPath = secrets.getStripeStaticRootPath().get();
        return String.format("""
                <html>
                <head>
                    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css">
                    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css">
                    <title>Donate form</title>
                </head>
                <body>
                <div class="container">
                    <div class="py-5 text-center">
                            <img class="d-block mx-auto mb-4" src="%s/logo.png" alt="" width="72" height="72">
                            <h2>Donations form</h2>
                            <p class="lead">Please enter payment description and amount and click Submit in the form below.</p>
                    </div>
                    <form action="/stripe" method="GET">
                      <div class="form-group row">
                        <label class="col-4 col-form-label" for="paymentDescription">Payment description</label>
                        <div class="col-8">
                          <div class="input-group">
                            <div class="input-group-prepend">
                              <div class="input-group-text">
                                <i class="fa fa-file-text-o"></i>
                              </div>
                            </div>
                            <input id="paymentDescription" name="paymentDescription" type="text" class="form-control" required="required">
                          </div>
                        </div>
                      </div>
                      <div class="form-group row">
                        <label for="paymentAmount" class="col-4 col-form-label">Payment amount</label>
                        <div class="col-8">
                          <div class="input-group">
                            <div class="input-group-prepend">
                              <div class="input-group-text">
                                <i class="fa fa-money"></i>
                              </div>
                            </div>
                            <input id="paymentAmount" name="paymentAmount" type="text" class="form-control">
                          </div>
                        </div>
                      </div>
                      <div class="form-group row">
                        <div class="offset-4 col-8">
                          <button name="submit" type="submit" class="btn btn-primary">Submit</button>
                        </div>
                      </div>
                    </form>
                </div>
                </body>
                </html>
                """, stripeStaticFilesRootPath);
    }

    @GET()
    @Path("return")
    @Produces(MediaType.TEXT_HTML)
    public String returnAction() {
        return """
                <html>
                    <head>
                    </head>
                    <body>
                        <h1>Thank you</h1>
                    </body>
                </html>
                """ ;
    }

    @POST
    @Path("create-checkout-session")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public String getClientSecret(@Context APIGatewayV2HTTPEvent event) throws StripeException, ExecutionException, InterruptedException {
        String paymentAmount = event.getQueryStringParameters().get("paymentAmount");
        String paymentDescription = event.getQueryStringParameters().get("paymentDescription");
        logger.info("Received request, Description is {}, Amount is {}", paymentDescription, paymentAmount);
        float paymentAmountFl = Float.parseFloat(paymentAmount)*100;
        // Set your secret key. Remember to switch to your live secret key in production.
        // See your keys here: https://dashboard.stripe.com/apikeys
        CompletableFuture<String> stripeApiKeyFuture = secrets.getStripeApiKey();
        CompletableFuture<String> stripeServiceRootUrlFuture = secrets.getStripeStaticRootPath();
        String stripeApiKey = stripeApiKeyFuture.get();
        String stripeServiceRootUrl = stripeServiceRootUrlFuture.get();
        StripeClient client = new StripeClient(stripeApiKey);

        SessionCreateParams params = SessionCreateParams.builder()
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPriceData(
                                                SessionCreateParams.LineItem.PriceData.builder()
                                                        .setCurrency("eur")
                                                        .setUnitAmount((long)paymentAmountFl)
                                                        .setTaxBehavior(SessionCreateParams.LineItem.PriceData.TaxBehavior.EXCLUSIVE)
                                                        .setProductData(
                                                                new SessionCreateParams.LineItem.PriceData.ProductData.Builder()
                                                                        .setName(paymentDescription)
                                                                        .build()
                                                        )
                                                        .build()
                                        )
                                        .setQuantity(1L)
                                        .build()
                        )
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                        .setReturnUrl(String.format("%s/stripe/return", stripeServiceRootUrl))
                        .build();

        Session session = client.checkout().sessions().create(params);

        return String.format("""
                {
                    "clientSecret":"%s"
                }
                """, session.getClientSecret());
    }

}
