package org.josejuansanchez.nanoplayboard.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.adapters.RecyclerViewAdapter;
import org.josejuansanchez.nanoplayboard.models.Project;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewActivity extends AppCompatActivity {

    private List<Project> mProjects;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setHasFixedSize(true);
        initializeProjects();
        initializeAdapter();
    }

    private void initializeProjects() {
        mProjects = new ArrayList<>();
        mProjects.add(new Project("Project 1", "Description 1", R.drawable._potentiometer));
        mProjects.add(new Project("Project 2", "Description 2", R.drawable._buzzer));
        mProjects.add(new Project("Project 3", "Description 3", R.drawable._ldr));
        mProjects.add(new Project("Project 4", "Description 4", R.drawable._ledmatrix));
        mProjects.add(new Project("Project 5", "Description 5", R.drawable._ledrgb));
        mProjects.add(new Project("Project 6", "Description 6", R.drawable._potentiometer));
        mProjects.add(new Project("Project 7", "Description 7", R.drawable._buzzer));
        mProjects.add(new Project("Project 8", "Description 8", R.drawable._ldr));
        mProjects.add(new Project("Project 9", "Description 9", R.drawable._ledmatrix));
        mProjects.add(new Project("Project 10", "Description 10", R.drawable._ledrgb));
    }

    private void initializeAdapter() {
        RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(mProjects);
        mRecyclerView.setAdapter(rvAdapter);
    }
}