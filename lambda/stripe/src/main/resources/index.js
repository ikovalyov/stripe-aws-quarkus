// Initialize Stripe.js
const stripe = Stripe(publishableKey);

initialize();

// Fetch Checkout Session and retrieve the client secret
async function initialize() {
    const fetchClientSecret = async () => {
        const response = await fetch("stripe/create-checkout-session?paymentDescription="+encodeURIComponent(paymentDescription)+"&paymentAmount="+encodeURIComponent(paymentAmount), {
            method: "POST",
        });
        const { clientSecret } = await response.json();
        return clientSecret;
    };

    // Initialize Checkout
    const checkout = await stripe.initEmbeddedCheckout({
        fetchClientSecret,
    });

    // Mount Checkout
    checkout.mount('#checkout');
}