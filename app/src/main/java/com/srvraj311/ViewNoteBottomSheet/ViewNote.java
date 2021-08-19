package com.srvraj311.ViewNoteBottomSheet;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.collection.LLRBNode;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.srvraj311.Modal.Note;
import com.srvraj311.Modal.TimeCalculator;
import com.srvraj311.NoteScreen.NotesScreen;
import com.srvraj311.R;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ViewNote extends BottomSheetDialogFragment {
    private FirebaseAuth mAuth =FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Note note;
    private Context context;
    private final String TAG = "BottomSheet Notes";
    private TextView noteTitle;
    private TextView noteBody;
    private TextView noteTime;
    private CardView noteBg;
    private NotesScreen notesScreen;
    ImageButton editButton;
    ImageButton deleteButton;
    ImageButton pinButton;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }
    public void closeBottomSheet(){
        this.dismiss();
    }

    public ViewNote(Context context, Note note, NotesScreen notesScreen){
        this.context = context;
        this.note = note;
        this.notesScreen = notesScreen;
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
        pinButton = view.findViewById(R.id.pin_note);
        deleteButton = view.findViewById(R.id.delete_note);
        editButton = view.findViewById(R.id.edit_note);

        // Assigners
        noteTitle.setText(note.getNote_title());
        noteBody.setText(note.getNote_body());
        String ts = note.getNote_date();
        System.out.println(note.getNote_date() + note.getNote_body() + note.getNote_title());
        long timeStamp = Long.parseLong(note.getNote_date());
        noteTime.setText(String.valueOf(TimeCalculator.getTimeAgo(timeStamp)));
        noteBg.setCardBackgroundColor(Color.parseColor(note.getNote_colour()));

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteNote(note);
            }
        });
        if(note.isPinned()) {
            pinButton.setImageResource(R.drawable.ic_baseline_favorite_24);
        }
        else pinButton.setImageResource(R.drawable.ic_baseline_favorite_border_24);

        pinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                note.setPinned(!note.isPinned());
                if(note.isPinned()) {
                    pinButton.setImageResource(R.drawable.ic_baseline_favorite_24);
                }
                else pinButton.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                CollectionReference n = db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser().getEmail())).collection("note");
                n.document(note.getNote_id()).set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        String s = "";
                        if(note.isPinned()) s = "Pinned";
                        else s = "Unpinned";
                        if(note.isPinned()) {
                            pinButton.setImageResource(R.drawable.ic_baseline_favorite_24);
                        }
                        else pinButton.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                        Toast.makeText(getContext(), "Note " + s, Toast.LENGTH_LONG).show();
                        notesScreen.getData();
                        closeBottomSheet();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        String s = "";
                        if(note.isPinned()) s = "Pin";
                        else s = "Unpin";
                        Toast.makeText(getContext(), "Note " + s + " failed", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                        closeBottomSheet();
                    }
                });
            }
        });

    }

    private void deleteNote(Note note) {
        CollectionReference n = db.collection("users")
                .document(Objects.requireNonNull(Objects.requireNonNull(mAuth.getCurrentUser())
                .getEmail()))
                .collection("note");
        n.document(note.getNote_id()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    notesScreen.getData();
                    closeBottomSheet();
                    Toast.makeText(getContext(), "Note Deleted Successfully", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getContext(), "Failed to Delete Note !! ", Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Failed to Delete Note !! ", Toast.LENGTH_LONG).show();
            }
        });
    }
}
