package hn.uth.reconocimientoocr.ui.camera;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import hn.uth.reconocimientoocr.databinding.FragmentCameraBinding;

public class CameraFragment extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSIONS = 100;

    private FragmentCameraBinding binding;
    private Button btnAbrirCamara;
    private ImageView imgVistaPrevia;
    private Bitmap imagenSeleccionada;
    private TextView txtResult;
    private String directorioImagen;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CameraViewModel dashboardViewModel =
                new ViewModelProvider(this).get(CameraViewModel.class);

        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        directorioImagen = "";

        imgVistaPrevia = binding.imgCamera;
        btnAbrirCamara = binding.btnCamara;
        txtResult = binding.txtResultCamera;


        Log.d("IMAGEN_CAMARA","Pantalla construida");
        btnAbrirCamara.setOnClickListener(v -> {
            if(checkAndRequestPermissions()){
                Log.d("IMAGEN_CAMARA","Permisos aceptados");
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(this.getActivity().getPackageManager()) != null) {
                    Log.d("IMAGEN_CAMARA","Intent camara resuelto");
                    File archivoImagen = null;
                    try{
                        archivoImagen = createImageFile();
                    }catch(Exception error){
                        error.printStackTrace();
                        Log.d("IMAGEN_CAMARA","Error al generar archivo de imagen");
                    }
                    if(archivoImagen != null){
                        Uri fotoUri = FileProvider.getUriForFile(this.getContext(), "hn.uth.reconocimientoocr.fileprovider", archivoImagen);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                    }

                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    Log.d("IMAGEN_CAMARA","Activity camara iniciada");
                }else{
                    Log.d("IMAGEN_CAMARA","No se encontró app para manejo de camara");
                }
            }
        });
        return root;
    }

    private File createImageFile() throws IOException {
        String fechaHoy = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreArchivo = "JPEG_"+fechaHoy+"_";
        File directorio = this.getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(nombreArchivo, ".jpg", directorio);
        directorioImagen = imagen.getAbsolutePath();

        return imagen;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Log.d("IMAGEN_CAMARA","Data de camara obtenida");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Log.d("IMAGEN_CAMARA","Data contiene imagen");

            if(!"".equals(directorioImagen) && directorioImagen != null){
                File imgFile = new File(directorioImagen);
                if(imgFile.exists()){
                    Bitmap imgAltaResolucion = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imagenSeleccionada = imgAltaResolucion;
                }else{
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imagenSeleccionada = imageBitmap;
                }
            }else{
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imagenSeleccionada = imageBitmap;
            }
            imgVistaPrevia.setImageBitmap(imagenSeleccionada);
            Log.d("IMAGEN_CAMARA","imagen mostrada en pantalla");
            ejecutarReconocedorOCR();
            Log.d("IMAGEN_CAMARA","Reconocedor OCR ejecutado");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean checkAndRequestPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.CAMERA);
        Log.d("IMAGEN_CAMARA","Evaluando permisos");

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
            int storagePermission = ContextCompat.checkSelfPermission(this.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (cameraPermission != PackageManager.PERMISSION_GRANTED || storagePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
                Log.d("IMAGEN_CAMARA","Permiso rechazado");
                return false;
            }
        }else{
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this.getActivity(),
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
                Log.d("IMAGEN_CAMARA","Permiso rechazado");
                return false;
            }
        }
        Log.d("IMAGEN_CAMARA","Permiso concedido");
        return true;
    }

    private void ejecutarReconocedorOCR(){
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage imagenEnPantalla = InputImage.fromBitmap(imagenSeleccionada, 0);
        recognizer.process(imagenEnPantalla)
                .addOnSuccessListener(this::processTextRecognitionResult)
                .addOnFailureListener(e -> {
                    Log.d("IMAGEN_CAMARA","Error al extraer texto de imagen mediante OCR");
                    e.printStackTrace();
                });

    }

    private void processTextRecognitionResult(Text text) {
        List<Text.TextBlock> bloques = text.getTextBlocks();
        if(bloques.size() == 0){
            showToast("No se encontró texto en la imagen");
            return;
        }
        StringBuilder textos = new StringBuilder();
        for (int i=0; i<bloques.size(); i++){
            List<Text.Line> lineas = bloques.get(i).getLines();

            for(Text.Line linea: lineas){
                textos.append(linea.getText()).append("\n");
            }
        }
        txtResult.setText(textos.toString());
    }

    private void showToast(String mensaje) {
        Toast.makeText(this.getContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}