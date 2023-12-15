package com.ocunapse.aplicondo.guard.ui.dashboard;

import static com.ocunapse.aplicondo.guard.api.RequestBase.LOG;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.ocunapse.aplicondo.guard.R;
import com.ocunapse.aplicondo.guard.databinding.ActivityLoginBinding;
import com.ocunapse.aplicondo.guard.databinding.ActivitySosBinding;

public class SOSActivity extends AppCompatActivity {
    MediaPlayer mPlayer;
    ActivitySosBinding binding;
    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent i = getIntent();
        String name = i.getStringExtra("name");
        String unit = i.getStringExtra("unit");
        String phone = i.getStringExtra("phone");

        Glide.with(this)
                .load(R.raw.warning)
                .into(binding.imageView);

        mPlayer = MediaPlayer.create(this, R.raw.siren_alarm);
        mPlayer.setLooping(true);
        mPlayer.start();

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] timings = new long[] { 800, 300, 800, 300, 800, 300, 800, 300, 800 };
        int[] amplitudes = new int[] { 100, 0, 50, 0, 150, 0, 50, 0, 250 };
        v.vibrate(VibrationEffect.createWaveform(timings, amplitudes,-1));

        binding.sosName.setText(name);
        binding.sosUnitNum.setText(unit);
        binding.sosPhoneNum.setText(phone);

        Phonenumber.PhoneNumber number = null;
        try {
            number = phoneNumberUtil.parse(phone, "MY");
            if (phoneNumberUtil.isValidNumber(number))
                binding.sosPhoneNum.setPaintFlags(binding.sosPhoneNum.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } catch (Exception ignored) {
            LOG("SOS", "Number invalid");
        }

        Phonenumber.PhoneNumber finalNumber = number;
        binding.sosPhoneNum.setOnClickListener(view -> {
            try {
                if (phoneNumberUtil.isValidNumber(finalNumber)) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneNumberUtil.format(finalNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)));
                    startActivity(intent);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Invalid Phone Number", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayer.stop();
    }

    public void pauseSound(View view) {
        mPlayer.stop();
    }

    @Override
    public void onBackPressed() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);

        assert upIntent != null;
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            startActivity(upIntent);
        }
        super.onBackPressed();
    }
}