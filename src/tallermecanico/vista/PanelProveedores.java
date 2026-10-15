package tallermecanico.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import tallermecanico.controlador.ControlCatalogos;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.modelo.Proveedor;
import tallermecanico.util.Estilos;

public class PanelProveedores extends JPanel implements ActionListener, Refrescable {

    private ControlCatalogos control;
    private ArrayList<Proveedor> listaActual;
    private Proveedor seleccionado;

    private JTextField txtNombre;
    private JTextField txtNit;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtDireccion;

    private JComboBox<String> cboCriterio;
    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnModificar;
    private JButton btnEstado;
    private JButton btnEliminar;

    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelProveedores() {
        control = new ControlCatalogos();
        listaActual = new ArrayList<>();
        iniciarComponentes();
        refrescar();
    }

    private void iniciarComponentes() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Estilos.FONDO);
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JPanel formulario = new JPanel(new GridLayout(0, 4, 12, 8));
        formulario.setBackground(Estilos.PANEL);
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        txtNombre = Estilos.campo();
        txtNit = Estilos.campo();
        txtTelefono = Estilos.campo();
        txtCorreo = Estilos.campo();
        txtDireccion = Estilos.campo();
        formulario.add(Estilos.etiqueta("Nombre del proveedor"));
        formulario.add(txtNombre);
        formulario.add(Estilos.etiqueta("NIT"));
        formulario.add(txtNit);
        formulario.add(Estilos.etiqueta("Telefono (8 digitos)"));
        formulario.add(txtTelefono);
        formulario.add(Estilos.etiqueta("Correo (opcional)"));
        formulario.add(txtCorreo);
        formulario.add(Estilos.etiqueta("Direccion"));
        formulario.add(txtDireccion);
        formulario.add(Estilos.etiqueta(""));
        formulario.add(Estilos.etiqueta(""));

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        busqueda.setBackground(Estilos.FONDO);
        cboCriterio = new JComboBox<>(new String[]{"Todos", "Nombre", "NIT", "Telefono"});
        Estilos.combo(cboCriterio);
        txtBuscar = Estilos.campo();
        txtBuscar.setPreferredSize(new java.awt.Dimension(260, 30));
        btnBuscar = Estilos.boton("Buscar", Estilos.NARANJA);
        btnBuscar.addActionListener(this);
        txtBuscar.addActionListener(this);
        busqueda.add(Estilos.etiqueta("Buscar por:"));
        busqueda.add(cboCriterio);
        busqueda.add(txtBuscar);
        busqueda.add(btnBuscar);

        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.setBackground(Estilos.FONDO);
        arriba.add(formulario, BorderLayout.CENTER);
        arriba.add(busqueda, BorderLayout.SOUTH);

        String[] columnas = {"Proveedor", "NIT", "Telefono", "Correo", "Direccion", "Productos", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        Estilos.tabla(tabla);
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarFila();
            }
        });
        JScrollPane scroll = new JScrollPane(tabla);
        Estilos.scroll(scroll);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        botones.setBackground(Estilos.FONDO);
        btnNuevo = Estilos.boton("Nuevo", Estilos.PANEL_CLARO);
        btnGuardar = Estilos.boton("Guardar", Estilos.VERDE);
        btnModificar = Estilos.boton("Modificar", Estilos.NARANJA);
        btnEstado = Estilos.boton("Activar/Inactivar", Estilos.AMARILLO);
        btnEliminar = Estilos.boton("Eliminar", Estilos.ROJO);
        JButton[] lista = {btnNuevo, btnGuardar, btnModificar, btnEstado, btnEliminar};
        for (int i = 0; i < lista.length; i++) {
            lista[i].addActionListener(this);
            botones.add(lista[i]);
        }

        add(arriba, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == btnGuardar) {
                control.agregarProveedor(txtNombre.getText(), txtNit.getText(), txtTelefono.getText(),
                        txtCorreo.getText(), txtDireccion.getText());
                Mensajes.exito(this, "El proveedor se registro correctamente.", "Registro guardado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnModificar) {
                requiereSeleccion();
                control.modificarProveedor(seleccionado, txtNombre.getText(), txtNit.getText(), txtTelefono.getText(),
                        txtCorreo.getText(), txtDireccion.getText());
                Mensajes.exito(this, "El proveedor se actualizo correctamente.", "Registro modificado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEstado) {
                requiereSeleccion();
                control.cambiarEstadoProveedor(seleccionado);
                Mensajes.exito(this, "El proveedor quedo " + (seleccionado.isEstado() ? "activo." : "inactivo."),
                        "Estado actualizado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEliminar) {
                requiereSeleccion();
                if (Mensajes.confirmar(this, "Desea eliminar al proveedor " + seleccionado.getNombre() + "?",
                        "Confirmar eliminacion")) {
                    control.eliminarProveedor(seleccionado);
                    Mensajes.exito(this, "El proveedor se elimino.", "Registro eliminado");
                    limpiar();
                    refrescar();
                }
            } else if (e.getSource() == btnBuscar || e.getSource() == txtBuscar) {
                cargarTabla(control.buscarProveedores(cboCriterio.getSelectedItem().toString(), txtBuscar.getText()));
            } else if (e.getSource() == btnNuevo) {
                limpiar();
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void requiereSeleccion() throws DatoInvalidoException {
        if (seleccionado == null) {
            throw new DatoInvalidoException("Debe seleccionar un proveedor de la tabla.");
        }
    }

    @Override
    public final void refrescar() {
        try {
            cargarTabla(control.listarProveedores());
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void cargarTabla(ArrayList<Proveedor> lista) {
        listaActual = lista;
        modelo.setRowCount(0);
        for (int i = 0; i < lista.size(); i++) {
            Proveedor p = lista.get(i);
            modelo.addRow(new Object[]{p.getNombre(), p.getNit(), p.getTelefono(), p.getCorreo(), p.getDireccion(),
                p.getTotalProductos(), p.isEstado() ? "Activo" : "Inactivo"});
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaActual.size()) {
            return;
        }
        seleccionado = listaActual.get(fila);
        txtNombre.setText(seleccionado.getNombre());
        txtNit.setText(seleccionado.getNit());
        txtTelefono.setText(seleccionado.getTelefono());
        txtCorreo.setText(seleccionado.getCorreo() == null ? "" : seleccionado.getCorreo());
        txtDireccion.setText(seleccionado.getDireccion() == null ? "" : seleccionado.getDireccion());
    }

    private void limpiar() {
        seleccionado = null;
        txtNombre.setText("");
        txtNit.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
        tabla.clearSelection();
    }
}
