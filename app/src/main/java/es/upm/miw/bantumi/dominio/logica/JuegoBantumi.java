package es.upm.miw.bantumi.dominio.logica;

import android.util.Log;
import es.upm.miw.bantumi.ui.viewmodel.BantumiViewModel;

public class JuegoBantumi {

    // Callback para notificar casilla seleccionada (J1 o J2)
    public interface OnPitSelectedListener { void onPitSelected(int pos); }
    private OnPitSelectedListener pitSelectedListener;
    public void setOnPitSelectedListener(OnPitSelectedListener listener) { this.pitSelectedListener = listener; }

    public static final int NUM_POSICIONES = 14;
    // 0-5: campo J1; 6: almacén J1; 7-12: campo J2; 13: almacén J2

    private final BantumiViewModel bantumiVM;

    public enum Turno { turnoJ1, turnoJ2, Turno_TERMINADO }

    private int numInicialSemillas;

    public JuegoBantumi(BantumiViewModel bantumiVM, Turno turno, int numInicialSemillas) {
        this.bantumiVM = bantumiVM;
        this.numInicialSemillas = numInicialSemillas;
        if (campoVacio(Turno.turnoJ1) && campoVacio(Turno.turnoJ2)) {
            inicializar(turno);
        }
    }

    public int getSemillas(int pos) {
        Integer v = bantumiVM.getNumSemillas(pos).getValue();
        return v == null ? 0 : v; // null-safety
    }

    public void setSemillas(int pos, int valor) {
        bantumiVM.setNumSemillas(pos, valor);
    }

    public void inicializar(Turno turno) {
        setTurno(turno);
        for (int i = 0; i < NUM_POSICIONES; i++)
            setSemillas(i, (i == 6 || i == 13) ? 0 : numInicialSemillas);
    }

    /** Recoge las semillas en pos y siembra */
    public void jugar(int pos) {
        if (pos < 0 || pos >= NUM_POSICIONES)
            throw new IndexOutOfBoundsException(String.format("Posición (%d) fuera de límites", pos));
        if (getSemillas(pos) == 0
                || (pos < 6 && turnoActual() != Turno.turnoJ1)
                || (pos > 6 && turnoActual() != Turno.turnoJ2))
            return;

        Log.i("MiW", String.format("jugar(%02d)", pos));

        // Notificar casilla elegida (permite a la UI resaltarla)
        if (pitSelectedListener != null) { pitSelectedListener.onPitSelected(pos); }

        // Recoger semillas
        int nSemillasHueco, numSemillas = getSemillas(pos);
        setSemillas(pos, 0);

        // Siembra
        int nextPos = pos;
        while (numSemillas > 0) {
            nextPos = (nextPos + 1) % NUM_POSICIONES;
            if (turnoActual() == Turno.turnoJ1 && nextPos == 13) nextPos = 0; // saltar almacén J2
            if (turnoActual() == Turno.turnoJ2 && nextPos == 6)  nextPos = 7; // saltar almacén J1
            nSemillasHueco = getSemillas(nextPos);
            setSemillas(nextPos, nSemillasHueco + 1);
            numSemillas--;
        }

        // Captura si termina en hueco propio vacío
        if (getSemillas(nextPos) == 1
                && ((turnoActual() == Turno.turnoJ1 && nextPos < 6)
                || (turnoActual() == Turno.turnoJ2 && nextPos > 6 && nextPos < 13))) {
            int posContrario = 12 - nextPos;
            int miAlmacen = (turnoActual() == Turno.turnoJ1) ? 6 : 13;
            setSemillas(miAlmacen, 1 + getSemillas(miAlmacen) + getSemillas(posContrario));
            setSemillas(nextPos, 0);
            setSemillas(posContrario, 0);
        }

        if (campoVacio(Turno.turnoJ1) || campoVacio(Turno.turnoJ2)) {
            recolectar(0);
            recolectar(7);
            setTurno(Turno.Turno_TERMINADO);
        }

        if (turnoActual() == Turno.turnoJ1 && nextPos != 6) setTurno(Turno.turnoJ2);
        else if (turnoActual() == Turno.turnoJ2 && nextPos != 13) setTurno(Turno.turnoJ1);

        Log.i("MiW", "\t turno = " + turnoActual());
    }

    public boolean juegoTerminado() { return (turnoActual() == Turno.Turno_TERMINADO); }

    private boolean campoVacio(Turno turno) {
        boolean vacio = true;
        int inicioCampo = (turno == Turno.turnoJ1) ? 0 : 7;
        for (int i = inicioCampo; i < inicioCampo + 6; i++)
            vacio &= (getSemillas(i) == 0);
        return vacio;
    }

    private void recolectar(int pos) {
        int semillasAlmacen = getSemillas(pos + 6);
        for (int i = pos; i < pos + 6; i++) {
            semillasAlmacen += getSemillas(i);
            setSemillas(i, 0);
        }
        setSemillas(pos + 6, semillasAlmacen);
        Log.i("MiW", "\tRecolectar - " + pos);
    }

    public Turno turnoActual() { return bantumiVM.getTurno().getValue(); }

    public void setTurno(Turno turno) { bantumiVM.setTurno(turno); }

    /** IA jugador 2: elige aleatorio válido; jugar() emite el callback */
    public void juegaComputador() {
        int pos = 7 + (int) (Math.random() * 6); // [7..12]
        if (this.getSemillas(pos) != 0 && (pos < NUM_POSICIONES - 1)) {
            this.jugar(pos);
        }
        if (this.turnoActual() == Turno.turnoJ2 && !juegoTerminado()) {
            juegaComputadorDelay();
        }
    }

    public void juegaComputadorDelay(){
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(
                this::juegaComputador, 500);
    }

    public String serializa(String nombreJ1) {
        StringBuilder sb = new StringBuilder();
        sb.append(nombreJ1 == null ? "" : nombreJ1.replace(";", ","))
                .append(';').append(turnoActual().name()).append(';');
        for (int i = 0; i < NUM_POSICIONES; i++) {
            if (i > 0) sb.append(',');
            sb.append(getSemillas(i));
        }
        return sb.toString();
    }

    public void deserializa(String juegoSerializado) {
        if (juegoSerializado == null || juegoSerializado.isEmpty()) {
            Log.w("MiW", "deserializa(): cadena nula o vacía, se ignora");
            return;
        }
        try {
            String[] parts = juegoSerializado.split(";");
            if (parts.length != 3) {
                Log.e("MiW", "deserializa(): formato inválido, partes=" + parts.length);
                return;
            }
            Turno turno = Turno.valueOf(parts[1].trim());

            String[] semillas = parts[2].split(",");
            if (semillas.length != NUM_POSICIONES) {
                Log.e("MiW", "deserializa(): nº posiciones inválido=" + semillas.length);
                return;
            }
            for (int i = 0; i < NUM_POSICIONES; i++) {
                int v = Integer.parseInt(semillas[i].trim());
                if (v < 0) v = 0;
                setSemillas(i, v);
            }
            setTurno(turno);
            Log.i("MiW", "deserializa(): estado restaurado OK, turno=" + turno);

        } catch (Exception e) {
            Log.e("MiW", "deserializa(): error restaurando estado: " + e.getMessage(), e);
        }
    }

    public int getNumInicialSemillas() { return numInicialSemillas; }
    public void setNumInicialSemillas(int numInicialSemillas){ this.numInicialSemillas = numInicialSemillas; }
}
