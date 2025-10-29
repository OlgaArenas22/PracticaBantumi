package es.upm.miw.bantumi.ui.actividades;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
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
        //Formato del cronómetro
        cronometro.setOnChronometerTickListener(ch -> {
            long elapsed = SystemClock.elapsedRealtime() - ch.getBase();
            long s = elapsed / 1000;
            long h = s / 3600;
            long m = (s % 3600) / 60;
            long sec = s % 60;
            if (h > 0) {
                ch.setText(String.format("%d:%02d:%02d", h, m, sec));
            } else {
                ch.setText(String.format("%02d:%02d", m, sec));
            }
        });

        resetCronometro();
        guardarMgr = new GuardarPartidaManager(getApplicationContext());
        miniaturaMgr = new MiniaturaManager();
        cargarVM = new ViewModelProvider(this).get(CargarPartidaViewModel.class);
        observarEventosCargarPartida();

        //Permite cambiar el nombre del jugador al empezar el juego
        new ElegirNombreDialog().show(getSupportFragmentManager(), "DIALOG_NOMBRE");
    }

    //region Cronómetro
    public void startCronometro() {
        cronometro.setBase(SystemClock.elapsedRealtime());
        cronometro.start();
    }

    public void resumeCronometro(){
        cronometro.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        cronometro.start();
    }

    public void stopCronometro() {
        pauseOffset = SystemClock.elapsedRealtime() - cronometro.getBase();
        cronometro.stop();
    }

    public void resetCronometro() {
        cronometro.stop();
        pauseOffset = 0L;
        cronometro.setBase(SystemClock.elapsedRealtime());
    }

    //endregion

    //region ElegirModoJuego
    public void onMostrarElegirModo() {
        new ElegirModoDialog().show(getSupportFragmentManager(), "DIALOG_MODO");
    }
    public void onModoSeleccionado(int semillasIniciales) {
        // Cambia dinámicamente las semillas iniciales según el modo
        numInicialSemillas = semillasIniciales;
        onMostrarElegirTurno();
    }

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

            GuardarPartidaManager.SaveResult result = guardarMgr.guardarNuevaPartida(estado, nombreJ1, cronoTexto, cronoMillis, thumb, juegoBantumi.getNumInicialSemillas());

            Snackbar.make(findViewById(android.R.id.content),
                    result.ok ? getString(R.string.txtPartidaGuardadaOK)
                            : getString(R.string.txtPartidaGuardadaERROR),
                    Snackbar.LENGTH_LONG).show();

        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.txtPartidaGuardadaERROR),
                    Snackbar.LENGTH_LONG).show();
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
        // Confirmación: cargar la partida y cerrar fragment
        cargarVM.selectedFilename.observe(this, filename -> {
            if (filename == null) return;
            cargarPartida(filename);
            getSupportFragmentManager().popBackStack(); // cerrar fragment
            cargarVM.clearEvents();
        });

        // Cancelación: reanuda cronómetro y cerrar fragment
        cargarVM.cancel.observe(this, cancel -> {
            if (cancel == null || !cancel) return;
            resumeCronometro();
            getSupportFragmentManager().popBackStack();
            cargarVM.clearEvents();
        });
    }
    private void cargarPartida(String filename) {
        // 1) Leer JSON completo de la partida
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
            // 2) Estado del juego (modelo) -> delega en tu propio motor
            String estado = save.getString("estado");
            juegoBantumi.deserializa(estado);

            // 3) Nombre del jugador 1 (UI)
            String nombreJ1 = save.optString("jugador1", "");
            ((android.widget.TextView) findViewById(R.id.tvPlayer1)).setText(nombreJ1);

            // 4) Cronómetro
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

    /**
     * Instancia el ViewModel y el juego, y asigna observadores a los huecos
     */
    public void onIniciarPartida(Turno turno){
        this.turnoInicial = turno;
        resetCronometro();
        startCronometro();
        bantumiVM = new ViewModelProvider(this).get(BantumiViewModel.class);
        juegoBantumi = new JuegoBantumi(bantumiVM, turnoInicial, numInicialSemillas);
        crearObservadores();
    }
    /**
     * Crea y subscribe los observadores asignados a las posiciones del tablero.
     * Si se modifica el contenido del tablero -> se actualiza la vista.
     */
    private void crearObservadores() {
        for (int i = 0; i < JuegoBantumi.NUM_POSICIONES; i++) {
            int finalI = i;
            bantumiVM.getNumSemillas(i).observe(    // Huecos y almacenes
                    this,
                    new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            mostrarValor(finalI, juegoBantumi.getSemillas(finalI));
                        }
                    });
        }
        bantumiVM.getTurno().observe(   // Turno
                this,
                new Observer<JuegoBantumi.Turno>() {
                    @Override
                    public void onChanged(JuegoBantumi.Turno turno) {
                        marcarTurno(juegoBantumi.turnoActual());
                    }
                }
        );
    }

    /**
     * Indica el turno actual cambiando el color del texto
     *
     * @param turnoActual turno actual
     */
    private void marcarTurno(@NonNull JuegoBantumi.Turno turnoActual) {
        TextView tvJugador1 = findViewById(R.id.tvPlayer1);
        TextView tvJugador2 = findViewById(R.id.tvPlayer2);
        switch (turnoActual) {
            case turnoJ1:
                tvJugador1.setTextColor(getColor(R.color.white));
                tvJugador1.setBackgroundColor(getColor(android.R.color.holo_blue_light));
                tvJugador2.setTextColor(getColor(R.color.black));
                tvJugador2.setBackgroundColor(getColor(R.color.white));
                break;
            case turnoJ2:
                tvJugador1.setTextColor(getColor(R.color.black));
                tvJugador1.setBackgroundColor(getColor(R.color.white));
                tvJugador2.setTextColor(getColor(R.color.white));
                tvJugador2.setBackgroundColor(getColor(android.R.color.holo_blue_light));
                break;
            default:
                tvJugador1.setTextColor(getColor(R.color.black));
                tvJugador2.setTextColor(getColor(R.color.black));
        }
    }

    /**
     * Muestra el valor <i>valor</i> en la posición <i>pos</i>
     *
     * @param pos posición a actualizar
     * @param valor valor a mostrar
     */
    private void mostrarValor(int pos, int valor) {
        String num2digitos = String.format(Locale.getDefault(), "%02d", pos);
        // Los identificadores de los huecos tienen el formato casilla_XX
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
//            case R.id.opcAjustes: // @todo Preferencias
//                startActivity(new Intent(this, BantumiPrefs.class));
//                return true;
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

            // @TODO!!! resto opciones

            default:
                Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.txtSinImplementar),
                        Snackbar.LENGTH_LONG
                ).show();
        }
        return true;
    }

    //endregion
    /**
     * Acción que se ejecuta al pulsar sobre cualquier hueco
     *
     * @param v Vista pulsada (hueco)
     */
    public void huecoPulsado(@NonNull View v) {
        String resourceName = getResources().getResourceEntryName(v.getId()); // pXY
        int num = Integer.parseInt(resourceName.substring(resourceName.length() - 2));
        Log.i(LOG_TAG, "huecoPulsado(" + resourceName + ") num=" + num);
        switch (juegoBantumi.turnoActual()) {
            case turnoJ1:
                Log.i(LOG_TAG, "* Juega Jugador");
                juegoBantumi.jugar(num);
                break;
            case turnoJ2:
                Log.i(LOG_TAG, "* Juega Computador");
                juegoBantumi.juegaComputador();
                break;
            default:    // JUEGO TERMINADO
                finJuego();
        }
        if (juegoBantumi.juegoTerminado()) {
            finJuego();
        }
    }

    /**
     * El juego ha terminado. Volver a jugar?
     */
    private void finJuego() {
        // 1) Pausar cronómetro
        stopCronometro();
        long elapsedMillis = android.os.SystemClock.elapsedRealtime() - cronometro.getBase();

        int seedsPlayer1 = juegoBantumi.getSemillas(6);
        int seedsPlayer2 = juegoBantumi.getSemillas(13);
        // 2) Datos de nombres
        String player1Name = getNombreJugador1();
        String winnerName;
        boolean player1Won = seedsPlayer1 > seedsPlayer2;
        if (seedsPlayer1 == seedsPlayer2) {
            player1Won = false;
            winnerName = getString(R.string.txtEmpate);
        } else {
            winnerName = player1Won ? player1Name : getString(R.string.txtPlayer2);
        }

        // 3) Modo por minSeeds
        String mode = mapMode();

        // 4) Guardar en Room (en background)
        ResultRepository repo =
                new ResultRepository(getApplicationContext());

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
        repo.insertAsync(entity);

        // 5) Mostrar diálogo fin de partida
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