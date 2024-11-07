package hn.uth.reconocimientoocr.ui.gallery;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.InputStream;
import java.util.List;

import hn.uth.reconocimientoocr.R;
import hn.uth.reconocimientoocr.databinding.FragmentGalleryBinding;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private ImageView galleryImage;
    private Button btnSeleccionarImagen;
    private Button btnProcesar;
    private TextView txtResult;
    private Bitmap imagenSeleccionada;
    private Integer maxImageWidth;
    private Integer maxImageHeight;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel notificationsViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        galleryImage = binding.imgGaleria; //ASOCIO EL CONTROL DEL LAYOUT A LA VARIABLE DEL ACTIVITY O FRAGMENT
        btnSeleccionarImagen = binding.btnSeleccionarImagen; //ASOCIO EL CONTROL DEL LAYOUT A LA VARIABLE DEL ACTIVITY O FRAGMENT
        View.OnClickListener btnClick = v -> mostrarImagen();
        btnSeleccionarImagen.setOnClickListener(btnClick);

        btnProcesar = binding.btnProcesar;//ASOCIO EL CONTROL DEL LAYOUT A LA VARIABLE DEL ACTIVITY O FRAGMENT
        btnProcesar.setOnClickListener(v -> ejecutarReconocedorOCR());

        txtResult = binding.txtResult;//ASOCIO EL CONTROL DEL LAYOUT A LA VARIABLE DEL ACTIVITY O FRAGMENT

        Log.d("IMAGEN_GALERIA","Control imagen creado");
        imagenSeleccionada = obtenerImagenDesdeAsset(this.getContext(), "identidad.jpg");

        return root;
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
            galleryImage.setImageBitmap(resizedBitmap);
            imagenSeleccionada = resizedBitmap;
            Log.d("IMAGEN_GALERIA","Imagen cargada en pantalla");
        }
        btnProcesar.setEnabled(imagenSeleccionada != null);
    }

    private Pair<Integer, Integer> getTargetedWidthHeight(){
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;

        return new Pair<>(targetWidth, targetHeight);
    }

    private int getImageMaxWidth(){
        if(maxImageWidth == null){
            maxImageWidth = galleryImage.getWidth();
        }
        return maxImageWidth;
    }

    private int getImageMaxHeight(){
        if(maxImageHeight == null){
            maxImageHeight= galleryImage.getHeight();
        }
        return maxImageHeight;
    }

    private void ejecutarReconocedorOCR(){
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage imagenEnPantalla = InputImage.fromBitmap(imagenSeleccionada, 0);
        recognizer.process(imagenEnPantalla)
                .addOnSuccessListener(this::processTextRecognitionResult)
                .addOnFailureListener(e -> {
                    Log.d("IMAGEN_GALERIA","Error al extraer texto de imagen mediante OCR");
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
            textos.append(lineas.get(0).getText()).append("\n");
            for(int l=0; l<lineas.size();l++){
                List<Text.Element> elementos = lineas.get(l).getElements();
                for(int e=0;e<elementos.size(); e++){
                    Log.d("IMAGEN_GALERIA","ELEMENT -> "+elementos.get(e));
                }
            }
        }
        txtResult.setText(textos.toString());
    }

    private void showToast(String mensaje) {
        Toast.makeText(this.getContext(), mensaje, Toast.LENGTH_LONG).show();
    }

    private static Bitmap obtenerImagenDesdeAsset(Context contexto, String nombreArchivo){
        AssetManager assetManager = contexto.getAssets();
        InputStream is;
        Bitmap bitmap = null;
        try{
            Log.d("IMAGEN_GALERIA","Buscando imagen a mostrar...");
            is = assetManager.open(nombreArchivo);
            bitmap = BitmapFactory.decodeStream(is);
            Log.d("IMAGEN_GALERIA","Imagen Cargada...");
        }catch(Exception e){
            Log.d("IMAGEN_GALERIA","Error al cargar la imagen");
            Log.d("IMAGEN_GALERIA",e.getMessage());
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}