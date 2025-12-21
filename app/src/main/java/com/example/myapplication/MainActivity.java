package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.myapplication.AuthManager authManager = new com.example.myapplication.AuthManager(this);

        // Проверяем, авторизован ли пользователь
        if (authManager.isLoggedIn()) {
            // Пользователь уже вошел - переходим в библиотеку игр
            startActivity(new Intent(this, com.example.myapplication.GameLibraryActivity.class));
        } else {
            // Пользователь не вошел - показываем окно входа
            startActivity(new Intent(this, com.example.myapplication.LoginActivity.class));
        }

        finish();
    }
}