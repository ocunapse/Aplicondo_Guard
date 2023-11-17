package com.ocunapse.aplicondo.guard.barcode;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCase;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.ocunapse.aplicondo.guard.barcode.BarcodeBoxView;
import com.ocunapse.aplicondo.guard.barcode.QrCodeAnalyzer;
import com.ocunapse.aplicondo.guard.databinding.ActivityScannerBinding;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kotlin.Metadata;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ScannerActivity extends AppCompatActivity {
    private ExecutorService cameraExecutor;
    private BarcodeBoxView barcodeBoxView;
    private ActivityScannerBinding binding;



    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScannerBinding.inflate(this.getLayoutInflater());
        this.setContentView((View)binding.getRoot());

        cameraExecutor = Executors.newSingleThreadExecutor();
        this.barcodeBoxView = new BarcodeBoxView((Context)this);

        this.addContentView((View)barcodeBoxView, new ViewGroup.LayoutParams(-1, -1));
        this.checkCameraPermission();
    }

    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.checkIfCameraPermissionIsGranted();
    }

    private void checkCameraPermission() {
        try {
            String[] requiredPermissions = new String[]{Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions((Activity)this, requiredPermissions, 0);
        } catch (IllegalArgumentException var2) {
            this.checkIfCameraPermissionIsGranted();
        }

    }

    private final void checkIfCameraPermissionIsGranted() {
        if (ContextCompat.checkSelfPermission((Context)this, Manifest.permission.CAMERA) == 0) {
            this.startCamera();
        } else {
            AlertDialog perm = (new MaterialAlertDialogBuilder((Context)this))
                    .setTitle("Permission required")
                    .setMessage("This application needs to access the camera to process barcodes")
                    .setPositiveButton("Ok",(dialogInterface,listener) -> ScannerActivity.this.checkCameraPermission())
                    .setCancelable(false).create();
            perm.setCanceledOnTouchOutside(false);
            perm.show();
        }

    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance((Context)this);
        cameraProviderFuture.addListener((Runnable)(() -> {
            ProcessCameraProvider cameraProvider = null;
            try {
                cameraProvider = (ProcessCameraProvider)cameraProviderFuture.get();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Preview preview = (new Preview.Builder()).build();
            preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());
            ImageAnalysis imageAnalyzer = (new ImageAnalysis.Builder()).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
            imageAnalyzer.setAnalyzer(cameraExecutor, (ImageAnalysis.Analyzer)(new QrCodeAnalyzer(this, barcodeBoxView, (float)binding.previewView.getWidth(), (float)binding.previewView.getHeight(), scanValue -> {
                cameraExecutor.shutdown();
                setResult(RESULT_OK,new Intent().putExtra("result",scanValue ));
                finish();
            })));
           CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

            try {
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this,cameraSelector,preview,imageAnalyzer);
            } catch (Exception err) {
                err.printStackTrace();
            }

        }), ContextCompat.getMainExecutor((Context)this));
    }
}
