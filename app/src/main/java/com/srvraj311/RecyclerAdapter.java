package com.srvraj311;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.common.collect.Comparators;
import com.srvraj311.Note;
import com.srvraj311.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>{

    private ArrayList<Note> notesArr;
    CircularRevealCardView cardView;
    
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
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
    }

    public void setData(ArrayList<Note> data){
        Collections.sort(data, new NoteComparator());
        this.notesArr = data;
        notifyItemChanged(notesArr.size(),null);
//        notifyDataSetChanged();
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

