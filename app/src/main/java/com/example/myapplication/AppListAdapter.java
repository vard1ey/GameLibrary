package com.example.myapplication;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AppListAdapter extends ArrayAdapter<ApplicationInfo> {

    private Context context;
    private List<ApplicationInfo> appsList;
    private PackageManager packageManager;

    public AppListAdapter(Context context, List<ApplicationInfo> appsList, PackageManager packageManager) {
        super(context, R.layout.list_item_app, appsList);
        this.context = context;
        this.appsList = appsList;
        this.packageManager = packageManager;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_app, parent, false);
        }

        ApplicationInfo appInfo = appsList.get(position);

        ImageView appIcon = convertView.findViewById(R.id.appIcon);
        TextView appName = convertView.findViewById(R.id.appName);
        TextView appPackage = convertView.findViewById(R.id.appPackage);

        appIcon.setImageDrawable(appInfo.loadIcon(packageManager));

        String name = appInfo.loadLabel(packageManager).toString();

        if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            name = "⚙️ " + name + " (системное)";
        } else if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            name = "🔄 " + name + " (обновлено системное)";
        }

        appName.setText(name);
        appPackage.setText(appInfo.packageName);

        return convertView;
    }
}