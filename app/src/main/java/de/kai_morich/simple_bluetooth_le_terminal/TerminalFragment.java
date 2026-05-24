package de.kai_morich.simple_bluetooth_le_terminal;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.preference.PreferenceManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import de.kai_morich.simple_bluetooth_le_terminal.SpeedometerView;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TerminalFragment extends Fragment implements ServiceConnection, SerialListener {

    private enum Connected { False, Pending, True }

    private String deviceAddress;
    private SerialService service;

    private TextView receiveText;
    private TextView sendText;
    private TextUtil.HexWatcher hexWatcher;

    // ← NEW: UI elements
    private TextView tvO2, tvFlow, tvTemp, tvFlowUnit, tvTempUnit, tvStatus, tvLive, tvBattery;
    private CardView o2Card;
    private SpeedometerView speedometerView;
    private StringBuilder dataBuffer = new StringBuilder();
    private String lastLine = "";
    private android.content.SharedPreferences prefs;

    private Connected connected = Connected.False;
    private boolean initialStart = true;
    private boolean hexEnabled = false;
    private boolean pendingNewline = false;
    private String newline = TextUtil.newline_crlf;

    /*
     * Lifecycle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        deviceAddress = getArguments().getString("device");
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class));
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(initialStart && service != null) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /*
     * UI
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);
        receiveText = view.findViewById(R.id.receive_text);
        receiveText.setTextColor(getResources().getColor(R.color.colorRecieveText));
        receiveText.setMovementMethod(ScrollingMovementMethod.getInstance());

        sendText = view.findViewById(R.id.send_text);
        hexWatcher = new TextUtil.HexWatcher(sendText);
        hexWatcher.enable(hexEnabled);
        sendText.addTextChangedListener(hexWatcher);
        sendText.setHint(hexEnabled ? "HEX mode" : "");

        View sendBtn = view.findViewById(R.id.send_btn);
        sendBtn.setOnClickListener(v -> send(sendText.getText().toString()));

        // ← NEW: bind UI elements
        tvO2     = view.findViewById(R.id.tvO2);
        tvFlow   = view.findViewById(R.id.tvFlow);
        tvTemp   = view.findViewById(R.id.tvTemp);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvLive   = view.findViewById(R.id.tvLive);
        tvBattery = view.findViewById(R.id.tvBattery);
        tvFlowUnit = view.findViewById(R.id.tvFlowUnit);
        tvTempUnit = view.findViewById(R.id.tvTempUnit);
        o2Card   = view.findViewById(R.id.o2Card);
        speedometerView = view.findViewById(R.id.speedometerView);
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        prefs.registerOnSharedPreferenceChangeListener((p, key) -> {
            if (!lastLine.isEmpty()) {
                getActivity().runOnUiThread(() -> parseAndUpdateUI(lastLine));
            }
        });

        return view;
    }

    private void setO2Alert(boolean alert) {
        int color = alert ? 0xFFFF3333 : 0xFF02799A;
        if (o2Card != null) o2Card.setCardBackgroundColor(color);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        menu.clear();
        menu.add(0, R.id.action_settings, 0, "Settings")
            .setIcon(android.R.drawable.ic_menu_manage)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * Serial + UI
     */
    private void connect() {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void send(String str) {
        if(connected != Connected.True) {
            Toast.makeText(getActivity(), "not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String msg;
            byte[] data;
            if(hexEnabled) {
                StringBuilder sb = new StringBuilder();
                TextUtil.toHexString(sb, TextUtil.fromHexString(str));
                TextUtil.toHexString(sb, newline.getBytes());
                msg = sb.toString();
                data = TextUtil.fromHexString(msg);
            } else {
                msg = str;
                data = (str + newline).getBytes();
            }
            SpannableStringBuilder spn = new SpannableStringBuilder(msg + '\n');
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSendText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            receiveText.append(spn);
            service.write(data);
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    // ← NEW: parse data and update UI cards
    private void parseAndUpdateUI(String line) {
        if (line.contains("ERR")) return;
        try {
            String[] parts = line.trim().split("\\s{2,}");
            if (parts.length >= 3) {
                float o2Val = Float.parseFloat(parts[0].replace("%Vol", "").trim());
                tvO2.setText(String.valueOf((int)o2Val));
                float flowVal = Float.parseFloat(parts[1].replace("L/min", "").trim());
                String flowUnit = prefs.getString(SettingsFragment.KEY_FLOW_UNIT, "L");
                if (flowUnit.equals("mL")) {
                    tvFlow.setText(String.valueOf((int)(flowVal * 1000)));
                    tvFlowUnit.setText("mL/min");
                } else {
                    tvFlow.setText(String.valueOf(flowVal));
                    tvFlowUnit.setText("L/min");
                }
                float tempVal = Float.parseFloat(parts[2].replace("C", "").trim());
                String tempUnit = prefs.getString(SettingsFragment.KEY_TEMP_UNIT, "C");
                if (tempUnit.equals("F")) {
                    float tempF = (tempVal * 9f / 5f) + 32f;
                    tvTemp.setText(String.valueOf((int)tempF));
                    tvTempUnit.setText("°F");
                } else {
                    tvTemp.setText(String.valueOf((int)tempVal));
                    tvTempUnit.setText("°C");
                }
                getActivity().runOnUiThread(() -> setO2Alert(o2Val < 80));
                float finalO2 = o2Val;
                getActivity().runOnUiThread(() -> { if (speedometerView != null) speedometerView.setValue(finalO2); });
            }
            if (parts.length >= 4) {
                Matcher m = Pattern.compile("\\(?(\\d+)%\\)?").matcher(parts[3]);
                if (m.find()) {
                    int pct = Integer.parseInt(m.group(1));
                    tvBattery.setText("🔋 " + m.group());
                    if (pct < 30) {
                        tvBattery.setTextColor(getResources().getColor(R.color.battery_red));
                    } else if (pct < 60) {
                        tvBattery.setTextColor(getResources().getColor(R.color.battery_orange));
                    } else {
                        tvBattery.setTextColor(getResources().getColor(R.color.battery_green));
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void receive(ArrayDeque<byte[]> datas) {
        SpannableStringBuilder spn = new SpannableStringBuilder();
        for (byte[] data : datas) {
            if (hexEnabled) {
                spn.append(TextUtil.toHexString(data)).append('\n');
            } else {
                String msg = new String(data);
                if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
                    msg = msg.replace(TextUtil.newline_crlf, TextUtil.newline_lf);
                    if (pendingNewline && msg.charAt(0) == '\n') {
                        if(spn.length() >= 2) {
                            spn.delete(spn.length() - 2, spn.length());
                        } else {
                            Editable edt = receiveText.getEditableText();
                            if (edt != null && edt.length() >= 2)
                                edt.delete(edt.length() - 2, edt.length());
                        }
                    }
                    pendingNewline = msg.charAt(msg.length() - 1) == '\r';
                }
                spn.append(TextUtil.toCaretString(msg, newline.length() != 0));

                // ← NEW: parse incoming data
                dataBuffer.append(msg);
                while (dataBuffer.indexOf("\n") != -1) {
                    int idx = dataBuffer.indexOf("\n");
                    String line = dataBuffer.substring(0, idx).trim();
                    dataBuffer.delete(0, idx + 1);
                    if (!line.isEmpty()) {
                        lastLine = line;
                        parseAndUpdateUI(line);
                    }
                }
            }
        }
        receiveText.append(spn);
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        receiveText.append(spn);
    }

    private void showNotificationSettings() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("android.provider.extra.APP_PACKAGE", getActivity().getPackageName());
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(Arrays.equals(permissions, new String[]{Manifest.permission.POST_NOTIFICATIONS}) &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !service.areNotificationsEnabled())
            showNotificationSettings();
    }

    /*
     * SerialListener
     */
    @Override
    public void onSerialConnect() {
        status("connected");
        tvStatus.setText("Connected");           // ← NEW
        tvLive.setVisibility(View.VISIBLE);       // ← NEW
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        status("connection failed: " + e.getMessage());
        tvStatus.setText("Connection failed");    // ← NEW
        tvLive.setVisibility(View.GONE);          // ← NEW
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        ArrayDeque<byte[]> datas = new ArrayDeque<>();
        datas.add(data);
        receive(datas);
    }

    public void onSerialRead(ArrayDeque<byte[]> datas) {
        receive(datas);
    }

    @Override
    public void onSerialIoError(Exception e) {
        status("connection lost: " + e.getMessage());
        tvStatus.setText("Connection lost");      // ← NEW
        tvLive.setVisibility(View.GONE);          // ← NEW
        disconnect();
    }
}