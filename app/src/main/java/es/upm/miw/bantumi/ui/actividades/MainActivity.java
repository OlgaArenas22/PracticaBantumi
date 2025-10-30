package es.upm.miw.bantumi.ui.actividades;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

import es.upm.miw.bantumi.data.database.entities.ResultEntity;
import es.upm.miw.bantumi.data.network.ResultRepository;
import es.upm.miw.bantumi.ui.fragmentos.CargarPartidaFragment;
import es.upm.miw.bantumi.ui.fragmentos.ElegirModoDialog;
import es.upm.miw.bantumi.ui.fragmentos.ElegirNombreDialog;
import es.upm.miw.bantumi.ui.fragmentos.ElegirTurnoDialog;
import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.dominio.logica.JuegoBantumi;
import es.upm.miw.bantumi.dominio.logica.JuegoBantumi.Turno;
import es.upm.miw.bantumi.ui.fragmentos.FinPartidaDialog;
import es.upm.miw.bantumi.ui.fragmentos.GuardarPartidaDialog;
import es.upm.miw.bantumi.ui.fragmentos.ReiniciarPartidaDialog;
import es.upm.miw.bantumi.ui.fragmentos.ResultadosFragment;
import es.upm.miw.bantumi.ui.managers.CargarPartidaManager;
import es.upm.miw.bantumi.ui.managers.GuardarPartidaManager;
import es.upm.miw.bantumi.ui.managers.MiniaturaManager;
import es.upm.miw.bantumi.ui.viewmodel.BantumiViewModel;
import es.upm.miw.bantumi.ui.viewmodel.CargarPartidaViewModel;
import android.os.Handler;
import android.os.Looper;

public class MainActivity extends AppCompatActivity {

    protected final String LOG_TAG = "MiW";
    private long pauseOffset = 0L;
    public JuegoBantumi juegoBantumi;
    private BantumiViewModel bantumiVM;
    private CargarPartidaViewModel cargarVM;
    private Turno turnoInicial;
    private Chronometer cronometro;
    int numInicialSemillas;
    private GuardarPartidaManager guardarMgr;
    private MiniaturaManager miniaturaMgr;

    private int selectedPitIndex = -1;
    private View selectedPitView = null;

    private View getPitView(int pos) {
        String num2digitos = String.format(Locale.getDefault(), "%02d", pos);
        int id = getResources().getIdentifier("casilla_" + num2digitos, "id", getPackageName());
        return (id != 0) ? findViewById(id) : null;
    }

    private void markSelected(int pos) {
        try {
            // Desmarcar anterior
            if (selectedPitView != null) selectedPitView.setSelected(false);

            selectedPitIndex = pos;
            View v = getPitView(pos);
            selectedPitView = v;
            if (v == null) { selectedPitIndex = -1; return; }

            v.setSelected(true);

            // Si la vista no está lista para animarse, difiere la animación
            boolean attached = androidx.core.view.ViewCompat.isAttachedToWindow(v);
            boolean laidOut = androidx.core.view.ViewCompat.isLaidOut(v); // true si ya tiene medidas/pos
            if (!attached || !laidOut || v.getWidth() == 0 || v.getHeight() == 0) {
                v.post(() -> safePulse(v));
            } else {
                safePulse(v);
            }
        } catch (Throwable t) {
            // Fallback ultra seguro: no animar, solo estado visual
            if (selectedPitView != null) selectedPitView.setSelected(true);
        }
    }
    private void safePulse(@NonNull View v) {
        try {
            // Usa referencia local; no dependas de selectedPitView que puede cambiar
            v.animate().cancel();
            v.animate()
                    .scaleX(1.06f)
                    .scaleY(1.06f)
                    .setDuration(120)
                    .withEndAction(() -> {
                        try {
                            v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                        } catch (Throwable ignore) {}
                    })
                    .start();
        } catch (Throwable ignore) {
            // Si fallara por cualquier motivo, simplemente no animamos
        }
    }


    private void clearSelected() {
        if (selectedPitView != null) {
            selectedPitView.setSelected(false);
            selectedPitView = null;
        }
        selectedPitIndex = -1;
    }
    // === Fin selección ===

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cronometro = findViewById(R.id.cronometro);
        cronometro.setOnChronometerTickListener(ch -> {
            long elapsed = SystemClock.elapsedRealtime() - ch.getBase();
            long s = elapsed / 1000;
            long h = s / 3600;
            long m = (s % 3600) / 60;
            long sec = s % 60;
            if (h > 0) ch.setText(String.format("%d:%02d:%02d", h, m, sec));
            else ch.setText(String.format("%02d:%02d", m, sec));
        });

        resetCronometro();
        guardarMgr = new GuardarPartidaManager(getApplicationContext());
        miniaturaMgr = new MiniaturaManager();
        cargarVM = new ViewModelProvider(this).get(CargarPartidaViewModel.class);
        observarEventosCargarPartida();

        new ElegirNombreDialog().show(getSupportFragmentManager(), "DIALOG_NOMBRE");
    }

    //region Cronómetro
    public void startCronometro() { cronometro.setBase(SystemClock.elapsedRealtime()); cronometro.start(); }
    public void resumeCronometro(){ cronometro.setBase(SystemClock.elapsedRealtime() - pauseOffset); cronometro.start(); }
    public void stopCronometro() { pauseOffset = SystemClock.elapsedRealtime() - cronometro.getBase(); cronometro.stop(); }
    public void resetCronometro() { cronometro.stop(); pauseOffset = 0L; cronometro.setBase(SystemClock.elapsedRealtime()); }
    //endregion

    //region ElegirModoJuego
    public void onMostrarElegirModo() { new ElegirModoDialog().show(getSupportFragmentManager(), "DIALOG_MODO"); }
    public void onModoSeleccionado(int semillasIniciales) { numInicialSemillas = semillasIniciales; onMostrarElegirTurno(); }
    //endregion

    //region GuardarPartida
    public void guardarPartida() {
        try {
            String nombreJ1 = getNombreJugador1();
            String estado = juegoBantumi.serializa(nombreJ1);
            String cronoTexto = cronometro.getText().toString();
            long cronoMillis = parseMmSsToMillis(cronoTexto);

            View root = findViewById(R.id.main);
            Bitmap thumb = miniaturaMgr.crearMiniaturaCuadrada(root, GuardarPartidaManager.THUMB_SIZE);

            GuardarPartidaManager.SaveResult result = guardarMgr.guardarNuevaPartida(
                    estado, nombreJ1, cronoTexto, cronoMillis, thumb, juegoBantumi.getNumInicialSemillas());

            Snackbar.make(findViewById(android.R.id.content),
                    result.ok ? getString(R.string.txtPartidaGuardadaOK) : getString(R.string.txtPartidaGuardadaERROR),
                    Snackbar.LENGTH_LONG).show();

        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.txtPartidaGuardadaERROR), Snackbar.LENGTH_LONG).show();
        }
    }

    private long parseMmSsToMillis(@NonNull String text) {
        String[] p = text.split(":");
        long m = Long.parseLong(p[0]);
        long s = Long.parseLong(p[1]);
        return (m * 60 + s) * 1000L;
    }
    //endregion

    //region CargarPartida
    private void observarEventosCargarPartida() {
        cargarVM.selectedFilename.observe(this, filename -> {
            if (filename == null) return;
            cargarPartida(filename);
            getSupportFragmentManager().popBackStack();
            cargarVM.clearEvents();
        });

        cargarVM.cancel.observe(this, cancel -> {
            if (cancel == null || !cancel) return;
            resumeCronometro();
            getSupportFragmentManager().popBackStack();
            cargarVM.clearEvents();
        });
    }

    private void cargarPartida(String filename) {
        CargarPartidaManager cargarMgr = new CargarPartidaManager(getApplicationContext());
        org.json.JSONObject save = cargarMgr.readSave(filename);
        if (save == null) {
            com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.txtPartidaGuardadaERROR),
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
            resumeCronometro();
            return;
        }
        try {
            String estado = save.getString("estado");
            juegoBantumi.deserializa(estado);

            String nombreJ1 = save.optString("jugador1", "");
            ((android.widget.TextView) findViewById(R.id.tvPlayer1)).setText(nombreJ1);

            long cronoMillis = save.optLong("cronometro_millis", 0L);
            cronometro.stop();
            cronometro.setBase(android.os.SystemClock.elapsedRealtime() - cronoMillis);
            cronometro.start();
            juegoBantumi.setNumInicialSemillas(save.optInt("seeds", -1));

        } catch (Exception e) {
            com.google.android.material.snackbar.Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.txtPartidaGuardadaERROR),
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show();
            resumeCronometro();
        }
    }
    //endregion

    public void onMostrarElegirTurno() {
        new ElegirTurnoDialog(getNombreJugador1())
                .show(getSupportFragmentManager(), "DIALOG_TURNO");
    }

    /** Instancia VM y juego; registra listener de selección y observadores */
    public void onIniciarPartida(Turno turno){
        this.turnoInicial = turno;
        resetCronometro();
        startCronometro();
        bantumiVM = new ViewModelProvider(this).get(BantumiViewModel.class);
        juegoBantumi = new JuegoBantumi(bantumiVM, turnoInicial, numInicialSemillas);

        // Escuchar casilla elegida (J1 y J2)
        juegoBantumi.setOnPitSelectedListener(pos -> runOnUiThread(() -> markSelected(pos)));

        crearObservadores();
    }

    private void crearObservadores() {
        for (int i = 0; i < JuegoBantumi.NUM_POSICIONES; i++) {
            int finalI = i;
            bantumiVM.getNumSemillas(i).observe(this, new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    mostrarValor(finalI, juegoBantumi.getSemillas(finalI));
                }
            });
        }
        bantumiVM.getTurno().observe(this, turno -> {
            // Pequeño delay para permitir que el highlight recién puesto se vea 1 instante
            new Handler(Looper.getMainLooper()).postDelayed(this::clearSelected, 1000);
            marcarTurno(juegoBantumi.turnoActual());
        });
    }

    private void marcarTurno(@NonNull JuegoBantumi.Turno turnoActual) {
        TextView tvJugador1 = findViewById(R.id.tvPlayer1);
        TextView tvJugador2 = findViewById(R.id.tvPlayer2);
        switch (turnoActual) {
            case turnoJ1:
                tvJugador1.setTextColor(getColor(R.color.black));
                tvJugador1.setBackgroundColor(getColor(R.color.white));
                tvJugador2.setTextColor(getColor(R.color.white));
                tvJugador2.setBackgroundColor(Color.TRANSPARENT);
                break;
            case turnoJ2:
                tvJugador1.setTextColor(getColor(R.color.white));
                tvJugador1.setBackgroundColor(Color.TRANSPARENT);
                tvJugador2.setTextColor(getColor(R.color.black));
                tvJugador2.setBackgroundColor(getColor(R.color.white));
                break;
            default:
                tvJugador1.setTextColor(getColor(R.color.white));
                tvJugador2.setTextColor(getColor(R.color.white));
        }
    }

    private void mostrarValor(int pos, int valor) {
        String num2digitos = String.format(Locale.getDefault(), "%02d", pos);
        int idBoton = getResources().getIdentifier("casilla_" + num2digitos, "id", getPackageName());
        if (0 != idBoton) {
            TextView viewHueco = findViewById(idBoton);
            viewHueco.setText(String.valueOf(valor));
        }
    }

    //region Menú
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.opciones_menu, menu);
        try {
            java.lang.reflect.Method m = menu.getClass()
                    .getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            m.invoke(menu, true);
        } catch (Exception ignore) { }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.opcMejoresResultados:
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.main, ResultadosFragment.newInstance())
                        .addToBackStack("results")
                        .commit();
                return true;
            case R.id.opcReiniciarPartida:
                new ReiniciarPartidaDialog().show(getSupportFragmentManager(),"DIALOG_REBOOT_GAME");
                return true;
            case R.id.opcGuardarPartida:
                new GuardarPartidaDialog().show(getSupportFragmentManager(), "DIALOG_SAVE_GAME");
                return true;
            case R.id.opcCargarPartida:
                stopCronometro();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.main, new CargarPartidaFragment())
                        .addToBackStack("cargar_partida")
                        .commit();
                return true;
            case R.id.opcAcercaDe:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.aboutTitle)
                        .setMessage(R.string.aboutMessage)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return true;
        }
        return true;
    }
    //endregion

    /** Acción al pulsar un hueco */
    public void huecoPulsado(@NonNull View v) {
        String resourceName = getResources().getResourceEntryName(v.getId());
        int num = Integer.parseInt(resourceName.substring(resourceName.length() - 2));
        Log.i(LOG_TAG, "huecoPulsado(" + resourceName + ") num=" + num);
        switch (juegoBantumi.turnoActual()) {
            case turnoJ1:
                Log.i(LOG_TAG, "* Juega Jugador");
                juegoBantumi.jugar(num); // emitirá onPitSelected(num) -> markSelected(num)
                break;
            case turnoJ2:
                Log.i(LOG_TAG, "* Juega Computador");
                juegoBantumi.juegaComputador(); // IA llamará a jugar() y también emitirá
                break;
            default:
                finJuego();
        }
        if (juegoBantumi.juegoTerminado()) {
            finJuego();
        }
    }

    /** Fin de juego */
    private void finJuego() {
        // limpiar selección por si quedara marcada
        clearSelected();

        stopCronometro();
        long elapsedMillis = SystemClock.elapsedRealtime() - cronometro.getBase();

        int seedsPlayer1 = juegoBantumi.getSemillas(6);
        int seedsPlayer2 = juegoBantumi.getSemillas(13);

        String player1Name = getNombreJugador1();
        String winnerName;
        boolean player1Won = seedsPlayer1 > seedsPlayer2;
        if (seedsPlayer1 == seedsPlayer2) {
            player1Won = false;
            winnerName = getString(R.string.txtEmpate);
        } else {
            winnerName = player1Won ? player1Name : getString(R.string.txtPlayer2);
        }

        String mode = mapMode();

        ResultEntity entity =
                new ResultEntity(
                        winnerName,
                        player1Name,
                        seedsPlayer1,
                        seedsPlayer2,
                        elapsedMillis,
                        mode,
                        System.currentTimeMillis(),
                        player1Won
                );
        ResultRepository repo = ResultRepository.getInstance(getApplicationContext());
        repo.insertAsync(entity);

        new FinPartidaDialog(player1Name,player1Won).show(getSupportFragmentManager(), "FIN_PARTIDA_DIALOG");
    }

    private String mapMode() {
        int minSeeds = juegoBantumi.getNumInicialSemillas();
        return (minSeeds == 2) ? getString(R.string.txtRapido)
                : (minSeeds == 8) ? getString(R.string.txtFiebre)
                : getString(R.string.txtClasico);
    }

    public Turno getTurnoInicial(){
        return this.turnoInicial;
    }

    private String getNombreJugador1() {
        TextView tvJugador1 = findViewById(R.id.tvPlayer1);
        return tvJugador1.getText().toString();
    }
}
