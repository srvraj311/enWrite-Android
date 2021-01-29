package com.srvraj311;

import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.common.collect.Comparators;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.srvraj311.Note;
import com.srvraj311.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>{

    private ArrayList<Note> notesArr;
    private ArrayList<Note> bin;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    
    // Constructor to get the array of items to this class
    public RecyclerAdapter(ArrayList<Note> NotesArr){
        Collections.sort(NotesArr, new NoteComparator());
        this.notesArr = NotesArr;
    }
    class NoteComparator implements Comparator<Note> {

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
        String title = notesArr.get(position).getNote_title();
        holder.NoteTitle.setText(title);
        String body = notesArr.get(position).getNote_body();
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
                delete(position);
            }
        });


        //Colour of the Holder
        String color = notesArr.get(position).getNote_colour();
        holder.cardView.setCardBackgroundColor(Color.parseColor(color));


    }

    private void delete(int position) {
        bin.add(this.notesArr.get(position));

        this.notesArr.remove(position);



        //Deleting Note from Firebase
        String email = Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();

        CollectionReference users = db.collection("users");
        assert email != null;
        int notesCount = notesArr.size();
        users.document(email).update("notes",this.notesArr,"notesCount", notesCount,"bin",this.bin).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.e("DELETE","Succes");
                }else {
                    Log.e("DELETE","Failed");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("DELETING NOTE", "Failed" + e);
            }
        });




        this.setData(notesArr);
    }

    public void setData(ArrayList<Note> data){
        Collections.sort(data, new NoteComparator());
        this.notesArr = data;
        notifyItemChanged(notesArr.size(),null);
    }

    @Override
    public int getItemCount() {
        return notesArr.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView NoteTitle;
        private final TextView NoteBody;
        CircularRevealCardView cardView;

        public MyViewHolder(final View itemView) {
            super(itemView);
            NoteTitle = itemView.findViewById(R.id.NoteTitleViewHolder);
            NoteBody = itemView.findViewById(R.id.NoteBodyViewHolder);
            cardView = itemView.findViewById(R.id.ViewHolderItem);

        }
    }
}

