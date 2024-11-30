package hn.uth.uthvisionapi.ui.reconocimiento_rostros;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ReconocimientoRostrosViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ReconocimientoRostrosViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}