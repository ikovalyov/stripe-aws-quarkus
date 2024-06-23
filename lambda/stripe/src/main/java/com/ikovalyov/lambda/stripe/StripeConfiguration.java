package com.ikovalyov.lambda.stripe;

import com.stripe.model.StripeError;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiRequest;
import com.stripe.param.checkout.SessionCreateParams;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets={
        StripeError.class,
        ApiRequest.class,
        SessionCreateParams.class,
        SessionCreateParams.LineItem.class,
        Session.class,
        Session.AutomaticTax.class
})
public class StripeConfiguration {
}
