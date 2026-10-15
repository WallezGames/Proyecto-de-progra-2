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
import tallermecanico.controlador.ControlEmpleados;
import tallermecanico.controlador.ControlInventario;
import tallermecanico.controlador.ControlOrdenes;
import tallermecanico.controlador.ControlRequerimientos;
import tallermecanico.controlador.Sesion;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.modelo.Bodeguero;
import tallermecanico.modelo.DetalleRequerimiento;
import tallermecanico.modelo.JefeTaller;
import tallermecanico.modelo.Mecanico;
import tallermecanico.modelo.OrdenTrabajo;
import tallermecanico.modelo.Producto;
import tallermecanico.modelo.Requerimiento;
import tallermecanico.util.Estilos;
import tallermecanico.util.Formato;

public class PanelRequerimientos extends JPanel implements ActionListener, Refrescable {

    private ControlRequerimientos control;
    private ControlOrdenes controlOrdenes;
    private ControlInventario controlInventario;
    private ControlEmpleados controlEmpleados;

    private ArrayList<Requerimiento> listaActual;
    private Requerimiento actual;
    private boolean cargando;

    private JLabel lblNumero;
    private JLabel lblEstado;
    private JComboBox<OrdenTrabajo> cboOrden;
    private JComboBox<Mecanico> cboSolicitante;
    private JTextField txtObservacion;
    private JComboBox<JefeTaller> cboJefe;
    private JComboBox<Bodeguero> cboBodeguero;

    private JComboBox<Producto> cboProducto;
    private JTextField txtCantidad;
    private JButton btnAgregarLinea;
    private JButton btnQuitarLinea;
    private JLabel lblTotal;
    private DefaultTableModel modeloDetalle;
    private JTable tablaDetalle;

    private JComboBox<String> cboFiltroEstado;
    private JTextField txtDesde;
    private JTextField txtHasta;
    private JComboBox<String> cboCriterio;
    private JTextField txtBuscar;
    private JButton btnFiltrar;
    private JButton btnTodos;
    private DefaultTableModel modelo;
    private JTable tabla;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnAutorizar;
    private JButton btnRechazar;
    private JButton btnProcesar;
    private JButton btnEliminar;

    public PanelRequerimientos() {
        control = new ControlRequerimientos();
        controlOrdenes = new ControlOrdenes();
        controlInventario = new ControlInventario();
        controlEmpleados = new ControlEmpleados();
        listaActual = new ArrayList<>();
        actual = new Requerimiento();
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
        lblNumero = Estilos.etiqueta("Nuevo requerimiento");
        lblNumero.setForeground(Estilos.NARANJA);
        lblNumero.setFont(Estilos.NEGRITA);
        lblEstado = Estilos.etiqueta("Solicitado");
        lblEstado.setForeground(Estilos.TEXTO);
        cboOrden = new JComboBox<>();
        cboSolicitante = new JComboBox<>();
        cboJefe = new JComboBox<>();
        cboBodeguero = new JComboBox<>();
        Estilos.combo(cboOrden);
        Estilos.combo(cboSolicitante);
        Estilos.combo(cboJefe);
        Estilos.combo(cboBodeguero);
        cboOrden.addActionListener(this);
        txtObservacion = Estilos.campo();

        encabezado.add(Estilos.etiqueta("Requerimiento"));
        encabezado.add(lblNumero);
        encabezado.add(Estilos.etiqueta("Orden de trabajo"));
        encabezado.add(cboOrden);
        encabezado.add(Estilos.etiqueta("Mecanico solicitante"));
        encabezado.add(cboSolicitante);
        encabezado.add(Estilos.etiqueta("Estado"));
        encabezado.add(lblEstado);
        encabezado.add(Estilos.etiqueta("Jefe que autoriza"));
        encabezado.add(cboJefe);
        encabezado.add(Estilos.etiqueta("Bodeguero que procesa"));
        encabezado.add(cboBodeguero);
        encabezado.add(Estilos.etiqueta("Observacion"));
        encabezado.add(txtObservacion);

        JPanel linea = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        linea.setBackground(Estilos.PANEL);
        cboProducto = new JComboBox<>();
        Estilos.combo(cboProducto);
        cboProducto.setPreferredSize(new Dimension(320, 30));
        txtCantidad = Estilos.campo();
        txtCantidad.setPreferredSize(new Dimension(60, 30));
        txtCantidad.setText("1");
        btnAgregarLinea = Estilos.boton("Agregar producto", Estilos.VERDE);
        btnQuitarLinea = Estilos.boton("Quitar producto", Estilos.PANEL_CLARO);
        btnAgregarLinea.addActionListener(this);
        btnQuitarLinea.addActionListener(this);
        lblTotal = new JLabel("Total: Q0.00");
        lblTotal.setFont(Estilos.NEGRITA);
        lblTotal.setForeground(Estilos.NARANJA);
        linea.add(Estilos.etiqueta("Detalle - Producto:"));
        linea.add(cboProducto);
        linea.add(Estilos.etiqueta("Cant."));
        linea.add(txtCantidad);
        linea.add(btnAgregarLinea);
        linea.add(btnQuitarLinea);
        linea.add(lblTotal);

        modeloDetalle = new DefaultTableModel(new String[]{"Codigo", "Producto", "Cantidad", "Precio unitario",
            "Subtotal", "Stock en bodega"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaDetalle = new JTable(modeloDetalle);
        Estilos.tabla(tablaDetalle);
        JScrollPane scrollDetalle = new JScrollPane(tablaDetalle);
        Estilos.scroll(scrollDetalle);
        scrollDetalle.setPreferredSize(new Dimension(100, 105));

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
        cboFiltroEstado = new JComboBox<>(ControlRequerimientos.ESTADOS);
        cboCriterio = new JComboBox<>(new String[]{"Todos", "Numero", "Orden", "Taller", "Solicitante", "Producto"});
        Estilos.combo(cboFiltroEstado);
        Estilos.combo(cboCriterio);
        cboFiltroEstado.setPreferredSize(new Dimension(120, 30));
        cboCriterio.setPreferredSize(new Dimension(110, 30));
        txtDesde = Estilos.campo();
        txtHasta = Estilos.campo();
        txtDesde.setPreferredSize(new Dimension(90, 30));
        txtHasta.setPreferredSize(new Dimension(90, 30));
        txtDesde.setToolTipText("dd/mm/aaaa");
        txtHasta.setToolTipText("dd/mm/aaaa");
        txtBuscar = Estilos.campo();
        txtBuscar.setPreferredSize(new Dimension(150, 30));
        btnFiltrar = Estilos.boton("Filtrar", Estilos.NARANJA);
        btnTodos = Estilos.boton("Todos", Estilos.PANEL_CLARO);
        btnFiltrar.addActionListener(this);
        btnTodos.addActionListener(this);
        txtBuscar.addActionListener(this);

        filtros.add(Estilos.etiqueta("Estado"));
        filtros.add(cboFiltroEstado);
        filtros.add(Estilos.etiqueta("Desde"));
        filtros.add(txtDesde);
        filtros.add(Estilos.etiqueta("Hasta"));
        filtros.add(txtHasta);
        filtros.add(cboCriterio);
        filtros.add(txtBuscar);
        filtros.add(btnFiltrar);
        filtros.add(btnTodos);

        String[] columnas = {"Requerimiento", "Fecha", "Orden", "Taller", "Solicito", "Autorizo", "Proceso",
            "Estado", "Total"};
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
        btnNuevo = Estilos.boton("Nuevo", Estilos.PANEL_CLARO);
        btnGuardar = Estilos.boton("Guardar requerimiento", Estilos.VERDE);
        btnAutorizar = Estilos.boton("Autorizar", Estilos.NARANJA);
        btnRechazar = Estilos.boton("Rechazar", Estilos.AMARILLO);
        btnProcesar = Estilos.boton("Procesar salida", Estilos.VERDE);
        btnEliminar = Estilos.boton("Eliminar", Estilos.ROJO);
        JButton[] lista = {btnNuevo, btnGuardar, btnAutorizar, btnRechazar, btnProcesar, btnEliminar};
        for (int i = 0; i < lista.length; i++) {
            lista[i].addActionListener(this);
            botones.add(lista[i]);
        }
        return botones;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cboOrden) {
            if (!cargando) {
                cargarSolicitantes();
            }
            return;
        }
        try {
            if (e.getSource() == btnAgregarLinea) {
                control.agregarLinea(actual, (Producto) cboProducto.getSelectedItem(), txtCantidad.getText());
                txtCantidad.setText("1");
                mostrarDetalle();
            } else if (e.getSource() == btnQuitarLinea) {
                control.quitarLinea(actual, tablaDetalle.getSelectedRow());
                mostrarDetalle();
            } else if (e.getSource() == btnGuardar) {
                boolean nuevo = actual.getIdRequerimiento() == 0;
                Requerimiento guardado = control.guardar(actual, (OrdenTrabajo) cboOrden.getSelectedItem(),
                        (Mecanico) cboSolicitante.getSelectedItem(), txtObservacion.getText());
                Mensajes.exito(this, (nuevo ? "Se genero el requerimiento " : "Se guardaron los cambios del requerimiento ")
                        + guardado.getNumero() + " con " + guardado.getDetalles().size() + " producto(s).\n"
                        + "Queda en estado Solicitado, pendiente de autorizacion del jefe de taller.", "Requerimiento guardado");
                refrescar();
                seleccionarPorId(guardado.getIdRequerimiento());
            } else if (e.getSource() == btnAutorizar) {
                requiereGuardado();
                control.autorizar(actual, (JefeTaller) cboJefe.getSelectedItem());
                Mensajes.exito(this, "El requerimiento " + actual.getNumero()
                        + " fue autorizado. Ya puede ser procesado por la bodega.", "Requerimiento autorizado");
                recargarSeleccion();
            } else if (e.getSource() == btnRechazar) {
                requiereGuardado();
                String motivo = JOptionPane.showInputDialog(this, "Indique el motivo del rechazo:",
                        "Rechazar requerimiento", JOptionPane.QUESTION_MESSAGE);
                if (motivo != null) {
                    control.rechazar(actual, (JefeTaller) cboJefe.getSelectedItem(), motivo);
                    Mensajes.exito(this, "El requerimiento " + actual.getNumero() + " fue rechazado.", "Requerimiento rechazado");
                    recargarSeleccion();
                }
            } else if (e.getSource() == btnProcesar) {
                requiereGuardado();
                Requerimiento procesado = control.procesar(actual, (Bodeguero) cboBodeguero.getSelectedItem());
                String texto = "Se proceso la salida de bodega del requerimiento " + procesado.getNumero() + ".\n"
                        + "Stock actualizado en una sola transaccion:\n";
                for (int i = 0; i < procesado.getDetalles().size(); i++) {
                    DetalleRequerimiento d = procesado.getDetalles().get(i);
                    texto = texto + "  " + d.getProducto().getNombre() + ": quedan " + d.getProducto().getStock() + "\n";
                }
                Mensajes.exito(this, texto, "Salida procesada");
                recargarSeleccion();
            } else if (e.getSource() == btnEliminar) {
                requiereGuardado();
                if (Mensajes.confirmar(this, "Esta seguro que desea eliminar el requerimiento " + actual.getNumero() + "?",
                        "Confirmar eliminacion")) {
                    control.eliminar(actual);
                    Mensajes.exito(this, "El requerimiento se elimino junto con su detalle.", "Registro eliminado");
                    nuevo();
                    refrescar();
                }
            } else if (e.getSource() == btnFiltrar || e.getSource() == txtBuscar) {
                cargarTabla(control.filtrar(cboFiltroEstado.getSelectedItem().toString(), txtDesde.getText(),
                        txtHasta.getText(), cboCriterio.getSelectedItem().toString(), txtBuscar.getText()));
                if (listaActual.isEmpty()) {
                    Mensajes.exito(this, "No hay requerimientos que cumplan con los filtros indicados.", "Sin resultados");
                }
            } else if (e.getSource() == btnTodos) {
                cboFiltroEstado.setSelectedIndex(0);
                txtDesde.setText("");
                txtHasta.setText("");
                txtBuscar.setText("");
                cargarTabla(control.listar());
            } else if (e.getSource() == btnNuevo) {
                nuevo();
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void requiereGuardado() throws DatoInvalidoException {
        if (actual.getIdRequerimiento() == 0) {
            throw new DatoInvalidoException("Debe seleccionar un requerimiento de la tabla.");
        }
    }

    @Override
    public final void refrescar() {
        try {
            cargando = true;
            Object ordenAnterior = cboOrden.getSelectedItem();
            cboOrden.removeAllItems();
            ArrayList<OrdenTrabajo> ordenes = controlOrdenes.listarActivas();
            for (int i = 0; i < ordenes.size(); i++) {
                cboOrden.addItem(ordenes.get(i));
            }
            if (ordenAnterior != null) {
                cboOrden.setSelectedItem(ordenAnterior);
            }
            cargando = false;
            cargarSolicitantes();

            Object productoAnterior = cboProducto.getSelectedItem();
            cboProducto.removeAllItems();
            ArrayList<Producto> productos = controlInventario.listarActivos();
            for (int i = 0; i < productos.size(); i++) {
                cboProducto.addItem(productos.get(i));
            }
            if (productoAnterior != null) {
                cboProducto.setSelectedItem(productoAnterior);
            }

            cboJefe.removeAllItems();
            ArrayList<JefeTaller> jefes = controlEmpleados.listarJefes();
            for (int i = 0; i < jefes.size(); i++) {
                cboJefe.addItem(jefes.get(i));
            }
            if (Sesion.getEmpleado() instanceof JefeTaller) {
                cboJefe.setSelectedItem(Sesion.getEmpleado());
            }

            cboBodeguero.removeAllItems();
            ArrayList<Bodeguero> bodegueros = controlEmpleados.listarBodegueros();
            for (int i = 0; i < bodegueros.size(); i++) {
                cboBodeguero.addItem(bodegueros.get(i));
            }
            if (Sesion.getEmpleado() instanceof Bodeguero) {
                cboBodeguero.setSelectedItem(Sesion.getEmpleado());
            }

            cargarTabla(control.listar());
        } catch (Exception ex) {
            cargando = false;
            Mensajes.error(this, ex);
        }
    }

    private void cargarSolicitantes() {
        Object anterior = cboSolicitante.getSelectedItem();
        cboSolicitante.removeAllItems();
        OrdenTrabajo orden = (OrdenTrabajo) cboOrden.getSelectedItem();
        if (orden == null) {
            return;
        }
        for (int i = 0; i < orden.getMecanicos().size(); i++) {
            cboSolicitante.addItem(orden.getMecanicos().get(i));
        }
        if (Sesion.getEmpleado() instanceof Mecanico && orden.getMecanicos().contains((Mecanico) Sesion.getEmpleado())) {
            cboSolicitante.setSelectedItem(Sesion.getEmpleado());
        } else if (anterior != null) {
            cboSolicitante.setSelectedItem(anterior);
        }
    }

    private void cargarTabla(ArrayList<Requerimiento> lista) {
        listaActual = lista;
        modelo.setRowCount(0);
        for (int i = 0; i < lista.size(); i++) {
            Requerimiento r = lista.get(i);
            Object[] fila = new Object[9];
            fila[0] = r.getNumero();
            fila[1] = Formato.fecha(r.getFechaSolicitud().toLocalDate());
            fila[2] = r.getOrdenTrabajo().getNumero();
            fila[3] = r.nombreTaller();
            fila[4] = r.getSolicitante().obtenerNombreCompleto();
            fila[5] = r.nombreAutorizadoPor();
            fila[6] = r.nombreProcesadoPor();
            fila[7] = r.getEstado();
            fila[8] = Formato.quetzales(r.total());
            modelo.addRow(fila);
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaActual.size()) {
            return;
        }
        try {
            Requerimiento completo = control.cargarCompleto(listaActual.get(fila));
            if (completo != null) {
                mostrar(completo);
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void seleccionarPorId(int id) {
        for (int i = 0; i < listaActual.size(); i++) {
            if (listaActual.get(i).getIdRequerimiento() == id) {
                tabla.setRowSelectionInterval(i, i);
                tabla.scrollRectToVisible(tabla.getCellRect(i, 0, true));
                seleccionarFila();
                return;
            }
        }
    }

    private void recargarSeleccion() {
        int id = actual.getIdRequerimiento();
        refrescar();
        seleccionarPorId(id);
    }

    private void mostrar(Requerimiento requerimiento) {
        actual = requerimiento;
        lblNumero.setText(requerimiento.getNumero() + " (" + requerimiento.getOrdenTrabajo().getNumero() + ")");
        lblEstado.setText(requerimiento.getEstado());
        txtObservacion.setText(requerimiento.getObservacion() == null ? "" : requerimiento.getObservacion());
        cboOrden.setEnabled(false);
        cboSolicitante.setEnabled(false);
        mostrarDetalle();
    }

    private void mostrarDetalle() {
        modeloDetalle.setRowCount(0);
        for (int i = 0; i < actual.getDetalles().size(); i++) {
            DetalleRequerimiento d = actual.getDetalles().get(i);
            modeloDetalle.addRow(new Object[]{d.getProducto().getCodigo(), d.getProducto().getNombre(), d.getCantidad(),
                Formato.quetzales(d.getPrecioUnitario()), Formato.quetzales(d.subtotal()), d.getProducto().getStock()});
        }
        lblTotal.setText("Total: " + Formato.quetzales(actual.total()));
    }

    private void nuevo() {
        actual = new Requerimiento();
        lblNumero.setText("Nuevo requerimiento");
        lblEstado.setText("Solicitado");
        txtObservacion.setText("");
        txtCantidad.setText("1");
        cboOrden.setEnabled(true);
        cboSolicitante.setEnabled(true);
        tabla.clearSelection();
        mostrarDetalle();
    }
}
