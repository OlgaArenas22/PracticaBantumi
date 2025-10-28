package es.upm.miw.bantumi.ui.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.upm.miw.bantumi.R;

public class CargarPartidaAdapter extends RecyclerView.Adapter<CargarPartidaAdapter.VH> {

    public interface OnSaveClick { void onSaveClicked(String filename); }
    public interface OnDeleteClick { void onDeleteClicked(String filename); }

    public static class Item {
        public final String title;
        public final Bitmap thumb;
        public final String filename;

        public Item(String title, Bitmap thumb, String filename) {
            this.title = title; this.thumb = thumb; this.filename = filename;
        }
        public static Item placeholder() { return new Item("", null, null); }
        public boolean isPlaceholder() { return filename == null; }
    }

    private final List<Item> data;
    private final OnSaveClick listener;
    private OnDeleteClick deleteListener;

    public CargarPartidaAdapter(List<Item> data, OnSaveClick listener) {
        this.data = data; this.listener = listener;
    }

    public void setOnDeleteClick(OnDeleteClick deleteListener) {
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_save_slot, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Item it = data.get(position);

        if (it.isPlaceholder()) {
            h.title.setText("Vacío");
            h.btnThumb.setImageResource(R.drawable.empty_slot);
            h.btnThumb.setEnabled(false);
            h.btnThumb.setOnClickListener(null);
            h.btnDelete.setVisibility(View.INVISIBLE); //
            h.btnDelete.setOnClickListener(null);
        } else {
            h.title.setText(it.title);
            if (it.thumb != null) {
                h.btnThumb.setImageBitmap(it.thumb);
            } else {
                h.btnThumb.setImageResource(R.drawable.empty_slot);
            }
            h.btnThumb.setEnabled(true);
            h.btnThumb.setOnClickListener(v -> {
                if (listener != null) listener.onSaveClicked(it.filename);
            });
            h.btnDelete.setVisibility(View.VISIBLE);
            h.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDeleteClicked(it.filename);
            });
        }
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageButton btnThumb;
        TextView title;
        ImageButton btnDelete;
        ImageView hiddenImg;

        VH(@NonNull View v) {
            super(v);
            btnThumb = v.findViewById(R.id.btn_thumb);
            btnDelete = v.findViewById(R.id.btn_delete);
            title = v.findViewById(R.id.tv_title);
            hiddenImg = v.findViewById(R.id.img_thumb);
        }
    }
}
