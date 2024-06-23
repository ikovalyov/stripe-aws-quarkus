package my.groupId;

import com.stripe.net.ApiResource;
import com.stripe.param.checkout.SessionCreateParams;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StripeResourceTest {
    private Logger logger = LoggerFactory.getLogger(StripeResourceTest.class.getName());
    @Test
    public void checkSerialisation() {
        SessionCreateParams params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(5L)
                                                .setTaxBehavior(SessionCreateParams.LineItem.PriceData.TaxBehavior.EXCLUSIVE)
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                .setReturnUrl("https://9zqyn70v32.execute-api.eu-central-1.amazonaws.com/stripe/return")
                .build();

        String json = ApiResource.INTERNAL_GSON.toJson(params);
        logger.info(json);
    }
}
