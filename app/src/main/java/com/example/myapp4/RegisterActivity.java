package com.example.myapp4;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button registerButton, backButton;
    private ImageView eyeImageView;
    private DatabaseHelper dbHelper;
    private boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);
        eyeImageView = findViewById(R.id.eyeImageView);

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

        registerButton.setOnClickListener(v -> {
            String u = usernameEditText.getText().toString().trim();
            String p = passwordEditText.getText().toString().trim();
            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if (dbHelper.isUserExists(u)) {
                Toast.makeText(RegisterActivity.this, "用户名已存在，请换一个", Toast.LENGTH_SHORT).show();
                return;
            }
            long id = dbHelper.insertUser(u, p);
            if (id != -1) {
                Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }
}
