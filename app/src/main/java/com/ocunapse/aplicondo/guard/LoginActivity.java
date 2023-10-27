package com.ocunapse.aplicondo.guard;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.ui.AppBarConfiguration;

import com.ocunapse.aplicondo.guard.databinding.ActivityMainBinding;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

public class LoginActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("userId", binding.loginUsername.getText().toString())
                .add("password", "bf9a36f1d8bc732a2d5a5925733b63d3")
                .build();
        Request request = new Request.Builder()
                .post(formBody)
                .url("https://aplicondo.ocunapse.com/api/login")
                .build();

        binding.loginButton.setOnClickListener(v -> {
            //TODO: http call
            //Response response = client.newCall(request).execute();

            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
        });
    }

}