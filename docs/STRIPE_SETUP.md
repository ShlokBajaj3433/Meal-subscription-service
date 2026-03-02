# Stripe Integration Guide

Complete guide for setting up and testing Stripe payment integration in the Meal Subscription Service.

## Table of Contents

- [Overview](#overview)
- [Account Setup](#account-setup)
- [API Keys Configuration](#api-keys-configuration)
- [Webhook Configuration](#webhook-configuration)
- [Testing Payments](#testing-payments)
- [Production Checklist](#production-checklist)
- [Troubleshooting](#troubleshooting)

## Overview

The Meal Subscription Service integrates with Stripe for:

- **Payment Processing**: Credit/debit card payments
- **Subscription Billing**: Recurring payments for meal subscriptions
- **Webhook Events**: Real-time payment notifications
- **Security**: PCI-compliant payment handling (Stripe handles card data)

### Integration Architecture

```
┌─────────────┐                    ┌──────────────────┐
│   Client    │                    │  Meal Service    │
│  (Browser)  │                    │   Application    │
└──────┬──────┘                    └────────┬─────────┘
       │                                    │
       │ 1. Request payment                 │
       ├────────────────────────────────────>
       │                                    │
       │ 2. Create PaymentIntent            │
       │    (server-side)                   │
       │                              ┌─────▼──────┐
       │                              │   Stripe   │
       │                              │    API     │
       │                              └─────┬──────┘
       │ 3. Return client_secret            │
       <────────────────────────────────────┤
       │                                    │
       │ 4. Confirm payment with card       │
       │    details (Stripe.js)             │
       ├────────────────────────────────────┼───────>
       │                                    │       │
       │                                    │   ┌───▼────┐
       │                                    │   │ Process│
       │                                    │   │Payment │
       │                                    │   └───┬────┘
       │ 5. Webhook: payment_intent.succeeded   │
       │    (async notification)            │   │
       │                              <─────────┤
       │                                    │
       │ 6. Update database, send receipt   │
       │                                    │
       <────────────────────────────────────┤
       │                                    │
```

**Key Points**:
- Card details **never** touch your server (handled by Stripe.js)
- Payment confirmation happens client-side
- Webhook ensures payment status is recorded server-side
- Idempotent webhook processing (prevents duplicate charges)

## Account Setup

### 1. Create Stripe Account

1. Go to [stripe.com](https://stripe.com)
2. Click **"Start now"** or **"Sign in"**
3. Complete registration with email and password
4. Verify your email address

### 2. Activate Test Mode

After logging in:

1. Toggle to **"Test mode"** (top right corner, should show orange indicator)
2. Test mode allows development without real money
3. All test transactions use test card numbers

### 3. Business Information (for Production)

Before going live:

1. Navigate to **Settings** → **Business profile**
2. Complete:
   - Legal business name
   - Business address
   - Tax ID (EIN/SSN in US)
   - Bank account for payouts
3. Submit for review (typically 1-3 business days)

## API Keys Configuration

### Obtaining API Keys

#### Test Keys (Development)

1. Go to [Stripe Dashboard](https://dashboard.stripe.com/test/apikeys)
2. Ensure **"Test mode"** is enabled (orange indicator)
3. You'll see:
   - **Publishable key**: `pk_test_...` (safe to expose in client code)
   - **Secret key**: `sk_test_...` (keep confidential, server-only)

#### Live Keys (Production)

1. Toggle to **"Live mode"** (top right)
2. Go to **Developers** → **API keys**
3. You'll see:
   - **Publishable key**: `pk_live_...`
   - **Secret key**: `sk_live_...`

**⚠️ IMPORTANT**: Never commit keys to version control!

### Configure Application

#### Option 1: Environment Variables (Recommended)

Edit your `.env` file:

```env
# Test environment
STRIPE_SECRET_KEY=sk_test_51ABCdef...your_test_key_here
STRIPE_WEBHOOK_SECRET=whsec_...your_webhook_secret_here

# Production environment (separate .env file)
STRIPE_SECRET_KEY=sk_live_51XYZ...your_live_key_here
STRIPE_WEBHOOK_SECRET=whsec_...your_webhook_secret_here
```

#### Option 2: Application Properties

Edit `application-dev.yml` (for local testing only):

```yaml
app:
  stripe:
    secret-key: sk_test_your_key_here
    webhook-secret: whsec_your_secret_here
    currency: usd
```

**⚠️ WARNING**: Don't commit actual keys to git!

### Verify Configuration

Start your application and check logs:

```bash
mvn spring-boot:run

# Look for:
# "Stripe SDK initialised (key prefix: sk_test)"
```

Test with API call:

```bash
curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "planType": "WEEKLY",
    "mealSelections": [
      {"mealId": 1, "deliveryDate": "2026-03-15", "quantity": 2}
    ]
  }'
```

## Webhook Configuration

Webhooks allow Stripe to notify your application about payment events in real-time.

### Why Webhooks?

- **Reliability**: Ensures payment status is recorded even if user closes browser
- **Asynchronous**: Handles events that occur after payment confirmation
- **Security**: Server-to-server communication with signature verification

### Local Development with Stripe CLI

#### Install Stripe CLI

**macOS** (Homebrew):
```bash
brew install stripe/stripe-cli/stripe
```

**Windows** (Scoop):
```bash
scoop bucket add stripe https://github.com/stripe/scoop-stripe-cli.git
scoop install stripe
```

**Linux**:
```bash
wget https://github.com/stripe/stripe-cli/releases/download/v1.19.5/stripe_1.19.5_linux_x86_64.tar.gz
tar -xvf stripe_1.19.5_linux_x86_64.tar.gz
sudo mv stripe /usr/local/bin/
```

#### Login to Stripe

```bash
stripe login
```

Follow the browser authentication flow.

#### Forward Webhooks to Local Server

```bash
# Start your application first
mvn spring-boot:run

# In another terminal, forward webhooks
stripe listen --forward-to localhost:8080/api/v1/payments/webhook
```

You'll see output like:

```
> Ready! Your webhook signing secret is whsec_abc123... (^C to quit)
```

**Copy this webhook secret** and add to your `.env`:

```env
STRIPE_WEBHOOK_SECRET=whsec_abc123...
```

Restart your application to load the new secret.

#### Test Webhook

Send test events:

```bash
# Test successful payment
stripe trigger payment_intent.succeeded

# Test failed payment
stripe trigger payment_intent.payment_failed

# Test subscription invoice
stripe trigger invoice.payment_succeeded
```

Check your application logs to verify webhooks are processed:

```
INFO  PaymentService : Processing Stripe webhook event: payment_intent.succeeded
INFO  PaymentService : Payment recorded: pi_3ABC123...
```

### Production Webhook Setup

#### Create Webhook Endpoint

1. Go to [Stripe Dashboard](https://dashboard.stripe.com/webhooks)
2. Click **"Add endpoint"**
3. Enter your endpoint URL:
   ```
   https://yourdomain.com/api/v1/payments/webhook
   ```
4. Select events to listen for:
   - ✅ `payment_intent.succeeded`
   - ✅ `payment_intent.payment_failed`
   - ✅ `invoice.payment_succeeded`
   - ✅ `invoice.payment_failed`
5. Click **"Add endpoint"**

#### Get Webhook Signing Secret

1. Click on the newly created endpoint
2. Copy the **"Signing secret"** (starts with `whsec_`)
3. Add to production environment variables:

```env
STRIPE_WEBHOOK_SECRET=whsec_production_secret_here
```

#### Verify Webhook

1. Click **"Send test webhook"** in Stripe dashboard
2. Select `payment_intent.succeeded`
3. Click **"Send test event"**
4. Check **"Event log"** tab for response

Expected:
- ✅ Status: 200 OK
- ✅ Response: `{"received":true}`

### Webhook Security

The application verifies webhook signatures using HMAC-SHA256:

```java
@PostMapping("/webhook")
public ResponseEntity<Map<String, Boolean>> handleWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader) {
    
    try {
        // Verify signature (prevents spoofing)
        Event event = Webhook.constructEvent(
            payload, 
            sigHeader, 
            webhookSecret
        );
        
        // Signature valid, process event
        paymentService.processWebhookEvent(event);
        
        return ResponseEntity.ok(Map.of("received", true));
        
    } catch (SignatureVerificationException e) {
        log.error("Invalid Stripe signature: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
```

**Security Features**:
- ✅ Signature verification ensures requests are from Stripe
- ✅ Idempotency prevents duplicate processing
- ✅ Event type validation
- ✅ Raw payload preservation (required for signature check)

## Testing Payments

### Test Card Numbers

Stripe provides test cards that simulate different scenarios:

| Card Number | Scenario |
|-------------|----------|
| `4242 4242 4242 4242` | ✅ Successful payment |
| `4000 0000 0000 9995` | ❌ Declined (insufficient funds) |
| `4000 0000 0000 0002` | ❌ Declined (card declined) |
| `4000 0025 0000 3155` | ✅ Requires authentication (3D Secure) |
| `4000 0000 0000 0069` | ❌ Card expired |
| `4000 0000 0000 0127` | ❌ Incorrect CVC |

**Additional details** (use any values):
- **Expiry**: Any future date (e.g., `12/30`)
- **CVC**: Any 3 digits (e.g., `123`)
- **ZIP**: Any 5 digits (e.g., `12345`)

### Manual Testing Flow

#### 1. Create User and Login

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "SecurePass123"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePass123"
  }'

# Save the JWT token from response
TOKEN="eyJhbGc..."
```

#### 2. Create Subscription

```bash
curl -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "planType": "WEEKLY",
    "mealSelections": [
      {
        "mealId": 1,
        "deliveryDate": "2026-03-15",
        "quantity": 2
      }
    ]
  }'
```

This creates a Stripe PaymentIntent and subscription.

#### 3. Simulate Payment (via Stripe CLI)

```bash
# Get the payment intent ID from subscription response
# Then simulate successful payment

stripe trigger payment_intent.succeeded \
  --override payment_intent:id=pi_your_payment_intent_id
```

#### 4. Verify Payment in Database

```bash
curl http://localhost:8080/api/v1/payments \
  -H "Authorization: Bearer $TOKEN"
```

Should show payment with `status: "SUCCEEDED"`.

### Frontend Testing (Browser)

If you have a frontend integration:

1. Navigate to subscription page
2. Fill out meal selections
3. Enter test card: `4242 4242 4242 4242`
4. Submit payment
5. Wait for success confirmation
6. Verify webhook processed (check server logs)

### Testing Different Scenarios

#### Test Failed Payment

```bash
# Create subscription, then trigger failure
stripe trigger payment_intent.payment_failed \
  --override payment_intent:id=pi_your_payment_intent_id
```

Verify `status: "FAILED"` in database.

#### Test Subscription Renewal

```bash
# Simulate monthly subscription invoice paid
stripe trigger invoice.payment_succeeded
```

#### Test Refund

From Stripe Dashboard:
1. Go to **Payments**
2. Click on a payment
3. Click **"Refund"**
4. Webhook will update payment status to `REFUNDED`

## Production Checklist

Before going live with Stripe:

### Account Verification

- [ ] Business information completed in Stripe
- [ ] Bank account added and verified (for payouts)
- [ ] Tax information submitted (if required)
- [ ] Account activated by Stripe

### Keys & Security

- [ ] Live API keys configured in production environment
- [ ] Keys stored in secure environment variables (NOT in code)
- [ ] Webhook signing secret configured
- [ ] HTTPS enforced on production domain
- [ ] Webhook endpoint accessible over HTTPS
- [ ] Webhook signature verification enabled

### Testing

- [ ] All payment flows tested in live mode (with real card)
- [ ] Webhook processing verified in production
- [ ] Failed payment handling tested
- [ ] Refund flow tested
- [ ] Error handling for edge cases tested

### Compliance

- [ ] Terms of service include payment/refund policies
- [ ] Privacy policy mentions payment processing
- [ ] PCI compliance understood (Stripe handles card data)
- [ ] GDPR/data protection considerations reviewed

### Monitoring

- [ ] Stripe Dashboard notifications enabled
- [ ] Application logging for payment events
- [ ] Error alerts configured (Sentry, Datadog, etc.)
- [ ] Webhook failure notifications set up

### Documentation

- [ ] Customer support trained on payment issues
- [ ] Refund policy documented
- [ ] Payment dispute process defined
- [ ] Troubleshooting guide created

## Troubleshooting

### Issue: "Invalid API Key"

**Symptom**: Errors when calling Stripe API

**Causes**:
- Wrong API key (using test key in live mode or vice versa)
- Key not set in environment variables
- Typo in key

**Solution**:
```bash
# Verify key in .env
cat .env | grep STRIPE_SECRET_KEY

# Check application logs
# Should see: "Stripe SDK initialised (key prefix: sk_test)"

# Test key manually
stripe --api-key YOUR_KEY payment_intents list --limit 1
```

### Issue: "Webhook Signature Verification Failed"

**Symptom**: 400 Bad Request on webhook endpoint

**Causes**:
- Wrong webhook secret
- Request not from Stripe (spoofed)
- Request body modified before verification

**Solution**:
```bash
# Verify webhook secret matches Stripe dashboard
# For local testing, use Stripe CLI secret

# Check logs for detailed error
# PaymentController should log: "Invalid Stripe signature"

# Test with Stripe CLI
stripe listen --forward-to localhost:8080/api/v1/payments/webhook
stripe trigger payment_intent.succeeded
```

### Issue: "Payment Succeeds but Not Recorded in Database"

**Symptom**: Payment shows in Stripe but not in application

**Causes**:
- Webhook not configured
- Webhook failing (500 error)
- Database error during processing

**Solution**:
```bash
# Check Stripe Dashboard → Webhooks → Event log
# Look for failed attempts (red icon)

# Check application logs for errors
grep "ERROR.*webhook" logs/application.log

# Manually replay event from Stripe Dashboard
# Go to event → Click "Resend event"
```

### Issue: "Duplicate Payment Records"

**Symptom**: Same payment appears multiple times

**Causes**:
- Webhook retries (Stripe retries failed webhooks)
- Idempotency check not working

**Solution**:

Check constraint:
```sql
-- Should have unique constraint
SELECT constraint_name, constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'payments' 
  AND constraint_type = 'UNIQUE';
```

Ensure `stripe_payment_intent_id` is unique.

### Issue: "Test Cards Not Working"

**Symptom**: Test card numbers declined in test mode

**Causes**:
- Using production keys instead of test keys
- Invalid card number
- Stripe.js not loaded

**Solution**:
```bash
# Verify test mode
curl -X GET https://api.stripe.com/v1/charges \
  -u sk_test_YOUR_KEY:

# Should return data, not "Invalid API Key provided"

# Ensure using correct test card
# 4242 4242 4242 4242 (16 digits, all 4s and 2s)
```

### Issue: "Webhook Not Receiving Events"

**Symptom**: No webhook calls after payment

**Causes**:
- Webhook URL incorrect
- Firewall blocking Stripe IPs
- HTTPS certificate invalid
- Application not running

**Solution**:
```bash
# Test endpoint accessibility
curl -X POST https://yourdomain.com/api/v1/payments/webhook \
  -H "Content-Type: application/json" \
  -d '{"test": true}'

# Should NOT return connection error (400 is OK)

# Check Stripe Dashboard → Webhooks → Attempts
# Look for timeout or connection errors

# For local testing, use Stripe CLI
stripe listen --forward-to localhost:8080/api/v1/payments/webhook
```

### Getting Help

1. **Stripe Documentation**: [stripe.com/docs](https://stripe.com/docs)
2. **Stripe Support**: Dashboard → "?" icon → Contact support
3. **Stack Overflow**: Tag `stripe-payments`
4. **Application Logs**: Check `logs/application.log` for errors
5. **GitHub Issues**: Open issue with error details

---

## Additional Resources

- **Stripe API Reference**: https://stripe.com/docs/api
- **Stripe Testing Guide**: https://stripe.com/docs/testing
- **Webhook Event Types**: https://stripe.com/docs/api/events/types
- **Security Best Practices**: https://stripe.com/docs/security/guide
- **PCI Compliance**: https://stripe.com/docs/security

---

**Stripe Integration Complete! 💳**

For questions about the application integration, see [CONTRIBUTING.md](CONTRIBUTING.md) or open an issue.
