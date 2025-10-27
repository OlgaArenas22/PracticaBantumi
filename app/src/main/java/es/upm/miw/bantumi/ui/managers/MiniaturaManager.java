package es.upm.miw.bantumi.ui.managers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
public class MiniaturaManager {

    public MiniaturaManager() {}

    /**
     * Crea una miniatura cuadrada y escalada a partir de una vista.
     *
     * @param root Vista raíz que se quiere capturar (por ejemplo findViewById(R.id.main))
     * @param thumbSize tamaño del lado del thumbnail (ej: 256)
     * @return Bitmap cuadrado (listo para guardar o mostrar)
     */
    public Bitmap crearMiniaturaCuadrada(View root, int thumbSize) {
        if (root == null) return null;

        // Si aún no se ha dibujado, medimos y maquetamos
        if (root.getWidth() == 0 || root.getHeight() == 0) {
            int specW = View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST);
            int specH = View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST);
            root.measure(specW, specH);
            root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
        }

        // 1) Bitmap del contenido completo
        Bitmap full = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(full);
        root.draw(canvas);

        // 2) Recorte central a cuadrado
        int side = Math.min(full.getWidth(), full.getHeight());
        int x = (full.getWidth() - side) / 2;
        int y = (full.getHeight() - side) / 2;
        Bitmap square = Bitmap.createBitmap(full, x, y, side, side);

        // 3) Escalar al tamaño final deseado
        Bitmap thumb = Bitmap.createScaledBitmap(square, thumbSize, thumbSize, true);

        // Liberamos bitmaps intermedios
        full.recycle();
        square.recycle();

        return thumb;
    }
}
