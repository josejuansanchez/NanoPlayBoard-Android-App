package org.josejuansanchez.nanoplayboard.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.josejuansanchez.nanoplayboard.R;
import org.josejuansanchez.nanoplayboard.models.Project;

import java.util.List;

/**
 * Created by josejuansanchez on 2/8/16.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ProjectViewHolder> {

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView projectTitle;
        TextView projectDescription;
        ImageView projectImage;

        public ProjectViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.cardview);
            projectTitle = (TextView) itemView.findViewById(R.id.project_title);
            projectDescription = (TextView) itemView.findViewById(R.id.project_description);
            projectImage = (ImageView) itemView.findViewById(R.id.project_image);
        }
    }

    List<Project> projects;

    public RecyclerViewAdapter(List<Project> projects) {
        this.projects = projects;
    }

    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, parent, false);
        ProjectViewHolder pvh = new ProjectViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(ProjectViewHolder holder, int position) {
        holder.projectTitle.setText(projects.get(position).getTitle());
        holder.projectDescription.setText(projects.get(position).getDescription());
        holder.projectImage.setImageResource(projects.get(position).getImageId());
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

}
