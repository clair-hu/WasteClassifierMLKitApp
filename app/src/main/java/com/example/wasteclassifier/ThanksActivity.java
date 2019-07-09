package com.example.wasteclassifier;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ThanksActivity extends AppCompatActivity {

    private Button continueClassifyingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanks);

        continueClassifyingButton = findViewById(R.id.continue_classify_button);
        continueClassifyingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueClassifying();
            }
        });
    }

    public void continueClassifying(){
        Intent intent = new Intent(this, AutoMLActivity.class);
        startActivity(intent);
    }


}
