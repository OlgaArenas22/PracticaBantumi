package es.upm.miw.bantumi.ui.actividades;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
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

import java.io.File;
import java.util.Locale;

import es.upm.miw.bantumi.ui.fragmentos.ElegirModoDialog;
import es.upm.miw.bantumi.ui.fragmentos.ElegirNombreDialog;
import es.upm.miw.bantumi.ui.fragmentos.ElegirTurnoDialog;
import es.upm.miw.bantumi.ui.fragmentos.FinalAlertDialog;
import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.dominio.logica.JuegoBantumi;
import es.upm.miw.bantumi.dominio.logica.JuegoBantumi.Turno;
import es.upm.miw.bantumi.ui.fragmentos.GuardarPartidaDialog;
import es.upm.miw.bantumi.ui.fragmentos.ReiniciarPartidaDialog;
import es.upm.miw.bantumi.ui.viewmodel.BantumiViewModel;

public class MainActivity extends AppCompatActivity {

    protected final String LOG_TAG = "MiW";
    //======= VARIABLES GUARDAR PARTIDA =======
    private static final String INDEX_FILENAME   = "index.json";
    private static final String SAVE_FILE_PREFIX = "bantumi_save_";
    private static final String SAVE_FILE_EXT    = ".json";
    private static final int    MAX_SAVES        = 10;

    //====== VARIABLES GUARDAR MINIATURA ======
    private static final String THUMB_PREFIX = "bantumi_thumb_";
    private static final String THUMB_EXT    = ".png";
    private static final int    THUMB_SIZE       = 256;

    //====== VARIABLES RUTAS MINIATURAS =======
    private static final String SAVES_DIR   = "saves";
    private static final String JSON_DIR    = "saves/json";
    private static final String THUMBS_DIR  = "saves/thumbs";
    //==========================================
    public JuegoBantumi juegoBantumi;
    private BantumiViewModel bantumiVM;
    private Turno turnoInicial;
    private Chronometer cronometro;
    int numInicialSemillas;

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

        //Permite cambiar el nombre del jugador al empezar el juego
        new ElegirNombreDialog().show(getSupportFragmentManager(), "DIALOG_NOMBRE");
    }

    //region Cronómetro
    public void startCronometro() {
        cronometro.setBase(SystemClock.elapsedRealtime());
        cronometro.start();
    }

    public void resumeCronometro(){
        cronometro.start();
    }

    public void stopCronometro() {
        cronometro.stop();
    }

    public void resetCronometro() {
        cronometro.stop();
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
            org.json.JSONObject index = loadSavesIndex();
            int nextId = index.optInt("next_id", 1);
            org.json.JSONArray saves = index.optJSONArray("saves");
            if (saves == null) saves = new org.json.JSONArray();

            if (saves.length() >= MAX_SAVES) {
                Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.txtLimitePartidasGuardadas),
                        Snackbar.LENGTH_LONG).show();
                return;
            }

            String nombreJ1 = getNombreJugador1();
            String estadoModelo = juegoBantumi.serializa(nombreJ1);
            String cronoTexto = cronometro.getText().toString();
            long cronoMillis = parseMmSsToMillis(cronoTexto);
            long ts = System.currentTimeMillis();

            String title = "Partida guardada " + nextId;
            String filename = SAVE_FILE_PREFIX + nextId + SAVE_FILE_EXT;

            // 1) miniatura (devuelve ruta relativa "saves/thumbs/xxx.png")
            String thumbPath = crearMiniaturaPartida(nextId);

            // 2) JSON de la partida -> /files/saves/json/
            org.json.JSONObject saveData = new org.json.JSONObject();
            saveData.put("id", nextId);
            saveData.put("title", title);
            saveData.put("filename", filename);
            saveData.put("timestamp", ts);
            saveData.put("estado", estadoModelo);
            saveData.put("jugador1", nombreJ1);
            saveData.put("cronometro_texto", cronoTexto);
            saveData.put("cronometro_millis", cronoMillis);
            saveData.put("thumbnail", thumbPath);

            writeWholeFileIn(JSON_DIR, filename, saveData.toString());

            // 3) índice mínimo + thumb -> /files/saves/index.json
            org.json.JSONObject meta = new org.json.JSONObject();
            meta.put("id", nextId);
            meta.put("title", title);
            meta.put("filename", filename);
            meta.put("thumb", thumbPath);
            saves.put(meta);

            index.put("saves", saves);
            index.put("next_id", nextId + 1);
            saveSavesIndex(index);

            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.txtPartidaGuardadaOK),
                    Snackbar.LENGTH_LONG).show();

        } catch (Exception e) {
            Snackbar.make(findViewById(android.R.id.content),
                    getString(R.string.txtPartidaGuardadaERROR),
                    Snackbar.LENGTH_LONG).show();
        }
    }


    /** Lee o crea el índice de partidas guardadas */
    private org.json.JSONObject loadSavesIndex() {
        try {
            String json = readWholeFileIn();
            if (json != null && !json.isEmpty()) return new org.json.JSONObject(json);
        } catch (Exception ignore) {}
        org.json.JSONObject idx = new org.json.JSONObject();
        try {
            idx.put("next_id", 1);
            idx.put("saves", new org.json.JSONArray());
        } catch (org.json.JSONException ignore) {}
        return idx;
    }

    private void saveSavesIndex(org.json.JSONObject index) throws java.io.IOException {
        writeWholeFileIn(SAVES_DIR, INDEX_FILENAME, index.toString());
    }

    private File ensureDir(String relativePath) {
        File d = new File(getFilesDir(), relativePath);
        if (!d.exists()) d.mkdirs();
        return d;
    }

    private void writeWholeFileIn(String relativeDir, String filename, String content) throws java.io.IOException {
        File dir = ensureDir(relativeDir);
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(new File(dir, filename))) {
            fos.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private String readWholeFileIn() throws java.io.IOException {
        File f = new File(getFilesDir(), SAVES_DIR + "/" + INDEX_FILENAME);
        try (java.io.FileInputStream fis = new java.io.FileInputStream(f);
             java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = fis.read(buf)) != -1) bos.write(buf, 0, n);
            return new String(bos.toByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.FileNotFoundException e) {
            return null;
        }
    }
    /** "mm:ss" -> milisegundos */
    private long parseMmSsToMillis(@NonNull String text) {
        String[] p = text.split(":");
        long m = Long.parseLong(p[0]);
        long s = Long.parseLong(p[1]);
        return (m * 60 + s) * 1000L;
    }

    private String crearMiniaturaPartida(int id) throws Exception {
        View root = findViewById(R.id.main);
        if (root.getWidth() == 0 || root.getHeight() == 0) {
            int wSpec = View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST);
            int hSpec = View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST);
            root.measure(wSpec, hSpec);
            root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
        }

        android.graphics.Bitmap full = android.graphics.Bitmap.createBitmap(
                root.getWidth(), root.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(full);
        root.draw(canvas);

        int side = Math.min(full.getWidth(), full.getHeight());
        int x = (full.getWidth() - side) / 2;
        int y = (full.getHeight() - side) / 2;
        android.graphics.Bitmap square = android.graphics.Bitmap.createBitmap(full, x, y, side, side);

        android.graphics.Bitmap thumb = android.graphics.Bitmap.createScaledBitmap(square, THUMB_SIZE, THUMB_SIZE, true);

        File dir = ensureDir(THUMBS_DIR);
        String filename = THUMB_PREFIX + id + THUMB_EXT;
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(new File(dir, filename))) {
            thumb.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos);
        }

        full.recycle();
        square.recycle();
        return THUMBS_DIR + "/" + filename;
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
            case R.id.opcReiniciarPartida:
                new ReiniciarPartidaDialog().show(getSupportFragmentManager(),"DIALOG_REBOOT_GAME");
                return true;
            case R.id.opcGuardarPartida:
                new GuardarPartidaDialog().show(getSupportFragmentManager(), "DIALOG_SAVE_GAME");
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
        stopCronometro();
        String texto = (juegoBantumi.getSemillas(6) > 6 * numInicialSemillas)
                ? "Gana Jugador 1"
                : "Gana Jugador 2";
        if (juegoBantumi.getSemillas(6) == 6 * numInicialSemillas) {
            texto = "¡¡¡ EMPATE !!!";
        }

        // @TODO guardar puntuación

        // terminar
        new FinalAlertDialog(texto).show(getSupportFragmentManager(), "ALERT_DIALOG");
    }

    public Turno getTurnoInicial(){
        return this.turnoInicial;
    }

    private String getNombreJugador1() {
        TextView tvJugador1 = findViewById(R.id.tvPlayer1);
        return tvJugador1.getText().toString();
    }
}