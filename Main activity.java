package com.example.antivirus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 1;
    private TextView resultsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultsText = findViewById(R.id.results_text);
        Button scanButton = findViewById(R.id.scan_button);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    scanForMalware();
                }
            }
        });
    }

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void scanForMalware() {
        Set<String> signatures = loadSignatures();
        File dir = Environment.getExternalStorageDirectory();
        StringBuilder results = new StringBuilder("Infected files:\n");

        for (File file : dir.listFiles()) {
            if (file.isFile() && containsMalwareSignature(file, signatures)) {
                results.append(file.getName()).append("\n");
            }
        }

        resultsText.setText(results.length() == 0 ? "No infected files found." : results.toString());
    }

    private Set<String> loadSignatures() {
        Set<String> signatures = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(getAssets().open("signatures.txt").getPath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                signatures.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return signatures;
    }

    private boolean containsMalwareSignature(File file, Set<String> signatures) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String content;
            while ((content = reader.readLine()) != null) {
                for (String signature : signatures) {
                    if (content.contains(signature)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanForMalware();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
              }
