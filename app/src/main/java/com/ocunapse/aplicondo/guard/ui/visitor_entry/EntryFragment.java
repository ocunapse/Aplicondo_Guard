package com.ocunapse.aplicondo.guard.ui.visitor_entry;

import static com.ocunapse.aplicondo.guard.api.RequestBase.LOG;
import static com.ocunapse.aplicondo.guard.util.GeneralComponent.AlertBox;
import static com.ocunapse.aplicondo.guard.util.StringUtil.hmacWithJava;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.ocunapse.aplicondo.guard.R;
import com.ocunapse.aplicondo.guard.api.VisitUpdateRequest;
import com.ocunapse.aplicondo.guard.api.VisitorCheckInRequest;
import com.ocunapse.aplicondo.guard.barcode.ScannerActivity;
import com.ocunapse.aplicondo.guard.databinding.FragmentEntryBinding;
import com.ocunapse.aplicondo.guard.ui.WalkInActivity;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;



public class EntryFragment extends Fragment {
    private FragmentEntryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEntryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        String res = data.getStringExtra("result");
                        if(res != null) process(res);
                    }
                });

        binding.scanQrBtn.setOnClickListener(view -> {
            view.setEnabled(false);
            activityResultLauncher.launch(new Intent(getActivity(), ScannerActivity.class));
        });

        binding.walkInBtn.setOnClickListener(view -> {
            view.setEnabled(false);
            Intent i = new Intent(getActivity(), WalkInActivity.class);
            startActivity(i);
        });


        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.scanQrBtn.setEnabled(true);
        binding.walkInBtn.setEnabled(true);
    }

    /****
     * REQUEST QR Scanner
     ****/




    /****
     * Verify Methods
     ****/



    private boolean VerifyData(String data, String signature) {
        boolean res = false;
        try {
            res = VerifySignature(data,signature);
        }catch (Exception e){
            System.out.print(e.getMessage());
        }
        return res;
    }

    private boolean VerifySignature(String data, String signature) {
        boolean res = false;
        try {
            String sha256 = "HmacSHA256";
            String key = "xV3483c#";

            String result = hmacWithJava(sha256, data, key);
            System.out.println("sig : " + signature);
            if (result.equals(signature)) res = true;
        }catch (Exception e){
            System.out.print(e.getMessage());
        }
        return res;
    }

    public static void VisitorDialog(VisitorCheckInRequest.Visitor visitor, Activity activity, SwipeRefreshLayout.OnRefreshListener listner){
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_visitor);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        EditText name = dialog.findViewById(R.id.visitor_name_edittext);
        EditText vehicle = dialog.findViewById(R.id.visitor_vehicle_edittext);
        EditText phone = dialog.findViewById(R.id.visitor_phone_edittext);
        EditText unit = dialog.findViewById(R.id.visitor_resident_unit_edittext);
        EditText resident = dialog.findViewById(R.id.visitor_resident_name_edittext);
        EditText resident_phone = dialog.findViewById(R.id.visitor_resident_phone_edittext);

        name.setText(visitor.name);
        phone.setText(visitor.mobile_number);
        vehicle.setText(visitor.vehicle_registration == null ? "-":visitor.vehicle_registration);
        unit.setText(visitor.unit.unit_label);
        resident.setText(visitor.profile.full_name);
        resident_phone.setText(" \uD83D\uDCDE " + visitor.profile.phone_number  );

        resident_phone.setOnClickListener(view -> {
            try {
                Phonenumber.PhoneNumber number = phoneNumberUtil.parse(visitor.profile.phone_number, "MY");
                LOG("scan_val", number.toString());
                if(phoneNumberUtil.isValidNumber(number)) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)));
                    activity.startActivity(intent);
                }
                else Snackbar.make(view,"Invalid Phone Number", Snackbar.LENGTH_LONG).show();
            }catch (Exception ignored){
                System.out.println(ignored.getMessage());
                Snackbar.make(view,"Invalid Phone Number", Snackbar.LENGTH_LONG).show();
            }

        });

        Button proceed =  dialog.findViewById(R.id.visitor_verified_btn);
        Button reject =  dialog.findViewById(R.id.visitor_reject_btn);

        proceed.setOnClickListener(view -> new VisitUpdateRequest(visitor.id, VisitUpdateRequest.Status.ARRIVED, res ->{
            dialog.dismiss();
            if(listner != null) listner.onRefresh();
        }).execute());
        reject.setOnClickListener(view -> new VisitUpdateRequest(visitor.id, VisitUpdateRequest.Status.GUARD_REJECTED, res -> {
            dialog.dismiss();
            if(listner != null) listner.onRefresh();
        }).execute());
        dialog.findViewById(R.id.close_btn_visitor_dialog).setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }



    private void process(String value){
        ProgressDialog pd = ProgressDialog.show(this.getContext(), "",
                "Loading. Please wait...", true);

        try {
            Log.e("scan_val",value);
            String[] part = value.split(",(?=[^,]*$)");
            String sig = part[1].trim();
            try {
                boolean isMatch = VerifyData(part[0].trim(), sig);
                String msg = "Sig : " + sig + " - The Signature is" + ( isMatch ? " " : " NOT ") + "match.";
//                        AlertBox(getContext(), msg);
                Log.i("Sig",msg);
                if(isMatch){
                    String[] data = part[0].trim().split(",");

                    Log.d("scan_val : ALL", Arrays.toString(data));
                    int visitorId = Integer.parseInt(data[0]);
                    long start = Long.parseLong(data[1].trim());
                    long end = Long.parseLong(data[2].trim());
                    Date s = new Date(start);
                    Date n = new Date();
                    Date e = new Date(end);
                    Log.d("scan_val : Now", String.valueOf(n.getTime()));
//                    if(n.getTime() > e.getTime()){
//                        pd.dismiss();
//                        AlertBox(getContext(),getString(R.string.expired_qr_error));
//                        return;
//                    }
                    VisitorCheckInRequest vci = new VisitorCheckInRequest(visitorId, res -> {
                        if(res.success) {
                            pd.dismiss();
                            LOG("visitData",res.data.visit_date.toString());
                            TimeZone malaysianTimeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur");
                            SimpleDateFormat Mrg = new SimpleDateFormat("yyyy/MM/dd 00:00:01 z");
                            SimpleDateFormat Nig = new SimpleDateFormat("yyyy/MM/dd 23:59:50 z");
                            Mrg.setTimeZone(malaysianTimeZone);
                            Nig.setTimeZone(malaysianTimeZone);
                            Date now = new Date();
                            Date todayMorning = new Date(Mrg.format(now));
                            Date todayNight = new Date(Nig.format(now));
                            Date endNight = new Date(Nig.format(res.data.end_date != null ? res.data.end_date :now));
                            LOG("visitData t-Mrg",todayMorning.toString());
                            LOG("visitData t-Nig",todayNight.toString());
                            LOG("visitData",now.toString());
                            if(todayMorning.compareTo(res.data.visit_date) >= 0){
                                Log.e("visitData", "a day - b4");
                                AlertBox(getContext(),"Visitor Pass expired");
                            }else {
                                if(res.data.visit_date.compareTo(todayNight) >= 0){
                                    Log.e("visitData", "a day-AF");
                                    AlertBox(getContext(),"Visitor Pass Not Valid for today");
                                }else {
                                    if(res.data.visit_date.compareTo(todayMorning) >= 0 && res.data.visit_date.compareTo(todayNight) <= 0) {
                                        Log.e("visitData", "tod - "+ endNight);
                                        VisitorDialog(res.data,this.getActivity(), null);
                                    }
                                }
                            }
//
                        }
                        else {
                            pd.dismiss();
                            AlertBox(getContext(),getString(R.string.no_visitor_error));
                        }
                    });
                    vci.execute();
                }else {
                    pd.dismiss();
                    AlertBox(getContext(),getString(R.string.invalid_qr_error));
                }
            } catch (Exception e) {
                Log.e("scan_val", Objects.requireNonNull(e.getMessage()));
                AlertBox(getContext(),getString(R.string.invalid_qr_error));
                pd.dismiss();
            }
        }catch (Exception e){
            Log.e("scan_val", Arrays.toString(e.getStackTrace()));
            pd.dismiss();
//            if(e instanceof ArrayIndexOutOfBoundsException){
                AlertBox(getContext(), R.string.invalid_qr_error);
//            }
        }
    }



}