package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.entities.Proyecto;

public interface ProyectoRepository extends  Repository<Proyecto,String> {
    void validarDatosProyecto(Proyecto entity);
}
