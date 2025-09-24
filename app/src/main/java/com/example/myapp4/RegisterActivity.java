package com.example.myapp4;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton, backButton;
    private ImageView eyeImageView, avatarImageView;
    private DatabaseHelper dbHelper;
    private boolean passwordVisible = false;
    private int selectedAvatarId = R.drawable.ic_avatar_default;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);
        eyeImageView = findViewById(R.id.eyeImageView);
        avatarImageView = findViewById(R.id.avatarImageView);

        // 点击头像选择
        avatarImageView.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, AvatarSelectionActivity.class);
            startActivityForResult(intent, 100);
        });

        // 切换密码可见性
        eyeImageView.setOnClickListener(v -> {
            if (passwordVisible) {
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                eyeImageView.setImageResource(R.drawable.ic_eye_closed);
                passwordVisible = false;
            } else {
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                eyeImageView.setImageResource(R.drawable.ic_eye_open);
                passwordVisible = true;
            }
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // 注册按钮
        registerButton.setOnClickListener(v -> {
            String u = usernameEditText.getText().toString().trim();
            String p = passwordEditText.getText().toString().trim();
            String cp = confirmPasswordEditText.getText().toString().trim();

            if (u.isEmpty() || p.isEmpty() || cp.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!p.equals(cp)) {
                Toast.makeText(RegisterActivity.this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbHelper.isUserExists(u)) {
                Toast.makeText(RegisterActivity.this, "用户名已存在，请换一个", Toast.LENGTH_SHORT).show();
                return;
            }

            long id = dbHelper.insertUser(u, p, selectedAvatarId);
            if (id != -1) {
                Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            int avatarId = data.getIntExtra("selectedAvatar", R.drawable.ic_avatar_default);
            avatarImageView.setImageResource(avatarId);
            selectedAvatarId = avatarId;
        }
    }
}
