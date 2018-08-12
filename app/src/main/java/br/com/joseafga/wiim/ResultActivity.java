/*
 * Copyright (C) 2018 José Almeida <jose.afga@gmail.com>
 *
 * https://creativecommons.org/licenses/by/4.0/
 */
package br.com.joseafga.wiim;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import br.com.joseafga.wiim.helpers.ResultAdapter;
import br.com.joseafga.wiim.models.Tag;

public class ResultActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Tag> tagsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // set process title
        getSupportActionBar().setTitle("Process X");

        Tag tag1 = new Tag();
        tag1.setDescription("Tag 1 Description");
        tag1.setComment("Tag 1 Comment");

        Tag tag2 = new Tag();
        tag2.setDescription("Taguinha 2 Description");
        tag2.setComment("Taguinha 2 Comment");

        Tag tag3 = new Tag();
        tag3.setDescription("Tag 1 Description");
        tag3.setComment("Tag 1 Comment");

        Tag tag4 = new Tag();
        tag4.setDescription("Tag 1 Description");
        tag4.setComment("Tag 1 Comment");

        Tag tag5 = new Tag();
        tag5.setDescription("Tag 1 Description");
        tag5.setComment("Tag 1 Comment");

        tagsList = new ArrayList<>();
        tagsList.add(tag1);
        tagsList.add(tag2);
        tagsList.add(tag3);
        tagsList.add(tag4);
        tagsList.add(tag5);
        tagsList.add(tag1);
        tagsList.add(tag2);
        tagsList.add(tag3);
        tagsList.add(tag4);
        tagsList.add(tag5);

        // set recycler view layout manager
        recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        ResultAdapter adapter = new ResultAdapter(tagsList);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.ac_settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);

                break;
            // ?more options
        }

        return super.onOptionsItemSelected(item);
    }
}
