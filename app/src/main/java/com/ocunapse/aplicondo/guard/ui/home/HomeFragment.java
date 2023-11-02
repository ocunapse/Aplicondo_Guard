package com.ocunapse.aplicondo.guard.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.ocunapse.aplicondo.guard.databinding.FragmentHomeBinding;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final ImageButton scanBtn = binding.scanQrBtn;

        scanBtn.setOnClickListener(view -> {
            GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                    .enableAutoZoom()
                    .setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_AZTEC)
                    .build();
            GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(view.getContext(), options);
            scanner.startScan().addOnSuccessListener(barcode -> {
                String val =  barcode.getRawValue();
                String[] part = val.split(",(?=[^,]*$)");
                System.out.println(part[1].trim());
            });
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


    private boolean VerifyData(String data, String signature) throws NoSuchAlgorithmException, InvalidKeyException {
        boolean res = false;
        try {
            String hmacSHA256Algorithm = "HmacSHA256";
            String key = "xV3483c#";

            String result = hmacWithJava(hmacSHA256Algorithm, data, key);
            if (result.equals(signature)) res = true;
        }catch (Exception e){
            System.out.print(e.getMessage());
        }
        return res;
    }

    public static String hmacWithJava(String algorithm, String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKeySpec);
        return byteArrayToHex(mac.doFinal(data.getBytes()));
    }
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }


}