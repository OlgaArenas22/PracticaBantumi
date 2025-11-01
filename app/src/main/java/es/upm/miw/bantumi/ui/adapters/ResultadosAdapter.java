package es.upm.miw.bantumi.ui.adapters;

import android.content.res.Resources;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.upm.miw.bantumi.R;
import es.upm.miw.bantumi.data.database.entities.ResultEntity;

public class ResultadosAdapter extends RecyclerView.Adapter<ResultadosAdapter.VH> {

    private final List<ResultEntity> data = new ArrayList<>();

    public void submit(List<ResultEntity> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_result_row, parent, false);
        return new VH(v);
    }

    public void setTrophy(VH h, @DrawableRes int trophy){
        h.posIcon.setImageResource(trophy);
        h.posIconShadow.setImageResource(trophy);
        h.posText.setText("");
        h.posIcon.setVisibility(View.VISIBLE);
        h.posIconShadow.setVisibility(View.VISIBLE);
    }
    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ResultEntity e = data.get(position);
        Resources r = h.itemView.getResources();

        if (position == 0) {
            setTrophy(h, R.drawable.ic_first_trophy);
        } else if (position == 1) {
            setTrophy(h, R.drawable.ic_second_trophy);
        } else if (position == 2) {
            setTrophy(h, R.drawable.ic_third_trophy);
        } else {
            h.posIcon.setVisibility(View.GONE);
            h.posIconShadow.setVisibility(View.GONE);
            h.posText.setText(String.valueOf(position + 1));
        }

        h.playerName.setText(e.player1Name);
        h.crownIcon.setImageResource(e.player1Won ? R.drawable.ic_crown : R.drawable.ic_crown_broken);

        h.seedsP1.setText(String.valueOf(e.seedsPlayer1));
        h.seedsP2.setText(String.valueOf(e.seedsPlayer2));

        h.modeIcon.setImageResource(iconForMode(e.mode));

        h.timeText.setText(formatElapsed(e.elapsedMillis));

        h.dateText.setText(formatDate(e.finishedAtUtc));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView posIcon, posIconShadow, crownIcon, modeIcon;
        TextView posText, playerName, seedsP1, seedsP2, timeText, dateText;

        VH(@NonNull View v) {
            super(v);
            posIcon   = v.findViewById(R.id.row_pos_icon);
            posIconShadow = v.findViewById(R.id.row_pos_icon_shadow);
            posText   = v.findViewById(R.id.row_pos_text);
            playerName= v.findViewById(R.id.row_player_name);
            crownIcon = v.findViewById(R.id.row_crown_icon);
            seedsP1   = v.findViewById(R.id.row_seeds_p1);
            seedsP2   = v.findViewById(R.id.row_seeds_p2);
            modeIcon  = v.findViewById(R.id.row_mode_icon);
            timeText  = v.findViewById(R.id.row_time_text);
            dateText  = v.findViewById(R.id.row_date_text);
        }
    }

    @DrawableRes
    private int iconForMode(String mode) {
        switch (mode) {
            case "Rápido": return R.drawable.modorapido;
            case "Fiebre de la semilla": return R.drawable.fiebredelasemilla;
            default:  return R.drawable.modoclasico;
        }
    }

    private String formatElapsed(long millis) {
        if (millis < 0) millis = 0;
        long totalSeconds = millis / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private String formatDate(long utcMillis) {
        Date d = new Date(utcMillis);
        // dd/MM/yyyy HH:mm
        return DateFormat.format("dd/MM/yyyy HH:mm", d).toString();
    }
}
