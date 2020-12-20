package pe.pucp.tel306.firebox.Adapters;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        holder.context = context;
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

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener
    {
        TextView nombreDocumento;
        ImageView tipoDocumento;
        String tipo;
        Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreDocumento = itemView.findViewById(R.id.nombreDocumento);
            tipoDocumento= itemView.findViewById(R.id.tipoImagenDocumento);
            itemView.setOnCreateContextMenuListener(this);
        }

        //Esto representa el menu
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem renombrar = menu.add(Menu.NONE,1 , 1, "Renombrar");
            MenuItem descargar = menu.add(Menu.NONE, 2, 2, "Descargar");
            MenuItem eliminar = menu.add(Menu.NONE, 3, 3, "Eliminar");
            renombrar.setOnMenuItemClickListener(onEditMenu);
            descargar.setOnMenuItemClickListener(onEditMenu);
            eliminar.setOnMenuItemClickListener(onEditMenu);
        }
        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1:
                        Toast.makeText(context, "Renombrando",Toast.LENGTH_SHORT).show();
                        return true;
                    case 2:
                        Toast.makeText(context, "Descargando",Toast.LENGTH_SHORT).show();
                        return true;
                    case 3:
                        Toast.makeText(context, "Eliminando",Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        return false;
                }
            }
        };
    }
}
