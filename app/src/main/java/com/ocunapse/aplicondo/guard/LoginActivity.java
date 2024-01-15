package com.ocunapse.aplicondo.guard;


import static com.ocunapse.aplicondo.guard.util.StringUtil.md5;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;

import com.auth0.android.jwt.JWT;
import com.google.android.material.snackbar.Snackbar;
import com.ocunapse.aplicondo.guard.api.LoginRequest;
import com.ocunapse.aplicondo.guard.databinding.ActivityLoginBinding;
import com.ocunapse.aplicondo.guard.ui.dashboard.SOSActivity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


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

//        final Drawable drawable= ContextCompat.getDrawable(this, R.drawable.mifi);
//        final ImageView iv = binding.loginLogo;
//        iv.setRotationY(0f);
//        binding.textView.setRotationX(0f);
//        AtomicInteger counter = new AtomicInteger(1);

//        iv.setOnClickListener(view -> {
//            if(counter.get() >5) {
//                iv.animate().rotationY(90f).setListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        iv.setImageDrawable(drawable);
//                        iv.setRotationY(270f);
//                        iv.animate().rotationY(360f).setListener(null);
//                        binding.textView.setText("FUCK YOU!!!");
//                        binding.textView.setRotationX(270f);
//                        binding.textView.animate().rotationX(360f).setListener(null);
//                    }
//                });
//            }else counter.addAndGet(1);
//        });


        binding.loginButton.setOnClickListener(v -> {
            String uname = binding.loginUsername.getText().toString().toLowerCase();
            String pwd = md5(binding.loginPassword.getText().toString()).toLowerCase();
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
                   binding.loginLogo.performClick();
                }
                dialog.dismiss();
            });
            lr.execute();
        });
    }



}