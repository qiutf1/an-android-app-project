package com.example.myapp4;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button registerButton, backButton;
    private ImageView avatarImageView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化控件
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        backButton = findViewById(R.id.backButton);
        avatarImageView = findViewById(R.id.avatarImageView);

        // 初始化数据库助手
        dbHelper = new DatabaseHelper(this);

        // 头像选择事件
        avatarImageView.setOnClickListener(v -> {
            // 让用户选择头像
            Intent intent = new Intent(RegisterActivity.this, AvatarSelectionActivity.class);
            startActivityForResult(intent, 1);
        });

        // 注册按钮点击事件
        registerButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
            } else {
                // 将用户数据插入数据库
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_USERNAME, username);
                values.put(DatabaseHelper.COLUMN_PASSWORD, password);
                // 保存头像路径
                values.put(DatabaseHelper.COLUMN_AVATAR, avatarImageView.getTag().toString()); // 保存头像资源ID

                long newRowId = db.insert(DatabaseHelper.TABLE_USERS, null, values);

                if (newRowId != -1) {
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    finish(); // 注册成功后返回登录界面
                } else {
                    Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 返回按钮点击事件
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // 结束当前活动
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                int avatarId = data.getIntExtra("selectedAvatar", R.drawable.ic_avatar_default); // 默认头像
                avatarImageView.setImageResource(avatarId);
                avatarImageView.setTag(avatarId); // 设置头像的ID，以便存入数据库
            }
        }
    }
}
