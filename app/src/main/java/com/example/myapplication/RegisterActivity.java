package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword;
    private Button buttonRegister;
    private TextView loginLink;
    private DatabaseHelper databaseHelper;
    private final Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.emailEditText);
        editTextPassword = findViewById(R.id.passwordEditText);
        buttonRegister = findViewById(R.id.registerButton);
        loginLink = findViewById(R.id.loginLink);
    }

    private void setupClickListeners() {
        buttonRegister.setOnClickListener(v -> registerUser());
        loginLink.setOnClickListener(v -> {
            finish();
        });
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Все поля должны быть заполнены", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!isValidLogin(name)){
            Toast.makeText(this, "В логине не должно быть спец.символов", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!isValidEmail(email)){
            Toast.makeText(this, "Некорректный email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        if(isStartsWithSpecialChar(password)){
            Toast.makeText(this, "Пароль не должен начинаться со спец.символа", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.checkUserExists(email)) {
            Toast.makeText(this, "Пользователь с таким email уже существует", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonRegister.setEnabled(false);
        buttonRegister.setText("Регистрация...");

        long result = databaseHelper.registerUser(name, email, password);

        buttonRegister.setEnabled(true);
        buttonRegister.setText("Зарегистрироваться");

        if (result != -1) {
            Toast.makeText(RegisterActivity.this,
                    "Регистрация успешна! Теперь войдите в систему", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(RegisterActivity.this,
                    "Ошибка регистрации", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isValidEmail(String email) {
        if (email.isEmpty()) {
            return false;
        }
        Matcher matcher = emailPattern.matcher(email);
        return matcher.matches();
    }

    boolean isStartsWithSpecialChar(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        String pattern = "^[^a-zA-Zа-яА-Я0-9].*";
        return password.matches(pattern);
    }

    boolean isValidLogin(String username){
        if (username == null || username.isEmpty()) {
            return false;
        }

        String pattern = "^[a-zA-Z0-9_-]+$";
        return username.matches(pattern);
    }

}