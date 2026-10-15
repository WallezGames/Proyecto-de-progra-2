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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import tallermecanico.controlador.ControlEmpleados;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.modelo.Empleado;
import tallermecanico.modelo.Mecanico;
import tallermecanico.util.Estilos;
import tallermecanico.util.Formato;

public class PanelEmpleados extends JPanel implements ActionListener, Refrescable {

    private ControlEmpleados control;
    private ArrayList<Empleado> listaActual;
    private Empleado seleccionado;

    private JComboBox<String> cboPuesto;
    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtDpi;
    private JTextField txtTelefono;
    private JTextField txtDireccion;
    private JTextField txtCorreo;
    private JTextField txtDato;
    private JLabel lblDato;

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

    public PanelEmpleados() {
        control = new ControlEmpleados();
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

        cboPuesto = new JComboBox<>(ControlEmpleados.PUESTOS);
        Estilos.combo(cboPuesto);
        cboPuesto.addActionListener(this);
        txtCodigo = Estilos.campo();
        txtNombre = Estilos.campo();
        txtApellido = Estilos.campo();
        txtDpi = Estilos.campo();
        txtTelefono = Estilos.campo();
        txtDireccion = Estilos.campo();
        txtCorreo = Estilos.campo();
        txtDato = Estilos.campo();
        lblDato = Estilos.etiqueta("Especialidad");

        formulario.add(Estilos.etiqueta("Puesto"));
        formulario.add(cboPuesto);
        formulario.add(Estilos.etiqueta("Codigo de empleado"));
        formulario.add(txtCodigo);
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
        formulario.add(lblDato);
        formulario.add(txtDato);
        formulario.add(Estilos.etiqueta(""));
        formulario.add(Estilos.etiqueta(""));
        return formulario;
    }

    private JPanel construirBusqueda() {
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        busqueda.setBackground(Estilos.FONDO);

        String[] criterios = {"Todos", "Nombre", "Codigo", "DPI", "Puesto", "Taller"};
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
        String[] columnas = {"Codigo", "Nombre completo", "Puesto", "Dato del puesto", "DPI", "Telefono",
            "Taller", "Horas", "Estado"};
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
        if (e.getSource() == cboPuesto) {
            lblDato.setText(control.datoSegunPuesto(cboPuesto.getSelectedItem().toString()));
            return;
        }
        try {
            if (e.getSource() == btnGuardar) {
                control.agregar(cboPuesto.getSelectedItem().toString(), txtNombre.getText(), txtApellido.getText(),
                        txtDpi.getText(), txtTelefono.getText(), txtDireccion.getText(), txtCorreo.getText(),
                        txtCodigo.getText(), txtDato.getText());
                Mensajes.exito(this, "El empleado se registro correctamente en la base de datos.", "Registro guardado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnModificar) {
                requiereSeleccion("modificarlo");
                control.modificar(seleccionado, txtNombre.getText(), txtApellido.getText(), txtDpi.getText(),
                        txtTelefono.getText(), txtDireccion.getText(), txtCorreo.getText(),
                        txtCodigo.getText(), txtDato.getText());
                Mensajes.exito(this, "Los datos del empleado se actualizaron correctamente.", "Registro modificado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEstado) {
                requiereSeleccion("activarlo o inactivarlo");
                control.cambiarEstado(seleccionado);
                Mensajes.exito(this, "El empleado quedo " + (seleccionado.isEstado() ? "activo." : "inactivo."),
                        "Estado actualizado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEliminar) {
                requiereSeleccion("eliminarlo");
                if (Mensajes.confirmar(this, "Esta seguro que desea eliminar al empleado "
                        + seleccionado.obtenerNombreCompleto() + "?", "Confirmar eliminacion")) {
                    control.eliminar(seleccionado);
                    Mensajes.exito(this, "El empleado se elimino de la base de datos.", "Registro eliminado");
                    limpiar();
                    refrescar();
                }
            } else if (e.getSource() == btnBuscar || e.getSource() == txtBuscar) {
                cargarTabla(control.buscar(cboCriterio.getSelectedItem().toString(), txtBuscar.getText()));
                if (listaActual.isEmpty()) {
                    Mensajes.exito(this, "No se encontraron empleados que coincidan con la busqueda.", "Sin resultados");
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
            throw new DatoInvalidoException("Debe seleccionar un empleado de la tabla para " + accion + ".");
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

    private void cargarTabla(ArrayList<Empleado> lista) {
        listaActual = lista;
        modelo.setRowCount(0);
        for (int i = 0; i < lista.size(); i++) {
            Empleado e = lista.get(i);
            String taller = "No aplica";
            if (e instanceof Mecanico) {
                Mecanico m = (Mecanico) e;
                taller = m.getTaller() == null ? "Sin asignar" : m.getTaller().getNombreTaller();
            }
            Object[] fila = new Object[9];
            fila[0] = e.getCodigoEmpleado();
            fila[1] = e.obtenerNombreCompleto();
            fila[2] = e.getPuesto();
            fila[3] = e.getDatoPuesto();
            fila[4] = e.getDpi();
            fila[5] = e.getTelefono();
            fila[6] = taller;
            fila[7] = Formato.decimal(e.horasTrabajadas());
            fila[8] = e.isEstado() ? "Activo" : "Inactivo";
            modelo.addRow(fila);
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaActual.size()) {
            return;
        }
        seleccionado = listaActual.get(fila);
        cboPuesto.setSelectedItem(seleccionado.getPuesto());
        txtCodigo.setText(seleccionado.getCodigoEmpleado());
        txtNombre.setText(seleccionado.getNombre());
        txtApellido.setText(seleccionado.getApellido());
        txtDpi.setText(seleccionado.getDpi());
        txtTelefono.setText(seleccionado.getTelefono());
        txtDireccion.setText(seleccionado.getDireccion());
        txtCorreo.setText(seleccionado.getCorreo());
        txtDato.setText(seleccionado.getDatoPuesto());
        cboPuesto.setEnabled(false);
    }

    private void limpiar() {
        seleccionado = null;
        txtCodigo.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtDpi.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        txtCorreo.setText("");
        txtDato.setText("");
        cboPuesto.setEnabled(true);
        cboPuesto.setSelectedIndex(0);
        tabla.clearSelection();
        txtCodigo.requestFocus();
    }
}
