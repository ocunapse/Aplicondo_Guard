package com.ocunapse.aplicondo.guard.ui;


import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.exifinterface.media.ExifInterface;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.ocunapse.aplicondo.guard.api.UnitListRequest;

import com.ocunapse.aplicondo.guard.api.UploadImage;
import com.ocunapse.aplicondo.guard.api.VisitUpdateRequest;
import com.ocunapse.aplicondo.guard.api.WalkInVisitorRequest;
import com.ocunapse.aplicondo.guard.databinding.ActivityWalkInBinding;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WalkInActivity extends AppCompatActivity {

    private ActivityWalkInBinding binding;
    AutoCompleteTextView unitView;
    AutoCompleteTextView residentView;
    EditText name;
    EditText phone;
    EditText vehicleNum;
    EditText reason;

    PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    int resident_id = 0;
    int unit_id = 0;

    Bitmap myBitmap;
    Uri picUri;


    private ArrayList<String> permissionsToRequest = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 107;

    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWalkInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ArrayList<UnitListRequest.Unit> units = new ArrayList<>();
        ArrayList<String> unitList = new ArrayList<>();
        HashMap<Integer, String> listmap = new HashMap<>();

        Toolbar tb = binding.toolbar2;
        tb.setTitle("Walk In Entry");

        residentView = binding.residentNameAuto;
        unitView = binding.unitNoAuto;
        name = binding.walkinNameEdittext;
        phone = binding.walkinPhoneEdittext;
        vehicleNum = binding.walkinVehicleEdittext;
        reason = binding.walkinReasonEdittext;
        binding.cameraButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        binding.documentView.setVisibility(View.GONE);

        binding.transportGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            if (i == binding.vehicleRadio.getId())
                binding.vehicleInputView.setVisibility(View.VISIBLE);
            else binding.vehicleInputView.setVisibility(View.GONE);
        });

        final String[] unitLabel = new String[1];

        permissionsToRequest.add("android.permission.CAMERA");
        permissionsToRequest.add("android.permission.READ_MEDIA_IMAGES");
//        permissionsToRequest.add("android.permission.READ_EXTERNAL_STORAGE");
//        permissionsToRequest.add("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION");
        permissionsToRequest.size();
        requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);

        new UnitListRequest(res -> {
            if (res.success) {
//               System.out.println(res.data);
                for (UnitListRequest.Unit o : res.data) {
                    units.add(o);
                    unitList.add(o.unit_label.toUpperCase());
                }
            }
        }).execute();
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.select_dialog_item, unitList);
        //Getting the instance of AutoCompleteTextView
        unitView.setThreshold(2);//will start working from first character
        unitView.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        unitView.setTextColor(Color.BLACK);

        ArrayAdapter<String> residentsAdapter = new ArrayAdapter<>
                (WalkInActivity.this, android.R.layout.select_dialog_item, new ArrayList<>(listmap.values()));
        residentsAdapter.setNotifyOnChange(true);
        residentView.setAdapter(residentsAdapter);
        residentView.setTextColor(Color.BLACK);

        unitView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (unitLabel[0] != null && unitLabel[0].length() > 0 && !unitView.getText().toString().equalsIgnoreCase(unitLabel[0])) {
                    listmap.clear();
                    Log.e("walkin", "clear");
                    residentView.setText("Invalid Unit");
                    residentView.setTextColor(Color.RED);
                    residentView.setTypeface(null, Typeface.BOLD);
                    residentView.setEnabled(false);
                }
            }
        });

        unitView.setOnItemClickListener((adapterView, view, i, l) -> {
            unitLabel[0] = String.valueOf(adapterView.getItemAtPosition(i));
            Log.d("walkin", unitLabel[0]);
            residentView.getText().clear();
            residentView.setTextColor(Color.BLACK);
            residentView.setTypeface(null, Typeface.NORMAL);
            residentView.setEnabled(true);
            residentsAdapter.clear();

            List<UnitListRequest.Unit> filtered = units.stream().filter(o -> o.unit_label.equalsIgnoreCase(unitLabel[0])).collect(Collectors.toList());
            if (filtered.size() > 0) {
                if (filtered.get(0).residents.length == 0) {
                    residentView.setText("-- NO OWNER --");
                    residentView.setTextColor(Color.RED);
                    residentView.setTypeface(null, Typeface.BOLD);
                    residentView.setEnabled(false);
                } else {
                    UnitListRequest.Unit unit = filtered.get(0);
                    unit_id = unit.id;
//                    Log.d("walkin", unit.owners.profile.full_name);
                    for (UnitListRequest.Residents r : unit.residents) {
                        listmap.put(r.profile_id, r.profile.full_name);
                    }

                    residentsAdapter.addAll(listmap.values());
                    residentsAdapter.notifyDataSetChanged();
                    residentsAdapter.getFilter().filter(null);
                }
            }
        });

        residentView.setOnFocusChangeListener((view, b) -> {
            if (b) residentView.showDropDown();
        });

        residentView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                List<Map.Entry<Integer, String>> filtered = listmap.entrySet().stream().filter(o -> o.getValue().equalsIgnoreCase(editable.toString())).collect(Collectors.toList());
                if (filtered.size() > 0) {
                    resident_id = filtered.get(0).getKey();
                } else {
                    resident_id = 0;
                }
                Log.d("walkin", String.valueOf(resident_id));
            }
        });


        ActivityResultLauncher<Intent> resultLauncher;

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    myBitmap = (Bitmap) data.getExtras().get("data");
//                        myBitmap = rotateImageIfRequired(myBitmap);
                    myBitmap = getResizedBitmap(myBitmap, 500);
                    binding.documentView.setImageBitmap(myBitmap);
                    binding.documentView.setVisibility(View.VISIBLE);
                    binding.cameraButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));

                }
            }
        });

        binding.cameraButton.setOnClickListener(view -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            resultLauncher.launch(cameraIntent);
        });


// Get your image

        binding.submitWalkin.setOnClickListener(view -> {
            ProgressDialog pd = ProgressDialog.show(this, "",
                    "Loading. Please wait...", true);
            boolean hasError = verify().length() > 0;
            String nameVal = name.getText().toString();
            String phoneVal = phone.getText().toString();
            String vnumVal = vehicleNum.getText().toString();
            WalkInVisitorRequest.Transport transport = binding.transportGroup.getCheckedRadioButtonId() == binding.walkinRadio.getId() ? WalkInVisitorRequest.Transport.WALK_IN : WalkInVisitorRequest.Transport.VEHICLE;
            long time = new Date().getTime();
            if (!hasError) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                try {

                    if (binding.documentView.getVisibility() == View.VISIBLE) {
                        File f = File.createTempFile("document_" + new Date().getTime(), ".png");
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        myBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                        byte[] bitmapdata = bos.toByteArray();

                        //write the bytes in file
                        FileOutputStream fos;
                        fos = new FileOutputStream(f);

                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();
                        f.deleteOnExit();

                        new UploadImage(unit_id, resident_id, f, Ures -> {
                            String image = null;
                            if (Ures.success) image = Ures.data.url;
                            walkInApi(nameVal, phoneVal, vnumVal, transport, time, image, builder, pd);
                        }).execute();
                    } else {
                        walkInApi(nameVal, phoneVal, vnumVal, transport, time, null, builder, pd);
                    }
                } catch (IOException e) {
                    builder.setMessage("Visitor Register Failed")
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, id) -> {
                                dialog.dismiss();
                                finish();
                            });
                    builder.create().show();
                }

            } else {
                pd.dismiss();
                Snackbar.make(view, verify(), Snackbar.LENGTH_LONG).show();
            }
        });
    }


    void walkInApi(String nameVal, String phoneVal, String vnumVal, WalkInVisitorRequest.Transport transport, long time, String image, AlertDialog.Builder builder, ProgressDialog pd) {
        new WalkInVisitorRequest(unit_id, resident_id, nameVal, phoneVal, vnumVal, transport, time, image, res -> {
            pd.dismiss();
            if (res.success) {
                new VisitUpdateRequest(res.data.id, VisitUpdateRequest.Status.ARRIVED, updateRes -> {
                    String alertMsg = "Visitor Record Added";
                    if (!updateRes.success) alertMsg = "Visitor Registered. Arrival Update failed";
                    builder.setMessage(alertMsg)
                            .setCancelable(false)
                            .setPositiveButton("OK", (dialog, id) -> {
                                dialog.dismiss();
                                finish();
                            });
                    builder.create().show();
                }).execute();
            } else {
                builder.setMessage("Visitor Register Failed")
                        .setCancelable(false)
                        .setPositiveButton("OK", (dialog, id) -> {
                            dialog.dismiss();
                            finish();
                        });
                builder.create().show();
            }
        }).execute();
    }

    private String verify() {

        if (residentView.getText().toString().trim().length() < 2)
            return "Resident name is not filled";
        if (unit_id == 0) return "Invalid Unit";
        if (resident_id == 0) return "Invalid resident name";
        if (name.getText().toString().trim().length() < 2) return "Name not long enough";
        if (binding.transportGroup.getCheckedRadioButtonId() == binding.vehicleRadio.getId())
            if (vehicleNum.getText().toString().trim().length() < 2)
                return "Vehicle No not long enough";
        if (reason.getText().toString().trim().length() < 3) return "Reason text not long enough";
        if (phone.getText().toString().trim().length() < 3) return "Invalid Phone Number";

        try {
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phone.getText(), "MY");
            if (!phoneNumberUtil.isValidNumber(number)) return "Invalid Phone Number";
            else
                phone.setText(phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164));
        } catch (Exception ignored) {
            System.out.println(ignored.getMessage());
            return "Invalid Phone Number";
        }

        return "";
    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

}