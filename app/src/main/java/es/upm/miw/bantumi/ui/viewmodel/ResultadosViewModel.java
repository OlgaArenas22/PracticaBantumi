package es.upm.miw.bantumi.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import es.upm.miw.bantumi.data.database.entities.ResultEntity;
import es.upm.miw.bantumi.data.network.ResultRepository;

public class ResultadosViewModel extends AndroidViewModel {

    private final ResultRepository repository;

    private final MutableLiveData<List<ResultEntity>> results = new MutableLiveData<>();
    public LiveData<List<ResultEntity>> getResults() { return results; }

    private final MutableLiveData<Boolean> deleteAllSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> deleteAllError = new MutableLiveData<>();
    public LiveData<Boolean> getDeleteAllSuccess() { return deleteAllSuccess; }
    public LiveData<Boolean> getDeleteAllError() { return deleteAllError; }

    private final MutableLiveData<FilterState> currentFilter = new MutableLiveData<>(FilterState.defaults());
    public FilterState getCurrentFilter() { return currentFilter.getValue(); }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ResultadosViewModel(@NonNull Application application) {
        super(application);
        repository = ResultRepository.getInstance(application);
        loadDefault();
    }

    /** Carga por defecto: top 10 por mayor nº de semillas (sin filtros), como ahora */
    public void loadDefault() {
        executor.execute(() -> {
            List<ResultEntity> list = repository.getTop10ByBestSeedsList();
            results.postValue(list);
            currentFilter.postValue(FilterState.defaults());
        });
    }

    /** Aplica un filtro */
    public void applyFilter(FilterState filter) {
        currentFilter.setValue(filter);
        if (isDefault(filter)) {
            loadDefault();
        } else {
            loadFiltered(filter);
        }
    }

    /** Limpia filtros → vuelve al estado por defecto */
    public void clearFilter() {
        applyFilter(FilterState.defaults());
    }

    private void loadFiltered(FilterState filter) {
        executor.execute(() -> {
            List<ResultEntity> list = repository.getFilteredResults(filter);
            results.postValue(list);
        });
    }

    /** Re-ejecuta con el estado actual (útil tras borrar) */
    private void refreshCurrent() {
        FilterState f = currentFilter.getValue();
        if (f == null || isDefault(f)) loadDefault();
        else loadFiltered(f);
    }

    public void confirmDeleteAll() {
        repository.deleteAll(new ResultRepository.Callback() {
            @Override public void onSuccess() {
                deleteAllSuccess.postValue(true);
                refreshCurrent();
            }
            @Override public void onError(Exception e) {
                deleteAllError.postValue(true);
            }
        });
    }


    /** Comprueba si el filtro equivale al comportamiento por defecto. */
    private boolean isDefault(FilterState f) {
        if (f == null) return true;
        return f.order == FilterState.Order.SEEDS_DESC
                && f.outcome == FilterState.Outcome.ALL
                && (f.mode == null || f.mode.isEmpty())
                && (f.nameContains == null || f.nameContains.isEmpty());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
