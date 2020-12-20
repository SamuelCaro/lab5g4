package pe.pucp.tel306.firebox.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import pe.pucp.tel306.firebox.R;

public class ListaArchivosAdapter extends RecyclerView.Adapter<ListaArchivosAdapter.ViewHolder> {

    ArrayList<StorageReference> references;
    Context context;
    int umbral;

    public ListaArchivosAdapter(ArrayList<StorageReference> references, Context context, int umbral)
    {
        this.context = context;
        this.references= references;
        this.umbral = umbral;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.documento,parent,false);
        ListaArchivosAdapter.ViewHolder viewHolder = new ListaArchivosAdapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StorageReference reference = references.get(position);
        holder.nombreDocumento.setText(reference.getName());
        if (position<umbral)
        {
            holder.tipo = "Carpeta";
            holder.tipoDocumento.setImageResource(R.drawable.folder);
        }
        else
        {
            holder.tipo= "Documento";
            holder.tipoDocumento.setImageResource(R.drawable.file);
        }
    }

    @Override
    public int getItemCount() {
        return references.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView nombreDocumento;
        ImageView tipoDocumento;
        String tipo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreDocumento = itemView.findViewById(R.id.nombreDocumento);
            tipoDocumento= itemView.findViewById(R.id.tipoImagenDocumento);
        }
        //TODO El eliminar, editar y entrar a la carpeta
    }
}
