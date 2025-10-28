package es.upm.miw.bantumi.ui.managers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

public class MiniaturaManager {
    public MiniaturaManager() {}

    /**
     * Crea una miniatura cuadrada de 72x72 con recorte centrado en el tablero,
     * escalada y con bordes redondeados.
     */
    public Bitmap crearMiniaturaCuadrada(View root, int thumbSize) {
        if (root == null) return null;

        // Asegurar que la vista está medida y maquetada
        if (root.getWidth() == 0 || root.getHeight() == 0) {
            int specW = View.MeasureSpec.makeMeasureSpec(3000, View.MeasureSpec.AT_MOST);
            int specH = View.MeasureSpec.makeMeasureSpec(3000, View.MeasureSpec.AT_MOST);
            root.measure(specW, specH);
            root.layout(0, 0, root.getMeasuredWidth(), root.getMeasuredHeight());
        }

        // 1️⃣ Dibujamos todo el contenido actual
        Bitmap full = Bitmap.createBitmap(root.getWidth(), root.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(full);
        root.draw(canvas);

        // 2️⃣ Definimos una zona central donde está el tablero
        int cropTop = (int) (full.getHeight() * 0.13f);
        int cropBottom = (int) (full.getHeight() * 0.42f);
        int cropHeight = cropBottom - cropTop;
        int cropWidth = full.getWidth();
        int side = Math.min(cropWidth, cropHeight);

        int x = (cropWidth - side) / 2;
        int y = cropTop + (cropHeight - side) / 2;

        Bitmap cropped = Bitmap.createBitmap(full, x, y, side, side);

        // 3️⃣ Escalamos a 72x72
        Bitmap scaled = Bitmap.createScaledBitmap(cropped, thumbSize, thumbSize, true);

        // 4️⃣ Bordes redondeados (radio = 12 px por ejemplo)
        Bitmap rounded = Bitmap.createBitmap(thumbSize, thumbSize, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(rounded);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Path path = new Path();
        float radius = 12f;
        path.addRoundRect(new RectF(0, 0, thumbSize, thumbSize), radius, radius, Path.Direction.CW);
        c.clipPath(path);
        c.drawBitmap(scaled, 0, 0, paint);

        // Liberamos bitmaps intermedios
        full.recycle();
        cropped.recycle();
        scaled.recycle();

        return rounded;
    }
}
