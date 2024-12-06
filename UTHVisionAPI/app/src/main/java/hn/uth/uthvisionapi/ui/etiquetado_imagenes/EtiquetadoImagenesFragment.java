package hn.uth.uthvisionapi.ui.etiquetado_imagenes;

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
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabelerOptionsBase;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.List;

import hn.uth.uthvisionapi.R;
import hn.uth.uthvisionapi.databinding.FragmentEtiquetadoImagenesBinding;

public class EtiquetadoImagenesFragment extends Fragment {

    private FragmentEtiquetadoImagenesBinding binding;
    private ImageView imgVistaPrevia;
    private Bitmap imagenSeleccionada;
    private TextView txtResult;
    private Button btnAnalizar;
    private Integer maxImageWidth;
    private Integer maxImageHeight;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EtiquetadoImagenesViewModel galleryViewModel =
                new ViewModelProvider(this).get(EtiquetadoImagenesViewModel.class);

        binding = FragmentEtiquetadoImagenesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        txtResult = binding.txtResult;
        imgVistaPrevia = binding.imgPreviewRostros;
        btnAnalizar = binding.btnEjecutar;
        btnAnalizar.setVisibility(View.GONE);
        try{
            Bitmap fotoCamara = obtenerImagenParcelable("camara");
            Bitmap fotoGaleria = obtenerImagenParcelable("galeria");
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
        }catch(Exception error){
            error.printStackTrace();
            Log.d("ETIQUETADO","Error al cargar datos de inicio del fragmento");
        }


        btnAnalizar.setOnClickListener(event -> {
            ejecutarLecturaContexto();
        });

        return root;
    }

    private void ejecutarLecturaContexto() {
        ImageLabelerOptionsBase options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.5f)
                .build();
        ImageLabeler labeler = ImageLabeling.getClient(options);

        InputImage imagen = InputImage.fromBitmap(imagenSeleccionada, 0);
        Log.d("ETIQUETADO","Comenzando analisis");
        labeler.process(imagen)
                .addOnSuccessListener(event -> {
                    Log.d("ETIQUETADO","Analisis Ejecutado correctamente");
                    obtenerContexto(event);
                })
                .addOnFailureListener(error -> {
                    Log.d("ETIQUETADO","Analisis Ejecutado con error");
                    showToast("Ocurrió un problema al obtener el contexto de la imagens");
                    error.printStackTrace();
                });
    }

    private Bitmap obtenerImagenParcelable(String parametro){
        Bitmap foto;
        try {
            foto = (Bitmap) getArguments().getParcelable(parametro);
        }catch(Exception error){
            foto = null;
        }
        return foto;
    }

    private void obtenerContexto(List<ImageLabel> event) {
        StringBuilder texto = new StringBuilder();
        texto.append(event.size()+" palabras identificadas\n");
        for (ImageLabel etiqueta: event) {
            texto.append("Palabra: "+etiqueta.getText()+" "+(etiqueta.getConfidence() * 100)+"%\n");
        }
        txtResult.setText(texto.toString());
        Log.d("ETIQUETADO","Extracción de contexto finalizado");
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