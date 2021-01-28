package com.srvraj311;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.ObservableSnapshotArray;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.local.LocalStore;
import com.google.firebase.firestore.model.Document;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.BufferedOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class NotesScreen<mAuth> extends AppCompatActivity {
    FloatingActionButton addBtn;
    BottomAppBar toolbar;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    ArrayList<Note> notesArr = new ArrayList<Note>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String umail = mAuth.getCurrentUser().getEmail();
    private CollectionReference notebookRef = db.collection("users");
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeDown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_screen);
        recyclerView = findViewById(R.id.recylerViewHolder);
        swipeDown = findViewById(R.id.swipe_container);




        getData();
        loadData();
        setUpButtons();
        RecyclerAdapter adapter = new RecyclerAdapter(notesArr);
        adapter.setData(notesArr);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);



        //Swipe down to refresh
        swipeDown.setOnRefreshListener(this::getData);
        swipeDown.setColorSchemeResources(R.color.colorAccent,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeDown.post(new Runnable() {
            @Override
            public void run() {

                swipeDown.setRefreshing(true);
                Toast.makeText(getApplicationContext(),"Refreshed",Toast.LENGTH_LONG).show();
                swipeDown.setRefreshing(false);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        recyclerView = findViewById(R.id.recylerViewHolder);
        getData();
        loadData();
        RecyclerAdapter adapter = new RecyclerAdapter(notesArr);
        adapter.setData(notesArr);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
    }


    private void setUpButtons() {
        addBtn = findViewById(R.id.addBtn);
        addBtn.bringToFront();
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On click of Add BTN, new Activity will be launched to add notes.
                finish();

                Intent intent = new Intent(NotesScreen.this, NewNoteAdd.class);
                //flags here
                startActivity(intent);

            }
        });

        toolbar = findViewById(R.id.bottomAppBar);
        toolbar.setNavigationIcon(null);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menuLogout) {
                    logout();
                    return true;
                } else return false;
            }
        });
    }

    private void setAdapter() {
        RecyclerAdapter adapter = new RecyclerAdapter(notesArr);
        adapter.setData(notesArr);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

    }
    private void getData() {

        //Getting current user email to match from database
        String umail = mAuth.getCurrentUser().getEmail();
        db.collection("users").whereEqualTo("email", umail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Setting The Received Notes Array to Local Array
                                ArrayList<HashMap<String,String>> arr;
                                arr = (ArrayList<HashMap<String, String>>) document.get("notes");
                                notesArr.clear();
                                for(int i = 0; i < arr.size();i++){
                                    String title = arr.get(i).get("note_title");
                                    String body = arr.get(i).get("note_body");
                                    String date = arr.get(i).get("note_date");
                                    String colour = arr.get(i).get("note_colour");
                                    String id = arr.get(i).get("note_id");
                                    Note note = new Note(title,body,date,colour,id);
                                    notesArr.add(note);
                                }
                                saveData(notesArr);
                            }
                        } else {
                            Log.e("Error getting documents", "Unsuccessful in getting the data from fireStore");
                        }
                    }
                });
    }

    private void saveData(ArrayList<Note> notes) {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(notes);
        editor.putString("notes",json);
        editor.apply();
    }
    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("notes",null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        notesArr = gson.fromJson(json,type);
        if(notesArr == null){
            notesArr = new ArrayList<>();
        }
    }


    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(NotesScreen.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}