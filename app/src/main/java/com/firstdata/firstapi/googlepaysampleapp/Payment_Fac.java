 package com.firstdata.firstapi.googlepaysampleapp;


 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 import android.widget.Toast;

 public class Payment_Fac extends Activity implements OnClickListener {

    EditText externalMerchantId;
    EditText paymentFacilitatorid;
    EditText saleOrganizationId;
    EditText Name;
    EditText mcc;
    EditText legalName;
    EditText timezone;
    EditText address1;
    EditText address2;
    EditText city;
    EditText region;
    EditText zip;
    Spinner doctype;
    EditText number;
    EditText merchantId;
    EditText country;
    EditText company;
    Button submit;
    boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment__fac);

        findAllViewsId();

        submit.setOnClickListener(this);
    }

    public void findAllViewsId()
    {
        externalMerchantId = findViewById(R.id.editText4);
        paymentFacilitatorid = findViewById(R.id.editText5);
        saleOrganizationId = findViewById(R.id.editText6);
        Name = findViewById(R.id.editText7);
        mcc = findViewById(R.id.editText8);
        legalName = findViewById(R.id.editText9);
        timezone = findViewById(R.id.editText10);
        address1 = findViewById(R.id.editText11);
        address2 = findViewById(R.id.editText12);
        city = findViewById(R.id.editText13);
        region = findViewById(R.id.editText14);
        zip = findViewById(R.id.editText15);
        doctype = findViewById(R.id.spinner3);
        number = findViewById(R.id.editText16);
        merchantId = findViewById(R.id.editText17);
        company = findViewById(R.id.editText18);
        country = findViewById(R.id.editText19);
        submit = findViewById(R.id.button3);
    }

    @Override
    public void onClick(View v){
        Intent intent = new Intent(getApplicationContext(),CheckoutActivity.class);
        Bundle b = new Bundle();

        b.putString("ExtMer",externalMerchantId.getText().toString());
        b.putString("PayFacId",paymentFacilitatorid.getText().toString());
        b.putString("saleOid",saleOrganizationId.getText().toString());
        b.putString("Name",Name.getText().toString());
        b.putString("mcc",mcc.getText().toString());
        b.putString("legalName",legalName.getText().toString());
        b.putString("timeZone",timezone.getText().toString());
        b.putString("add1",address1.getText().toString());
        b.putString("add2",address2.getText().toString());
        b.putString("city",city.getText().toString());
        b.putString("region",region.getText().toString());
        b.putString("zip",zip.getText().toString());
        b.putString("dtype",doctype.getSelectedItem().toString());
        b.putString("num",number.getText().toString());
        b.putString("mid",merchantId.getText().toString());
        b.putString("company",company.getText().toString());
        b.putString("country",country.getText().toString());

        if(TextUtils.isEmpty(externalMerchantId.getText())||TextUtils.isEmpty(paymentFacilitatorid.getText())||TextUtils.isEmpty(Name.getText())||TextUtils.isEmpty(mcc.getText())||TextUtils.isEmpty(number.getText()))
            Toast.makeText(getApplicationContext(),"Enter the required feilds",Toast.LENGTH_LONG).show();
        else if(!TextUtils.isDigitsOnly(saleOrganizationId.getText())){
            Toast.makeText(getApplicationContext(),"Enter numeric value",Toast.LENGTH_LONG).show();
        }
        else {
            intent.putExtras(b);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
