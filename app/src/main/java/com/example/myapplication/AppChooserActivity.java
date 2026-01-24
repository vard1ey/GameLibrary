package com.example.myapplication;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class AppChooserActivity extends AppCompatActivity {

    private ListView appsListView;
    private PackageManager packageManager;
    private List<ApplicationInfo> appsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_chooser);

        appsListView = findViewById(R.id.appsListView);
        packageManager = getPackageManager();

        loadGameApps();
        setupListViewListener();
    }

    private void loadGameApps() {
        List<ApplicationInfo> allApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        List<ApplicationInfo> gameApps = new ArrayList<>();

        for (ApplicationInfo app : allApps) {
            if (isGameApp(app)) {
                gameApps.add(app);
            }
        }

        if (gameApps.isEmpty()) {
            Toast.makeText(this, "Игры не найдены. Установите игры из Google Play.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        gameApps.sort((app1, app2) ->
                app1.loadLabel(packageManager).toString()
                        .compareToIgnoreCase(app2.loadLabel(packageManager).toString()));

        List<String> appNames = new ArrayList<>();
        for (ApplicationInfo app : gameApps) {
            appNames.add(app.loadLabel(packageManager).toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, appNames);
        appsListView.setAdapter(adapter);

        appsList = gameApps;
    }

    private boolean isGameApp(ApplicationInfo app) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (app.category == ApplicationInfo.CATEGORY_GAME) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setupListViewListener() {
        appsListView.setOnItemClickListener((parent, view, position, id) -> {
            ApplicationInfo selectedApp = appsList.get(position);
            String packageName = selectedApp.packageName;
            String appName = selectedApp.loadLabel(packageManager).toString();

            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_package", packageName);
            resultIntent.putExtra("selected_app_name", appName);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }
}