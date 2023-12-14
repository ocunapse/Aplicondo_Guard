package com.ocunapse.aplicondo.guard.barcode;

import static com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC;
import static com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;


public final class QrCodeAnalyzer implements ImageAnalysis.Analyzer {
    private float scaleX;
    private float scaleY;
    private final Context context;
    private final BarcodeBoxView barcodeBoxView;
    private final float previewViewWidth;
    private final float previewViewHeight;

    private final float translateX(float x) {
        return x * this.scaleX;
    }

    private final float translateY(float y) {
        return y * this.scaleY;
    }

    private final RectF adjustBoundingRect(Rect rect) {
        return new RectF(this.translateX((float)rect.left), this.translateY((float)rect.top), this.translateX((float)rect.right), this.translateY((float)rect.bottom));
    }

    private ScanResult scanResult;

    @SuppressLint({"UnsafeOptInUsageError"})
    public void analyze(@NotNull ImageProxy image) {
        Image img = image.getImage();
        if (img != null) {
            this.scaleX = this.previewViewWidth / (float)img.getHeight();
            this.scaleY = this.previewViewHeight / (float)img.getWidth();

            InputImage inputImage = InputImage.fromMediaImage(img, image.getImageInfo().getRotationDegrees());
            BarcodeScannerOptions options =
                    new BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(
                                   FORMAT_QR_CODE,
                                    FORMAT_AZTEC)
                            .build();
            BarcodeScanner scanner = BarcodeScanning.getClient(options);
//            FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
//                    .getVisionBarcodeDetector(options);

            scanner.process(inputImage).addOnSuccessListener(new OnSuccessListener() {

                public void onSuccess(Object var1) {
                    this.onSuccess((List)var1);
                }

                public void onSuccess(List<Barcode> barcodes) {
                    if (!barcodes.isEmpty()) {
                        barcodes.forEach(barcode -> {
                            if (barcode.getBoundingBox() != null) {
                                barcodeBoxView.setRect(adjustBoundingRect(barcode.getBoundingBox()));
                            }
                            scanResult.get(barcode.getRawValue());
                            scanner.close();
                        });

                    } else {
                        QrCodeAnalyzer.this.barcodeBoxView.setRect(new RectF());
                    }

                }
            });


//            Task<List<FirebaseVisionBarcode>> result = detector.detectInImage(inputImage)
//                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
//                        @Override
//                        public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
//                            if (!barcodes.isEmpty()) {
//                        barcodes.forEach(barcode -> {
//                            if (barcode.getBoundingBox() != null) {
//                                barcodeBoxView.setRect(adjustBoundingRect(barcode.getBoundingBox()));
//                            }
//                            scanResult.get(barcode.getRawValue());
//                            try {
//                                detector.close();
//                            } catch (IOException e) {
//                                Log.e("scan_val", "onSuccess: ", e);
//                            }
//                        });
//
//                    } else {
//                        QrCodeAnalyzer.this.barcodeBoxView.setRect(new RectF());
//                    }
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            // Task failed with an exception
//                            // ...
//                        }
//                    });

        }

        image.close();
    }


    public interface ScanResult{ void get(String scanValue); }

    public QrCodeAnalyzer(@NotNull Context context, @NotNull BarcodeBoxView barcodeBoxView, float previewViewWidth, float previewViewHeight, ScanResult scanResult) {
        super();
        this.context = context;
        this.barcodeBoxView = barcodeBoxView;
        this.previewViewWidth = previewViewWidth;
        this.previewViewHeight = previewViewHeight;
        this.scaleX = 1.0F;
        this.scaleY = 1.0F;
        this.scanResult = scanResult;
    }
}


