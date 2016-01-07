package com.bcgs.bruno.drawing;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import static org.opencv.imgproc.Imgproc.threshold;


public class MainActivity extends ActionBarActivity {

    static{
        System.loadLibrary("opencv_java3");
    }

    private static int RESULT_LOAD_IMG = 1;

    private CanvasView canvas = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton btn_loadImg = (FloatingActionButton) findViewById(R.id.fabLoadImageBtn);
        FloatingActionButton btn_clearImg = (FloatingActionButton) findViewById(R.id.fabClearScreenBtn);

        //FloatingActionButton btn_Eraser = (FloatingActionButton) findViewById(R.id.fabEraserBtn);
        //FloatingActionButton btn_Caneta = (FloatingActionButton) findViewById(R.id.fabCanetaBtn);
        FloatingActionButton btn_Desfazer = (FloatingActionButton) findViewById(R.id.fabDesfazerBtn);
        FloatingActionButton btn_Salvar = (FloatingActionButton) findViewById(R.id.fabSalvarBtn);

        this.canvas = (CanvasView)this.findViewById(R.id.canvas);

        this.canvas.setMode(CanvasView.Mode.DRAW);
        this.canvas.setDrawer(CanvasView.Drawer.PEN);
        this.canvas.setPaintStyle(Paint.Style.STROKE);
        this.canvas.setOpacity(128);
        //this.canvas.setPaintStrokeWidth(10);
        this.canvas.setBlur(15);
        //this.canvas.setMode(CanvasView.Mode.DRAW);

        //final MainDrawingView mdv = (MainDrawingView) findViewById(R.id.single_touch_view);

        btn_loadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                );
                startActivityForResult(intent, RESULT_LOAD_IMG);
            }
        });

        btn_clearImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvas.clear();
            }
        });

        /*
        btn_Eraser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvas.setMode(CanvasView.Mode.ERASER);
            }
        });


        btn_Caneta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvas.setMode(CanvasView.Mode.DRAW);
            }
        }); */

        btn_Desfazer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                canvas.undo();
            }
        });

        btn_Salvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(canvas.getBitmap());
                Toast.makeText(getBaseContext(), "Imagem Salva com Sucesso na sua pasta Imagens", Toast.LENGTH_LONG).show();

            }
        });

    }

    private void saveImage(Bitmap pictureBitmap) {
        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut = null;
        File file = new File(path, "drawing_"+java.util.UUID.randomUUID()+".jpg"); // the File to save to

        try{
            fOut = new FileOutputStream(file);

            pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());
        } catch (IOException e) {
            Log.d("saveImage", "IOException ao salvar imgem");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadImage(String path) {
        Mat imgSource = Imgcodecs.imread(path);

        Imgproc.cvtColor(imgSource, imgSource, Imgproc.COLOR_RGB2GRAY, 4); //gray

        Mat edge = new Mat();
        Mat edgeResized = new Mat();
        Mat dst = new Mat();

        Mat Img_Thres_Gray = new Mat();

        double CannyAccThresh = threshold(imgSource,Img_Thres_Gray,0,255, 8);

        double CannyThresh = 0.1 * CannyAccThresh;


        //precisa dar um blur antes de aplicar canny
        //como dar o threshhold correto
        //redimensionar a imagem

        //Imgproc.GaussianBlur(imgSource, imgSource, new Size(5, 5), 0.6);
        //Imgproc.Canny(imgSource, edge, 0, 100, 5, true);
        Imgproc.Canny(imgSource, edge, 50, 150, 3, true);

        Mat image255 = Mat.ones(imgSource.size(), CvType.CV_8UC1);

        Core.multiply(image255, new Scalar(255), image255);
        Core.subtract(image255, edge, edge);

        Imgproc.GaussianBlur(image255, image255, new Size(5, 5), 0.6);

        Imgproc.resize(edge, edgeResized, new Size(this.canvas.getWidth(), this.canvas.getHeight()));
        Imgproc.cvtColor(edgeResized, dst, Imgproc.COLOR_GRAY2RGBA, 4);

        Bitmap newImage = Bitmap.createBitmap(this.canvas.getWidth(), this.canvas.getHeight(), Bitmap.Config.ARGB_8888);


        Utils.matToBitmap(dst, newImage);

        this.canvas.drawBitmap(newImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            loadImage(picturePath);
            //ImageView imageView = (ImageView) findViewById(R.id.imgView);
        }

    }

}
