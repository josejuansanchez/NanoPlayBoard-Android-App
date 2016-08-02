package org.josejuansanchez.nanoplayboard.adapters;

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

    public interface OnItemClickListener {
        void onItemClick(Project item);
    }

    private List<Project> projects;
    private final OnItemClickListener listener;

    public RecyclerViewAdapter(List<Project> projects, OnItemClickListener listener) {
        this.projects = projects;
        this.listener = listener;
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
        holder.bind(projects.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private TextView projectTitle;
        private TextView projectDescription;
        private ImageView projectImage;

        public ProjectViewHolder(View itemView) {
            super(itemView);
            projectTitle = (TextView) itemView.findViewById(R.id.project_title);
            projectDescription = (TextView) itemView.findViewById(R.id.project_description);
            projectImage = (ImageView) itemView.findViewById(R.id.project_image);
        }

        public void bind(final Project item, final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }
}
