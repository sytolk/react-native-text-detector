
package com.fetchsky.RNTextDetector;

import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.io.IOException;
import java.net.URL;

public class RNTextDetectorModule extends ReactContextBaseJavaModule {

    public RNTextDetectorModule(ReactApplicationContext reactContext) {
        super(reactContext);
        try {
            FirebaseApp.initializeApp(reactContext);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void detectFromFile(String uri, final Promise promise) {
        try {
            FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(getReactApplicationContext(), android.net.Uri.parse(uri));
            Task<FirebaseVisionText> result =
                    FirebaseVision.getInstance().getOnDeviceTextRecognizer().processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    promise.resolve(getDataAsArray(firebaseVisionText));
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            e.printStackTrace();
                                            promise.reject(e);
                                        }
                                    });
        } catch (IOException e) {
            promise.reject(e);
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void detectFromUri(String uri, final Promise promise) {
        try {
            URL url = new URL(uri);
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()));
            Task<FirebaseVisionText> result =
                    FirebaseVision.getInstance().getOnDeviceTextRecognizer().processImage(image)
                            .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                @Override
                                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                    promise.resolve(getDataAsArray(firebaseVisionText));
                                }
                            })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            e.printStackTrace();
                                            promise.reject(e);
                                        }
                                    });
        } catch (IOException e) {
            promise.reject(e);
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void detectFromContentUri(String uri, final Promise promise) {
        try {
            TextRecognizer textRecognizer = new TextRecognizer.Builder(getReactApplicationContext()).build();
            if (textRecognizer.isOperational()) {
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(BitmapFactory.decodeStream(getReactApplicationContext().getContentResolver().openInputStream(android.net.Uri.parse(uri))));
                Task<FirebaseVisionText> result =
                        FirebaseVision.getInstance().getOnDeviceTextRecognizer().processImage(image)
                                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                    @Override
                                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                        promise.resolve(getDataAsArray(firebaseVisionText));
                                    }
                                })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                e.printStackTrace();
                                                promise.reject(e);
                                            }
                                        });
            } else throw new Exception("Not operational textRecognizer");
        } catch (IOException e) {
            promise.reject(e);
            e.printStackTrace();
        } catch (Exception e) {
            promise.reject(e);
            e.printStackTrace();
        }
    }

    /*private FirebaseVisionText processVisionImage(FirebaseVisionImage image) {
        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                promise.resolve(getDataAsArray(firebaseVisionText));
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                        promise.reject(e);
                                    }
                                });;
    }*/

    /**
     * Converts firebaseVisionText into a map
     *
     * @param firebaseVisionText
     * @return
     */
    private WritableArray getDataAsArray(FirebaseVisionText firebaseVisionText) {
        WritableArray data = Arguments.createArray();

        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
            WritableArray blockElements = Arguments.createArray();

            for (FirebaseVisionText.Line line : block.getLines()) {
                WritableArray lineElements = Arguments.createArray();
                for (FirebaseVisionText.Element element : line.getElements()) {
                    WritableMap e = Arguments.createMap();

                    WritableMap eCoordinates = Arguments.createMap();
                    eCoordinates.putInt("top", element.getBoundingBox().top);
                    eCoordinates.putInt("left", element.getBoundingBox().left);
                    eCoordinates.putInt("width", element.getBoundingBox().width());
                    eCoordinates.putInt("height", element.getBoundingBox().height());

                    e.putString("text", element.getText());
//                    e.putDouble("confidence", element.getConfidence());
                    e.putMap("bounding", eCoordinates);
                    lineElements.pushMap(e);
                }

                WritableMap l = Arguments.createMap();

                WritableMap lCoordinates = Arguments.createMap();
                lCoordinates.putInt("top", line.getBoundingBox().top);
                lCoordinates.putInt("left", line.getBoundingBox().left);
                lCoordinates.putInt("width", line.getBoundingBox().width());
                lCoordinates.putInt("height", line.getBoundingBox().height());

                l.putString("text", line.getText());
//                l.putDouble("confidence", line.getConfidence());
                l.putMap("bounding", lCoordinates);
                l.putArray("elements", lineElements);

                blockElements.pushMap(l);
            }
            WritableMap info = Arguments.createMap();
            WritableMap coordinates = Arguments.createMap();

            coordinates.putInt("top", block.getBoundingBox().top);
            coordinates.putInt("left", block.getBoundingBox().left);
            coordinates.putInt("width", block.getBoundingBox().width());
            coordinates.putInt("height", block.getBoundingBox().height());

            info.putMap("bounding", coordinates);
            info.putString("text", block.getText());
            info.putArray("lines", blockElements);
//            info.putDouble("confidence", block.getConfidence());
            data.pushMap(info);
        }

        return data;
    }


    @Override
    public String getName() {
        return "RNTextDetector";
    }
}
