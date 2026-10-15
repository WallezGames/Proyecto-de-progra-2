package tallermecanico.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import tallermecanico.excepciones.BaseDatosException;
import tallermecanico.modelo.Categoria;

public class CategoriaDAO extends BaseDAO implements CrudDAO<Categoria> {

    private static final String CONSULTA = "SELECT c.id_categoria, c.nombre, c.descripcion, "
            + "(SELECT COUNT(*) FROM producto p WHERE p.id_categoria = c.id_categoria) AS total_productos "
            + "FROM categoria c ";

    @Override
    public int insertar(Categoria categoria) throws BaseDatosException {
        String sql = "INSERT INTO categoria (nombre, descripcion) VALUES (?, ?)";
        try (PreparedStatement ps = prepararConLlave(sql)) {
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.executeUpdate();
            int id = idGenerado(ps);
            categoria.setIdCategoria(id);
            return id;
        } catch (SQLException e) {
            throw error("registrar la categoria", e);
        }
    }

    @Override
    public void actualizar(Categoria categoria) throws BaseDatosException {
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id_categoria = ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, categoria.getNombre());
            ps.setString(2, categoria.getDescripcion());
            ps.setInt(3, categoria.getIdCategoria());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("modificar la categoria", e);
        }
    }

    @Override
    public void eliminar(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar("DELETE FROM categoria WHERE id_categoria = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw error("eliminar la categoria", e);
        }
    }

    @Override
    public Categoria buscarPorId(int id) throws BaseDatosException {
        try (PreparedStatement ps = preparar(CONSULTA + "WHERE c.id_categoria = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        } catch (SQLException e) {
            throw error("consultar la categoria", e);
        }
        return null;
    }

    @Override
    public ArrayList<Categoria> listar() throws BaseDatosException {
        ArrayList<Categoria> lista = new ArrayList<>();
        try (PreparedStatement ps = preparar(CONSULTA + "ORDER BY c.nombre");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw error("listar las categorias", e);
        }
        return lista;
    }

    public ArrayList<Categoria> buscar(String texto) throws BaseDatosException {
        ArrayList<Categoria> lista = new ArrayList<>();
        String sql = CONSULTA + "WHERE c.nombre LIKE ? OR c.descripcion LIKE ? ORDER BY c.nombre";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, patron(texto));
            ps.setString(2, patron(texto));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        } catch (SQLException e) {
            throw error("buscar categorias", e);
        }
        return lista;
    }

    public boolean existeNombre(String nombre, int excluirId) throws BaseDatosException {
        String sql = "SELECT COUNT(*) FROM categoria WHERE nombre = ? AND id_categoria <> ?";
        try (PreparedStatement ps = preparar(sql)) {
            ps.setString(1, nombre.trim());
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw error("verificar la categoria", e);
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setIdCategoria(rs.getInt("id_categoria"));
        c.setNombre(rs.getString("nombre"));
        c.setDescripcion(rs.getString("descripcion"));
        c.setTotalProductos(rs.getInt("total_productos"));
        return c;
    }
}
