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
import tallermecanico.controlador.ControlClientes;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.modelo.Cliente;
import tallermecanico.util.Estilos;

public class PanelClientes extends JPanel implements ActionListener, Refrescable {

    private ControlClientes control;
    private ArrayList<Cliente> listaActual;
    private Cliente seleccionado;

    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtDpi;
    private JTextField txtTelefono;
    private JTextField txtDireccion;
    private JTextField txtCorreo;
    private JTextField txtNit;

    private JComboBox<String> cboCriterio;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JButton btnMostrarTodos;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnModificar;
    private JButton btnEstado;
    private JButton btnEliminar;

    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelClientes() {
        control = new ControlClientes();
        listaActual = new ArrayList<>();
        iniciarComponentes();
        refrescar();
    }

    private void iniciarComponentes() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Estilos.FONDO);

        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.setBackground(Estilos.FONDO);
        arriba.add(construirFormulario(), BorderLayout.CENTER);
        arriba.add(construirBusqueda(), BorderLayout.SOUTH);

        add(arriba, BorderLayout.NORTH);
        add(construirTabla(), BorderLayout.CENTER);
        add(construirBotones(), BorderLayout.SOUTH);
    }

    private JPanel construirFormulario() {
        JPanel formulario = new JPanel(new GridLayout(0, 4, 12, 8));
        formulario.setBackground(Estilos.PANEL);
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        txtNombre = Estilos.campo();
        txtApellido = Estilos.campo();
        txtDpi = Estilos.campo();
        txtTelefono = Estilos.campo();
        txtDireccion = Estilos.campo();
        txtCorreo = Estilos.campo();
        txtNit = Estilos.campo();

        formulario.add(Estilos.etiqueta("Nombre"));
        formulario.add(txtNombre);
        formulario.add(Estilos.etiqueta("Apellido"));
        formulario.add(txtApellido);
        formulario.add(Estilos.etiqueta("DPI (13 digitos)"));
        formulario.add(txtDpi);
        formulario.add(Estilos.etiqueta("Telefono (8 digitos)"));
        formulario.add(txtTelefono);
        formulario.add(Estilos.etiqueta("Direccion"));
        formulario.add(txtDireccion);
        formulario.add(Estilos.etiqueta("Correo electronico"));
        formulario.add(txtCorreo);
        formulario.add(Estilos.etiqueta("NIT"));
        formulario.add(txtNit);
        formulario.add(Estilos.etiqueta(""));
        formulario.add(Estilos.etiqueta(""));
        return formulario;
    }

    private JPanel construirBusqueda() {
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        busqueda.setBackground(Estilos.FONDO);

        String[] criterios = {"Todos", "Nombre", "DPI", "NIT", "Telefono", "Correo", "Direccion"};
        cboCriterio = new JComboBox<>(criterios);
        Estilos.combo(cboCriterio);
        txtBuscar = Estilos.campo();
        txtBuscar.setPreferredSize(new java.awt.Dimension(260, 30));
        btnBuscar = Estilos.boton("Buscar", Estilos.NARANJA);
        btnMostrarTodos = Estilos.boton("Mostrar todos", Estilos.PANEL_CLARO);
        btnBuscar.addActionListener(this);
        btnMostrarTodos.addActionListener(this);
        txtBuscar.addActionListener(this);

        busqueda.add(Estilos.etiqueta("Buscar por:"));
        busqueda.add(cboCriterio);
        busqueda.add(txtBuscar);
        busqueda.add(btnBuscar);
        busqueda.add(btnMostrarTodos);
        return busqueda;
    }

    private JScrollPane construirTabla() {
        String[] columnas = {"Nombre completo", "DPI", "NIT", "Telefono", "Correo", "Direccion", "Vehiculos", "Estado"};
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
        return scroll;
    }

    private JPanel construirBotones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        botones.setBackground(Estilos.FONDO);

        btnNuevo = Estilos.boton("Nuevo", Estilos.PANEL_CLARO);
        btnGuardar = Estilos.boton("Guardar", Estilos.VERDE);
        btnModificar = Estilos.boton("Modificar", Estilos.NARANJA);
        btnEstado = Estilos.boton("Activar/Inactivar", Estilos.AMARILLO);
        btnEliminar = Estilos.boton("Eliminar", Estilos.ROJO);

        btnNuevo.addActionListener(this);
        btnGuardar.addActionListener(this);
        btnModificar.addActionListener(this);
        btnEstado.addActionListener(this);
        btnEliminar.addActionListener(this);

        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnModificar);
        botones.add(btnEstado);
        botones.add(btnEliminar);
        return botones;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == btnGuardar) {
                control.agregar(txtNombre.getText(), txtApellido.getText(), txtDpi.getText(), txtTelefono.getText(),
                        txtDireccion.getText(), txtCorreo.getText(), txtNit.getText());
                Mensajes.exito(this, "El cliente se registro correctamente en la base de datos.", "Registro guardado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnModificar) {
                requiereSeleccion("modificarlo");
                control.modificar(seleccionado, txtNombre.getText(), txtApellido.getText(), txtDpi.getText(),
                        txtTelefono.getText(), txtDireccion.getText(), txtCorreo.getText(), txtNit.getText());
                Mensajes.exito(this, "Los datos del cliente se actualizaron correctamente.", "Registro modificado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEstado) {
                requiereSeleccion("activarlo o inactivarlo");
                control.cambiarEstado(seleccionado);
                Mensajes.exito(this, "El cliente quedo " + (seleccionado.isEstado() ? "activo." : "inactivo."),
                        "Estado actualizado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEliminar) {
                requiereSeleccion("eliminarlo");
                if (Mensajes.confirmar(this, "Esta seguro que desea eliminar al cliente "
                        + seleccionado.obtenerNombreCompleto() + "?", "Confirmar eliminacion")) {
                    control.eliminar(seleccionado);
                    Mensajes.exito(this, "El cliente se elimino de la base de datos.", "Registro eliminado");
                    limpiar();
                    refrescar();
                }
            } else if (e.getSource() == btnBuscar || e.getSource() == txtBuscar) {
                cargarTabla(control.buscar(cboCriterio.getSelectedItem().toString(), txtBuscar.getText()));
                if (listaActual.isEmpty()) {
                    Mensajes.exito(this, "No se encontraron clientes que coincidan con la busqueda.", "Sin resultados");
                }
            } else if (e.getSource() == btnMostrarTodos) {
                txtBuscar.setText("");
                cboCriterio.setSelectedIndex(0);
                refrescar();
            } else if (e.getSource() == btnNuevo) {
                limpiar();
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void requiereSeleccion(String accion) throws DatoInvalidoException {
        if (seleccionado == null) {
            throw new DatoInvalidoException("Debe seleccionar un cliente de la tabla para " + accion + ".");
        }
    }

    @Override
    public final void refrescar() {
        try {
            cargarTabla(control.listar());
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void cargarTabla(ArrayList<Cliente> lista) {
        listaActual = lista;
        modelo.setRowCount(0);
        for (int i = 0; i < lista.size(); i++) {
            Cliente c = lista.get(i);
            Object[] fila = new Object[8];
            fila[0] = c.obtenerNombreCompleto();
            fila[1] = c.getDpi();
            fila[2] = c.getNit();
            fila[3] = c.getTelefono();
            fila[4] = c.getCorreo();
            fila[5] = c.getDireccion();
            fila[6] = c.getTotalVehiculos();
            fila[7] = c.isEstado() ? "Activo" : "Inactivo";
            modelo.addRow(fila);
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaActual.size()) {
            return;
        }
        seleccionado = listaActual.get(fila);
        txtNombre.setText(seleccionado.getNombre());
        txtApellido.setText(seleccionado.getApellido());
        txtDpi.setText(seleccionado.getDpi());
        txtTelefono.setText(seleccionado.getTelefono());
        txtDireccion.setText(seleccionado.getDireccion());
        txtCorreo.setText(seleccionado.getCorreo());
        txtNit.setText(seleccionado.getNit());
    }

    private void limpiar() {
        seleccionado = null;
        txtNombre.setText("");
        txtApellido.setText("");
        txtDpi.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        txtCorreo.setText("");
        txtNit.setText("");
        tabla.clearSelection();
        txtNombre.requestFocus();
    }
}
