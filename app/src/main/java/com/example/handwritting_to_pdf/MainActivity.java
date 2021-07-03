package com.example.handwritting_to_pdf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {
    private static final int PHOTO_REQUEST = 10;
    private TextView scantext ;
    private Uri imageuri;
    private Button button;
    private Button eit_mode;
    private TextRecognizer reecognizer;
    private static final int REQUEST_WRITE_PERMISSION = 20;
    private static final String SAVED_INSTANCE_URI = "uri";
    private static final String SAVEED_INSTANce_STATE_RESULT = "results";
    private  StringBuilder Blocks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.clickImage);
        scantext = findViewById(R.id.resultant_textview);
        eit_mode = findViewById(R.id.edit_mode);
        if(savedInstanceState != null){
            imageuri = Uri.parse(savedInstanceState.getString(SAVED_INSTANCE_URI));
            scantext.setText(savedInstanceState.getString(SAVEED_INSTANce_STATE_RESULT));
        }

        eit_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageuri == null || Blocks.length() == 0){
                    Toast.makeText(MainActivity.this, "Please Select image ", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(MainActivity.this, EditMode.class);
                    intent.putExtra("text", (CharSequence) Blocks);
                    startActivity(intent);
                }
            }
        });
        reecognizer  = new TextRecognizer.Builder(getApplicationContext()).build();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this,new
                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_PERMISSION);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePicture();
            } else {
                Toast.makeText(MainActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "picture.jpg");
        imageuri = FileProvider.getUriForFile(MainActivity.this,
                BuildConfig.APPLICATION_ID + ".provider", photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageuri);
        startActivityForResult(intent, PHOTO_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PHOTO_REQUEST && resultCode == RESULT_OK ){
            launchMediaScan();
            try {
                Bitmap bitmap = decodeBitmapUri(this, imageuri);
                if(reecognizer.isOperational() && bitmap != null){
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> texteblock = reecognizer.detect(frame);
                    Blocks  = new StringBuilder();
                    StringBuilder Words = new StringBuilder();
                    StringBuilder lines = new StringBuilder();

                    for(int i=0;i<texteblock.size()-1;i++){
                        TextBlock textBlock = texteblock.valueAt(i);
                        Blocks.append(textBlock.getValue()).append("\n").append("\n");
                        for( Text line : textBlock.getComponents()){
                            lines.append(line.getValue()).append(". ");

                            for(Text element : line.getComponents()){
                                Words.append(element.getValue()).append(". ");
                            }
                        }
                    }
                    if(texteblock.size() == 0){
                        scantext.setText("Scan Failed : Nothing to Scan ");
                    }else {
                        scantext.setText(scantext.getText() + Blocks.toString() + "\n");
                    }
                }
            }   catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load Image", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private Bitmap decodeBitmapUri(Context context, Uri imageuri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageuri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(context.getContentResolver()
                .openInputStream(imageuri), null, bmOptions);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (imageuri != null) {
            outState.putString(SAVED_INSTANCE_URI, imageuri.toString());
            outState.putString(SAVEED_INSTANce_STATE_RESULT, scantext.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }
    private void launchMediaScan() {
        Intent mediaScan = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScan.setData(imageuri);
        this.sendBroadcast(mediaScan);
    }
}
