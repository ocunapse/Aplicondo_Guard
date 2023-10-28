package com.ocunapse.aplicondo.guard;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.ui.AppBarConfiguration;

import com.ocunapse.aplicondo.guard.databinding.ActivityMainBinding;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        binding.loginButton.setOnClickListener(v -> {
            String uname = binding.loginUsername.getText().toString();
            String pwd = binding.loginPassword.getText().toString();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final MediaType JSON = MediaType.get("application/json");
                        OkHttpClient client = new OkHttpClient();
                        RequestBody formBody = RequestBody.create("{\"userId\":\""+uname+"\",\"password\":\""+md5(pwd).toLowerCase()+"\"}",JSON);
                        Request request = new Request.Builder()
                                .post(formBody)
                                .url("https://aplicondo.ocunapse.com/api/login")
                                .build();
                        try{
                            Response response = client.newCall(request).execute();
                            System.out.println(response.body().string());
                            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                            startActivity(i);
                        }catch ( IOException err){
                            System.out.println("err = " + err);;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();

        });
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(String.format("%02X", messageDigest[i]));

            return hexString.toString();
        }catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}