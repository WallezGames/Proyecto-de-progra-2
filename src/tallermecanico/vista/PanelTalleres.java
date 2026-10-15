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
import tallermecanico.controlador.ControlEmpleados;
import tallermecanico.controlador.ControlTalleres;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.modelo.JefeTaller;
import tallermecanico.modelo.Mecanico;
import tallermecanico.modelo.Taller;
import tallermecanico.util.Estilos;

public class PanelTalleres extends JPanel implements ActionListener, Refrescable {

    private ControlTalleres control;
    private ControlEmpleados controlEmpleados;
    private ArrayList<Taller> listaActual;
    private Taller seleccionado;

    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JComboBox<JefeTaller> cboJefe;
    private JComboBox<Mecanico> cboMecanico;

    private JComboBox<String> cboCriterio;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JButton btnMostrarTodos;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnModificar;
    private JButton btnAsignar;
    private JButton btnQuitar;
    private JButton btnEstado;
    private JButton btnEliminar;

    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelTalleres() {
        control = new ControlTalleres();
        controlEmpleados = new ControlEmpleados();
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
        txtDescripcion = Estilos.campo();
        cboJefe = new JComboBox<>();
        cboMecanico = new JComboBox<>();
        Estilos.combo(cboJefe);
        Estilos.combo(cboMecanico);

        formulario.add(Estilos.etiqueta("Nombre del taller"));
        formulario.add(txtNombre);
        formulario.add(Estilos.etiqueta("Jefe a cargo"));
        formulario.add(cboJefe);
        formulario.add(Estilos.etiqueta("Descripcion"));
        formulario.add(txtDescripcion);
        formulario.add(Estilos.etiqueta("Mecanico a asignar o quitar"));
        formulario.add(cboMecanico);
        return formulario;
    }

    private JPanel construirBusqueda() {
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        busqueda.setBackground(Estilos.FONDO);

        String[] criterios = {"Todos", "Nombre", "Jefe", "Descripcion"};
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
        String[] columnas = {"ID", "Taller", "Descripcion", "Jefe a cargo", "Mecanicos asignados", "Cantidad",
            "Ordenes activas", "Estado"};
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
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        botones.setBackground(Estilos.FONDO);

        btnNuevo = Estilos.boton("Nuevo", Estilos.PANEL_CLARO);
        btnGuardar = Estilos.boton("Guardar", Estilos.VERDE);
        btnModificar = Estilos.boton("Modificar", Estilos.NARANJA);
        btnAsignar = Estilos.boton("Asignar mecanico", Estilos.NARANJA);
        btnQuitar = Estilos.boton("Quitar mecanico", Estilos.PANEL_CLARO);
        btnEstado = Estilos.boton("Activar/Inactivar", Estilos.AMARILLO);
        btnEliminar = Estilos.boton("Eliminar", Estilos.ROJO);

        btnNuevo.addActionListener(this);
        btnGuardar.addActionListener(this);
        btnModificar.addActionListener(this);
        btnAsignar.addActionListener(this);
        btnQuitar.addActionListener(this);
        btnEstado.addActionListener(this);
        btnEliminar.addActionListener(this);

        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnModificar);
        botones.add(btnAsignar);
        botones.add(btnQuitar);
        botones.add(btnEstado);
        botones.add(btnEliminar);
        return botones;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == btnGuardar) {
                control.agregar(txtNombre.getText(), txtDescripcion.getText(), (JefeTaller) cboJefe.getSelectedItem());
                Mensajes.exito(this, "El taller se registro correctamente en la base de datos.", "Registro guardado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnModificar) {
                requiereSeleccion();
                control.modificar(seleccionado, txtNombre.getText(), txtDescripcion.getText(),
                        (JefeTaller) cboJefe.getSelectedItem());
                Mensajes.exito(this, "Los datos del taller se actualizaron correctamente.", "Registro modificado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnAsignar) {
                requiereSeleccion();
                Mecanico mecanico = (Mecanico) cboMecanico.getSelectedItem();
                control.asignarMecanico(seleccionado, mecanico);
                Mensajes.exito(this, "El mecanico " + mecanico.obtenerNombreCompleto() + " se asigno al taller "
                        + seleccionado.getNombreTaller() + ".", "Mecanico asignado");
                refrescar();
            } else if (e.getSource() == btnQuitar) {
                requiereSeleccion();
                control.quitarMecanico(seleccionado, (Mecanico) cboMecanico.getSelectedItem());
                Mensajes.exito(this, "El mecanico se quito del taller.", "Mecanico retirado");
                refrescar();
            } else if (e.getSource() == btnEstado) {
                requiereSeleccion();
                control.cambiarEstado(seleccionado);
                Mensajes.exito(this, "El taller quedo " + (seleccionado.isEstado() ? "activo." : "inactivo."),
                        "Estado actualizado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEliminar) {
                requiereSeleccion();
                if (Mensajes.confirmar(this, "Esta seguro que desea eliminar el taller "
                        + seleccionado.getNombreTaller() + "?", "Confirmar eliminacion")) {
                    control.eliminar(seleccionado);
                    Mensajes.exito(this, "El taller se elimino de la base de datos.", "Registro eliminado");
                    limpiar();
                    refrescar();
                }
            } else if (e.getSource() == btnBuscar || e.getSource() == txtBuscar) {
                cargarTabla(control.buscar(cboCriterio.getSelectedItem().toString(), txtBuscar.getText()));
                if (listaActual.isEmpty()) {
                    Mensajes.exito(this, "No se encontraron talleres que coincidan con la busqueda.", "Sin resultados");
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

    private void requiereSeleccion() throws DatoInvalidoException {
        if (seleccionado == null) {
            throw new DatoInvalidoException("Debe seleccionar un taller de la tabla.");
        }
    }

    @Override
    public final void refrescar() {
        try {
            JefeTaller jefeActual = (JefeTaller) cboJefe.getSelectedItem();
            cboJefe.removeAllItems();
            ArrayList<JefeTaller> jefes = controlEmpleados.listarJefes();
            for (int i = 0; i < jefes.size(); i++) {
                cboJefe.addItem(jefes.get(i));
            }
            if (jefeActual != null) {
                cboJefe.setSelectedItem(jefeActual);
            }

            Mecanico mecanicoActual = (Mecanico) cboMecanico.getSelectedItem();
            cboMecanico.removeAllItems();
            ArrayList<Mecanico> mecanicos = controlEmpleados.listarMecanicos();
            for (int i = 0; i < mecanicos.size(); i++) {
                cboMecanico.addItem(mecanicos.get(i));
            }
            if (mecanicoActual != null) {
                cboMecanico.setSelectedItem(mecanicoActual);
            }

            cargarTabla(control.listar());
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void cargarTabla(ArrayList<Taller> lista) {
        listaActual = lista;
        modelo.setRowCount(0);
        for (int i = 0; i < lista.size(); i++) {
            Taller t = lista.get(i);
            String mecanicos = "";
            for (int j = 0; j < t.getMecanicos().size(); j++) {
                if (j > 0) {
                    mecanicos = mecanicos + ", ";
                }
                mecanicos = mecanicos + t.getMecanicos().get(j).obtenerNombreCompleto();
            }
            if (mecanicos.isEmpty()) {
                mecanicos = "Sin mecanicos";
            }
            Object[] fila = new Object[8];
            fila[0] = t.getId();
            fila[1] = t.getNombreTaller();
            fila[2] = t.getDescripcion();
            fila[3] = t.nombreJefe();
            fila[4] = mecanicos;
            fila[5] = t.cantidadMecanicos();
            fila[6] = t.getTotalOrdenesActivas();
            fila[7] = t.isEstado() ? "Activo" : "Inactivo";
            modelo.addRow(fila);
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaActual.size()) {
            return;
        }
        seleccionado = listaActual.get(fila);
        txtNombre.setText(seleccionado.getNombreTaller());
        txtDescripcion.setText(seleccionado.getDescripcion());
        cboJefe.setSelectedItem(seleccionado.getJefeCargo());
    }

    private void limpiar() {
        seleccionado = null;
        txtNombre.setText("");
        txtDescripcion.setText("");
        tabla.clearSelection();
        txtNombre.requestFocus();
    }
}
