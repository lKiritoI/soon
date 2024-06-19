package com.example.soon;

import static com.example.soon.R.color.background_alex;
import static com.example.soon.R.color.background_amelie;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private final Context context;
    private final ArrayList<User> list;
    private final ArrayList<String> list_key;
    EditText popup_name, popup_betrag, popup_grund;
    TextView popup_date, popup_title;
    CardView popup_cardView;
    DatabaseReference databaseReference;
    LinearLayout popup_linlay;

    public MyAdapter(Context context, ArrayList<User> list, ArrayList<String> list_key) {
        this.context = context;
        this.list = list;
        this.list_key = list_key;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.userentry, parent, false);
        return new MyViewHolder(v);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        User user = list.get(position);
        Float c = 0.00f;

        if (user.getBetrag() != null) {
            c = Float.valueOf(user.getBetrag().replace(",", "."));
        }

        holder.name_title.setText("Name");
        holder.betrag_title.setText("Betrag");
        holder.grund_title.setText("Grund");

        holder.name_id.setText("\t" + user.getName());
        holder.betrag_id.setText("\t" + String.format("%.2f€", c).replace(".", ","));
        String Grund = user.getGrund();
        holder.grund_id.setText("\t" + Grund.substring(0, 1).toUpperCase() + Grund.substring(1).toLowerCase());
        holder.date_id.setText(user.getDate());

        if (Objects.equals(user.getName(), "Alex")) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, background_alex));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, background_amelie));
        }

        holder.count.setText("Eintrag: " + position);

        holder.itemView.setOnLongClickListener(v -> {
            showPopupMenu(v, holder.getAdapterPosition());
            return true;
        });

        holder.itemView.setOnLongClickListener(v -> {
            showPopupMenu(v, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void openEditPopup(int position, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialogTheme);

        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.popup_layout, null);
        View customTitleView = inflater.inflate(R.layout.popup_title, null);

        User user = list.get(position);
        String key = list_key.get(position);

        popup_title = customTitleView.findViewById(R.id.popup_title);
        popup_title.setText("Eintrag ändern?");
        popup_title.setGravity(Gravity.CENTER);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        popup_cardView = dialogView.findViewById(R.id.popup_cardView);
        popup_linlay = dialogView.findViewById(R.id.popup_lin_lay);

        popup_cardView.setCardBackgroundColor(Objects.equals(name, "Alex") ? ContextCompat.getColor(context, background_alex) : ContextCompat.getColor(context, background_amelie));

        popup_name = dialogView.findViewById(R.id.popup_name);
        popup_betrag = dialogView.findViewById(R.id.popup_betrag);
        popup_grund = dialogView.findViewById(R.id.popup_grund);
        popup_date = dialogView.findViewById(R.id.popup_date);

        popup_name.setText(user.getName());
        popup_betrag.setText(user.getBetrag());
        popup_grund.setText(user.getGrund());
        popup_date.setText(user.getDate());

        builder.setCustomTitle(customTitleView).setTitle("Eintrag ändern?").setPositiveButton("OK", null).setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());

        builder.setCancelable(false);

        builder.setView(dialogView);

        AlertDialog alertDialog = builder.create();

        alertDialog.show();

        Button okButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button cancelButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        okButton.setTextColor(Color.BLACK);
        cancelButton.setTextColor(Color.BLACK);

        okButton.setOnClickListener(v -> {
            user.setName(popup_name.getText().toString());
            user.setBetrag(popup_betrag.getText().toString());
            user.setGrund(popup_grund.getText().toString());

            databaseReference.child(key).setValue(user).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Eintrag geändert!", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                } else {
                    Toast.makeText(context, "Eintrag nicht geändert! (@Alex)", Toast.LENGTH_SHORT).show();
                    alertDialog.dismiss();
                }
            });
        });

        cancelButton.setOnClickListener(v -> {
            Toast.makeText(context, "Nichts geändert (Abgebrochen)!", Toast.LENGTH_SHORT).show();
            alertDialog.dismiss();
        });
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.options_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.edit) {
                openEditPopup(position, list.get(position).getName());
                return true;
            } else if (id == R.id.delete) {
                deleteEntry(position);
                return true;
            } else {
                return false;
            }
        });

        popup.show();
    }

    private void deleteEntry(int position) {
        if (position < 0 || position >= list.size()) {
            Toast.makeText(context, "Ungültige Position zum Löschen", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialogTheme);
        builder.setTitle("Eintrag löschen");
        builder.setMessage("Möchten Sie diesen Eintrag wirklich löschen?");
        builder.setPositiveButton("Löschen", (dialog, which) -> {
            String key = list_key.get(position);
            databaseReference = FirebaseDatabase.getInstance().getReference("users");
            databaseReference.child(key).removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Eintrag gelöscht!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Eintrag nicht gelöscht! (@Alex)", Toast.LENGTH_SHORT).show();
                }
            });
        });
        builder.setNegativeButton("Abbrechen", (dialog, which) -> dialog.dismiss());
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialog -> {
            Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

            positiveButton.setTextColor(Color.BLACK);
            negativeButton.setTextColor(Color.BLACK);
        });

        alertDialog.show();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView count, name_id, betrag_id, grund_id, date_id, name_title, betrag_title, grund_title;
        CardView cardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name_id = itemView.findViewById(R.id.textname);
            betrag_id = itemView.findViewById(R.id.textbetrag);
            grund_id = itemView.findViewById(R.id.textgrund);
            date_id = itemView.findViewById(R.id.textDate);
            cardView = itemView.findViewById(R.id.cardView);
            count = itemView.findViewById(R.id.tv_count);

            name_title = itemView.findViewById(R.id.name_title);
            betrag_title = itemView.findViewById(R.id.betrag_title);
            grund_title = itemView.findViewById(R.id.grund_title);
        }
    }

}
