package com.ocunapse.aplicondo.guard.ui;

import static android.Manifest.permission_group.CAMERA;
import static android.Manifest.permission_group.READ_MEDIA_AURAL;
import static android.os.Environment.getExternalStorageDirectory;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.ExifInterface;
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
import android.widget.RadioGroup;

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
import java.util.concurrent.atomic.AtomicReference;
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


    private ArrayList<String> permissionsToRequest= new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 107;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWalkInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ArrayList<UnitListRequest.Unit> units = new ArrayList<UnitListRequest.Unit>();
        ArrayList<String> unitList = new ArrayList<String>();
        HashMap<Integer, String> listmap = new HashMap<>();

        residentView = binding.residentNameAuto;
        unitView = binding.unitNoAuto;
        name = binding.walkinNameEdittext;
        phone = binding.walkinPhoneEdittext;
        vehicleNum = binding.walkinVehicleEdittext;
        reason = binding.walkinReasonEdittext;
        binding.cameraButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT ));
        binding.documentView.setVisibility(View.GONE);

        binding.transportGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == binding.vehicleRadio.getId())  binding.vehicleInputView.setVisibility(View.VISIBLE);
                else  binding.vehicleInputView.setVisibility(View.GONE);
            }
        });

        final String[] unitLabel = new String[1];

        permissionsToRequest.add("android.permission.CAMERA");
        permissionsToRequest.add("android.permission.READ_MEDIA_IMAGES");
        permissionsToRequest.add("android.permission.READ_EXTERNAL_STORAGE");
        permissionsToRequest.size();
        requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);

        new UnitListRequest(res -> {
           if(res.success){
//               System.out.println(res.data);
               for(UnitListRequest.Unit o :res.data){
                   units.add(o);
                   unitList.add(o.unit_label.toUpperCase());
               }
           }
        }).execute();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, unitList);
        //Getting the instance of AutoCompleteTextView
        unitView.setThreshold(2);//will start working from first character
        unitView.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
        unitView.setTextColor(Color.BLACK);

        ArrayAdapter<String> residentsAdapter = new ArrayAdapter<String>
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
                if(unitLabel[0] != null && unitLabel[0].length()>0 && !unitView.getText().toString().equalsIgnoreCase(unitLabel[0])) {
                    listmap.clear();
                    Log.e("walkin","clear");
                    residentView.setText("Invalid Unit");
                    residentView.setTextColor(Color.RED);
                    residentView.setTypeface(null, Typeface.BOLD);
                    residentView.setEnabled(false);
                }
            }
        });

        unitView.setOnItemClickListener((adapterView, view, i, l) -> {

            unitLabel[0] =  String.valueOf(adapterView.getItemAtPosition(i));
            Log.d("walkin",units.get(i+1).unit_label +" - "+unitLabel[0] +" - " + i);
            Log.d("walkin", unitLabel[0]);
            residentView.getText().clear();
            residentView.setTextColor(Color.BLACK);
            residentView.setTypeface(null, Typeface.NORMAL);
            residentView.setEnabled(true);
            residentsAdapter.clear();

            List<UnitListRequest.Unit> filtered = units.stream().filter(o -> o.unit_label.equalsIgnoreCase(unitLabel[0])).collect(Collectors.toList());
            if(filtered.size() > 0){
                if(filtered.get(0).owners == null){
                   residentView.setText("-- NO OWNER --");
                   residentView.setTextColor(Color.RED);
                   residentView.setTypeface(null, Typeface.BOLD);
                   residentView.setEnabled(false);
                }
                else {
                    UnitListRequest.Unit unit = filtered.get(0);
                    unit_id = unit.id;
                    Log.d("walkin", unit.owners.profile.full_name);

                    listmap.put(unit.owners.profile_id, unit.owners.profile.full_name);
                    for(UnitListRequest.Family f: unit.owners.family){
                        listmap.put(f.profile_id, f.profile.full_name);
                    }
                    for(UnitListRequest.Tenant t: unit.tenants){
                        listmap.put(t.profile_id, t.profile.full_name);
                        for(UnitListRequest.Family f: t.family){
                            listmap.put(f.profile_id, f.profile.full_name);
                        }
                    }
                    residentsAdapter.addAll(listmap.values());
                    residentsAdapter.notifyDataSetChanged();
                    residentsAdapter.getFilter().filter(null);
                }
            }
        });

        residentView.setOnFocusChangeListener((View.OnFocusChangeListener) (view, b) -> {
            if(b) residentView.showDropDown();
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
                if(filtered.size() > 0) {
                    resident_id = filtered.get(0).getKey();
                }else {
                    resident_id = 0;
                }
                Log.d("walkin", String.valueOf(resident_id));
            }
        });


        binding.cameraButton.setOnClickListener(view -> {

            startActivityForResult(getPickImageChooserIntent(), 200);
        });

        binding.submitWalkin.setOnClickListener(view -> {
            ProgressDialog pd = ProgressDialog.show(this,"",
                    "Loading. Please wait...", true);
            boolean hasError = verify().length() > 0;
            String nameVal = name.getText().toString();
            String phoneVal = phone.getText().toString();
            String vnumVal = vehicleNum.getText().toString();
            WalkInVisitorRequest.Transport transport  = binding.transportGroup.getCheckedRadioButtonId() == binding.walkinRadio.getId() ? WalkInVisitorRequest.Transport.WALK_IN : WalkInVisitorRequest.Transport.VEHICLE;
            long time = new Date().getTime();
            if(!hasError){
                File f = new File(this.getCacheDir().getAbsolutePath(), "document.png");
                try {
                    f.createNewFile();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    myBitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                    byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
                    FileOutputStream fos = null;
                        fos = new FileOutputStream(f);

                        fos.write(bitmapdata);
                        fos.flush();
                        fos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                new UploadImage(unit_id, resident_id, f, Ures -> {
                    String image = null;
                    if(Ures.success) image = Ures.data.url;
                    new WalkInVisitorRequest(unit_id, resident_id, nameVal,phoneVal,vnumVal,transport,time, image, res -> {
                        pd.dismiss();
                        if(res.success) {
                            new VisitUpdateRequest(res.data.id, VisitUpdateRequest.Status.ARRIVED, updateRes -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                String alertMsg = "Visitor Record Added";
                                if(!updateRes.success) alertMsg = "Visitor Registered. Arrival Update failed";
                                builder.setMessage(alertMsg)
                                        .setCancelable(false)
                                        .setPositiveButton("OK", (dialog, id) -> {
                                            dialog.dismiss();
                                            finish();
                                        });
                                builder.create().show();
                            }).execute();
                        }else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage("Visitor Register Failed")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialog, id) -> {
                                        dialog.dismiss();
                                        finish();
                                    });
                            builder.create().show();
                        }
                    }).execute();
                }).execute();

            }
            else {
                pd.dismiss();
                Snackbar.make(view,verify(),Snackbar.LENGTH_LONG).show();
            }

        });
    }


    private String verify() {

        if(residentView.getText().toString().trim().length() < 2) return "Resident name is not filled";
        if(unit_id == 0 ) return "Invalid Unit";
        if(resident_id == 0 ) return "Invalid resident name";
        if(name.getText().toString().trim().length() < 2) return "Name not long enough";
        if(binding.transportGroup.getCheckedRadioButtonId() == binding.vehicleRadio.getId())
            if(vehicleNum.getText().toString().trim().length() < 2 ) return "Vehicle No not long enough";
        if(reason.getText().toString().trim().length() < 3) return "Reason text not long enough";
        if(phone.getText().toString().trim().length() < 3) return "Invalid Phone Number";;

        try {
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phone.getText(), "MY");
            if(!phoneNumberUtil.isValidNumber(number)) return "Invalid Phone Number";
            else phone.setText(phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164));
        }catch (Exception ignored){
            System.out.println(ignored.getMessage());
            return "Invalid Phone Number";
        }

        return "";
    }




    /**
     * Create a chooser intent to select the source to get image from.<br />
     * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br />
     * All possible sources are added to the intent chooser.
     */
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


    /**
     * Get URI to image received from capture by camera.
     */
    public static Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = new File(Environment.getExternalStorageDirectory(), "profile.png");
        if (getImage != null) {
            outputFileUri = Uri.fromFile(getImage);
        }
        return outputFileUri;
    }


    /**
     * Get the URI of the selected image from {@link #getPickImageChooserIntent()}.<br />
     * Will return the correct URI for camera and gallery image.
     *
     * @param data the returned data of the activity result
     */
    public static Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        System.out.println(data);
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    public static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;

        Log.e("ss-d", "onActivityResult: "+ getPickImageResultUri(data));
        if (resultCode == Activity.RESULT_OK && getPickImageResultUri(data) != null) {

            ImageView imageView = binding.documentView;

            Log.e("ss-d", "onActivityResult: "+ getCaptureImageOutputUri());
            if (getPickImageResultUri(data) != null) {
                picUri = getPickImageResultUri(data);
                try {
                    myBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri);
                    myBitmap = rotateImageIfRequired(myBitmap, picUri);
                    myBitmap = getResizedBitmap(myBitmap, 500);

                    imageView.setImageBitmap(myBitmap);
                    binding.documentView.setVisibility(View.VISIBLE);
                    binding.cameraButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT ));

                } catch (IOException e) {
                    Log.e("ss-d", "onActivityResult: ",e );
                    e.printStackTrace();
                }


            } else {

                bitmap = (Bitmap) data.getExtras().get("data");

                myBitmap = bitmap;
                Log.e("ss-d", "onActivityResult: "+ bitmap);
                assert myBitmap != null;
                myBitmap = getResizedBitmap(myBitmap, 500);
                imageView.setImageBitmap(myBitmap);
                binding.documentView.setVisibility(View.VISIBLE);
                binding.cameraButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT ));
            }

        }

    }

}