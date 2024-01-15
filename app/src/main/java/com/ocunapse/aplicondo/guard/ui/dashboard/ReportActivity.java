package com.ocunapse.aplicondo.guard.ui.dashboard;

import static com.ocunapse.aplicondo.guard.api.RequestBase.LOG;
import static com.ocunapse.aplicondo.guard.ui.WalkInActivity.getResizedBitmap;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.ocunapse.aplicondo.guard.api.ReportRequest;
import com.ocunapse.aplicondo.guard.api.UploadReportImages;
import com.ocunapse.aplicondo.guard.databinding.ActivityReportBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportActivity extends AppCompatActivity {
    ActivityReportBinding binding;

    ReportRequest.GuardReportType type;
    Uri picUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent i = getIntent();
        int sos_id = i.getIntExtra("sos_id", -1);
        String unit_name = i.getStringExtra("unit_label");
        String name = i.getStringExtra("name");
        String phone = i.getStringExtra("phone");
        LOG("--report", String.valueOf(sos_id));
        Toolbar tb = binding.reportToolbar;
        if (sos_id > 0) {
            tb.setTitle("New SOS Report");
            type = ReportRequest.GuardReportType.SOS;
            binding.reportUnitNumEt.setText(unit_name);
            binding.reportNameEt.setText(name);
            binding.reportPhoneNumEt.setText(phone);
            binding.reportUnitNumEt.setEnabled(false);
            binding.reportNameEt.setEnabled(false);
        } else {
            tb.setTitle("New Report");
            type = ReportRequest.GuardReportType.Incident;
        }

        binding.reportDocumentView.setVisibility(View.GONE);
        binding.reportCameraButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

        binding.submitReport.setOnClickListener(view -> {
            if (verify().length() > 0) {
                Snackbar.make(view, verify(), Snackbar.LENGTH_LONG).show();
                return;
            }

            ProgressDialog pd = ProgressDialog.show(this, "Loading...",
                    "Loading. Please wait...", true);

            String unit_number = binding.reportUnitNumEt.getText().toString();
            String resident_name = binding.reportNameEt.getText().toString();
            String contact = binding.reportPhoneNumEt.getText().toString();
            String details = binding.reportMsgEt.getText().toString();
            ArrayList<File> files = new ArrayList();
            for (int j = 0; j < binding.reportDocumentView.getChildCount(); j++) {
                ImageView img = (ImageView) binding.reportDocumentView.getChildAt(j);


                try {
                    File f = File.createTempFile("document_" + new Date().getTime(), ".png");
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ((BitmapDrawable) img.getDrawable()).getBitmap().compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                    byte[] bitmapdata = bos.toByteArray();

                    //write the bytes in file
                    FileOutputStream fos = null;
                    fos = new FileOutputStream(f);

                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                    files.add(f);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (files.size() > 0) {
                new UploadReportImages(files.toArray(new File[0]), uRes -> {
                    String[] images = uRes.success ? uRes.data.url : null;
                    report(pd, unit_number, resident_name, contact, details, images, sos_id);
                }).execute();
            } else report(pd, unit_number, resident_name, contact, details, null, sos_id);

        });

        ActivityResultLauncher<Intent> resultLauncher;

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    ImageView imv = new ImageView(this);
                    Bitmap myBitmap = (Bitmap) data.getExtras().get("data");
                    myBitmap = getResizedBitmap(myBitmap, 500);
                    imv.setImageBitmap(myBitmap);
                    binding.reportDocumentView.setVisibility(View.VISIBLE);
                    binding.reportDocumentView.addView(imv);
                    binding.reportCameraButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
                }
            }
        });


        binding.reportCameraButton.setOnClickListener(view -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            resultLauncher.launch(cameraIntent);
        });

    }


    private void report(ProgressDialog pd, String unit_name, String name, String contact, String details, String[] images, int sos_id) {
        if (type == ReportRequest.GuardReportType.SOS) {
            new ReportRequest(type, unit_name, name, contact, details, images, sos_id, res -> {
                pd.dismiss();
                LOG("Report--", String.valueOf(res.success));
                if (res.success) {
                    Toast.makeText(this, "SOS Report Added", Toast.LENGTH_LONG).show();
                    finish();
                } else Toast.makeText(this, "SOS Report Failed", Toast.LENGTH_LONG).show();
            }).execute();
        } else {
            new ReportRequest(type, unit_name, name, contact, details, images, res -> {
                pd.dismiss();
                if (res.success) {
                    Toast.makeText(this, "Report Added", Toast.LENGTH_LONG).show();
                    finish();
                } else Toast.makeText(this, "Report Adding failed", Toast.LENGTH_LONG).show();
            }).execute();
        }
    }

    private String verify() {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        if (type == ReportRequest.GuardReportType.SOS) {
            if (binding.reportMsgEt.getText().toString().trim().length() < 3)
                return "Report message should be more than 3 letters";
        } else {
            int ulen = binding.reportUnitNumEt.getText().toString().trim().length();
            if (ulen > 0 && ulen < 3) return "Invalid Unit";
            int plen = binding.reportPhoneNumEt.getText().toString().trim().length();
            if (plen > 0) {
                try {
                    Phonenumber.PhoneNumber number = phoneNumberUtil.parse(binding.reportPhoneNumEt.getText().toString().trim(), "MY");
                    if (!phoneNumberUtil.isValidNumber(number)) return "Invalid Phone Number";
                    else
                        binding.reportPhoneNumEt.setText(phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164));
                } catch (Exception ignored) {
                    System.out.println(ignored.getMessage());
                    return "Invalid Phone Number";
                }
            }
        }

        return "";
    }

}