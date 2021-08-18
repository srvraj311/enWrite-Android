package com.srvraj311.ViewNoteBottomSheet;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.collection.LLRBNode;
import com.srvraj311.Modal.Note;
import com.srvraj311.Modal.TimeCalculator;
import com.srvraj311.R;

public class ViewNote extends BottomSheetDialogFragment {
    private Note note;
    private Context context;
    private final String TAG = "BottomSheet Notes";
    private TextView noteTitle;
    private TextView noteBody;
    private TextView noteTime;
    private CardView noteBg;
    BottomNavigationView bottomAppBar;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);

    }

    public ViewNote(Context context, Note note){
        this.context = context;
        this.note = note;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_note_detail_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Hooks
        noteTitle = view.findViewById(R.id.note_title);
        noteBody = view.findViewById(R.id.note_body);
        noteTime = view.findViewById(R.id.note_date);
        noteBg = view.findViewById(R.id.noteDetailsBottomSheet);
        bottomAppBar = view.findViewById(R.id.bottomToolbar);

        // Assigners
        noteTitle.setText(note.getNote_title());
        noteBody.setText(note.getNote_body());
        String ts = note.getNote_date();
        System.out.println(note.getNote_date() + note.getNote_body() + note.getNote_title());
        long timeStamp = Long.parseLong(note.getNote_date());
        noteTime.setText(String.valueOf(TimeCalculator.getTimeAgo(timeStamp)));
        noteBg.setCardBackgroundColor(Color.parseColor(note.getNote_colour()));
        bottomAppBar.setBackgroundColor(Color.parseColor(note.getNote_colour()));
    }
}
