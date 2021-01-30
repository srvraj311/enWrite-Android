package com.srvraj311;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText email;
    EditText password;
    Button login;
    TextView report;
    ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_fields);

        mAuth = FirebaseAuth.getInstance();

        // Variables Assigned to Input Fields
        progressBar = findViewById(R.id.progressBar2);
        email = findViewById(R.id.emailField);
        password = findViewById(R.id.editTextTextPassword);
        login = findViewById(R.id.login_2);
        report = findViewById(R.id.loginReport);
        email.requestFocus();

            }
    private Boolean validateUsername(){
        String value = email.getEditableText().toString();
        if(value.isEmpty()){
            email.setError("Enter Email");
            return false;
        } else
        {
            email.setError(null);
            return true;
        }
    }

    private Boolean validatePwd(){
        String value = password.getEditableText().toString();
        if(value.isEmpty()){
            password.setError("Enter a Password");
            return false;
        } else{
            password.setError(null);
            return true;
        }
    }

    public void loginUser(View view){
        if(!validateUsername() | !validatePwd()){
            progressBar.setVisibility(View.INVISIBLE);
            //Add message for No user found
        } else{
            progressBar.setVisibility(View.VISIBLE);
            isUser();
        }

    }

    private void isUser() {
        String email_entered = email.getEditableText().toString();
        String password_entered = password.getEditableText().toString();


        mAuth.signInWithEmailAndPassword(email_entered, password_entered).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //finish();
                    progressBar.setVisibility(View.INVISIBLE);
                    report.setText(R.string.login_succes);
                    Intent intent = new Intent(LoginActivity.this, NotesScreen.class );
                    // Flag to Close all other activities
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();

                } else {
                    report.setText("Unable to Log-In , Seems Like a network error");
                    //task.getException().getMessage() to get actual message
                    if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        report.setText(task.getException().getMessage());
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}