package hn.uth.uthvisionapi.ui.reconocimiento_codigos_barra;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ReconocimientoCodigosBarraViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ReconocimientoCodigosBarraViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}