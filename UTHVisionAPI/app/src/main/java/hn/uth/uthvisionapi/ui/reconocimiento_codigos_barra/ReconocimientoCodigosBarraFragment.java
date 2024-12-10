package hn.uth.uthvisionapi.ui.reconocimiento_codigos_barra;

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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

import hn.uth.uthvisionapi.R;
import hn.uth.uthvisionapi.databinding.FragmentReconocimientoCodigosBarraBinding;

public class ReconocimientoCodigosBarraFragment extends Fragment {

    private FragmentReconocimientoCodigosBarraBinding binding;
    private ImageView imgVistaPrevia;
    private Bitmap imagenSeleccionada;
    private TextView txtResult;
    private Button btnAnalizar;
    private Integer maxImageWidth;
    private Integer maxImageHeight;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ReconocimientoCodigosBarraViewModel slideshowViewModel =
                new ViewModelProvider(this).get(ReconocimientoCodigosBarraViewModel.class);

        binding = FragmentReconocimientoCodigosBarraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        txtResult = binding.txtResult;
        imgVistaPrevia = binding.imgPreviewRostros;
        btnAnalizar = binding.btnEjecutar;
        btnAnalizar.setVisibility(View.GONE);
        Log.d("CODIGO_BARRAS","cargando pantalla de codigos de barra");
        try{
            Bitmap fotoCamara = obtenerImagenParcelable("camara");
            Bitmap fotoGaleria = obtenerImagenParcelable("galeria");
            if(fotoCamara != null){
                Log.d("CODIGO_BARRAS","cargando imagen de camara en pantalla");
                binding.imgPreviewRostros.setImageBitmap(fotoCamara);
                showToast(this.getActivity().getString(R.string.mensaje_foto_existe));
                imagenSeleccionada = fotoCamara;
                btnAnalizar.setVisibility(View.VISIBLE);
            }else if(fotoGaleria != null){
                Log.d("CODIGO_BARRAS","cargando imagen de galeria en pantalla");
                showToast(this.getActivity().getString(R.string.mensaje_foto_existe));
                imagenSeleccionada = fotoGaleria;
                mostrarImagen();
            }
        }catch(Exception error){
            error.printStackTrace();
            Log.d("CODIGO_BARRAS","Error al cargar datos de inicio del fragmento");
        }

        btnAnalizar.setOnClickListener(event -> {
            ejecutarLecturaCodigoBarra();
        });

        return root;
    }

    private void ejecutarLecturaCodigoBarra() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC)
                .build();

        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        InputImage image = InputImage.fromBitmap(imagenSeleccionada, 0);

        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    procesarCodigoBarra(barcodes);
                })
                .addOnFailureListener(error -> {
                    Log.d("CODIGO_BARRAS","Analisis Ejecutado con error");
                    showToast("Ocurrió un problema al leer el codigo de barras");
                    error.printStackTrace();
                });
    }

    private void procesarCodigoBarra(List<Barcode> barcodes) {
        StringBuilder resultado = new StringBuilder();
        if(barcodes.size() == 0){
            resultado.append("No hay ningún código de barras reconocible en la imagen");
        }
        for (Barcode barcode: barcodes) {
            String rawValue = barcode.getRawValue();
            resultado.append(rawValue+"\n");
            int valueType = barcode.getValueType();
            switch (valueType) {
                case Barcode.TYPE_WIFI:
                    String ssid = barcode.getWifi().getSsid();
                    String password = barcode.getWifi().getPassword();
                    int type = barcode.getWifi().getEncryptionType();
                    resultado.append("ES UN CODIGO DE WIFI\n");
                    break;
                case Barcode.TYPE_URL:
                    String title = barcode.getUrl().getTitle();
                    String url = barcode.getUrl().getUrl();
                    resultado.append("ES UN CODIGO DE URL\n");
                    break;
            }
        }
        txtResult.setText(resultado.toString());
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

    private void mostrarImagen(){
        Log.d("CODIGO_BARRAS","Validando imagen a mostrar en pantalla");
        if(imagenSeleccionada != null){
            Log.d("IMAGEN_GALERIA","Imagen valida");
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();
            int targetedWidth = targetedSize.first;
            int targetedHeight = targetedSize.second;
            Log.d("CODIGO_BARRAS","Tamanio maximo calculado");
            Log.d("CODIGO_BARRAS","Imagen "+imagenSeleccionada.getWidth()+"x"+imagenSeleccionada.getHeight());
            Log.d("CODIGO_BARRAS","Pantalla "+targetedWidth+"x"+targetedHeight);
            float scaleFactor = Math.max(
                    (float) imagenSeleccionada.getWidth() / (float) targetedWidth,
                    (float) imagenSeleccionada.getHeight() / (float) targetedHeight);

            Log.d("CODIGO_BARRAS","Factor de escala calculada: "+scaleFactor);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(imagenSeleccionada,
                    (int) (imagenSeleccionada.getWidth() / scaleFactor),
                    (int) (imagenSeleccionada.getHeight() / scaleFactor),
                    true);
            Log.d("CODIGO_BARRAS","Imagen redimencionada");

            imagenSeleccionada = resizedBitmap;
            binding.imgPreviewRostros.setImageBitmap(imagenSeleccionada);
            btnAnalizar.setVisibility(View.VISIBLE);
            Log.d("CODIGO_BARRAS","Imagen cargada en pantalla");
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