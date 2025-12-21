package com.example.myapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddGameActivity extends AppCompatActivity {

    private EditText titleEditText, noteEditText;
    private Spinner genreSpinner, platformSpinner;
    private Button addButton;
    private DatabaseHelper databaseHelper;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_game);

        databaseHelper = new DatabaseHelper(this);
        authManager = new AuthManager(this);

        if (!authManager.isLoggedIn()) {
            finish();
            return;
        }

        initViews();
        setupSpinners();
        setupClickListeners();
    }

    private void initViews() {
        titleEditText = findViewById(R.id.titleEditText);
        noteEditText = findViewById(R.id.notesEditText);
        genreSpinner = findViewById(R.id.genreSpinner);
        platformSpinner = findViewById(R.id.platformSpinner);
        addButton = findViewById(R.id.addButton);
    }

    private void setupSpinners() {
        String[] zhanr = {"Выберите жанр", "Экшен", "RPG", "Стратегия", "Приключение",
                "Гонки", "Симулятор", "Спорт", "Хоррор", "Инди", "Другое"};
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, zhanr);
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genreSpinner.setAdapter(genreAdapter);

        String[] platforms = {"Выберите платформу", "PC", "PlayStation", "Xbox",
                "Nintendo Switch", "Mobile", "Другое"};
        ArrayAdapter<String> platformAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, platforms);
        platformAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        platformSpinner.setAdapter(platformAdapter);
    }

    private void setupClickListeners() {
        addButton.setOnClickListener(v -> addGame());
    }

    private void addGame() {
        String title = titleEditText.getText().toString().trim();
        String genre = genreSpinner.getSelectedItem().toString();
        String platform = platformSpinner.getSelectedItem().toString();
        String note = noteEditText.getText().toString().trim();
        int userId = authManager.getCurrentUserId();

        if (title.isEmpty()) {
            titleEditText.setError("Введите название игры");
            return;
        }

        if (genre.equals("Выберите жанр")) {
            Toast.makeText(this, "Выберите жанр", Toast.LENGTH_SHORT).show();
            return;
        }

        if (platform.equals("Выберите платформу")) {
            Toast.makeText(this, "Выберите платформу", Toast.LENGTH_SHORT).show();
            return;
        }

        Game game = new Game(title, genre, platform, "Не начата", userId, note);

        long result = databaseHelper.addGame(game);

        if (result != -1) {
            Toast.makeText(this, "Игра добавлена!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка при добавлении игры", Toast.LENGTH_LONG).show();
        }
    }
}