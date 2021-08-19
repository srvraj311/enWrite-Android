package com.srvraj311.NewNoteScreen;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.Document;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.srvraj311.Modal.Note;
import com.srvraj311.NoteScreen.NotesScreen;
import com.srvraj311.R;
import com.srvraj311.NoteScreen.RecyclerAdapter;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;


public class NewNoteAdd extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    EditText noteTitleBox, noteBodyBox;
    FloatingActionButton saveBtn;
    ArrayList<Note> notesArr;
    CardView colorViewer;
    ImageView white;
    ImageView grey;
    ImageView yellow;
    ImageView pink;
    ImageView cyan;
    ImageView red;
    ImageView green;
    ImageView purple;
    String noteColor;

    BottomAppBar bottomAppBar;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_note);
        mAuth = FirebaseAuth.getInstance();

        // Initiating Per user Database and Setting the Current Data Set of notes from the cloud

        noteColor = "#FFFFFF";
        // Hooks
        noteTitleBox = findViewById(R.id.note_title);
        noteBodyBox = findViewById(R.id.note);
        saveBtn = findViewById(R.id.saveBtn);
        bottomAppBar = findViewById(R.id.bottomAppBarSave);
        colorViewer = findViewById(R.id.color_viewer);
        white = findViewById(R.id.whiteColor);
        grey = findViewById(R.id.greyColor);
        yellow = findViewById(R.id.yellowColor);
        pink = findViewById(R.id.pinkColor);
        cyan = findViewById(R.id.cyanColor);
        red = findViewById(R.id.redColor);
        green = findViewById(R.id.greenColor);
        purple = findViewById(R.id.purpleColor);

        // Setting Color Changers
        setColorListener(white , "#FFFFFF");
        setColorListener(grey , "#ACACAC");
        setColorListener(yellow , "#FFF171");
        setColorListener(pink , "#e090d3");
        setColorListener(cyan , "#427C83");
        setColorListener(red , "#ff7f7f");
        setColorListener(green , "#519F54");
        setColorListener(purple , "#aa90e0");


        //Initialising Array
        notesArr = new ArrayList<>();

        //Focus on the Title Box
        noteTitleBox.requestFocus();

        // Save Button Function - To create a new Object of Class Note;
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNote();
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

    private void setColorListener(ImageView btn, String color){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteColor = color;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        colorViewer.setCardBackgroundColor(Color.parseColor(color));
                    }
                });

            }
        });
    }

    private void createNote() {
        //Generating Time String
        String ts = String.valueOf(System.currentTimeMillis());

        // Getting Text
            String title = noteTitleBox.getEditableText().toString();
            String note = noteBodyBox.getEditableText().toString();
            if(note.equals("") && title.equals("")){
                Toast.makeText(getApplicationContext(),"Ahh !! What will i do with a Empty Note",Toast.LENGTH_LONG).show();
            }else {
                Note newNote = new Note(title, note, ts , noteColor, false);
                System.out.println(newNote.getNote_id());
                updateNotesToFstore(newNote);
                Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_LONG).show();
            }
        }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        createNote();
    }

    @Override
    protected void onPause() {
        super.onPause();
        RecyclerAdapter adapter = new RecyclerAdapter(getApplicationContext(), notesArr);
        adapter.setData(this.notesArr);
    }

    private void updateNotesToFstore(Note newNote) {
        // Then ran an update function with document id
        String email = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

        CollectionReference users = db.collection("users");
        assert email != null;
        users.document(email).collection("note").document(newNote.getNote_id()).set(newNote)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.e("CREATE NOTE", "Successful");
                            finish();
                        }else{
                            Log.e("CREATE NOTE", "FAILED");
                        }
                    }
                });
    }

    private void clearInputs () {
        noteTitleBox.setText("");
        noteBodyBox.setText("");
    }
}