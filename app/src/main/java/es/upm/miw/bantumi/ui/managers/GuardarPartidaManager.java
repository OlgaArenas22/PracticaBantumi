package es.upm.miw.bantumi.ui.managers;

import android.content.Context;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
public class GuardarPartidaManager {

    // ======= Configuración general =======
    public static final int MAX_SAVES   = 10;
    public static final int THUMB_SIZE  = 200;
    private static final String SAVES_DIR   = "saves";
    private static final String JSON_DIR    = "saves/json";
    private static final String THUMBS_DIR  = "saves/thumbs";
    private static final String INDEX_FILE  = "index.json";
    private static final String SAVE_FILE_PREFIX = "bantumi_save_";
    private static final String SAVE_FILE_EXT    = ".json";
    private static final String THUMB_PREFIX     = "bantumi_thumb_";
    private static final String THUMB_EXT        = ".png";

    private final Context appContext;
    public GuardarPartidaManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    // ======= Clase auxiliar para devolver resultado =======
    public static class SaveResult {
        public final boolean ok;
        public final String titleOrMessage;
        public SaveResult(boolean ok, String msg) {
            this.ok = ok;
            this.titleOrMessage = msg;
        }
    }
    public SaveResult guardarNuevaPartida(
            String estadoSerializado,
            String nombreJ1,
            String cronoTexto,
            long cronoMillis,
            Bitmap thumbnail
    ) {
        try {
            JSONObject index = loadIndex();
            int nextId = index.optInt("next_id", 1);
            JSONArray saves = index.optJSONArray("saves");
            if (saves == null) saves = new JSONArray();

            if (saves.length() >= MAX_SAVES) {
                return new SaveResult(false, "Has alcanzado el máximo de partidas guardadas.");
            }

            long ts = System.currentTimeMillis();
            String title = "Partida guardada " + nextId;
            String jsonFilename  = SAVE_FILE_PREFIX + nextId + SAVE_FILE_EXT;
            String thumbFilename = THUMB_PREFIX + nextId + THUMB_EXT;

            // 1) Guardar miniatura
            String thumbRelPath = saveThumbnail(thumbnail, thumbFilename);

            // 2) Guardar JSON de la partida
            JSONObject saveData = new JSONObject();
            saveData.put("id", nextId);
            saveData.put("title", title);
            saveData.put("filename", jsonFilename);
            saveData.put("timestamp", ts);
            saveData.put("estado", estadoSerializado);
            saveData.put("jugador1", nombreJ1);
            saveData.put("cronometro_texto", cronoTexto);
            saveData.put("cronometro_millis", cronoMillis);
            saveData.put("thumbnail", thumbRelPath);

            writeWholeFileIn(JSON_DIR, jsonFilename, saveData.toString());

            // 3) Actualizar índice
            JSONObject meta = new JSONObject();
            meta.put("id", nextId);
            meta.put("title", title);
            meta.put("filename", jsonFilename);
            meta.put("thumb", thumbRelPath);
            saves.put(meta);

            index.put("saves", saves);
            index.put("next_id", nextId + 1);
            saveIndex(index);

            return new SaveResult(true, title);

        } catch (Exception e) {
            return new SaveResult(false, "No se pudo guardar la partida.");
        } finally {
            if (thumbnail != null && !thumbnail.isRecycled()) {
                thumbnail.recycle();
            }
        }
    }

    private JSONObject loadIndex() {
        try {
            String json = readWholeFileIn();
            if (json != null && !json.isEmpty()) return new JSONObject(json);
        } catch (Exception ignore) {}
        JSONObject idx = new JSONObject();
        try {
            idx.put("next_id", 1);
            idx.put("saves", new JSONArray());
        } catch (Exception ignore) {}
        return idx;
    }

    private void saveIndex(JSONObject index) throws Exception {
        writeWholeFileIn(SAVES_DIR, INDEX_FILE, index.toString());
    }

    private String saveThumbnail(Bitmap bmp, String filename) throws Exception {
        ensureDir(THUMBS_DIR);
        File out = new File(appContext.getFilesDir(), THUMBS_DIR + "/" + filename);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }
        return THUMBS_DIR + "/" + filename;
    }

    private File ensureDir(String relativePath) {
        File d = new File(appContext.getFilesDir(), relativePath);
        if (!d.exists()) d.mkdirs();
        return d;
    }

    private void writeWholeFileIn(String relativeDir, String filename, String content) throws Exception {
        File dir = ensureDir(relativeDir);
        try (FileOutputStream fos = new FileOutputStream(new File(dir, filename))) {
            fos.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    private String readWholeFileIn() throws Exception {
        File f = new File(appContext.getFilesDir(), SAVES_DIR + "/" + INDEX_FILE);
        try (FileInputStream fis = new FileInputStream(f);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = fis.read(buf)) != -1) bos.write(buf, 0, n);
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        } catch (java.io.FileNotFoundException e) {
            return null;
        }
    }
}
