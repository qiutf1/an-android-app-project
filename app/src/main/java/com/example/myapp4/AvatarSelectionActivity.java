package com.example.myapp4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class AvatarSelectionActivity extends AppCompatActivity {

    private ImageView avatar1, avatar2, avatar3, avatar4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_selection);

        avatar1 = findViewById(R.id.avatar1);
        avatar2 = findViewById(R.id.avatar2);
        avatar3 = findViewById(R.id.avatar3);
        avatar4 = findViewById(R.id.avatar4);

        // 头像选择事件
        avatar1.setOnClickListener(v -> returnAvatar(R.drawable.avatar1));
        avatar2.setOnClickListener(v -> returnAvatar(R.drawable.avatar2));
        avatar3.setOnClickListener(v -> returnAvatar(R.drawable.avatar3));
        avatar4.setOnClickListener(v -> returnAvatar(R.drawable.avatar4));
    }

    private void returnAvatar(int avatarId) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selectedAvatar", avatarId);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
