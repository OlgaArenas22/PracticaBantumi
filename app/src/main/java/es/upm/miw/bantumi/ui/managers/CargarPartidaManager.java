package es.upm.miw.bantumi.ui.managers;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CargarPartidaManager {
    private static final String SAVES_DIR  = "saves";
    private static final String JSON_DIR   = "saves/json";
    private static final String INDEX_FILE = "index.json";
    public static final int MAX_SAVES = 10;

    private final Context appContext;

    public CargarPartidaManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static class SaveMeta {
        public final int id;
        public final String title;
        public final String filename;
        public final String thumbRelativePath;
        public SaveMeta(int id, String title, String filename, String thumbRelativePath) {
            this.id = id; this.title = title; this.filename = filename; this.thumbRelativePath = thumbRelativePath;
        }
    }

    /** Lista los metadatos del índice. */
    public List<SaveMeta> listSaves() {
        List<SaveMeta> out = new ArrayList<>();
        try {
            JSONObject idx = loadIndex();
            JSONArray arr = idx.optJSONArray("saves");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.optJSONObject(i);
                    if (o == null) continue;
                    int id = o.optInt("id");
                    String title = o.optString("title", "Partida " + id);
                    String filename = o.optString("filename", "");
                    String thumb = o.optString("thumb", "");
                    out.add(new SaveMeta(id, title, filename, thumb));
                }
            }
            out.sort(Comparator.comparingInt(m -> m.id));
        } catch (Exception ignore) {}
        return out;
    }

    /** Lee el JSON completo de la partida. */
    public JSONObject readSave(String filename) {
        try {
            String json = readWholeFileIn(JSON_DIR, filename);
            return (json != null && !json.isEmpty()) ? new JSONObject(json) : null;
        } catch (Exception e) {
            return null;
        }
    }


    //region Borrar partida
    public boolean deleteSave(String filename) {
        try {
            JSONObject idx = loadIndex();
            JSONArray saves = idx.optJSONArray("saves");
            if (saves == null) return false;

            JSONArray newSaves = new JSONArray();
            String thumbRel = null;

            for (int i = 0; i < saves.length(); i++) {
                JSONObject o = saves.optJSONObject(i);
                if (o == null) continue;
                String fn = o.optString("filename", "");
                if (filename.equals(fn)) {
                    thumbRel = o.optString("thumb", null); // p.ej: "saves/thumbs/thumb_3.png" o "thumbs/thumb_3.png"
                    // no lo añadimos -> eliminado
                } else {
                    newSaves.put(o);
                }
            }

            // Mantener next_id tal cual
            idx.put("saves", newSaves);
            writeIndex(idx);

            // Borrar JSON de la partida
            deleteFileIn(JSON_DIR, filename);

            // Borrar miniatura (thumbRelativePath se guarda respecto a filesDir; usamos tal cual)
            if (thumbRel != null && !thumbRel.isEmpty()) {
                File thumbFile = new File(appContext.getFilesDir(), thumbRel);
                if (thumbFile.exists()) thumbFile.delete();
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void writeIndex(JSONObject idx) throws Exception {
        writeFileIn(SAVES_DIR, INDEX_FILE, idx.toString());
    }

    private void writeFileIn(String relativeDir, String filename, String contents) throws Exception {
        File dir = new File(appContext.getFilesDir(), relativeDir);
        if (!dir.exists()) dir.mkdirs();
        File f = new File(dir, filename);
        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(f)) {
            fos.write(contents.getBytes(StandardCharsets.UTF_8));
            fos.flush();
        }
    }

    private void deleteFileIn(String relativeDir, String filename) {
        File f = new File(appContext.getFilesDir(), relativeDir + "/" + filename);
        if (f.exists()) f.delete();
    }
    //endregion
    private JSONObject loadIndex() {
        try {
            String json = readWholeFileIn(SAVES_DIR, INDEX_FILE);
            if (json != null && !json.isEmpty()) return new JSONObject(json);
        } catch (Exception ignore) {}
        JSONObject idx = new JSONObject();
        try {
            idx.put("next_id", 1);
            idx.put("saves", new JSONArray());
        } catch (Exception ignore) {}
        return idx;
    }

    private String readWholeFileIn(String relativeDir, String filename) throws Exception {
        File f = new File(appContext.getFilesDir(), relativeDir + "/" + filename);
        try (FileInputStream fis = new FileInputStream(f);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096]; int n;
            while ((n = fis.read(buf)) != -1) bos.write(buf, 0, n);
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        } catch (java.io.FileNotFoundException e) {
            return null;
        }
    }
}
