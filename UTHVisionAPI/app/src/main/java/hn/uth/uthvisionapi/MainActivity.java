package hn.uth.uthvisionapi;

import android.Manifest;
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
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.mlkit.vision.common.InputImage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import hn.uth.uthvisionapi.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static int PICK_IMAGE = 2;
    private static final int REQUEST_PERMISSIONS = 100;
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private String directorioImagen;
    private Bitmap imagenSeleccionada;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        directorioImagen = "";
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.btnAbrirCamara.setOnClickListener(view -> {
            if(checkAndRequestPermissions()){
                Log.d("IMAGEN_CAMARA","Permisos aceptados");
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    Log.d("IMAGEN_CAMARA","Intent camara resuelto");
                    File archivoImagen = null;
                    try{
                        archivoImagen = createImageFile();
                    }catch(Exception error){
                        error.printStackTrace();
                        Log.d("IMAGEN_CAMARA","Error al generar archivo de imagen");
                    }
                    if(archivoImagen != null){
                        Uri fotoUri = FileProvider.getUriForFile(getBaseContext(), "hn.uth.uthvisionapi.fileprovider", archivoImagen);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                    }

                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    Log.d("IMAGEN_CAMARA","Activity camara iniciada");
                }else{
                    Log.d("IMAGEN_CAMARA","No se encontró app para manejo de camara");
                }
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_reconocimiento_rostros, R.id.nav_etiquetado, R.id.nav_reconocimiento_objetos)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_open_gallery){
            seleccionarImagenGaleria();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
            Log.d("IMAGEN_CAMARA","imagen mostrada en pantalla");
            navegarProcesamiento("camara");
        }
        if(requestCode == PICK_IMAGE){
            if(data == null){
                showToast("Favor seleccionar una foto de la galeria");
            }else{
                final Uri uri = data.getData();
                InputImage image;
                try{
                    image = InputImage.fromFilePath(this, uri);
                    imagenSeleccionada = image.getBitmapInternal();
                    navegarProcesamiento("galeria");
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void navegarProcesamiento(String tipoFoto) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(tipoFoto, imagenSeleccionada);
        navController.navigate(R.id.nav_reconocimiento_rostros, bundle);
    }

    private void seleccionarImagenGaleria(){
        Intent galeriaIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galeriaIntent.setType("image/*");

        Intent seleccionIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        seleccionIntent.setType("image/*");

        Intent menuIntent = Intent.createChooser(galeriaIntent, "Seleccione una Imagen");
        menuIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{seleccionIntent});

        startActivityForResult(menuIntent, PICK_IMAGE);
    }

    private File createImageFile() throws IOException {
        String fechaHoy = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreArchivo = "JPEG_"+fechaHoy+"_";
        File directorio = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imagen = File.createTempFile(nombreArchivo, ".jpg", directorio);
        directorioImagen = imagen.getAbsolutePath();

        return imagen;
    }

    private boolean checkAndRequestPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        Log.d("IMAGEN_CAMARA","Evaluando permisos");

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
            int storagePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (cameraPermission != PackageManager.PERMISSION_GRANTED || storagePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
                Log.d("IMAGEN_CAMARA","Permiso rechazado");
                return false;
            }
        }else{
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
                Log.d("IMAGEN_CAMARA","Permiso rechazado");
                return false;
            }
        }
        Log.d("IMAGEN_CAMARA","Permiso concedido");
        return true;
    }

    private void showToast(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }
}