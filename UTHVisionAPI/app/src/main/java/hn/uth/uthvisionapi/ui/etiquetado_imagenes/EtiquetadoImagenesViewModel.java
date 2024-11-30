package hn.uth.uthvisionapi.ui.etiquetado_imagenes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class EtiquetadoImagenesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public EtiquetadoImagenesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}