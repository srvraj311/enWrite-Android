package com.srvraj311.SignupScreen;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.srvraj311.R;

public class SignUpActivity extends AppCompatActivity {
    // Importing Firebase Auth
    private FirebaseAuth mAuth;


    EditText regemail, regpassword;
    Button regsignupButton;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initiated Firebase Main Instance



        mAuth = FirebaseAuth.getInstance();



        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_fields);

        //Hooks
        progressBar = findViewById(R.id.progressBar);
        regemail = findViewById(R.id.signup_email);
        regpassword = findViewById(R.id.signup_password);
        regsignupButton = findViewById(R.id.signup_button);

    }

    // Checking if user is already signed In
//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);




    private Boolean validateEmail(){
        String value = regemail.getText().toString();
        String emailPatter = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(value.isEmpty()){
            regemail.setError("Please provide an Email");
            return false;
        }else if(!value.matches(emailPatter)){
            regemail.setError("Invalid Email Address");
            return false;
        }
        else{
            regemail.setError(null);
            return true;
        }
    }





    private Boolean validatePwd(){
        String value = regpassword.getText().toString();
        String passwordVal = "^(?=.*[0-9])"
                + "(?=.*[a-z])(?=.*[A-Z])"
                + "(?=.*[@#$%^&+=])"
                + "(?=\\S+$).{8,20}$";
        if(value.isEmpty()){
            regpassword.setError("You want anyone else to see your notes ?");
            return false;
        }else if(!value.matches(passwordVal)){
            regpassword.setError("Password too Weak");
            return false;
        }
        else{
            regpassword.setError(null);
            return true;
        }
    }


    public void registerUser(View view){
        //Save data in firebase on Click
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        if(!validateEmail() | !validatePwd()){
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        // Obsolete Method Below

        String email = regemail.getText().toString();
        String password = regpassword.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    finish();
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),"User registered Succesfully",Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(getApplicationContext(), ProfileInfo.class );
                    // Flag to Close all other activities
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(getApplicationContext(),"User Already Registered, Try Log-In", Toast.LENGTH_LONG).show();

                    }
                }
            }
        });


//        rootNode = FirebaseDatabase.getInstance();
//        reference = rootNode.getReference("users");
//
//        String name = regname.getText().toString();
//        String email = regemail.getText().toString();
//        String username = regusername.getText().toString();
//        String password = regpassword.getText().toString();
//
//        UserHelperClass helperClass = new UserHelperClass(name, email, username, password);
//        reference.child(username).setValue(helperClass);
    }
}