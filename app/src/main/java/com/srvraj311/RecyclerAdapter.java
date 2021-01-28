package com.srvraj311;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.srvraj311.Note;
import com.srvraj311.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>{

    private ArrayList<Note> notesArr;

    // Constructor to get the array of itmes to this class
    public RecyclerAdapter(ArrayList<Note> NotesArr){
        this.notesArr = NotesArr;
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
    }

    public void setData(ArrayList<Note> data){
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

        public MyViewHolder(final View itemView) {
            super(itemView);
            NoteTitle = itemView.findViewById(R.id.NoteTitleViewHolder);
            NoteBody = itemView.findViewById(R.id.NoteBodyViewHolder);
        }
    }
}
