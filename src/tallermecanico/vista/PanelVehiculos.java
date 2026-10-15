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
import tallermecanico.controlador.ControlVehiculos;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.modelo.Cliente;
import tallermecanico.modelo.Vehiculo;
import tallermecanico.util.Estilos;

public class PanelVehiculos extends JPanel implements ActionListener, Refrescable {

    private ControlVehiculos control;
    private ControlClientes controlClientes;
    private ArrayList<Vehiculo> listaActual;
    private Vehiculo seleccionado;

    private JTextField txtPlaca;
    private JTextField txtMarca;
    private JTextField txtModelo;
    private JTextField txtAnio;
    private JTextField txtColor;
    private JTextField txtKilometraje;
    private JTextField txtChasis;
    private JComboBox<Cliente> cboPropietario;

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

    public PanelVehiculos() {
        control = new ControlVehiculos();
        controlClientes = new ControlClientes();
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

        txtPlaca = Estilos.campo();
        txtMarca = Estilos.campo();
        txtModelo = Estilos.campo();
        txtAnio = Estilos.campo();
        txtColor = Estilos.campo();
        txtKilometraje = Estilos.campo();
        txtChasis = Estilos.campo();
        cboPropietario = new JComboBox<>();
        Estilos.combo(cboPropietario);

        formulario.add(Estilos.etiqueta("Placa"));
        formulario.add(txtPlaca);
        formulario.add(Estilos.etiqueta("Cliente propietario"));
        formulario.add(cboPropietario);
        formulario.add(Estilos.etiqueta("Marca"));
        formulario.add(txtMarca);
        formulario.add(Estilos.etiqueta("Modelo"));
        formulario.add(txtModelo);
        formulario.add(Estilos.etiqueta("Anio"));
        formulario.add(txtAnio);
        formulario.add(Estilos.etiqueta("Color"));
        formulario.add(txtColor);
        formulario.add(Estilos.etiqueta("Kilometraje"));
        formulario.add(txtKilometraje);
        formulario.add(Estilos.etiqueta("Numero de chasis"));
        formulario.add(txtChasis);
        return formulario;
    }

    private JPanel construirBusqueda() {
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        busqueda.setBackground(Estilos.FONDO);

        String[] criterios = {"Todos", "Placa", "Marca", "Modelo", "Anio", "Propietario"};
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
        String[] columnas = {"Placa", "Marca", "Modelo", "Anio", "Color", "Kilometraje", "Chasis", "Propietario",
            "Situacion", "Servicios"};
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
                control.agregar(txtPlaca.getText(), txtMarca.getText(), txtModelo.getText(), txtAnio.getText(),
                        txtColor.getText(), txtKilometraje.getText(), txtChasis.getText(),
                        (Cliente) cboPropietario.getSelectedItem());
                Mensajes.exito(this, "El vehiculo se registro correctamente en la base de datos.", "Registro guardado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnModificar) {
                requiereSeleccion("modificarlo");
                control.modificar(seleccionado, txtPlaca.getText(), txtMarca.getText(), txtModelo.getText(),
                        txtAnio.getText(), txtColor.getText(), txtKilometraje.getText(), txtChasis.getText(),
                        (Cliente) cboPropietario.getSelectedItem());
                Mensajes.exito(this, "Los datos del vehiculo se actualizaron correctamente.", "Registro modificado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEstado) {
                requiereSeleccion("activarlo o inactivarlo");
                control.cambiarEstado(seleccionado);
                Mensajes.exito(this, "Se cambio el estado del vehiculo " + seleccionado.getPlaca() + ".", "Estado actualizado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEliminar) {
                requiereSeleccion("eliminarlo");
                if (Mensajes.confirmar(this, "Esta seguro que desea eliminar el vehiculo con placa "
                        + seleccionado.getPlaca() + "?", "Confirmar eliminacion")) {
                    control.eliminar(seleccionado);
                    Mensajes.exito(this, "El vehiculo se elimino de la base de datos.", "Registro eliminado");
                    limpiar();
                    refrescar();
                }
            } else if (e.getSource() == btnBuscar || e.getSource() == txtBuscar) {
                cargarTabla(control.buscar(cboCriterio.getSelectedItem().toString(), txtBuscar.getText()));
                if (listaActual.isEmpty()) {
                    Mensajes.exito(this, "No se encontraron vehiculos que coincidan con la busqueda.", "Sin resultados");
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
            throw new DatoInvalidoException("Debe seleccionar un vehiculo de la tabla para " + accion + ".");
        }
    }

    @Override
    public final void refrescar() {
        try {
            Cliente actual = (Cliente) cboPropietario.getSelectedItem();
            cboPropietario.removeAllItems();
            ArrayList<Cliente> clientes = controlClientes.listarActivos();
            for (int i = 0; i < clientes.size(); i++) {
                cboPropietario.addItem(clientes.get(i));
            }
            if (actual != null) {
                cboPropietario.setSelectedItem(actual);
            }
            cargarTabla(control.listar());
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void cargarTabla(ArrayList<Vehiculo> lista) {
        listaActual = lista;
        modelo.setRowCount(0);
        for (int i = 0; i < lista.size(); i++) {
            Vehiculo v = lista.get(i);
            Object[] fila = new Object[10];
            fila[0] = v.getPlaca();
            fila[1] = v.getMarca();
            fila[2] = v.getModelo();
            fila[3] = v.getAnio();
            fila[4] = v.getColor();
            fila[5] = v.getKilometraje();
            fila[6] = v.getChasis();
            fila[7] = v.getPropietario().obtenerNombreCompleto();
            fila[8] = v.getEstado();
            fila[9] = v.getTotalServicios();
            modelo.addRow(fila);
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaActual.size()) {
            return;
        }
        seleccionado = listaActual.get(fila);
        txtPlaca.setText(seleccionado.getPlaca());
        txtMarca.setText(seleccionado.getMarca());
        txtModelo.setText(seleccionado.getModelo());
        txtAnio.setText(String.valueOf(seleccionado.getAnio()));
        txtColor.setText(seleccionado.getColor());
        txtKilometraje.setText(String.valueOf(seleccionado.getKilometraje()));
        txtChasis.setText(seleccionado.getChasis());
        cboPropietario.setSelectedItem(seleccionado.getPropietario());
    }

    private void limpiar() {
        seleccionado = null;
        txtPlaca.setText("");
        txtMarca.setText("");
        txtModelo.setText("");
        txtAnio.setText("");
        txtColor.setText("");
        txtKilometraje.setText("");
        txtChasis.setText("");
        tabla.clearSelection();
        txtPlaca.requestFocus();
    }
}
