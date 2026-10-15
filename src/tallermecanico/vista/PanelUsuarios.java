package tallermecanico.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
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
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import tallermecanico.controlador.ControlEmpleados;
import tallermecanico.controlador.ControlUsuarios;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.modelo.Empleado;
import tallermecanico.modelo.Usuario;
import tallermecanico.util.Estilos;
import tallermecanico.util.Formato;

public class PanelUsuarios extends JPanel implements ActionListener, Refrescable {

    private ControlUsuarios control;
    private ControlEmpleados controlEmpleados;
    private ArrayList<Usuario> listaActual;
    private Usuario seleccionado;

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JPasswordField txtConfirmacion;
    private JComboBox<Empleado> cboEmpleado;

    private JComboBox<String> cboCriterio;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JButton btnMostrarTodos;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnModificar;
    private JButton btnContrasena;
    private JButton btnEstado;
    private JButton btnEliminar;
    private JButton btnPermisos;

    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelUsuarios() {
        control = new ControlUsuarios();
        controlEmpleados = new ControlEmpleados();
        listaActual = new ArrayList<>();
        iniciarComponentes();
        refrescar();
    }

    private JPasswordField campoContrasena() {
        JPasswordField campo = new JPasswordField();
        campo.setFont(Estilos.NORMAL);
        campo.setBackground(Estilos.PANEL_CLARO);
        campo.setForeground(Estilos.TEXTO);
        campo.setCaretColor(Estilos.NARANJA);
        campo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return campo;
    }

    private void iniciarComponentes() {
        setLayout(new BorderLayout(0, 12));
        setBackground(Estilos.FONDO);

        JPanel formulario = new JPanel(new GridLayout(0, 4, 12, 8));
        formulario.setBackground(Estilos.PANEL);
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        txtUsuario = Estilos.campo();
        txtContrasena = campoContrasena();
        txtConfirmacion = campoContrasena();
        cboEmpleado = new JComboBox<>();
        Estilos.combo(cboEmpleado);
        formulario.add(Estilos.etiqueta("Nombre de usuario"));
        formulario.add(txtUsuario);
        formulario.add(Estilos.etiqueta("Empleado asociado"));
        formulario.add(cboEmpleado);
        formulario.add(Estilos.etiqueta("Contrasena (minimo 6)"));
        formulario.add(txtContrasena);
        formulario.add(Estilos.etiqueta("Confirmar contrasena"));
        formulario.add(txtConfirmacion);

        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        busqueda.setBackground(Estilos.FONDO);
        cboCriterio = new JComboBox<>(new String[]{"Todos", "Usuario", "Empleado", "Puesto"});
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

        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.setBackground(Estilos.FONDO);
        arriba.add(formulario, BorderLayout.CENTER);
        arriba.add(busqueda, BorderLayout.SOUTH);

        String[] columnas = {"ID", "Usuario", "Empleado", "Puesto", "Estado", "Ultimo acceso", "Contrasena (SHA-256)"};
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

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        botones.setBackground(Estilos.FONDO);
        btnNuevo = Estilos.boton("Nuevo", Estilos.PANEL_CLARO);
        btnGuardar = Estilos.boton("Guardar", Estilos.VERDE);
        btnModificar = Estilos.boton("Modificar", Estilos.NARANJA);
        btnContrasena = Estilos.boton("Cambiar contrasena", Estilos.AMARILLO);
        btnEstado = Estilos.boton("Activar/Inactivar", Estilos.PANEL_CLARO);
        btnEliminar = Estilos.boton("Eliminar", Estilos.ROJO);
        btnPermisos = Estilos.boton("Permisos por rol", Estilos.NARANJA);
        JButton[] lista = {btnPermisos, btnNuevo, btnGuardar, btnModificar, btnContrasena, btnEstado, btnEliminar};
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
                control.agregar(txtUsuario.getText(), new String(txtContrasena.getPassword()),
                        new String(txtConfirmacion.getPassword()), (Empleado) cboEmpleado.getSelectedItem());
                Mensajes.exito(this, "El usuario se registro correctamente.\n"
                        + "La contrasena se guardo encriptada con SHA-256.", "Usuario guardado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnModificar) {
                requiereSeleccion();
                control.modificar(seleccionado, txtUsuario.getText(), (Empleado) cboEmpleado.getSelectedItem());
                Mensajes.exito(this, "El usuario se actualizo correctamente.", "Usuario modificado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnContrasena) {
                requiereSeleccion();
                control.cambiarContrasena(seleccionado, new String(txtContrasena.getPassword()),
                        new String(txtConfirmacion.getPassword()));
                Mensajes.exito(this, "La contrasena se actualizo correctamente.", "Contrasena cambiada");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEstado) {
                requiereSeleccion();
                control.cambiarEstado(seleccionado);
                Mensajes.exito(this, "El usuario quedo " + (seleccionado.isEstado() ? "activo." : "inactivo."),
                        "Estado actualizado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEliminar) {
                requiereSeleccion();
                if (Mensajes.confirmar(this, "Esta seguro que desea eliminar el usuario " + seleccionado.getUsername() + "?",
                        "Confirmar eliminacion")) {
                    control.eliminar(seleccionado);
                    Mensajes.exito(this, "El usuario se elimino.", "Usuario eliminado");
                    limpiar();
                    refrescar();
                }
            } else if (e.getSource() == btnPermisos) {
                DlgPermisos dialogo = new DlgPermisos((Frame) SwingUtilities.getWindowAncestor(this));
                dialogo.setVisible(true);
            } else if (e.getSource() == btnBuscar || e.getSource() == txtBuscar) {
                cargarTabla(control.buscar(cboCriterio.getSelectedItem().toString(), txtBuscar.getText()));
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

    private void requiereSeleccion() throws DatoInvalidoException {
        if (seleccionado == null) {
            throw new DatoInvalidoException("Debe seleccionar un usuario de la tabla.");
        }
    }

    @Override
    public final void refrescar() {
        try {
            Object actual = cboEmpleado.getSelectedItem();
            cboEmpleado.removeAllItems();
            ArrayList<Empleado> empleados = controlEmpleados.listar();
            for (int i = 0; i < empleados.size(); i++) {
                if (empleados.get(i).isEstado()) {
                    cboEmpleado.addItem(empleados.get(i));
                }
            }
            if (actual != null) {
                cboEmpleado.setSelectedItem(actual);
            }
            cargarTabla(control.listar());
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void cargarTabla(ArrayList<Usuario> lista) {
        listaActual = lista;
        modelo.setRowCount(0);
        for (int i = 0; i < lista.size(); i++) {
            Usuario u = lista.get(i);
            String acceso = u.getUltimoAcceso() == null ? "Nunca" : Formato.fecha(u.getUltimoAcceso().toLocalDate())
                    + " " + Formato.hora(u.getUltimoAcceso().toLocalTime());
            modelo.addRow(new Object[]{u.getId(), u.getUsername(), u.getEmpleado().obtenerNombreCompleto(),
                u.getPuesto(), u.isEstado() ? "Activo" : "Inactivo", acceso, u.getContrasena().substring(0, 20) + "..."});
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaActual.size()) {
            return;
        }
        seleccionado = listaActual.get(fila);
        txtUsuario.setText(seleccionado.getUsername());
        cboEmpleado.setSelectedItem(seleccionado.getEmpleado());
        txtContrasena.setText("");
        txtConfirmacion.setText("");
    }

    private void limpiar() {
        seleccionado = null;
        txtUsuario.setText("");
        txtContrasena.setText("");
        txtConfirmacion.setText("");
        tabla.clearSelection();
        txtUsuario.requestFocus();
    }
}
