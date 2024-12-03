package hn.uth.uthvisionapi.ui.reconocimiento_rostros;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

import hn.uth.uthvisionapi.R;
import hn.uth.uthvisionapi.databinding.FragmentReconocimientoRostrosBinding;

public class ReconocimientoRostrosFragment extends Fragment {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSIONS = 100;

    private FragmentReconocimientoRostrosBinding binding;
    private ImageView imgVistaPrevia;
    private Bitmap imagenSeleccionada;
    private TextView txtResult;
    private Button btnAnalizar;
    private Integer maxImageWidth;
    private Integer maxImageHeight;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ReconocimientoRostrosViewModel homeViewModel =
                new ViewModelProvider(this).get(ReconocimientoRostrosViewModel.class);

        binding = FragmentReconocimientoRostrosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        txtResult = binding.txtResult;
        imgVistaPrevia = binding.imgPreviewRostros;
        btnAnalizar = binding.btnEjecutar;
        btnAnalizar.setVisibility(View.GONE);

        Bitmap fotoCamara = (Bitmap)getArguments().getParcelable("camara");
        Bitmap fotoGaleria = (Bitmap)getArguments().getParcelable("galeria");
        if(fotoCamara != null){
            binding.imgPreviewRostros.setImageBitmap(fotoCamara);
            showToast(this.getActivity().getString(R.string.mensaje_foto_existe));
            imagenSeleccionada = fotoCamara;
            btnAnalizar.setVisibility(View.VISIBLE);
        }else if(fotoGaleria != null){
            showToast(this.getActivity().getString(R.string.mensaje_foto_existe));
            imagenSeleccionada = fotoGaleria;
            mostrarImagen();
        }

        btnAnalizar.setOnClickListener(event -> {
            ejecutarAnalisisRostro();
        });

        return root;
    }

    public void ejecutarAnalisisRostro(){
        //ESCANEO DE EJEMPLO
        InputImage imagen = InputImage.fromBitmap(imagenSeleccionada, 0);
        Log.d("RECONOCIMIENTO_ROSTRO","Comenzando analisis");
        FaceDetectorOptions opciones = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)//escaneo rápido, poca exactitud
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)//escaneo de expresiones faciales
                .build();
        Log.d("RECONOCIMIENTO_ROSTRO","opciones configuradas");
        FaceDetector detector = FaceDetection.getClient(opciones);
        detector.process(imagen)
                .addOnSuccessListener(this::procesarDatosRostro)
                .addOnFailureListener(error -> {
                    Log.d("RECONOCIMIENTO_ROSTRO","analisis fallido");
                    showToast(error.getLocalizedMessage());
                    error.printStackTrace();
                });
    }

    private void procesarDatosRostro(List<Face> faces) {
        Log.d("RECONOCIMIENTO_ROSTRO","analisis exitoso");
        if(faces.size() <= 0 ){
            showToast("No hay rostros en la imagen seleccionada");
            Log.d("RECONOCIMIENTO_ROSTRO","No hay rostros en la imagen seleccionada");
            return;
        }
        StringBuilder texto = new StringBuilder();
        texto.append("Hay "+faces.size()+" rostro(s) en la imagen \n");
        Log.d("RECONOCIMIENTO_ROSTRO","Hay "+faces.size()+" rostro(s) en la imagen");
        int contador = 1;
        for (Face rostro:faces) {
            if(rostro.getSmilingProbability() != null){
                float probabilidadSonriza = rostro.getSmilingProbability();

                if(probabilidadSonriza >= 0.5){
                    texto.append("Rostro "+contador+" está sonriendo  ("+probabilidadSonriza+")\n");
                    Log.d("RECONOCIMIENTO_ROSTRO","Rostro "+contador+" está sonriendo ("+probabilidadSonriza+")");
                }else{
                    texto.append("Rostro "+contador+" está serio  ("+probabilidadSonriza+")\n");
                    Log.d("RECONOCIMIENTO_ROSTRO","Rostro "+contador+" está serio ("+probabilidadSonriza+")");
                }
            }
        }

        txtResult.setText(texto);
    }


    private void mostrarImagen(){
        Log.d("IMAGEN_GALERIA","Validando imagen a mostrar en pantalla");
        if(imagenSeleccionada != null){
            Log.d("IMAGEN_GALERIA","Imagen valida");
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();
            int targetedWidth = targetedSize.first;
            int targetedHeight = targetedSize.second;
            Log.d("IMAGEN_GALERIA","Tamanio maximo calculado");
            Log.d("IMAGEN_GALERIA","Imagen "+imagenSeleccionada.getWidth()+"x"+imagenSeleccionada.getHeight());
            Log.d("IMAGEN_GALERIA","Pantalla "+targetedWidth+"x"+targetedHeight);
            float scaleFactor = Math.max(
                    (float) imagenSeleccionada.getWidth() / (float) targetedWidth,
                    (float) imagenSeleccionada.getHeight() / (float) targetedHeight);

            Log.d("IMAGEN_GALERIA","Factor de escala calculada: "+scaleFactor);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imagenSeleccionada,
                    (int) (imagenSeleccionada.getWidth() / scaleFactor),
                    (int) (imagenSeleccionada.getHeight() / scaleFactor),
                    true);
            Log.d("IMAGEN_GALERIA","Imagen redimencionada");

            imagenSeleccionada = resizedBitmap;
            binding.imgPreviewRostros.setImageBitmap(imagenSeleccionada);
            btnAnalizar.setVisibility(View.VISIBLE);
            Log.d("IMAGEN_GALERIA","Imagen cargada en pantalla");
        }
    }

    private Pair<Integer, Integer> getTargetedWidthHeight(){
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;

        return new Pair<>(targetWidth == 0?400:targetWidth, targetHeight == 0? 600:targetHeight);
    }

    private int getImageMaxWidth(){
        if(maxImageWidth == null){
            maxImageWidth = imgVistaPrevia.getWidth();
        }
        return maxImageWidth;
    }

    private int getImageMaxHeight(){
        if(maxImageHeight == null){
            maxImageHeight= imgVistaPrevia.getHeight();
        }
        return maxImageHeight;
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