package es.upm.miw.bantumi.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CargarPartidaViewModel extends ViewModel {

    // Evento filename seleccionado
    private final MutableLiveData<String> _selectedFilename = new MutableLiveData<>(null);
    public LiveData<String> selectedFilename = _selectedFilename;

    // Evento cancelar (cerrar fragment)
    private final MutableLiveData<Boolean> _cancel = new MutableLiveData<>(false);
    public LiveData<Boolean> cancel = _cancel;

    public void confirmLoad(String filename) { _selectedFilename.setValue(filename); }
    public void requestCancel() { _cancel.setValue(true); }

    public void clearEvents() {
        _selectedFilename.setValue(null);
        _cancel.setValue(false);
    }
}
