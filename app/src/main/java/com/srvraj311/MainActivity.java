package com.srvraj311;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;

public class MainActivity extends AppCompatActivity {

    String TAG = "ActivityOne";
    TextView textView;
    TextView login_message;
    Boolean correctDetails;
    Boolean userExists;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = (TextView) findViewById(R.id.textView);
        login_message = (TextView) findViewById(R.id.login_message);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null){
            startActivity(new Intent(this, NotesScreen.class));
        finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
//    public void clickHandler(View View){
//        Date date = new Date();
//        Toast.makeText(this, "You Clicked Button", Toast.LENGTH_LONG).show();
//        textView.setText("Today is: "+date.toString());
//    }
    public void loginHandler(View View){
        //Change below two variables to match results of POST request's resoponse
        correctDetails = true;
        userExists = true;
        if(correctDetails || userExists){
            login_message.setText(R.string.welcome_message_2);
            //showLoginField();
        } else {
            login_message.setText(R.string.welcome_message);
        }
        Intent intent = new Intent(MainActivity.this, LoginActivity.class );
        startActivity(intent);
    }
    public void signUpHandler(View View){
        //  setContentView(R.layout.signup_fields);

        //// navigation to Another Activity A1 to A2:
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class );
        startActivity(intent);
    }


}