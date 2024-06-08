package com.example.soon;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private float __GLOBAL_BETRAG_ALEX = 0.00f;
    private float __GLOBAL_BETRAG_AMELIE = 0.00f;
    private List<Float> floatListAlex = new ArrayList<>();
    private List<Float> floatListAmelie = new ArrayList<>();
    ArrayList<User> list = new ArrayList<>();
    ArrayList<String> list_key = new ArrayList<>();
    MyAdapter adapter;
    String leer = "";
    DatabaseReference databaseReference;
    float betAl = 0.00f;
    float betAm = 0.00f;
    RadioButton rb_alex, rb_amelie;
    EditText betrag, grund;
    TextView result, result_alex, result_amelie;
    Button insert, view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        System.out.println("Start");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        resetFields();
        loadData();

        rb_alex = findViewById(R.id.rb_alex);
        rb_amelie = findViewById(R.id.rb_amelie);

        betrag = findViewById(R.id.et_betrag);
        grund = findViewById(R.id.et_grund);

        insert = findViewById(R.id.btnInsert);
        view = findViewById(R.id.btnView);

        insert.setOnClickListener(v -> {
            insertData();
            resetFields();
        });

        view.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, Userlist.class));
            finish();
        });
    }

    private void resetFields() {
        rb_alex = findViewById(R.id.rb_alex);
        rb_amelie = findViewById(R.id.rb_amelie);

        betrag = findViewById(R.id.et_betrag);
        grund = findViewById(R.id.et_grund);

        insert = findViewById(R.id.btnInsert);
        view = findViewById(R.id.btnView);

        rb_alex.setChecked(true);
        rb_amelie.setChecked(false);

        betrag.setText(leer);
        grund.setText(leer);

        betrag.setHint("Wie viel €?");
        grund.setHint("Wofür?");
    }

    private void loadData() {
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        adapter = new MyAdapter(this, list, list_key);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    String parent = dataSnapshot.getKey();

                    list_key.add(parent);
                    list.add(user);
                }
                getBetraege();
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error: " + error);
            }
        });
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    private void getBetraege() {
        __GLOBAL_BETRAG_ALEX = 0.00f;
        __GLOBAL_BETRAG_AMELIE = 0.00f;

        result = findViewById(R.id.result);
        result_alex = findViewById(R.id.tv_betrag_alex);
        result_amelie = findViewById(R.id.tv_betrag_amelie);

        readData(floating -> {
            betAl = __GLOBAL_BETRAG_ALEX;
            betAm = __GLOBAL_BETRAG_AMELIE;

            String res_alex = String.format("Alex Ausgaben:\n%.2f€", betAl).replace(".", ",");
            String res_amelie = String.format("Amelies Ausgaben:\n%.2f€", betAm).replace(".", ",");

            result_alex.setText(res_alex);
            result_amelie.setText(res_amelie);

            String resultAlex = String.format("Alex ist %.2f€ hinten", Math.abs(betAl - betAm)).replace(".", ",");
            String resultAmelie = String.format("Amelie ist %.2f€ hinten", Math.abs(betAm - betAl)).replace(".", ",");

            if (betAl == betAm) {
                result.setText("Gleichstand! Let's Go!");
            } else if (betAl < betAm) {
                result.setText(resultAlex + ".");
            } else {
                result.setText(resultAmelie + ".");
            }
        });
    }

    private void readData(FirebaseCallback firebaseCallback) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                floatListAmelie.clear();
                floatListAlex.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String parent = dataSnapshot.getKey();
                    list_key.add(parent);

                    User user = dataSnapshot.getValue(User.class);
                    list.add(user);

                    if (Objects.equals(user.getName(), "Alex")) {
                        Float sumBetragAlex = Float.valueOf((String) user.getBetrag().replace(",", "."));
                        floatListAlex.add(sumBetragAlex);
                    }

                    if (Objects.equals(user.getName(), "Amelie")) {
                        Float sumBetragAmelie = Float.valueOf((String) user.getBetrag().replace(",", "."));
                        floatListAmelie.add(sumBetragAmelie);
                    }
                }
                for (int i = 0; i < floatListAlex.size(); i++) {
                    __GLOBAL_BETRAG_ALEX += floatListAlex.get(i);
                }

                for (int i = 0; i < floatListAmelie.size(); i++) {
                    __GLOBAL_BETRAG_AMELIE += floatListAmelie.get(i);
                }
                firebaseCallback.onCallback(__GLOBAL_BETRAG_ALEX);
                firebaseCallback.onCallback(__GLOBAL_BETRAG_AMELIE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Error: " + error);
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private void insertData() {
        String nameX = rb_alex.isChecked() ? "Alex" : "Amelie";
        String betragX = betrag.getText().toString().replace(".", ",");
        String grundX = grund.getText().toString();
        String id = databaseReference.push().getKey();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String dateX = df.format(new Date());

        User user = new User(nameX, betragX, grundX, dateX);

        if (betragX.isEmpty()) {
            Toast.makeText(MainActivity.this, "Betrag Leer", Toast.LENGTH_SHORT).show();
            return;
        } else if (grundX.isEmpty()) {
            Toast.makeText(MainActivity.this, "Grund leer", Toast.LENGTH_SHORT).show();
            return;
        }

        assert id != null;
        databaseReference.child(id).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(MainActivity.this, "Klappt", Toast.LENGTH_SHORT).show();
                loadData();
            }
        });
    }

    private interface FirebaseCallback {
        void onCallback(float floating);
    }
}