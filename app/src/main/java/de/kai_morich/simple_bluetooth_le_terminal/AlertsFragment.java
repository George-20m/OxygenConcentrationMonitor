package de.kai_morich.simple_bluetooth_le_terminal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertsFragment extends Fragment {

    public static class AlertItem {
        public String title;
        public String message;
        public String time;
        public int type; // 0=red, 1=orange, 2=blue, 3=green

        public AlertItem(String title, String message, int type) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        }
    }

    public static final List<AlertItem> alertLog = new ArrayList<>();

    public static void addAlert(String title, String message, int type) {
        alertLog.add(0, new AlertItem(title, message, type));
        if (alertLog.size() > 50) alertLog.remove(alertLog.size() - 1);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && getView() != null) {
            refreshAlerts(getView());
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alerts, container, false);
        refreshAlerts(view);
        return view;
    }

    private void refreshAlerts(View view) {
        LinearLayout container2 = view.findViewById(R.id.alertsContainer);
        container2.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (alertLog.isEmpty()) {
            View empty = inflater.inflate(R.layout.item_alert_empty, container2, false);
            container2.addView(empty);
        } else {
            for (AlertItem alert : alertLog) {
                View item = inflater.inflate(R.layout.item_alert, container2, false);
                TextView tvTitle = item.findViewById(R.id.alertTitle);
                TextView tvMessage = item.findViewById(R.id.alertMessage);
                TextView tvTime = item.findViewById(R.id.alertTime);
                View indicator = item.findViewById(R.id.alertIndicator);
                View card = item.findViewById(R.id.alertCard);

                tvTitle.setText(alert.title);
                tvMessage.setText(alert.message);
                tvTime.setText(alert.time);

                int indicatorColor, bgColor;
                switch (alert.type) {
                    case 0: indicatorColor = 0xFFE53935; bgColor = 0xFFFFF0F0; break;
                    case 1: indicatorColor = 0xFFFFA000; bgColor = 0xFFFFF8E1; break;
                    case 2: indicatorColor = 0xFF0288D1; bgColor = 0xFFE1F5FE; break;
                    case 3: indicatorColor = 0xFF43A047; bgColor = 0xFFF1F8E9; break;
                    default: indicatorColor = 0xFF888888; bgColor = 0xFFF5F5F5;
                }
                indicator.setBackgroundColor(indicatorColor);
                
                android.graphics.drawable.GradientDrawable bgDrawable = 
                    new android.graphics.drawable.GradientDrawable();
                bgDrawable.setColor(bgColor);
                bgDrawable.setCornerRadius(16f);
                card.setBackground(bgDrawable);

                container2.addView(item);
            }
        }
    }
}
