package com.jby.ride.home.dialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jby.ride.R;

public class NoteDialog extends DialogFragment {
    View rootView;
    private EditText noteDialogNote;
    public Button noteDialogButton;
    NoteDialogCallBack noteDialogCallBack;
    String note;

    public NoteDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_main_note_dialog, container);
        objectInitialize();
        noteDialogCallBack = (NoteDialogCallBack) getActivity();
        return rootView;
    }

    private void objectInitialize() {

        noteDialogNote = (EditText)rootView.findViewById(R.id.activity_main_text_view_note);
        noteDialogButton = (Button)rootView.findViewById(R.id.activity_main_note_button);

        Bundle mArgs = getArguments();
        if (mArgs != null) {
            note = mArgs.getString("note");
            noteDialogNote.setText(note);
        }

        noteDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                note = noteDialogNote.getText().toString().trim();
                noteDialogCallBack.setNote(note);
                dismiss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            d.getWindow().setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public interface NoteDialogCallBack {
        void setNote(String note);
    }

}