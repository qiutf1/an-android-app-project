package com.example.myapp4;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, registerButton;
    private ImageView eyeImageView;
    private ProgressBar progressBar;
    private DatabaseHelper dbHelper;
    private boolean passwordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        eyeImageView = findViewById(R.id.eyeImageView);
        progressBar = findViewById(R.id.progressBar);

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

        // 登录
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            new android.os.Handler().postDelayed(() -> {
                progressBar.setVisibility(View.GONE);

                if (!dbHelper.isUserExists(username)) {
                    Toast.makeText(LoginActivity.this, "用户未注册，请先注册", Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean ok = dbHelper.checkUserPassword(username, password);
                if (ok) {
                    // ✅ 获取用户头像
                    int avatarRes = dbHelper.getUserAvatar(username);

                    // ✅ 保存用户名和头像
                    SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
                    prefs.edit()
                            .putString("logged_in_user", username)
                            .putInt("logged_in_avatar", avatarRes)
                            .apply();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                }
            }, 800);
        });

        // 跳转注册
        registerButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }
}
