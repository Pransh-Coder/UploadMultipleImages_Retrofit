package com.example.uploadmultipleimages_retrofit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    Button upload;
    int CODE_GALLERY_REQUEST = 777;
    Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        upload = findViewById(R.id.upload);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_GALLERY_REQUEST);
        }

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Pictures: "), 1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {        //&& data != null
            ClipData clipData = data.getClipData();
            ArrayList<Uri> fileUris = new ArrayList<Uri>();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                fileUris.add(uri);
            }

            uploadAlbum(fileUris);
        }
    }

    public void uploadAlbum(ArrayList<Uri> fileUris) {
        String name = editText.getText().toString();

        //RequestBody descriptionPart = RequestBody.create(MultipartBody.FORM, name);

        /*File orignalfile = null;
        try {
            orignalfile = FileUtil.from(this, filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RequestBody filepart = RequestBody.create(
                MediaType.parse(getContentResolver().getType(filePath)),
                orignalfile
        );*/

        //MultipartBody.Part file = MultipartBody.Part.createFormData("photo", orignalfile.getName(), filepart);

        Retrofit retrofit = NetworkClient.getRetrofit();
        UploadApi uploadApi = retrofit.create(UploadApi.class);

        List<MultipartBody.Part> parts = new ArrayList<>();

        for (int i = 0; i < fileUris.size(); i++) {
            parts.add(prepareFilePart(""+i,fileUris.get(i)));
        }

        Call<ResponseBody> call = uploadApi.uploadAlbum(
                createPartFromString(name,parts),parts
        );
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Toast.makeText(MainActivity.this, "yeah!"+response.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "nooo :(" + t.toString(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString, List<MultipartBody.Part> parts) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        File file = null;
        try {
            file = FileUtil.from(this, fileUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse(getContentResolver().getType(fileUri)),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }
}
