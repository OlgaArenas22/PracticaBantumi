package es.upm.miw.bantumi.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CargarPartidaViewModel extends ViewModel {

    private final MutableLiveData<String> _selectedFilename = new MutableLiveData<>(null);
    public LiveData<String> selectedFilename = _selectedFilename;
    private final MutableLiveData<Boolean> _cancel = new MutableLiveData<>(false);
    public LiveData<Boolean> cancel = _cancel;

    private final MutableLiveData<String> _deleteConfirmed = new MutableLiveData<>(null);
    public LiveData<String> deleteConfirmed = _deleteConfirmed;

    public void confirmLoad(String filename) { _selectedFilename.setValue(filename); }
    public void requestCancel() { _cancel.setValue(true); }

    public void confirmDelete(String filename) { _deleteConfirmed.setValue(filename); }

    public void clearEvents() {
        _selectedFilename.setValue(null);
        _cancel.setValue(false);
        _deleteConfirmed.setValue(null);
    }
}
