package com.ocunapse.aplicondo.guard.ui.dashboard;

import static com.ocunapse.aplicondo.guard.api.RequestBase.LOG;
import static com.ocunapse.aplicondo.guard.ui.WalkInActivity.getCaptureImageOutputUri;
import static com.ocunapse.aplicondo.guard.ui.WalkInActivity.getPickImageResultUri;
import static com.ocunapse.aplicondo.guard.ui.WalkInActivity.getResizedBitmap;
import static com.ocunapse.aplicondo.guard.ui.WalkInActivity.rotateImageIfRequired;

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


        binding.reportCameraButton.setOnClickListener(view -> {
            startActivityForResult(getPickImageChooserIntent(), 200);
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


    public Intent getPickImageChooserIntent() {

        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = (Intent) allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap myBitmap;

        Log.e("ss-d", "onActivityResult: " + getPickImageResultUri(data));
        Log.e("ss-d", "onActivityResult: " + binding.reportDocumentView.getChildAt(0));
        if (resultCode == Activity.RESULT_OK && getPickImageResultUri(data) != null) {

            ImageView imageView = new ImageView(this);
            imageView.setPadding(30, 20, 30, 20);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

            Log.e("ss-d", "onActivityResult: " + getCaptureImageOutputUri());
            if (getPickImageResultUri(data) != null) {
                picUri = getPickImageResultUri(data);
                try {
                    myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri);
                    myBitmap = rotateImageIfRequired(myBitmap, picUri);
                    myBitmap = getResizedBitmap(myBitmap, 500);

                    imageView.setImageBitmap(myBitmap);
                    binding.reportDocumentView.setVisibility(View.VISIBLE);
                    binding.reportDocumentView.addView(imageView);
                    binding.reportCameraButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));

                } catch (IOException e) {
                    Log.e("ss-d", "onActivityResult: ", e);
                    e.printStackTrace();
                }


            } else {

                myBitmap = (Bitmap) data.getExtras().get("data");

                Log.e("ss-d", "onActivityResult: " + myBitmap);
                assert myBitmap != null;
                myBitmap = getResizedBitmap(myBitmap, 500);
                imageView.setImageBitmap(myBitmap);
                binding.reportDocumentView.setVisibility(View.VISIBLE);
                binding.reportDocumentView.addView(imageView);
                binding.reportCameraButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
            }
            binding.reportDocumentView.setWeightSum(binding.reportDocumentView.getChildCount());
        }


    }

}