package tallermecanico.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import tallermecanico.controlador.ControlCatalogos;
import tallermecanico.controlador.ControlOrdenes;
import tallermecanico.controlador.ControlTalleres;
import tallermecanico.controlador.ControlVehiculos;
import tallermecanico.controlador.Sesion;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.modelo.DetalleOrden;
import tallermecanico.modelo.Mecanico;
import tallermecanico.modelo.OrdenTrabajo;
import tallermecanico.modelo.Servicio;
import tallermecanico.modelo.Taller;
import tallermecanico.modelo.Vehiculo;
import tallermecanico.util.Estilos;
import tallermecanico.util.Formato;

public class PanelOrdenes extends JPanel implements ActionListener, Refrescable {

    private ControlOrdenes control;
    private ControlVehiculos controlVehiculos;
    private ControlTalleres controlTalleres;
    private ControlCatalogos controlCatalogos;

    private ArrayList<OrdenTrabajo> listaActual;
    private OrdenTrabajo ordenActual;
    private boolean cargando;

    private JLabel lblNumero;
    private JLabel lblEstado;
    private JComboBox<Vehiculo> cboVehiculo;
    private JComboBox<Taller> cboTaller;
    private JTextField txtMotivo;
    private JComboBox<Mecanico> cboMecanico;

    private JComboBox<Servicio> cboServicio;
    private JTextField txtCantidad;
    private JButton btnAgregarLinea;
    private JButton btnQuitarLinea;
    private JLabel lblTotal;
    private DefaultTableModel modeloDetalle;
    private JTable tablaDetalle;

    private JComboBox<String> cboFiltroEstado;
    private JComboBox<Object> cboFiltroTaller;
    private JTextField txtDesde;
    private JTextField txtHasta;
    private JComboBox<String> cboCriterio;
    private JTextField txtBuscar;
    private JButton btnFiltrar;
    private JButton btnLimpiarFiltros;
    private DefaultTableModel modelo;
    private JTable tabla;

    private JButton btnNueva;
    private JButton btnGuardar;
    private JButton btnAsignar;
    private JButton btnQuitar;
    private JComboBox<String> cboNuevoEstado;
    private JButton btnCambiarEstado;
    private JButton btnAvance;
    private JButton btnEliminar;

    public PanelOrdenes() {
        control = new ControlOrdenes();
        controlVehiculos = new ControlVehiculos();
        controlTalleres = new ControlTalleres();
        controlCatalogos = new ControlCatalogos();
        listaActual = new ArrayList<>();
        ordenActual = new OrdenTrabajo();
        iniciarComponentes();
        refrescar();
    }

    private void iniciarComponentes() {
        setLayout(new BorderLayout(0, 8));
        setBackground(Estilos.FONDO);
        add(construirEditor(), BorderLayout.NORTH);
        add(construirListado(), BorderLayout.CENTER);
        add(construirBotones(), BorderLayout.SOUTH);
    }

    private JPanel construirEditor() {
        JPanel editor = new JPanel(new BorderLayout(0, 6));
        editor.setBackground(Estilos.PANEL);
        editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JPanel encabezado = new JPanel(new GridLayout(0, 6, 10, 6));
        encabezado.setBackground(Estilos.PANEL);
        lblNumero = Estilos.etiqueta("Nueva orden");
        lblNumero.setForeground(Estilos.NARANJA);
        lblNumero.setFont(Estilos.NEGRITA);
        lblEstado = Estilos.etiqueta("Pendiente");
        lblEstado.setForeground(Estilos.TEXTO);
        cboVehiculo = new JComboBox<>();
        cboTaller = new JComboBox<>();
        cboMecanico = new JComboBox<>();
        Estilos.combo(cboVehiculo);
        Estilos.combo(cboTaller);
        Estilos.combo(cboMecanico);
        cboTaller.addActionListener(this);
        txtMotivo = Estilos.campo();

        encabezado.add(Estilos.etiqueta("Orden"));
        encabezado.add(lblNumero);
        encabezado.add(Estilos.etiqueta("Vehiculo"));
        encabezado.add(cboVehiculo);
        encabezado.add(Estilos.etiqueta("Taller"));
        encabezado.add(cboTaller);
        encabezado.add(Estilos.etiqueta("Estado"));
        encabezado.add(lblEstado);
        encabezado.add(Estilos.etiqueta("Motivo de ingreso"));
        encabezado.add(txtMotivo);
        encabezado.add(Estilos.etiqueta("Mecanico"));
        encabezado.add(cboMecanico);

        JPanel linea = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        linea.setBackground(Estilos.PANEL);
        cboServicio = new JComboBox<>();
        Estilos.combo(cboServicio);
        cboServicio.setPreferredSize(new Dimension(320, 30));
        txtCantidad = Estilos.campo();
        txtCantidad.setPreferredSize(new Dimension(60, 30));
        txtCantidad.setText("1");
        btnAgregarLinea = Estilos.boton("Agregar servicio", Estilos.VERDE);
        btnQuitarLinea = Estilos.boton("Quitar servicio", Estilos.PANEL_CLARO);
        btnAgregarLinea.addActionListener(this);
        btnQuitarLinea.addActionListener(this);
        lblTotal = new JLabel("Total servicios: Q0.00");
        lblTotal.setFont(Estilos.NEGRITA);
        lblTotal.setForeground(Estilos.NARANJA);
        linea.add(Estilos.etiqueta("Detalle - Servicio:"));
        linea.add(cboServicio);
        linea.add(Estilos.etiqueta("Cant."));
        linea.add(txtCantidad);
        linea.add(btnAgregarLinea);
        linea.add(btnQuitarLinea);
        linea.add(lblTotal);

        modeloDetalle = new DefaultTableModel(new String[]{"Servicio", "Cantidad", "Precio unitario", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaDetalle = new JTable(modeloDetalle);
        Estilos.tabla(tablaDetalle);
        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        Estilos.scroll(scrollDetalle);
        scrollDetalle.setPreferredSize(new Dimension(100, 110));

        JPanel abajo = new JPanel(new BorderLayout(0, 4));
        abajo.setBackground(Estilos.PANEL);
        abajo.add(linea, BorderLayout.NORTH);
        abajo.add(scrollDetalle, BorderLayout.CENTER);

        editor.add(encabezado, BorderLayout.NORTH);
        editor.add(abajo, BorderLayout.CENTER);
        return editor;
    }

    private JPanel construirListado() {
        JPanel listado = new JPanel(new BorderLayout(0, 4));
        listado.setBackground(Estilos.FONDO);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        filtros.setBackground(Estilos.FONDO);
        String[] estados = {"Todos", "Pendiente", "En proceso", "Terminado", "Entregado", "Cancelado"};
        cboFiltroEstado = new JComboBox<>(estados);
        cboFiltroTaller = new JComboBox<>();
        cboCriterio = new JComboBox<>(new String[]{"Todos", "Numero", "Placa", "Cliente", "Mecanico"});
        Estilos.combo(cboFiltroEstado);
        Estilos.combo(cboFiltroTaller);
        Estilos.combo(cboCriterio);
        cboFiltroEstado.setPreferredSize(new Dimension(110, 30));
        cboCriterio.setPreferredSize(new Dimension(100, 30));
        txtDesde = Estilos.campo();
        txtHasta = Estilos.campo();
        txtDesde.setPreferredSize(new Dimension(90, 30));
        txtHasta.setPreferredSize(new Dimension(90, 30));
        txtDesde.setToolTipText("dd/mm/aaaa");
        txtHasta.setToolTipText("dd/mm/aaaa");
        txtBuscar = Estilos.campo();
        txtBuscar.setPreferredSize(new Dimension(130, 30));
        btnFiltrar = Estilos.boton("Filtrar", Estilos.NARANJA);
        btnLimpiarFiltros = Estilos.boton("Todas", Estilos.PANEL_CLARO);
        btnFiltrar.addActionListener(this);
        btnLimpiarFiltros.addActionListener(this);
        txtBuscar.addActionListener(this);

        filtros.add(Estilos.etiqueta("Estado"));
        filtros.add(cboFiltroEstado);
        filtros.add(Estilos.etiqueta("Taller"));
        filtros.add(cboFiltroTaller);
        filtros.add(Estilos.etiqueta("Desde"));
        filtros.add(txtDesde);
        filtros.add(Estilos.etiqueta("Hasta"));
        filtros.add(txtHasta);
        filtros.add(cboCriterio);
        filtros.add(txtBuscar);
        filtros.add(btnFiltrar);
        filtros.add(btnLimpiarFiltros);

        String[] columnas = {"Orden", "Ingreso", "Vehiculo", "Cliente", "Taller", "Mecanicos", "Estado",
            "Servicios", "Repuestos", "Total"};
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

        listado.add(filtros, BorderLayout.NORTH);
        listado.add(scroll, BorderLayout.CENTER);
        return listado;
    }

    private JPanel construirBotones() {
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        botones.setBackground(Estilos.FONDO);

        btnNueva = Estilos.boton("Nueva", Estilos.PANEL_CLARO);
        btnGuardar = Estilos.boton("Guardar orden", Estilos.VERDE);
        btnAsignar = Estilos.boton("Asignar mecanico", Estilos.NARANJA);
        btnQuitar = Estilos.boton("Quitar mecanico", Estilos.PANEL_CLARO);
        cboNuevoEstado = new JComboBox<>(new String[]{"En proceso", "Terminado", "Entregado", "Cancelado"});
        Estilos.combo(cboNuevoEstado);
        cboNuevoEstado.setPreferredSize(new Dimension(120, 30));
        btnCambiarEstado = Estilos.boton("Cambiar estado", Estilos.AMARILLO);
        btnAvance = Estilos.boton("Registrar avance", Estilos.PANEL_CLARO);
        btnEliminar = Estilos.boton("Eliminar", Estilos.ROJO);

        JButton[] lista = {btnNueva, btnGuardar, btnAsignar, btnQuitar};
        for (int i = 0; i < lista.length; i++) {
            lista[i].addActionListener(this);
            botones.add(lista[i]);
        }
        botones.add(cboNuevoEstado);
        btnCambiarEstado.addActionListener(this);
        botones.add(btnCambiarEstado);
        btnAvance.addActionListener(this);
        botones.add(btnAvance);
        btnEliminar.addActionListener(this);
        botones.add(btnEliminar);
        return botones;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cboTaller) {
            if (!cargando) {
                cargarCombosDelTaller();
            }
            return;
        }
        try {
            if (e.getSource() == btnAgregarLinea) {
                control.agregarLinea(ordenActual, (Servicio) cboServicio.getSelectedItem(), txtCantidad.getText());
                txtCantidad.setText("1");
                mostrarDetalle();
            } else if (e.getSource() == btnQuitarLinea) {
                control.quitarLinea(ordenActual, tablaDetalle.getSelectedRow());
                mostrarDetalle();
            } else if (e.getSource() == btnGuardar) {
                boolean nueva = ordenActual.getIdOrden() == 0;
                control.guardar(ordenActual, (Vehiculo) cboVehiculo.getSelectedItem(), (Taller) cboTaller.getSelectedItem(),
                        txtMotivo.getText(), nueva ? (Mecanico) cboMecanico.getSelectedItem() : null);
                Mensajes.exito(this, (nueva ? "Se genero la orden " : "Se guardaron los cambios de la orden ")
                        + ordenActual.getNumero() + " con " + ordenActual.getDetalles().size() + " servicio(s).\n"
                        + "El encabezado y el detalle se guardaron en una sola transaccion.", "Orden guardada");
                int id = ordenActual.getIdOrden();
                refrescar();
                seleccionarPorId(id);
            } else if (e.getSource() == btnAsignar) {
                control.asignarMecanico(ordenActual, (Mecanico) cboMecanico.getSelectedItem());
                Mensajes.exito(this, "El mecanico se asigno a la orden " + ordenActual.getNumero() + ".", "Mecanico asignado");
                recargarSeleccion();
            } else if (e.getSource() == btnQuitar) {
                control.quitarMecanico(ordenActual, (Mecanico) cboMecanico.getSelectedItem());
                Mensajes.exito(this, "El mecanico se quito de la orden.", "Mecanico retirado");
                recargarSeleccion();
            } else if (e.getSource() == btnCambiarEstado) {
                String nuevo = cboNuevoEstado.getSelectedItem().toString();
                control.cambiarEstado(ordenActual, nuevo);
                OrdenTrabajo actualizada = control.cargarCompleta(ordenActual);
                String total = actualizada == null ? "" : "\nTotal de la orden: " + Formato.quetzales(actualizada.getTotal());
                Mensajes.exito(this, "La orden " + ordenActual.getNumero() + " cambio al estado " + nuevo + "." + total,
                        "Estado actualizado");
                recargarSeleccion();
            } else if (e.getSource() == btnAvance) {
                requiereOrdenGuardada();
                String avance = JOptionPane.showInputDialog(this, "Describa el avance realizado en la orden "
                        + ordenActual.getNumero() + ":", "Registrar avance", JOptionPane.QUESTION_MESSAGE);
                if (avance != null) {
                    control.registrarAvance(ordenActual, (Mecanico) cboMecanico.getSelectedItem(), avance);
                    Mensajes.exito(this, "El avance se registro en la orden.", "Avance registrado");
                    recargarSeleccion();
                }
            } else if (e.getSource() == btnEliminar) {
                requiereOrdenGuardada();
                if (Mensajes.confirmar(this, "Esta seguro que desea eliminar la orden " + ordenActual.getNumero()
                        + "? Tambien se eliminara su detalle.", "Confirmar eliminacion")) {
                    control.eliminar(ordenActual);
                    Mensajes.exito(this, "La orden se elimino junto con su detalle.", "Orden eliminada");
                    nueva();
                    refrescar();
                }
            } else if (e.getSource() == btnFiltrar || e.getSource() == txtBuscar) {
                filtrar();
            } else if (e.getSource() == btnLimpiarFiltros) {
                cboFiltroEstado.setSelectedIndex(0);
                cboFiltroTaller.setSelectedIndex(0);
                txtDesde.setText("");
                txtHasta.setText("");
                txtBuscar.setText("");
                cargarTabla(control.listar());
            } else if (e.getSource() == btnNueva) {
                nueva();
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void requiereOrdenGuardada() throws DatoInvalidoException {
        if (ordenActual.getIdOrden() == 0) {
            throw new DatoInvalidoException("Debe seleccionar una orden de la tabla.");
        }
    }

    private void filtrar() throws Exception {
        Taller taller = null;
        if (cboFiltroTaller.getSelectedItem() instanceof Taller) {
            taller = (Taller) cboFiltroTaller.getSelectedItem();
        }
        cargarTabla(control.filtrar(cboFiltroEstado.getSelectedItem().toString(), taller, txtDesde.getText(),
                txtHasta.getText(), cboCriterio.getSelectedItem().toString(), txtBuscar.getText()));
        if (listaActual.isEmpty()) {
            Mensajes.exito(this, "No hay ordenes que cumplan con los filtros indicados.", "Sin resultados");
        }
    }

    @Override
    public final void refrescar() {
        try {
            cargando = true;
            Object vehiculoActual = cboVehiculo.getSelectedItem();
            cboVehiculo.removeAllItems();
            ArrayList<Vehiculo> vehiculos = controlVehiculos.listarActivos();
            for (int i = 0; i < vehiculos.size(); i++) {
                cboVehiculo.addItem(vehiculos.get(i));
            }
            if (vehiculoActual != null) {
                cboVehiculo.setSelectedItem(vehiculoActual);
            }

            Object tallerActual = cboTaller.getSelectedItem();
            cboTaller.removeAllItems();
            cboFiltroTaller.removeAllItems();
            cboFiltroTaller.addItem("Todos");
            ArrayList<Taller> talleres = controlTalleres.listarActivos();
            for (int i = 0; i < talleres.size(); i++) {
                cboTaller.addItem(talleres.get(i));
                cboFiltroTaller.addItem(talleres.get(i));
            }
            if (tallerActual != null) {
                cboTaller.setSelectedItem(tallerActual);
            }
            cargando = false;
            cargarCombosDelTaller();
            cargarTabla(control.listar());
        } catch (Exception ex) {
            cargando = false;
            Mensajes.error(this, ex);
        }
    }

    private void cargarCombosDelTaller() {
        try {
            Taller taller = (Taller) cboTaller.getSelectedItem();
            Object servicioActual = cboServicio.getSelectedItem();
            cboServicio.removeAllItems();
            ArrayList<Servicio> servicios = controlCatalogos.listarServiciosDeTaller(taller);
            for (int i = 0; i < servicios.size(); i++) {
                cboServicio.addItem(servicios.get(i));
            }
            if (servicioActual != null) {
                cboServicio.setSelectedItem(servicioActual);
            }

            Object mecanicoActual = cboMecanico.getSelectedItem();
            cboMecanico.removeAllItems();
            ArrayList<Mecanico> mecanicos = control.mecanicosDelTaller(taller);
            for (int i = 0; i < mecanicos.size(); i++) {
                cboMecanico.addItem(mecanicos.get(i));
            }
            if (Sesion.getEmpleado() instanceof Mecanico && mecanicos.contains((Mecanico) Sesion.getEmpleado())) {
                cboMecanico.setSelectedItem(Sesion.getEmpleado());
            } else if (mecanicoActual != null) {
                cboMecanico.setSelectedItem(mecanicoActual);
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void cargarTabla(ArrayList<OrdenTrabajo> lista) {
        listaActual = lista;
        modelo.setRowCount(0);
        for (int i = 0; i < lista.size(); i++) {
            OrdenTrabajo o = lista.get(i);
            Object[] fila = new Object[10];
            fila[0] = o.getNumero();
            fila[1] = Formato.fecha(o.getFechaIngreso().toLocalDate());
            fila[2] = o.getVehiculo().getPlaca() + " " + o.getVehiculo().getDescripcion();
            fila[3] = o.getVehiculo().getPropietario().obtenerNombreCompleto();
            fila[4] = o.getTaller().getNombreTaller();
            fila[5] = o.nombresMecanicos();
            fila[6] = o.getEstado();
            fila[7] = Formato.quetzales(o.getTotalServicios());
            fila[8] = Formato.quetzales(o.getTotalRepuestos());
            fila[9] = Formato.quetzales(o.getTotal());
            modelo.addRow(fila);
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaActual.size()) {
            return;
        }
        try {
            OrdenTrabajo completa = control.cargarCompleta(listaActual.get(fila));
            if (completa != null) {
                mostrarOrden(completa);
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void seleccionarPorId(int idOrden) {
        for (int i = 0; i < listaActual.size(); i++) {
            if (listaActual.get(i).getIdOrden() == idOrden) {
                tabla.setRowSelectionInterval(i, i);
                tabla.scrollRectToVisible(tabla.getCellRect(i, 0, true));
                seleccionarFila();
                return;
            }
        }
    }

    private void recargarSeleccion() {
        int id = ordenActual.getIdOrden();
        try {
            cargarTabla(control.listar());
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
        seleccionarPorId(id);
    }

    private void mostrarOrden(OrdenTrabajo orden) {
        ordenActual = orden;
        lblNumero.setText(orden.getNumero());
        lblEstado.setText(orden.getEstado());
        txtMotivo.setText(orden.getDescripcion());
        boolean existe = false;
        for (int i = 0; i < cboVehiculo.getItemCount(); i++) {
            if (cboVehiculo.getItemAt(i).equals(orden.getVehiculo())) {
                existe = true;
            }
        }
        if (!existe) {
            cboVehiculo.addItem(orden.getVehiculo());
        }
        cboVehiculo.setSelectedItem(orden.getVehiculo());
        cboVehiculo.setEnabled(false);
        cboTaller.setSelectedItem(orden.getTaller());
        mostrarDetalle();
    }

    private void mostrarDetalle() {
        modeloDetalle.setRowCount(0);
        for (int i = 0; i < ordenActual.getDetalles().size(); i++) {
            DetalleOrden d = ordenActual.getDetalles().get(i);
            modeloDetalle.addRow(new Object[]{d.getServicio().getNombre(), d.getCantidad(),
                Formato.quetzales(d.getPrecioUnitario()), Formato.quetzales(d.subtotal())});
        }
        lblTotal.setText("Total servicios: " + Formato.quetzales(control.totalDetalle(ordenActual))
                + (ordenActual.getIdOrden() > 0 ? "   |   Repuestos: " + Formato.quetzales(ordenActual.getTotalRepuestos()) : ""));
    }

    private void nueva() {
        ordenActual = new OrdenTrabajo();
        lblNumero.setText("Nueva orden");
        lblEstado.setText("Pendiente");
        txtMotivo.setText("");
        txtCantidad.setText("1");
        cboVehiculo.setEnabled(true);
        tabla.clearSelection();
        mostrarDetalle();
        txtMotivo.requestFocus();
    }
}
