package com.ocunapse.aplicondo.guard.ui.home;

import static com.ocunapse.aplicondo.guard.util.GeneralComponent.AlertBox;
import static com.ocunapse.aplicondo.guard.util.StringUtil.hmacWithJava;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.ocunapse.aplicondo.guard.R;
import com.ocunapse.aplicondo.guard.databinding.FragmentHomeBinding;
import com.ocunapse.aplicondo.guard.ui.WalkInActivity;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final ImageButton scanBtn = binding.scanQrBtn;
        final ImageButton walkinBtn = binding.walkInBtn;

        scanBtn.setOnClickListener(view -> {
            GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                    .enableAutoZoom()
                    .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_AZTEC)
                    .build();
            GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(view.getContext(), options);
            scanner.startScan().addOnSuccessListener(barcode -> {
                try {
                    String val = barcode.getRawValue();
                    String[] part = val.split(",(?=[^,]*$)");
                    String sig = part[1].trim();
                    try {
                        boolean isMatch = VerifyData(part[0].trim(), sig);
                        String msg = "Sig : " + sig + " - The Signature is" + ( isMatch ? " " : " NOT ") + "match.";
//                        AlertBox(getContext(), msg);
                        AlertBox(getContext(), isMatch?"\uD83D\uDC4D":getString(R.string.invalid_qr_error));
                    } catch (Exception e) {
                        System.out.print(e.getMessage());
                    }
                }catch (Exception e){
                    if(e instanceof ArrayIndexOutOfBoundsException){
                        AlertBox(getContext(), getString(R.string.invalid_qr_error));
                    }
                    System.out.println(e);;
                }
            });
        });

        walkinBtn.setOnClickListener(view -> {
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
//            Snackbar.make(requireView(),signature,Snackbar.LENGTH_LONG).show();
            if (result.equals(signature)) res = true;
        }catch (Exception e){
            System.out.print(e.getMessage());
        }
        return res;
    }





}