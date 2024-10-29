package hn.uth.reconocimientoocr;

import android.os.Bundle;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import hn.uth.reconocimientoocr.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        showMessage(getString(R.string.message_on_create));
        Log.d("CICLO_VIDA",getString(R.string.message_on_start));
    }

    private void showMessage(String message){
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.nav_host_fragment_activity_main),
                message, Snackbar.LENGTH_LONG);
        mySnackbar.show();
    }

   /* @Override
    protected void onStart() {
        super.onStart();
        showMessage(getString(R.string.message_on_start));
        Log.d("CICLO_VIDA",getString(R.string.message_on_start));
    }

    @Override
    protected void onStop() {
        showMessage(getString(R.string.message_on_stop));
        Log.d("CICLO_VIDA",getString(R.string.message_on_stop));
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        showMessage(getString(R.string.message_on_destroy));
        Log.d("CICLO_VIDA",getString(R.string.message_on_destroy));
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        showMessage(getString(R.string.message_on_pause));
        Log.d("CICLO_VIDA",getString(R.string.message_on_pause));
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showMessage(getString(R.string.message_on_resume));
        Log.d("CICLO_VIDA",getString(R.string.message_on_resume));
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        showMessage(getString(R.string.message_on_resume));
        Log.d("CICLO_VIDA",getString(R.string.message_on_resume));
    }

    @Override
    protected void onRestart() {
        showMessage(getString(R.string.message_on_restart));
        Log.d("CICLO_VIDA",getString(R.string.message_on_restart));
        super.onRestart();
    }*/
}