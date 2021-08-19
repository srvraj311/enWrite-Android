package com.srvraj311.NoteScreen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.srvraj311.Modal.Note;
import com.srvraj311.Modal.TimeCalculator;
import com.srvraj311.NewNoteScreen.NewNoteAdd;
import com.srvraj311.R;
import com.srvraj311.ViewNoteBottomSheet.ViewNote;

import org.w3c.dom.Text;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>{

    private ArrayList<Note> notesArr;
    private ArrayList<Note> notesBin;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Context context;
    NotesScreen notesScreen;

    
    // Constructor to get the array of items to this class
    public RecyclerAdapter(Context context, ArrayList<Note> NotesArr, NotesScreen notesScreen){
        Collections.sort(NotesArr, new NoteComparator());
        this.notesArr = NotesArr;
        this.notesBin = notesBin;
        this.context = context;
        this.notesScreen = notesScreen;
    }

    public RecyclerAdapter(Context context, ArrayList<Note> NotesArr){
        Collections.sort(NotesArr, new NoteComparator());
        this.context = context;
        this.notesArr = NotesArr;
    }

    static class NoteComparator implements Comparator<Note> {
        @Override
        public int compare(Note n1, Note n2) {
            return n2.getNote_date().compareTo(n1.getNote_date());
        }
    }

    @NonNull
    @Override
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item,parent,false);
        return new MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.MyViewHolder holder, int position) {
        Note note = notesArr.get(position);
        String title = note.getNote_title();
        holder.NoteTitle.setText(title);
        String body = note.getNote_body();
        holder.NoteBody.setText(body);

        //LONG PRESS ON CARD
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.e("LONG PRESS","Working");
                return true;
            }
        });
        //CLICK ON CARD
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewNote viewNote = new ViewNote(context, notesArr.get(position), notesScreen);
                viewNote.show(notesScreen.getSupportFragmentManager(), viewNote.getTag());
            }
        });


        //Colour of the Holder
        String color = note.getNote_colour();
        holder.cardView.setCardBackgroundColor(Color.parseColor(color));

        String ts = note.getNote_date();
        String t = TimeCalculator.getTimeAgo(Long.parseLong(ts));
        holder.noteDate.setText(t);

    }



    public void setData(ArrayList<Note> data){
        Collections.sort(data, new NoteComparator());
        this.notesArr = data;
        notifyDataSetChanged();
//        notifyItemChanged(notesArr.size(),null);
    }

    @Override
    public int getItemCount() {
        return notesArr.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView NoteTitle;
        private final TextView NoteBody;
        CircularRevealCardView cardView;
        private TextView noteDate;

        public MyViewHolder(final View itemView) {
            super(itemView);
            NoteTitle = itemView.findViewById(R.id.NoteTitleViewHolder);
            NoteBody = itemView.findViewById(R.id.NoteBodyViewHolder);
            cardView = itemView.findViewById(R.id.ViewHolderItem);
            noteDate = itemView.findViewById(R.id.note_date);
        }
    }
}

