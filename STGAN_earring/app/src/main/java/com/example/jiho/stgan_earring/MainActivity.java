package com.example.jiho.stgan_earring;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import static android.os.Environment.DIRECTORY_DCIM;


public class MainActivity extends Activity implements View.OnClickListener {
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_iMAGE = 2;

    private Uri mlmageCaptureUri;
    private ImageView iv_UserPhoto;
    private ImageView iv_Earring_Photo;
    private int id_view;
    public String absoultePath;

    private DB_Manger dbmanger;
    private String mode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbmanger = new DB_Manger();

        iv_UserPhoto = (ImageView) this.findViewById(R.id.user_image);
        iv_Earring_Photo = (ImageView) this.findViewById(R.id.earring_image);

        Button btn_upload = (Button) this.findViewById(R.id.btn_UploadPicture);
        btn_upload.setOnClickListener(this);

        Button btn_upload_earring = (Button) this.findViewById(R.id.btn_UploadEarring);
        btn_upload_earring.setOnClickListener(this);


        Button newpage = (Button) findViewById(R.id.newActivity);
        newpage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, SubActivity.class);
                        Bitmap sendBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.picture);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        sendBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        intent.putExtra("image",byteArray);
                        startActivity(intent);

                    }
                });
    }

    /*
     * 카메라에서 사진 촬영
     * */

    public void doTakePhotoAction() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //임시로 사용할 파일의 경로 생성
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".png";
        mlmageCaptureUri = Uri.fromFile(new File
                (Environment.getExternalStorageDirectory(), url));

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mlmageCaptureUri);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    /*
     * 앨범에서 이미지 가져오기
     * */


    public void doTakeAlbumAction() {
        //앨범호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case PICK_FROM_ALBUM: {
                mlmageCaptureUri = data.getData();
                Log.d("SmartWheel", mlmageCaptureUri.getPath().toString());

            }

            case PICK_FROM_CAMERA: {
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mlmageCaptureUri, "image/*");

                intent.putExtra("outputX", 144);
                intent.putExtra("outputY", 144);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("sale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_iMAGE);
                break;
            }
            case CROP_FROM_iMAGE: {

                if (resultCode != RESULT_OK) {
                    return;
                }

                final Bundle extras = data.getExtras();
                 String filePath =Environment.getExternalStorageDirectory().getAbsolutePath() +
                         "/SmartWheel/" + System.currentTimeMillis() + ".png";

                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data");

                    if(mode.equals("user"))
                      iv_UserPhoto.setImageBitmap(photo);
                     else if(mode.equals("earring"))
                       iv_Earring_Photo.setImageBitmap(photo);


                    storeCropImage(photo, filePath);
                    absoultePath = filePath;
                    break;

                }

                //임시 파일 삭제
                File f = new File(mlmageCaptureUri.getPath());
                if (f.exists()) {
                    f.delete();
                }

            }

        }

    }

    private void storeCropImage(Bitmap bitmap, String filePath) {
        //smartWheel폴더를 생성하여 이미지를 저장하는 방식
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+
                "/SmartWheel/";

        File directory_SmartWheel = new File(dirPath);

        if(!directory_SmartWheel.exists()) directory_SmartWheel.mkdir();

        File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try {
            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onClick(View v) {

        id_view = v.getId();

        if (v.getId() == R.id.btn_UploadPicture) {
            mode="user";
            DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doTakePhotoAction();
                }
            };

            DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doTakeAlbumAction();
                }
            };

            DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };


            new AlertDialog.Builder(this).setTitle("업로드할 이미지 선택")
                    .setPositiveButton("사진촬영", cameraListener)
                    .setNeutralButton("앨범선택", albumListener)
                    .setNegativeButton("취소", cancelListener)
                    .show();

        }

        if (v.getId() == R.id.btn_UploadEarring) {
            mode="earring";
            DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doTakePhotoAction();
                }
            };

            DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doTakeAlbumAction();
                }

            };

            DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            };

            new AlertDialog.Builder(this).setTitle("업로드할 이미지 선택")
                    .setPositiveButton("사진촬영", cameraListener)
                    .setNeutralButton("앨범선택", albumListener)
                    .setNegativeButton("취소", cancelListener)
                    .show();

        }




    }
}