package grupo.proyecto_aula_carpethome.repositories;

import grupo.proyecto_aula_carpethome.entities.Gasto;
import grupo.proyecto_aula_carpethome.entities.Medida;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MedidaRepositoryImpl implements MedidaRepository{

    private Medida mapResultSetToMedida(ResultSet rs) throws SQLException {
        return Medida.builder()
                .idMedida(rs.getString("id_medida"))
                .nombreMedida(rs.getString("nombre_medida"))
                .tipoMedida(rs.getString("tipo_medida"))
                .cBusto(rs.getDouble("c_busto"))
                .cCintura(rs.getDouble("c_cintura"))
                .cCadera(rs.getDouble("c_cadera"))
                .alturaBusto(rs.getDouble("altura_busto"))
                .separacionBusto(rs.getDouble("separacion_busto"))
                .radioBusto(rs.getDouble("radio_busto"))
                .bajoBusto(rs.getDouble("bajo_busto"))
                .largoFalda(rs.getDouble("largo_falda"))
                .largoCadera(rs.getDouble("largo_cadera"))
                .largoVestido(rs.getDouble("largo_vestido"))
                .largoPantalon(rs.getDouble("largo_pantalon"))
                .largoManga(rs.getDouble("largo_manga"))
                .build();
    }



    @Override
    public Medida save(Medida entity) throws SQLException {
        return null;
    }

    @Override
    public Optional<Medida> findById(String s) throws SQLException {
        return Optional.empty();
    }

    @Override
    public List<Medida> findAll() throws SQLException {
        return List.of();
    }

    @Override
    public void update(Medida entity) throws SQLException {

    }

    @Override
    public void delete(String s) throws SQLException {

    }
}
