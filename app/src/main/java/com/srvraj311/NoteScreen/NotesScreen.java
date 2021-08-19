package com.srvraj311.NoteScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.ImageButton;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
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
    RecyclerView pinnedNotesRecycler;
    TextInputEditText searchBox;
    ImageButton clearSearch;
    TextView pinTitle;
    TextView unPinTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_screen);
        recyclerView = findViewById(R.id.recylerViewHolder);
        swipeDown = findViewById(R.id.swipe_container);
        pinnedNotesRecycler = findViewById(R.id.recylerViewHolder2);
        clearSearch = findViewById(R.id.clear_search);
        searchBox = findViewById(R.id.search_notes);
        pinTitle = findViewById(R.id.pin_title);
        unPinTitle = findViewById(R.id.unpin_title);
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
                setAdapter(notesArr);
                searchBox.setText("");
                swipeDown.setRefreshing(false);
            }
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ArrayList<Note> filteredArr = new ArrayList<>();
                for(Note note : notesArr){
                    if (note.getNote_title().toLowerCase().contains(charSequence) || note.getNote_body().toLowerCase().contains(charSequence) || note.getNote_date().toLowerCase().contains(charSequence)){
                        filteredArr.add(note);
                    }
                }
                setAdapter(filteredArr);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBox.setText("");
                setAdapter(notesArr);
            }
        });

        setAdapter(notesArr);
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

    private void setAdapter(ArrayList<Note> notesArr) {
        ArrayList<Note> pinned = getPinnedNotes(notesArr);
        ArrayList<Note> unPinned = getUnPinnedNotes(notesArr);
        if(pinned.size() == 0) pinTitle.setVisibility(View.INVISIBLE);
        else pinTitle.setVisibility(View.VISIBLE);
        if(unPinned.size() == 0) unPinTitle.setVisibility(View.INVISIBLE);
        else unPinTitle.setVisibility(View.VISIBLE);

        RecyclerAdapter adapter = new RecyclerAdapter(getApplicationContext(), unPinned, NotesScreen.this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        adapter.setData(unPinned);

        RecyclerAdapter adapter1 = new RecyclerAdapter(getApplicationContext(), pinned, NotesScreen.this);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getApplicationContext());
        pinnedNotesRecycler.setLayoutManager(layoutManager1);
        pinnedNotesRecycler.setItemAnimator(new DefaultItemAnimator());
        pinnedNotesRecycler.setAdapter(adapter1);
        adapter1.setData(pinned);
    }

    private ArrayList<Note> getPinnedNotes(ArrayList<Note> notesArr) {
        ArrayList<Note> pinned = new ArrayList<>();
        for(Note note : notesArr){
            if(note.isPinned()){
                pinned.add(note);
            }
        }
        return pinned;
    }

    private ArrayList<Note> getUnPinnedNotes(ArrayList<Note> notesArr) {
        ArrayList<Note> unPinned = new ArrayList<>();
        for(Note note : notesArr){
            if(!note.isPinned()){
                System.out.println(note.getNote_title());
                unPinned.add(note);
            }
        }
        return unPinned;
    }

    public void getData() {
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
                                (String) documentSnapshot.get("note_colour"),
                                (String) documentSnapshot.get("note_id"),
                                Boolean.parseBoolean(String.valueOf(documentSnapshot.get("pinned"))));
                        newData.add(newNote);
                    }
                    Collections.sort(newData, new NoteComparator());
                    notesArr = newData;
                    setAdapter(notesArr);
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
    private void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("Notes",MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("localNotes",null);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();
        notesArr = gson.fromJson(json,type);
        setAdapter(notesArr);
    }


    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(NotesScreen.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}