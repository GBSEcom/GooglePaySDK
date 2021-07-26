/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firstdata.firstapi.googlepaysampleapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


public class CheckoutActivity extends Activity {
    // Arbitrarily-picked result code.
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    private static final int PAYMENT_FACLITATOR_CODE = 2;

    private PaymentsClient mPaymentsClient;

    private View mPwgButton;
    private TextView mPwgStatusText;
    private ToggleButton mpur;
    private ToggleButton mauth;
    private Button mPayFac;
    private EditText mTextAmount;
    private EditText mCount;
    private Switch sw;
    private Spinner env;
    private EditText mItem;
    //FD
    private String mEnv;
    private String type;
    private String cur;
    private String price;
    private String count;
    private boolean finalShipment;
    private boolean clicked;

    //Payment Facilitator

    private String externalMid;
    private String payFacId;
    private String sid;
    private String name;
    private String mcc;
    private String legalName;
    private String timezone;
    private String company;
    private String add1;
    private String add2;
    private String city;
    private String region;
    private String zip;
    private String country;
    private String dType;
    private String num;
    private String mid;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        //Amount
        mTextAmount = findViewById(R.id.editText2);
        mItem = findViewById(R.id.editText);

        sw = findViewById(R.id.switch1);
        mCount = findViewById(R.id.editText3);
        mpur = findViewById(R.id.toggleButton);
        mauth = findViewById(R.id.toggleButton2);

        mpur.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mpur.isChecked()) {
                    type = "WalletSaleTransaction";
                    mCount.setFocusable(false);
                    Toast.makeText(getApplicationContext(), "Disabled split shipment please continue to payment", Toast.LENGTH_SHORT).show();
                    count = null;
                }
                else {
                    type=null;
                    mCount.setFocusableInTouchMode(true);
                    Toast.makeText(getApplicationContext(), "Split shipment enabled", Toast.LENGTH_SHORT).show();
                }
            }

        });

        mauth.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(mauth.isChecked()) {
                    type = "WalletPreAuthTransaction";
                    mCount.setFocusableInTouchMode(true);
                }

            }
        });

        mPayFac = findViewById(R.id.button2);
        mPayFac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPayfac();
                clicked = true;
            }
        });

        mPwgButton = findViewById(R.id.pwg_button);
        mPwgStatusText = findViewById(R.id.pwg_status);

        mPwgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPayment(view);
            }
        });

        // It's recommended to create the PaymentsClient object inside of the onCreate method.
        mPaymentsClient = PaymentsUtil.createPaymentsClient(this);
        //mEnv="QA";
        checkIsReadyToPay();
        setPwgAvailable(true);
    }



    public void openPayfac(){
        Intent intent = new Intent(this, Payment_Fac.class);
        startActivityForResult(intent,PAYMENT_FACLITATOR_CODE);
    }


    private void checkIsReadyToPay() {
        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        PaymentsUtil.isReadyToPay(mPaymentsClient).addOnCompleteListener(
                new OnCompleteListener<Boolean>() {
                    public void onComplete(Task<Boolean> task) {
                        try {
                            boolean result = task.getResult(ApiException.class);
                            setPwgAvailable(result);
                        } catch (ApiException exception) {
                            // Process error
                            Log.w("isReadyToPay failed", exception);
                        }
                    }
                });
    }

    private void setPwgAvailable(boolean available) {
        // If isReadyToPay returned true, show the button and hide the "checking" text. Otherwise,
        // notify the user that GooglePay is not available.
        // Please adjust to fit in with your current user flow. You are not required to explicitly
        // let the user know if isReadyToPay returns false.
        if (available) {
            mPwgStatusText.setVisibility(View.GONE);
            mPwgButton.setVisibility(View.VISIBLE);
        } else {
            mPwgStatusText.setText(R.string.pwg_status_unavailable);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:

                        PaymentData paymentData = PaymentData.getFromIntent(data);

                        handlePaymentSuccess(paymentData);
                        break;
                    case Activity.RESULT_CANCELED:
                        // Nothing to here normally - the user simply cancelled without selecting a
                        // payment method.
                        Toast.makeText(this, "In cancelled!!", Toast.LENGTH_LONG).show();
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Toast.makeText(this, "Before displaying status!!", Toast.LENGTH_LONG).show();
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        Toast.makeText(this, "after status!!", Toast.LENGTH_LONG).show();
                        handleError(status.getStatusCode());
                        break;

                }
            case PAYMENT_FACLITATOR_CODE:
                if(resultCode==RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    externalMid = bundle.getString("ExtMer");
                    payFacId = bundle.getString("PayFacId");
                    sid = bundle.getString("saleOid");
                    name = bundle.getString("Name");
                    mcc = bundle.getString("mcc");
                    legalName = bundle.getString("legalName");
                    timezone = bundle.getString("timeZone");
                    company = bundle.getString("company");
                    add1 = bundle.getString("add1");
                    add2 = bundle.getString("add2");
                    city = bundle.getString("city");
                    region = bundle.getString("region");
                    zip = bundle.getString("zip");
                    country = bundle.getString("country");
                    dType = bundle.getString("dtype");
                    num = bundle.getString("num");
                    mid = bundle.getString("mid");

                }
                // Re-enables the Pay with Google button.
                mPwgButton.setClickable(true);
                break;
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        // PaymentMethodToken contains the payment information, as well as any additional
        // requested information, such as billing and shipping address.

        PaymentMethodToken token = paymentData.getPaymentMethodToken();

        // getPaymentMethodToken will only return null if PaymentMethodTokenizationParameters was
        // not set in the PaymentRequest.
        if (token != null) {
            String billingName = paymentData.getCardInfo().getBillingAddress().getName();
            Log.d("PaymentData", "PaymentMethodToken received");
            sendRequestToFirstData(paymentData);
        }
    }

    private void handleError(int statusCode) {
        // At this stage, the user has already seen a popup informing them an error occurred.
        // Normally, only logging is required.
        // statusCode will hold the value of any constant from CommonStatusCode or one of the
        // WalletConstants.ERROR_CODE_* constants.
        Toast.makeText(this, "Way too much!!", Toast.LENGTH_LONG).show();
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

    // This method is called when the Pay with Google button is clicked.
    public void requestPayment(View view) {
        // Disables the button to prevent multiple clicks.
        mPwgButton.setClickable(false);

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.

        env  = findViewById(R.id.spinner2);
        mEnv = env.getSelectedItem().toString();

        price = mTextAmount.getText().toString();

        try {
            Double.parseDouble(price);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        Spinner spinner = findViewById(R.id.spinner);
        cur = spinner.getSelectedItem().toString();
        count = mCount.getText().toString();



        if(sw.isChecked()){
            finalShipment = true;
        }
        else
            finalShipment = false;




        TransactionInfo transaction = PaymentsUtil.createTransaction(price);
        PaymentDataRequest request = PaymentsUtil.createPaymentDataRequest(transaction);
        Task<PaymentData> futurePaymentData = mPaymentsClient.loadPaymentData(request);

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.

        AutoResolveHelper.resolveTask(futurePaymentData, this, LOAD_PAYMENT_DATA_REQUEST_CODE);

    }


    public static Intent newIntent(Context ctx, boolean doLogout) {
        Intent intent = new Intent(ctx, CheckoutActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("LOGOUT", doLogout);
        return intent;
    }


    /*******
     *Added for FD processing
     */
    /**
     * Send a request to the First Data server to process the payment. The REST request
     * includes HTTP headers that identify the developer and the merchant issuing the request:
     * <ul>
     * <li>{@code apikey} - identifies the developer</li>
     * <li>{@code token} - identifies the merchant</li>
     * </ul>
     * The values for the two headers are provided by First Data.
     * <p>
     * The token created is extracted from the paymentData object. The token
     * is in JSON format and consists of the following fields:
     * <ul>
     * <li>{@code signedMessage} - the encrypted details of the transaction</li>
     * <li>{@code protocolVersion} - protocolVersion indicationg it is GooglePay Payload
     *</li>
     * <li>{@code signature} - a signature field-signed Message</li>
     * </ul>
     * These items, are used
     * to create the transaction payload. The payload is sent to the First Data servers
     * for execution.
     * </p>
     *
     * @param paymentData PaymentData object
     * //@param env        First Data environment to be used
     */
    public void sendRequestToFirstData(final PaymentData paymentData) {

        try {
            //  Parse the Json token retrieved
            String tokenJSON = paymentData.getPaymentMethodToken().getToken();
            final JSONObject jsonObject = new JSONObject(tokenJSON);

            String signedMessage=jsonObject.getString("signedMessage");//contains encryptedMessage, protocolVersion and Signature
            String protocolVersion=jsonObject.getString("protocolVersion");
            String signature = jsonObject.getString("signature");


            //  Create a First Data Json request
            JSONObject requestPayload = getRequestPayload(signedMessage, protocolVersion, signature);
            final String payloadString = requestPayload.toString();
            final Map<String, String> HMACMap = computeHMAC(payloadString);


            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    getUrl(mEnv),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //  request completed - launch the response activity
                            startResponseActivity("SUCCESS", response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            startResponseActivity("ERROR", formatErrorResponse(error));
                        }
                    }) {

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }

                @Override
                public byte[] getBody() {
                    try {
                        return payloadString.getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headerMap = new HashMap<>(HMACMap);

                    //  First data issued APIKey identifies the developer
                    headerMap.put("Api-Key", EnvData.getProperties(mEnv).getApiKey());

                    //  First data issued token identifies the merchant
                   // headerMap.put("token", EnvData.getProperties(mEnv).getToken());

                    return headerMap;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestQueue queue = Volley.newRequestQueue(CheckoutActivity.this);

            queue.add(request);

        } catch (JSONException e) {
            Toast.makeText(CheckoutActivity.this, "Error parsing JSON payload", Toast.LENGTH_LONG).show();
        }
    }

    private void startResponseActivity(String status, String message) {

       Intent  intent = ResponseActivity.newIntent(CheckoutActivity.this, status, message);

        startActivity(intent);



    }

    /**
     * Convert JSON object into a String.
     * @param jo    JSON object
     * @return  String representation of the object
     */
    private String formatResponse(JSONObject jo) {
        try {
            return jo.toString(2);
        } catch (JSONException e) {
            return "Invalid JSON response";
        }
    }

    private String formatErrorResponse(VolleyError ve) {
        return String.format("Status code = %d%nError message = %s",
                ve.networkResponse.statusCode, new String(ve.networkResponse.data));
    }

    /**
     * Select the appropriate First Data server for the environment.
     *
     * @param env Environment
     * @return URL
     */
    private static String getUrl(String env) {
        return EnvData.getProperties(env).getUrl();
    }

    /**
     * Format the amount to decimal without the decimal point as required by First Data servers.
     * For example, "25.30" is converted into "2530"
     *
     * @param amount Amount with decimal point
     * @return Amount without the decimal point
     */
    private String formatAmount(String amount) {
        BigDecimal a = new BigDecimal(amount);
        BigDecimal scaled = a.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        return scaled.toString().replace(".", "");
    }

    /**
     *
     * @param signedMessage
     * @param protocolVersion
     * @param signature
     * @return
     */
    private JSONObject getRequestPayload(String signedMessage, String protocolVersion, String signature) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("requestType",type);

        Map<String,Object> transaction = new HashMap<>();
        transaction.put("total", formatAmount(price));
        transaction.put("currency",cur);

        Map<String, Object> encryptedPayload = new HashMap<>();
        //encryptedPayload.put("type", "G");             //  Identify the request as Android Pay request
        encryptedPayload.put("version", protocolVersion); // New field "version" identifies Android or Google Pay
        encryptedPayload.put("data", signedMessage);
        encryptedPayload.put("signature", signature); // This is a new field "signature"

        Map<String,Object> wallet = new HashMap<>();
        wallet.put("walletType","EncryptedGooglePayWalletPaymentMethod");
        wallet.put("encryptedGooglePay",encryptedPayload);

        payload.put("transactionAmount",transaction);
        payload.put("walletPaymentMethod",wallet);

        Map<String, Object> ship = new HashMap<>();
        ship.put("totalCount",count);
        ship.put("finalShipment",finalShipment);


        if(clicked&&externalMid!=null) {
            Map<String, Object> address = new HashMap<>();
            address.put("company", company);
            address.put("address1", add1);
            address.put("address2", add2);
            address.put("city", city);
            address.put("region", region);
            address.put("postalCode", zip);
            address.put("country", country);


            Map<String, Object> document = new HashMap<>();
            document.put("type", dType);
            document.put("number", num);

            Map<String, Object> subMerchant = new HashMap<>();
            subMerchant.put("mcc", mcc);
            subMerchant.put("legalName", legalName);
            subMerchant.put("timezone", timezone);
            subMerchant.put("Address", address);
            subMerchant.put("document", document);
            subMerchant.put("merchantId", mid);


            Map<String, Object> payFac = new HashMap<>();
            payFac.put("externalMerchantId", externalMid);
            payFac.put("paymentFacilitatorId", payFacId);
            payFac.put("saleOrganizationId", sid);
            payFac.put("name", name);
            payFac.put("subMerchantData", subMerchant);

            payload.put("paymentFacilitator", payFac);

        }
        if(type.equals("authorize")&&!(count.equals("COUNT")))
        {
            payload.put("splitShipment",ship);
        }
       // pm.put("3DS", ccmap);
        return new JSONObject(payload);
    }

    /**
     * Compute HMAC signature for the payload. The signature is based on the APIKey and the
     * APISecret provided by First Data. If the APISecret is not specified, the HMAC is
     * not computed.
     *
     * @param payload The payload as a String
     * @return Map of HTTP headers to be added to the request
     */
    private Map<String, String> computeHMAC(String payload) {

        EnvProperties ep = EnvData.getProperties(mEnv);
        String apiSecret = ep.getApiSecret();
        String apiKey = ep.getApiKey();

        Map<String, String> headerMap = new HashMap<>();
        if (apiSecret != null) {
            try {
                String messageSignature;
                String clientRequestId = Long.toString(Math.abs(SecureRandom.getInstance("SHA1PRNG").nextLong()));
                String timestamp = Long.toString(System.currentTimeMillis());

                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secretKey = new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256");
                mac.init(secretKey);

                StringBuilder buffer = new StringBuilder()
                        .append(apiKey)
                        .append(clientRequestId)
                        .append(timestamp)
                        .append(payload);

                byte[] macHash = mac.doFinal(buffer.toString().getBytes("UTF-8"));
                messageSignature = Base64.encodeToString(bytesToHex(macHash).getBytes(), Base64.NO_WRAP);

                headerMap.put("Client-Request-Id", clientRequestId);
                headerMap.put("Timestamp", timestamp);
                headerMap.put("Message-Signature", messageSignature);
            } catch (Exception e) {
                //  Nothing to do
            }
        }
        return headerMap;
    }

    private static String bytesToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }


}
