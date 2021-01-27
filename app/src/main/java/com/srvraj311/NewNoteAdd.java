package com.srvraj311;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ActionMenuView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class NewNoteAdd extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText noteTitleBox, noteBodyBox;
    FloatingActionButton saveBtn;
    ArrayList<Note> notesArr;

    BottomAppBar bottomAppBar;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);
        mAuth = FirebaseAuth.getInstance();

        // Initiating Per user Database and Setting the Current Data Set of notes from the cloud


        // Buttons Assignment
        noteTitleBox = findViewById(R.id.note_title);
        noteBodyBox = findViewById(R.id.note);
        saveBtn = findViewById(R.id.saveBtn);
        bottomAppBar = findViewById(R.id.bottomAppBarSave);


        //Initialising Array
        notesArr = new ArrayList<>();

        //Focus on the Title Box
        noteTitleBox.requestFocus();

        //Generating Time String
        Date date = new Date();

        //Getting Primary Array Of Data;
        getData();
        // Save Button Function - To create a new Object of Class Note;


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting Text
                String title = noteTitleBox.getEditableText().toString();
                String note = noteBodyBox.getEditableText().toString();
                if(note.equals("") && title.equals("")){
                    Toast.makeText(getApplicationContext(),"Ahh !! What will i do with a Empty Note",Toast.LENGTH_LONG).show();
                }else {
                    Note newNote = new Note(title, note, date.toString());
                    notesArr.add(newNote);
                    updateNotesToFstore();
                    Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_LONG).show();
                }
            }
        });


        // Bottom Bar Functions
        bottomAppBar.setNavigationIcon(null);
        bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menuClear) {
                    clearInputs();
                    return true;
                } else return false;

            }
        });

    }

    private void getData() {
        db.collection("users").whereEqualTo("email", Objects.requireNonNull(mAuth.getCurrentUser()).getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                // Setting The Received Notes Array to Local Array
                                notesArr = (ArrayList<Note>) document.get("notes");
                            }
                        } else {
                            Log.e("Error getting documents", "Unsuccesfull in getting the data from firestore");
                        }
                    }
                });
    }

    private void updateNotesToFstore() {
        // Then ran an update function with document id
        String email = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

        CollectionReference users = db.collection("users");
        assert email != null;
        users.document(email)
                .update("notes", notesArr,"notesCount",notesArr.size())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.e("UPDATE FIREBASE", "Succesfull");

                            finish();
//                            Intent intent = new Intent(NewNoteAdd.this, NotesScreen.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            startActivity(intent);

                        }else{
//                            Intent intent = new Intent(NewNoteAdd.this, NotesScreen.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            startActivity(intent);
                            finish();
                            Log.e("UPDATE FIREBASE", "Failed");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("UPDATE FIREBASE", "Failed" + e);
            }
        })  ;
        Log.e("FB UPDATE", "Succefully Updated");
    }


    private void clearInputs () {
        noteTitleBox.setText("");
        noteBodyBox.setText("");
    }
}