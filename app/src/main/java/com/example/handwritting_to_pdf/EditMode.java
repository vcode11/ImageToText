package com.example.handwritting_to_pdf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

public class EditMode extends AppCompatActivity {
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_mode);
        editText = findViewById(R.id.editable);

        Intent intent = getIntent();
        String message = intent.getStringExtra("text");
        editText.setText(message);
    }

}
