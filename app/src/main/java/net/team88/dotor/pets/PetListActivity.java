package net.team88.dotor.pets;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import net.team88.dotor.R;

public class PetListActivity extends AppCompatActivity {
    private final int REQUEST_ADD_PET = 1;

    private RecyclerView recyclerViewPetList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PetListActivity.this, PetEditActivity.class);
                startActivityForResult(intent, REQUEST_ADD_PET);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerViewPetList = (RecyclerView)findViewById(R.id.recyclerViewPetList);
        recyclerViewPetList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();
        PetsRecyclerViewAdapter adapter = new PetsRecyclerViewAdapter(this);
        recyclerViewPetList.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_PET) {
            if (resultCode == RESULT_OK) {

            } else if (resultCode == RESULT_CANCELED) {

            }
        }
    }
}
