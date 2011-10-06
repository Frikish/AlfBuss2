package com.alfsimen.bybuss.paypal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.paypal.android.MEP.*;

import java.math.BigDecimal;

public class PayPalActivity extends Activity implements View.OnClickListener {

    private static final int server = PayPal.ENV_SANDBOX;
    private static final String appID = "";
    private static final int request = 1;

    protected static final int INITIALIZE_SUCCESS = 0;
    protected static final int INITIALIZE_FAILURE = 1;

    ScrollView scroller;
    TextView labelSimplePayment;
    TextView labelKey;
    TextView appVersion;
    EditText enterPreapprovalKey;
    Button exitPayPal;
    TextView title;
    TextView info;
    TextView extra;
    LinearLayout layoutSimplePayment;

    CheckoutButton launchSimplePayment;

    public static String resultTitle;
    public static String resultInfo;
    public static String resultExtra;


    Handler hRefresh = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case INITIALIZE_SUCCESS:
                    setupButtons();
                    break;
                case INITIALIZE_FAILURE:
                    showFailure();
                    break;
            }
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread libraryInitializationThread = new Thread() {
            public void run() {
                initLibrary();

                if(PayPal.getInstance().isLibraryInitialized()) {
                    hRefresh.sendEmptyMessage(INITIALIZE_SUCCESS);
                }
                else {
                    hRefresh.sendEmptyMessage(INITIALIZE_FAILURE);
                }
            }
        };
        libraryInitializationThread.start();

        scroller = new ScrollView(this);
        scroller.setLayoutParams(new ViewGroup.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        scroller.setBackgroundColor(Color.BLACK);

        LinearLayout content = new LinearLayout(this);
        content.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(10, 10, 10, 10);
        content.setBackgroundColor(Color.TRANSPARENT);

        layoutSimplePayment = new LinearLayout(this);
		layoutSimplePayment.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layoutSimplePayment.setGravity(Gravity.CENTER_HORIZONTAL);
		layoutSimplePayment.setOrientation(LinearLayout.VERTICAL);
		layoutSimplePayment.setPadding(0, 5, 0, 5);

        labelSimplePayment = new TextView(this);
		labelSimplePayment.setGravity(Gravity.CENTER_HORIZONTAL);
		labelSimplePayment.setText("Simple Payment:");
		layoutSimplePayment.addView(labelSimplePayment);
		labelSimplePayment.setVisibility(View.GONE);

		content.addView(layoutSimplePayment);

//        LinearLayout layoutKey = new LinearLayout(this);
//		layoutKey.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//		layoutKey.setGravity(Gravity.CENTER_HORIZONTAL);
//		layoutKey.setOrientation(LinearLayout.VERTICAL);
//		layoutKey.setPadding(0, 1, 0, 5);

        title = new TextView(this);
		title.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		title.setPadding(0, 5, 0, 5);
		title.setGravity(Gravity.CENTER_HORIZONTAL);
		title.setTextSize(30.0f);
		title.setVisibility(TextView.GONE);
		content.addView(title);

        info = new TextView(this);
		info.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		info.setPadding(0, 5, 0, 5);
		info.setGravity(Gravity.CENTER_HORIZONTAL);
		info.setTextSize(20.0f);
		info.setVisibility(TextView.VISIBLE);
		info.setText("Initializing Library...");
		content.addView(info);

        extra = new TextView(this);
		extra.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		extra.setPadding(0, 5, 0, 5);
		extra.setGravity(Gravity.CENTER_HORIZONTAL);
		extra.setTextSize(12.0f);
		extra.setVisibility(TextView.GONE);
		content.addView(extra);

        LinearLayout layoutExit = new LinearLayout(this);
		layoutExit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		layoutExit.setGravity(Gravity.CENTER_HORIZONTAL);
		layoutExit.setOrientation(LinearLayout.VERTICAL);
		layoutExit.setPadding(0, 15, 0, 5);

        exitPayPal = new Button(this);
		exitPayPal.setLayoutParams(new ViewGroup.LayoutParams(200, ViewGroup.LayoutParams.WRAP_CONTENT)); //Semi mimic PP button sizes
		exitPayPal.setOnClickListener(this);
		exitPayPal.setText("Exit");
		layoutExit.addView(exitPayPal);
		content.addView(layoutExit);

        scroller.addView(content);
        setContentView(scroller);
    }

    public void setupButtons() {
        PayPal pp = PayPal.getInstance();
        launchSimplePayment = pp.getCheckoutButton(this, PayPal.BUTTON_194x37, CheckoutButton.TEXT_DONATE);
        launchSimplePayment.setOnClickListener(this);
        layoutSimplePayment.addView(launchSimplePayment);

        labelSimplePayment.setVisibility(View.VISIBLE);

        info.setText("");
        info.setVisibility(View.GONE);
    }

    public void showFailure() {
        title.setText("FAILURE");
        info.setText("Could not initialize the PayPal library");
        title.setVisibility(View.VISIBLE);
        info.setVisibility(View.VISIBLE);
    }

    private void initLibrary() {
        PayPal pp = PayPal.getInstance();
        if(pp == null) {
            pp = PayPal.initWithAppID(this, appID, server);
            pp.setLanguage("en_US");
            pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);
            pp.setShippingEnabled(false);
            pp.setDynamicAmountCalculationEnabled(false);
        }
    }

    private PayPalPayment exampleSimplePayment() {
        // Create a basic PayPalPayment.
		PayPalPayment payment = new PayPalPayment();
		// Sets the currency type for this payment.
    	payment.setCurrencyType("NOK");
    	// Sets the recipient for the payment. This can also be a phone number.
    	payment.setRecipient("example-merchant-1@paypal.com");
    	// Sets the amount of the payment, not including tax and shipping amounts.
    	payment.setSubtotal(new BigDecimal("8.25"));
    	// Sets the payment type. This can be PAYMENT_TYPE_GOODS, PAYMENT_TYPE_SERVICE, PAYMENT_TYPE_PERSONAL, or PAYMENT_TYPE_NONE.
    	payment.setPaymentType(PayPal.PAYMENT_TYPE_GOODS);

    	// PayPalInvoiceData can contain tax and shipping amounts. It also contains an ArrayList of PayPalInvoiceItem which can
    	// be filled out. These are not required for any transaction.
    	PayPalInvoiceData invoice = new PayPalInvoiceData();
    	// Sets the tax amount.
    	invoice.setTax(new BigDecimal("1.25"));
    	// Sets the shipping amount.
    	invoice.setShipping(new BigDecimal("4.50"));

    	// PayPalInvoiceItem has several parameters available to it. None of these parameters is required.
    	PayPalInvoiceItem item1 = new PayPalInvoiceItem();
    	// Sets the name of the item.
    	item1.setName("Pink Stuffed Bunny");
    	// Sets the ID. This is any ID that you would like to have associated with the item.
    	item1.setID("87239");
    	// Sets the total price which should be (quantity * unit price). The total prices of all PayPalInvoiceItem should add up
    	// to less than or equal the subtotal of the payment.
    	item1.setTotalPrice(new BigDecimal("6.00"));
    	// Sets the unit price.
    	item1.setUnitPrice(new BigDecimal("2.00"));
    	// Sets the quantity.
    	item1.setQuantity(3);
    	// Add the PayPalInvoiceItem to the PayPalInvoiceData. Alternatively, you can create an ArrayList<PayPalInvoiceItem>
    	// and pass it to the PayPalInvoiceData function setInvoiceItems().
    	invoice.getInvoiceItems().add(item1);

    	// Create and add another PayPalInvoiceItem to add to the PayPalInvoiceData.
    	PayPalInvoiceItem item2 = new PayPalInvoiceItem();
    	item2.setName("Well Wishes");
    	item2.setID("56691");
    	item2.setTotalPrice(new BigDecimal("2.25"));
    	item2.setUnitPrice(new BigDecimal("0.25"));
    	item2.setQuantity(9);
    	invoice.getInvoiceItems().add(item2);

    	// Sets the PayPalPayment invoice data.
    	payment.setInvoiceData(invoice);
    	// Sets the merchant name. This is the name of your Application or Company.
    	payment.setMerchantName("The Gift Store");
    	// Sets the description of the payment.
    	//payment.setDescription("Quite a simple payment");
    	// Sets the Custom ID. This is any ID that you would like to have associated with the payment.
    	payment.setCustomID("8873482296");
    	// Sets the Instant Payment Notification url. This url will be hit by the PayPal server upon completion of the payment.
    	payment.setIpnUrl("http://www.exampleapp.com/ipn");
    	// Sets the memo. This memo will be part of the notification sent by PayPal to the necessary parties.
    	payment.setMemo("Hi! I'm making a memo for a simple payment.");

    	return payment;
    }

    public void onClick(View v) {
        if(v == launchSimplePayment) {
            PayPalPayment payment = exampleSimplePayment();
            Intent checkoutIntent = PayPal.getInstance().checkout(payment, this);
            startActivityForResult(checkoutIntent, request);
        }
        else if(v == exitPayPal) {
            finish();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != request)
            return;

        switch(resultCode) {
		case Activity.RESULT_OK:
			resultTitle = "SUCCESS";
			resultInfo = "You have successfully completed this " + ("payment.");
			//resultExtra = "Transaction ID: " + data.getStringExtra(PayPalActivity.EXTRA_PAY_KEY);
			break;
		case Activity.RESULT_CANCELED:
			resultTitle = "CANCELED";
			resultInfo = "The transaction has been cancelled.";
			resultExtra = "";
			break;
//		case PayPalActivity.RESULT_CANCELED:
//			resultTitle = "FAILURE";
//			resultInfo = data.getStringExtra(PayPalActivity.);
//			resultExtra = "Error ID: " + data.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
		}
        launchSimplePayment.updateButton();

        title.setText(resultTitle);
        title.setVisibility(View.VISIBLE);
        info.setText(resultInfo);
        info.setVisibility(View.VISIBLE);
        extra.setText(resultExtra);
        extra.setVisibility(View.VISIBLE);
    }
}
