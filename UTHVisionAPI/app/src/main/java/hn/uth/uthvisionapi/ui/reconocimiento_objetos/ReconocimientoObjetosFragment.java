package hn.uth.uthvisionapi.ui.reconocimiento_objetos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import hn.uth.uthvisionapi.databinding.FragmentReconocimientoObjetosBinding;

public class ReconocimientoObjetosFragment extends Fragment {

    private FragmentReconocimientoObjetosBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ReconocimientoObjetosViewModel slideshowViewModel =
                new ViewModelProvider(this).get(ReconocimientoObjetosViewModel.class);

        binding = FragmentReconocimientoObjetosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSlideshow;
        slideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}