package com.srvraj311.NoteScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.srvraj311.MainActivity;
import com.srvraj311.Modal.Note;
import com.srvraj311.NewNoteScreen.NewNoteAdd;
import com.srvraj311.R;
import com.srvraj311.ViewNoteBottomSheet.ViewNote;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class NotesScreen<mAuth> extends AppCompatActivity {
    FloatingActionButton addBtn;
    BottomAppBar toolbar;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    public ArrayList<Note> notesArr = new ArrayList<Note>();
    public ArrayList<Note> notesBin = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String umail = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

    RecyclerView recyclerView;
    SwipeRefreshLayout swipeDown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_screen);
        recyclerView = findViewById(R.id.recylerViewHolder);
        swipeDown = findViewById(R.id.swipe_container);

        addBtn = findViewById(R.id.addBtn);
        addBtn.bringToFront();
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotesScreen.this, NewNoteAdd.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        // Check local Storage for notes, if not get from fb
        if(isLocalNotesEmpty()){
            getData();
        }else{
            loadData();
        }


        //Swipe down to refresh
        swipeDown.setOnRefreshListener(this::getData);
        swipeDown.setColorSchemeResources(R.color.colorAccent,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeDown.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeDown.setRefreshing(true);
                setAdapter();
                swipeDown.setRefreshing(false);
            }
        });

        setAdapter();
    }



    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }


    static class NoteComparator implements Comparator<Note> {
        @Override
        public int compare(Note n1, Note n2) {
            return n2.getNote_date().compareTo(n1.getNote_date());
        }
    }

    private void setAdapter() {
        RecyclerAdapter adapter = new RecyclerAdapter(getApplicationContext(), notesArr,notesBin, NotesScreen.this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.setData(notesArr);
    }
    private void getData() {
        CollectionReference users = db.collection("users");
        users.document(umail).collection("note").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<Note> newData = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        Note newNote = new Note(
                                (String) documentSnapshot.get("note_title"),
                                (String) documentSnapshot.get("note_body"),
                                (String) documentSnapshot.get("note_date"),
                                (String) documentSnapshot.get("note_colour"));
                        newData.add(newNote);
                    }
                    Collections.sort(newData, new NoteComparator());
                    notesArr = newData;
                    setAdapter();
                    saveData(newData, "localNotes");
                }
            }
        });
    }

    private boolean isLocalNotesEmpty(){
        SharedPreferences sharedPreferences = getSharedPreferences("Notes", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("localNotes",null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        return gson.fromJson(json,type) == null;
    }

    private void saveData(ArrayList<Note> notes, String place) {
        SharedPreferences sharedPreferences = getSharedPreferences("Notes",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(notes);
        editor.putString(place,json);
        editor.apply();
    }
    public void loadBin(){
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("bin",null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        notesBin = gson.fromJson(json,type);
        if(notesBin == null){
            notesBin = new ArrayList<Note>();
        }
    }
    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("Notes",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("localNotes",null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        notesArr = gson.fromJson(json,type);
        setAdapter();
    }


    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(NotesScreen.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}