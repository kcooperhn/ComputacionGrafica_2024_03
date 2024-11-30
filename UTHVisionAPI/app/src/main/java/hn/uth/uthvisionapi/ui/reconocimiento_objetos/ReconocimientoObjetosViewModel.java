package hn.uth.uthvisionapi.ui.reconocimiento_objetos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ReconocimientoObjetosViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ReconocimientoObjetosViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}