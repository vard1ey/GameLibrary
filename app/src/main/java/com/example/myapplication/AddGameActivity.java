package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class AddGameActivity extends AppCompatActivity {

    private EditText titleEditText, noteEditText;
    private Spinner genreSpinner, platformSpinner;
    private Button addButton, chooseAppButton;
    private TextView selectedAppTextView, chooseAppLabel;
    private DatabaseHelper databaseHelper;
    private AuthManager authManager;

    private String selectedPackageName = null;
    private String selectedAppName = null;
    private boolean isMobilePlatform = false;

    private final ActivityResultLauncher<Intent> appChooserLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String packageName = result.getData().getStringExtra("selected_package");
                    String appName = result.getData().getStringExtra("selected_app_name");

                    if (packageName != null && appName != null) {
                        selectedPackageName = packageName;
                        selectedAppName = appName;
                        selectedAppTextView.setText("Выбрано: " + appName);
                    }
                }
            }
    );

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

        updateAppSelectionVisibility();
    }

    private void initViews() {
        titleEditText = findViewById(R.id.titleEditText);
        noteEditText = findViewById(R.id.notesEditText);
        genreSpinner = findViewById(R.id.genreSpinner);
        platformSpinner = findViewById(R.id.platformSpinner);
        addButton = findViewById(R.id.addButton);
        chooseAppButton = findViewById(R.id.chooseAppButton);
        selectedAppTextView = findViewById(R.id.selectedAppTextView);

        chooseAppLabel = findViewById(R.id.chooseAppLabel);
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

        // Слушатель для платформы
        platformSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPlatform = parent.getItemAtPosition(position).toString();
                isMobilePlatform = "Mobile".equals(selectedPlatform);
                updateAppSelectionVisibility();

                // Если платформа не Mobile, сбрасываем выбор приложения
                if (!isMobilePlatform) {
                    selectedPackageName = null;
                    selectedAppName = null;
                    selectedAppTextView.setText("Приложение не выбрано");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                isMobilePlatform = false;
                updateAppSelectionVisibility();
            }
        });
    }

    private void updateAppSelectionVisibility() {
        if (chooseAppButton != null && selectedAppTextView != null) {
            if (isMobilePlatform) {
                chooseAppButton.setVisibility(View.VISIBLE);
                selectedAppTextView.setVisibility(View.VISIBLE);
                if (chooseAppLabel != null) {
                    chooseAppLabel.setVisibility(View.VISIBLE);
                }
                chooseAppButton.setEnabled(true);
            } else {
                chooseAppButton.setVisibility(View.GONE);
                selectedAppTextView.setVisibility(View.GONE);
                if (chooseAppLabel != null) {
                    chooseAppLabel.setVisibility(View.GONE);
                }
            }
        }
    }

    private void setupClickListeners() {
        addButton.setOnClickListener(v -> addGame());
        chooseAppButton.setOnClickListener(v -> openAppChooser());
    }

    private void openAppChooser() {
        Intent intent = new Intent(this, AppChooserActivity.class);
        appChooserLauncher.launch(intent);
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

        if (isMobilePlatform && (selectedPackageName == null || selectedPackageName.isEmpty())) {
            Toast.makeText(this, "Выберите приложение для запуска", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isMobilePlatform && selectedPackageName != null && !selectedPackageName.isEmpty()) {
            selectedPackageName = null;
        }

        Game game = new Game(title, genre, platform, "Не начата", userId, note);
        game.setAppPackageName(selectedPackageName);

        long result = databaseHelper.addGame(game);

        if (result != -1) {
            Toast.makeText(this, "Игра добавлена!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Ошибка при добавлении игры", Toast.LENGTH_LONG).show();
        }
    }
}