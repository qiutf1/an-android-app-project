package com.example.myapp4;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, registerButton;
    private ImageView avatarImageView, eyeImageView;
    private ProgressBar progressBar;
    private String username, password;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);
        avatarImageView = findViewById(R.id.avatarImageView);
        progressBar = findViewById(R.id.progressBar);
        eyeImageView = findViewById(R.id.eyeImageView);

        dbHelper = new DatabaseHelper(this);

        // 登录按钮点击事件
        loginButton.setOnClickListener(v -> {
            username = usernameEditText.getText().toString();
            password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                new android.os.Handler().postDelayed(() -> {
                    progressBar.setVisibility(View.GONE);

                    // 验证用户信息
                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    Cursor cursor = db.query(
                            DatabaseHelper.TABLE_USERS,
                            new String[]{DatabaseHelper.COLUMN_USERNAME, DatabaseHelper.COLUMN_PASSWORD},
                            DatabaseHelper.COLUMN_USERNAME + "=?",
                            new String[]{username},
                            null, null, null
                    );

                    if (cursor != null && cursor.moveToFirst()) {
                        String storedPassword = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD));
                        if (storedPassword.equals(password)) {
                            // 登录成功
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
                        }
                        cursor.close();
                    } else {
                        Toast.makeText(LoginActivity.this, "用户未注册，请先注册", Toast.LENGTH_SHORT).show();
                    }
                }, 2000);
            }
        });

        // 注册按钮点击事件
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 头像选择事件
        avatarImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);
        });

        // 眼睛图标切换明文和密文
        eyeImageView.setOnClickListener(v -> {
            if (passwordEditText.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                // 如果是密文，则切换为明文
                passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_NORMAL);
                eyeImageView.setImageResource(R.drawable.ic_eye_open); // 切换为打开眼睛图标
            } else {
                // 如果是明文，则切换为密文
                passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                eyeImageView.setImageResource(R.drawable.ic_eye_closed); // 切换为关闭眼睛图标
            }
            passwordEditText.setSelection(passwordEditText.getText().length()); // 保持光标位置
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                avatarImageView.setImageURI(data.getData());
            }
        }
    }
}
