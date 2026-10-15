package tallermecanico.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import tallermecanico.controlador.ControlCatalogos;
import tallermecanico.controlador.ControlInventario;
import tallermecanico.excepciones.DatoInvalidoException;
import tallermecanico.modelo.Categoria;
import tallermecanico.modelo.Producto;
import tallermecanico.modelo.Proveedor;
import tallermecanico.util.Estilos;
import tallermecanico.util.Formato;

public class PanelInventario extends JPanel implements ActionListener, Refrescable {

    private ControlInventario control;
    private ControlCatalogos controlCatalogos;
    private ArrayList<Producto> listaActual;
    private Producto seleccionado;

    private JTextField txtCodigo;
    private JTextField txtNombre;
    private JTextField txtMarca;
    private JComboBox<Categoria> cboCategoria;
    private JComboBox<Proveedor> cboProveedor;
    private JTextField txtPrecioCompra;
    private JTextField txtPrecioVenta;
    private JTextField txtStock;
    private JTextField txtStockMin;
    private JTextField txtStockMax;

    private JComboBox<String> cboCriterio;
    private JComboBox<Object> cboFiltroCategoria;
    private JTextField txtBuscar;
    private JButton btnBuscar;
    private JButton btnMostrarTodos;
    private JButton btnBajoMinimo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnModificar;
    private JButton btnIngresar;
    private JButton btnEstado;
    private JButton btnEliminar;

    private DefaultTableModel modelo;
    private JTable tabla;

    public PanelInventario() {
        control = new ControlInventario();
        controlCatalogos = new ControlCatalogos();
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
        JPanel formulario = new JPanel(new GridLayout(0, 6, 10, 8));
        formulario.setBackground(Estilos.PANEL);
        formulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        txtCodigo = Estilos.campo();
        txtNombre = Estilos.campo();
        txtMarca = Estilos.campo();
        cboCategoria = new JComboBox<>();
        cboProveedor = new JComboBox<>();
        Estilos.combo(cboCategoria);
        Estilos.combo(cboProveedor);
        txtPrecioCompra = Estilos.campo();
        txtPrecioVenta = Estilos.campo();
        txtStock = Estilos.campo();
        txtStockMin = Estilos.campo();
        txtStockMax = Estilos.campo();

        formulario.add(Estilos.etiqueta("Codigo"));
        formulario.add(txtCodigo);
        formulario.add(Estilos.etiqueta("Producto"));
        formulario.add(txtNombre);
        formulario.add(Estilos.etiqueta("Marca"));
        formulario.add(txtMarca);
        formulario.add(Estilos.etiqueta("Categoria"));
        formulario.add(cboCategoria);
        formulario.add(Estilos.etiqueta("Proveedor"));
        formulario.add(cboProveedor);
        formulario.add(Estilos.etiqueta("Stock actual"));
        formulario.add(txtStock);
        formulario.add(Estilos.etiqueta("Precio compra"));
        formulario.add(txtPrecioCompra);
        formulario.add(Estilos.etiqueta("Precio venta"));
        formulario.add(txtPrecioVenta);
        formulario.add(Estilos.etiqueta("Stock min / max"));
        JPanel rango = new JPanel(new GridLayout(1, 2, 6, 0));
        rango.setBackground(Estilos.PANEL);
        rango.add(txtStockMin);
        rango.add(txtStockMax);
        formulario.add(rango);
        return formulario;
    }

    private JPanel construirBusqueda() {
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        busqueda.setBackground(Estilos.FONDO);

        String[] criterios = {"Todos", "Codigo", "Producto", "Marca", "Proveedor"};
        cboCriterio = new JComboBox<>(criterios);
        Estilos.combo(cboCriterio);
        cboFiltroCategoria = new JComboBox<>();
        Estilos.combo(cboFiltroCategoria);
        txtBuscar = Estilos.campo();
        txtBuscar.setPreferredSize(new java.awt.Dimension(200, 30));
        btnBuscar = Estilos.boton("Buscar", Estilos.NARANJA);
        btnMostrarTodos = Estilos.boton("Mostrar todos", Estilos.PANEL_CLARO);
        btnBajoMinimo = Estilos.boton("Ver bajo minimo", Estilos.ROJO);
        btnBuscar.addActionListener(this);
        btnMostrarTodos.addActionListener(this);
        btnBajoMinimo.addActionListener(this);
        txtBuscar.addActionListener(this);

        busqueda.add(Estilos.etiqueta("Buscar por:"));
        busqueda.add(cboCriterio);
        busqueda.add(txtBuscar);
        busqueda.add(Estilos.etiqueta("Categoria:"));
        busqueda.add(cboFiltroCategoria);
        busqueda.add(btnBuscar);
        busqueda.add(btnMostrarTodos);
        busqueda.add(btnBajoMinimo);
        return busqueda;
    }

    private JScrollPane construirTabla() {
        String[] columnas = {"Codigo", "Producto", "Marca", "Categoria", "Proveedor", "P. compra", "P. venta",
            "Stock", "Min", "Max", "Situacion"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        Estilos.tabla(tabla);

        DefaultTableCellRenderer pintor = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tablaRef, Object valor, boolean seleccionada,
                    boolean enfocada, int fila, int columna) {
                Component celda = super.getTableCellRendererComponent(tablaRef, valor, seleccionada,
                        enfocada, fila, columna);
                String situacion = String.valueOf(tablaRef.getValueAt(fila, 10));
                if (!seleccionada) {
                    celda.setBackground(Estilos.PANEL);
                    if (situacion.equals("Bajo minimo")) {
                        celda.setForeground(new Color(239, 141, 141));
                    } else if (situacion.equals("Inactivo")) {
                        celda.setForeground(Estilos.TEXTO_SUAVE);
                    } else {
                        celda.setForeground(Estilos.TEXTO);
                    }
                }
                return celda;
            }
        };
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(pintor);
        }
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
        btnIngresar = Estilos.boton("Ingresar stock", Estilos.AMARILLO);
        btnEstado = Estilos.boton("Activar/Inactivar", Estilos.PANEL_CLARO);
        btnEliminar = Estilos.boton("Eliminar", Estilos.ROJO);

        btnNuevo.addActionListener(this);
        btnGuardar.addActionListener(this);
        btnModificar.addActionListener(this);
        btnIngresar.addActionListener(this);
        btnEstado.addActionListener(this);
        btnEliminar.addActionListener(this);

        botones.add(btnNuevo);
        botones.add(btnGuardar);
        botones.add(btnModificar);
        botones.add(btnIngresar);
        botones.add(btnEstado);
        botones.add(btnEliminar);
        return botones;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getSource() == btnGuardar) {
                control.agregar(txtCodigo.getText(), txtNombre.getText(), txtMarca.getText(),
                        (Categoria) cboCategoria.getSelectedItem(), (Proveedor) cboProveedor.getSelectedItem(),
                        txtPrecioCompra.getText(), txtPrecioVenta.getText(), txtStock.getText(),
                        txtStockMin.getText(), txtStockMax.getText());
                Mensajes.exito(this, "El producto se registro correctamente en la base de datos.", "Registro guardado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnModificar) {
                requiereSeleccion();
                control.modificar(seleccionado, txtCodigo.getText(), txtNombre.getText(), txtMarca.getText(),
                        (Categoria) cboCategoria.getSelectedItem(), (Proveedor) cboProveedor.getSelectedItem(),
                        txtPrecioCompra.getText(), txtPrecioVenta.getText(), txtStock.getText(),
                        txtStockMin.getText(), txtStockMax.getText());
                Mensajes.exito(this, "El producto se actualizo correctamente.", "Registro modificado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnIngresar) {
                requiereSeleccion();
                String cantidad = JOptionPane.showInputDialog(this,
                        "Cantidad a ingresar de " + seleccionado.getNombre()
                        + "\nStock actual: " + seleccionado.getStock()
                        + "\nStock maximo: " + seleccionado.getStockMax(),
                        "Ingreso a bodega", JOptionPane.QUESTION_MESSAGE);
                if (cantidad != null) {
                    control.ingresarStock(seleccionado, cantidad);
                    Mensajes.exito(this, "El ingreso se registro. Nuevo stock: " + seleccionado.getStock() + ".",
                            "Ingreso registrado");
                    refrescar();
                }
            } else if (e.getSource() == btnEstado) {
                requiereSeleccion();
                control.cambiarEstado(seleccionado);
                Mensajes.exito(this, "El producto quedo " + (seleccionado.isEstado() ? "activo." : "inactivo."),
                        "Estado actualizado");
                limpiar();
                refrescar();
            } else if (e.getSource() == btnEliminar) {
                requiereSeleccion();
                if (Mensajes.confirmar(this, "Esta seguro que desea eliminar el producto "
                        + seleccionado.getNombre() + "?", "Confirmar eliminacion")) {
                    control.eliminar(seleccionado);
                    Mensajes.exito(this, "El producto se elimino de la base de datos.", "Registro eliminado");
                    limpiar();
                    refrescar();
                }
            } else if (e.getSource() == btnBuscar || e.getSource() == txtBuscar) {
                buscar();
            } else if (e.getSource() == btnBajoMinimo) {
                cargarTabla(control.listarBajoMinimo());
            } else if (e.getSource() == btnMostrarTodos) {
                txtBuscar.setText("");
                cboCriterio.setSelectedIndex(0);
                cboFiltroCategoria.setSelectedIndex(0);
                refrescar();
            } else if (e.getSource() == btnNuevo) {
                limpiar();
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void buscar() throws Exception {
        Categoria categoria = null;
        if (cboFiltroCategoria.getSelectedItem() instanceof Categoria) {
            categoria = (Categoria) cboFiltroCategoria.getSelectedItem();
        }
        cargarTabla(control.buscar(cboCriterio.getSelectedItem().toString(), txtBuscar.getText(), categoria));
        if (listaActual.isEmpty()) {
            Mensajes.exito(this, "No se encontraron productos que coincidan con la busqueda.", "Sin resultados");
        }
    }

    private void requiereSeleccion() throws DatoInvalidoException {
        if (seleccionado == null) {
            throw new DatoInvalidoException("Debe seleccionar un producto de la tabla.");
        }
    }

    @Override
    public final void refrescar() {
        try {
            ArrayList<Categoria> categorias = controlCatalogos.listarCategorias();
            Object categoriaActual = cboCategoria.getSelectedItem();
            cboCategoria.removeAllItems();
            cboFiltroCategoria.removeAllItems();
            cboFiltroCategoria.addItem("Todas");
            for (int i = 0; i < categorias.size(); i++) {
                cboCategoria.addItem(categorias.get(i));
                cboFiltroCategoria.addItem(categorias.get(i));
            }
            if (categoriaActual != null) {
                cboCategoria.setSelectedItem(categoriaActual);
            }

            Object proveedorActual = cboProveedor.getSelectedItem();
            cboProveedor.removeAllItems();
            ArrayList<Proveedor> proveedores = controlCatalogos.listarProveedoresActivos();
            for (int i = 0; i < proveedores.size(); i++) {
                cboProveedor.addItem(proveedores.get(i));
            }
            if (proveedorActual != null) {
                cboProveedor.setSelectedItem(proveedorActual);
            }

            cargarTabla(control.listar());
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void cargarTabla(ArrayList<Producto> lista) {
        listaActual = lista;
        modelo.setRowCount(0);
        for (int i = 0; i < lista.size(); i++) {
            Producto p = lista.get(i);
            String situacion;
            if (!p.isEstado()) {
                situacion = "Inactivo";
            } else if (p.estaBajoMinimo()) {
                situacion = "Bajo minimo";
            } else if (p.getStock() >= p.getStockMax()) {
                situacion = "En el maximo";
            } else {
                situacion = "Normal";
            }
            Object[] fila = new Object[11];
            fila[0] = p.getCodigo();
            fila[1] = p.getNombre();
            fila[2] = p.getMarca();
            fila[3] = p.getCategoria().getNombre();
            fila[4] = p.getProveedor().getNombre();
            fila[5] = Formato.quetzales(p.getPrecioCompra());
            fila[6] = Formato.quetzales(p.getPrecioVenta());
            fila[7] = p.getStock();
            fila[8] = p.getStockMin();
            fila[9] = p.getStockMax();
            fila[10] = situacion;
            modelo.addRow(fila);
        }
    }

    private void seleccionarFila() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= listaActual.size()) {
            return;
        }
        seleccionado = listaActual.get(fila);
        txtCodigo.setText(seleccionado.getCodigo());
        txtNombre.setText(seleccionado.getNombre());
        txtMarca.setText(seleccionado.getMarca());
        cboCategoria.setSelectedItem(seleccionado.getCategoria());
        cboProveedor.setSelectedItem(seleccionado.getProveedor());
        txtPrecioCompra.setText(Formato.decimal(seleccionado.getPrecioCompra()));
        txtPrecioVenta.setText(Formato.decimal(seleccionado.getPrecioVenta()));
        txtStock.setText(String.valueOf(seleccionado.getStock()));
        txtStockMin.setText(String.valueOf(seleccionado.getStockMin()));
        txtStockMax.setText(String.valueOf(seleccionado.getStockMax()));
    }

    private void limpiar() {
        seleccionado = null;
        txtCodigo.setText("");
        txtNombre.setText("");
        txtMarca.setText("");
        txtPrecioCompra.setText("");
        txtPrecioVenta.setText("");
        txtStock.setText("");
        txtStockMin.setText("");
        txtStockMax.setText("");
        tabla.clearSelection();
        txtCodigo.requestFocus();
    }
}
