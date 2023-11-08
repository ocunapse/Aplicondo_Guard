package com.ocunapse.aplicondo.guard;


import static com.ocunapse.aplicondo.guard.util.StringUtil.md5;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.ui.AppBarConfiguration;

import com.auth0.android.jwt.JWT;
import com.google.android.material.snackbar.Snackbar;
import com.ocunapse.aplicondo.guard.api.LoginRequest;
import com.ocunapse.aplicondo.guard.databinding.ActivityLoginBinding;

import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    private GuardApp application = new GuardApp();
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if(application.getToken() != null) {
            finish();
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
        }

        binding.loginButton.setOnClickListener(v -> {
            String uname = binding.loginUsername.getText().toString();
            String pwd = md5(binding.loginPassword.getText().toString());
            System.out.println(uname + " " + pwd);
            ProgressDialog dialog = ProgressDialog.show(this, "",
                    "Loading. Please wait...", true);
            LoginRequest lr = new LoginRequest(uname,pwd, res -> {
                if(res.success){
                    JWT jwt = new JWT(res.tokens.accessToken);
                    String role = Objects.requireNonNull(jwt.getClaim("role").asString());
                    if(!role.equals("Security")) {
                        Snackbar.make(v, "Not Authorized", Snackbar.LENGTH_LONG).show();
                    }
                    application.setToken(res.tokens.accessToken);
                    finish();
                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(i);
                }else{
                    Snackbar.make(v,"Not Authorized",Snackbar.LENGTH_LONG).show();
                }
                dialog.dismiss();
            });
            lr.execute();
        });
    }



}