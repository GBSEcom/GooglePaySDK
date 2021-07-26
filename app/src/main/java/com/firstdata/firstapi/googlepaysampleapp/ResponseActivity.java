package com.firstdata.firstapi.googlepaysampleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Displays the Json response from the First Data servers and allows the user to quit
 * the application.
 */
public class ResponseActivity extends Activity {

    private String message;

    private Button mQuitButton;
    private TextView mTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        message = getIntent().getStringExtra(Constants.EXTRA_RESULT_MESSAGE);

        String res = formatString(message);

        mTextMessage = (TextView) findViewById(R.id.response_message);
        mQuitButton = (Button) findViewById(R.id.quit_button);

        mQuitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = CheckoutActivity.newIntent(getApplicationContext(), true);
                startActivity(intent);
            }
        });
        mTextMessage.setText(res);
    }

    public String formatString(String text){
        StringBuilder json = new StringBuilder();
        String indentString = "";

        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            switch (letter) {
                case '{':
                case '[':
                    json.append("\n" + indentString + letter + "\n");
                    indentString = indentString + "\t";
                    json.append(indentString);
                    break;
                case '}':
                case ']':
                    indentString = indentString.replaceFirst("\t", "");
                    json.append("\n" + indentString + letter);
                    break;
                case ',':
                    json.append(letter + "\n" + indentString);
                    break;

                default:
                    json.append(letter);
                    break;
            }
        }

        return json.toString();

    }

    public static Intent newIntent(Activity activity, String status, String message) {
        Intent intent = new Intent(activity, ResponseActivity.class);
        intent.putExtra(Constants.EXTRA_RESULT_STATUS, status);
        intent.putExtra(Constants.EXTRA_RESULT_MESSAGE, message);
        return intent;
    }


}
