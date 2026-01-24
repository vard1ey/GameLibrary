package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class GameLibraryActivity extends AppCompatActivity {

    private LinearLayout gamesContainer;
    private Button addGameButton, logoutButton;
    private TextView welcomeText;
    private DatabaseHelper databaseHelper;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        databaseHelper = new DatabaseHelper(this);
        authManager = new AuthManager(this);

        if (!authManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        displayWelcomeMessage();
        loadGames();
    }

    private void initViews() {
        gamesContainer = findViewById(R.id.gamesContainer);
        addGameButton = findViewById(R.id.addGameButton);
        logoutButton = findViewById(R.id.logoutButton);
        welcomeText = findViewById(R.id.welcomeText);
    }

    private void setupClickListeners() {
        addGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameLibraryActivity.this, AddGameActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> logout());
    }

    private void displayWelcomeMessage() {
        if (authManager.isLoggedIn()) {
            String userName = authManager.getCurrentUserName();
            String userEmail = authManager.getCurrentUserEmail();
            welcomeText.setText("Добро пожаловать, " + userName + "!\n" + userEmail);
        }
    }

    private void loadGames() {
        int userId = authManager.getCurrentUserId();
        List<Game> gameList = databaseHelper.getUserGames(userId);

        gamesContainer.removeAllViews();

        if (gameList.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Ваша библиотека игр пуста\n\n" +
                    "Добавьте первую игру!");
            emptyText.setTextColor(getResources().getColor(R.color.text_gray));
            emptyText.setTextSize(18);
            emptyText.setGravity(View.TEXT_ALIGNMENT_CENTER);
            emptyText.setPadding(0, 100, 0, 0);
            gamesContainer.addView(emptyText);
            return;
        }

        for (Game game : gameList) {
            addGameToView(game);
        }
    }

    private void addGameToView(Game game) {
        View gameCard = getLayoutInflater().inflate(R.layout.item_game, null);

        TextView titleTextView = gameCard.findViewById(R.id.titleTextView);
        TextView genreTextView = gameCard.findViewById(R.id.genreTextView);
        TextView statusTextView = gameCard.findViewById(R.id.statusTextView);
        TextView noteTextView = gameCard.findViewById(R.id.noteTextView);
        Button deleteButton = gameCard.findViewById(R.id.deleteButton);
        Button launchButton = gameCard.findViewById(R.id.launch_button);

        titleTextView.setText(game.getTitle());
        genreTextView.setText("Жанр: " + game.getGenre());
        statusTextView.setText("Статус: " + game.getStatus());
        noteTextView.setText(game.getNote());

        // Проверяем, является ли игра мобильной
        boolean isMobileGame = "Mobile".equals(game.getPlatform());

        // Настраиваем кнопку запуска в зависимости от платформы
        if (isMobileGame) {
            if (game.getAppPackageName() != null && !game.getAppPackageName().isEmpty()) {
                launchButton.setEnabled(true);
                launchButton.setVisibility(View.VISIBLE);
            } else {
                launchButton.setText("Приложение не выбрано");
                launchButton.setEnabled(false);
                launchButton.setVisibility(View.VISIBLE);
            }
        } else {
            // Для не-мобильных игр скрываем кнопку запуска
            launchButton.setVisibility(View.GONE);
        }

        deleteButton.setOnClickListener(v -> {
            databaseHelper.deleteGame(game.getId());
            Toast.makeText(this,"Игра удалена", Toast.LENGTH_SHORT).show();
            loadGames();
        });

        launchButton.setOnClickListener(v -> {
            if (game.getAppPackageName() != null && !game.getAppPackageName().isEmpty()) {
                launchApp(game.getAppPackageName());
            } else {
                Toast.makeText(this, "Приложение для запуска не выбрано", Toast.LENGTH_SHORT).show();
            }
        });

        // Обработчик клика по карточке для изменения статуса
        gameCard.setOnClickListener(v -> {
            String currentStatus = game.getStatus();
            String newStatus;

            if (currentStatus.equals("Не начата")) {
                newStatus = "В процессе";
            } else if (currentStatus.equals("В процессе")) {
                newStatus = "Завершена";
            } else {
                newStatus = "Не начата";
            }

            databaseHelper.updateGameStatus(game.getId(), newStatus);
            loadGames();
        });

        gamesContainer.addView(gameCard);
    }

    private void launchApp(String packageName) {
        try {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                startActivity(launchIntent);
            } else {
                // Если не удалось получить launch intent, пытаемся открыть настройки приложения
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(android.net.Uri.parse("package:" + packageName));
                startActivity(intent);
                Toast.makeText(this, "Приложение не может быть запущено напрямую", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка при запуске приложения", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void logout() {
        authManager.logout();
        Toast.makeText(this, "Вы вышли из системы", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGames();
    }
}