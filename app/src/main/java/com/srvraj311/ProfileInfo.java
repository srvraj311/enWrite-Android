package com.srvraj311;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.core.UserData;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProfileInfo extends AppCompatActivity {
    //Declarations
    private static final String TAG = "ProfileInfo" ;
    private static final int PICK_IMAGE = 100;
    private StorageReference mStorageRef;
    String profileImageUrl;
    ImageView image;
    EditText name;
    Button nextButton;
    ProgressBar bar;
    Uri uriProfileImage ;
    FirebaseAuth mAuth;

    //OnCreate Method Main
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);
        mAuth = FirebaseAuth.getInstance();

        bar = findViewById(R.id.p3);

        // Image Edit after SignUp
        image = findViewById(R.id.imageView3);
        image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // Method to show image on Screen
                showImageChooser();
            }
        });
        nextButton = findViewById(R.id.nextButton);
        nextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                bar = findViewById(R.id.p3);
                bar.setVisibility(View.VISIBLE);
                name = findViewById(R.id.signup_name);
                String nameData = name.getText().toString();

                if(nameData != null){
                        updateData(nameData);
                    Intent intent = new Intent(ProfileInfo.this, NotesScreen.class);
                    startActivity(intent);
                    finish();
                    //updateName();
                } else {
                    name.setError("Provide A Name");
                }
                bar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void updateData(String nameData) {
        mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("profilepics/"+System.currentTimeMillis()+".jpg");
        if(uriProfileImage != null){
            profileImageRef.putFile(uriProfileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    profileImageUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                    Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_LONG).show();
                }
            });
        }
        String displayName = name.getText().toString();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null && profileImageUrl != null){

            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName).setPhotoUri(Uri.parse(profileImageUrl)).build();
            user.updateProfile(profile)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(getApplicationContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("users");

        String currentEmail = mAuth.getCurrentUser().getEmail();
        HashMap<String, Object> person = new HashMap<>();
        person.put("email", currentEmail);
        person.put("name", nameData);
        person.put("notes", new ArrayList<Note>(0));
        person.put("bin", new ArrayList<Note>(0));
        person.put("notesCount", 0);

        users.document(currentEmail).set(person);

    }




    private void showImageChooser(){
    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
    startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"),PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                image.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
