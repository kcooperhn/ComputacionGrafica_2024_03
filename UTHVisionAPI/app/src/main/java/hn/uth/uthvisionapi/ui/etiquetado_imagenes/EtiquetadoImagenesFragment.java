package hn.uth.uthvisionapi.ui.etiquetado_imagenes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import hn.uth.uthvisionapi.databinding.FragmentEtiquetadoImagenesBinding;

public class EtiquetadoImagenesFragment extends Fragment {

    private FragmentEtiquetadoImagenesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EtiquetadoImagenesViewModel galleryViewModel =
                new ViewModelProvider(this).get(EtiquetadoImagenesViewModel.class);

        binding = FragmentEtiquetadoImagenesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textGallery;
        galleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}