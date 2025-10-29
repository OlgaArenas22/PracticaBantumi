package es.upm.miw.bantumi.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import es.upm.miw.bantumi.data.database.entities.ResultEntity;
import es.upm.miw.bantumi.data.network.ResultRepository;

public class ResultadosViewModel extends AndroidViewModel {

    private final ResultRepository repository;
    private final LiveData<List<ResultEntity>> top10;

    private final MutableLiveData<Boolean> deleteAllSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteAllError = new MutableLiveData<>();

    public ResultadosViewModel(@NonNull Application application) {
        super(application);
        repository = new ResultRepository(application);
        top10 = repository.getTop10ByBestSeeds();
    }

    public LiveData<List<ResultEntity>> getTop10() {
        return top10;
    }

    public LiveData<Boolean> getDeleteAllSuccess() { return deleteAllSuccess; }
    public LiveData<Boolean> getDeleteAllError() { return deleteAllError; }
    public void confirmDeleteAll() {
        repository.deleteAll(new ResultRepository.Callback() {
            @Override public void onSuccess() { deleteAllSuccess.postValue(true); }
            @Override public void onError(Exception e) { deleteAllError.postValue(true); }
        });
    }
}
