package tallermecanico.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import tallermecanico.controlador.ControlUsuarios;
import tallermecanico.controlador.Sesion;
import tallermecanico.util.Estilos;

public class FrmPrincipal extends JFrame implements ActionListener {

    private JPanel contenido;
    private CardLayout tarjetas;
    private ArrayList<JButton> botonesMenu;
    private ArrayList<String> clavesMenu;
    private JButton btnCerrarSesion;
    private JLabel lblTitulo;
    private JLabel lblSubtitulo;
    private PanelResumen panelResumen;

    public FrmPrincipal() {
        botonesMenu = new ArrayList<>();
        clavesMenu = new ArrayList<>();
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        setTitle("Sistema de Talleres - Taller Mecanico Central");
        setSize(1280, 720);
        setMinimumSize(new Dimension(1100, 640));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Estilos.FONDO);
        setLayout(new BorderLayout());

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                salir();
            }
        });

        add(construirMenuLateral(), BorderLayout.WEST);
        add(construirCabecera(), BorderLayout.NORTH);
        add(construirContenido(), BorderLayout.CENTER);

        mostrarModulo("Resumen", "Resumen general", "Indicadores principales del taller");
    }

    private JPanel construirMenuLateral() {
        JPanel lateral = new JPanel();
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(Estilos.BARRA);
        lateral.setPreferredSize(new Dimension(235, 0));
        lateral.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Estilos.BORDE));

        JPanel marca = new JPanel();
        marca.setLayout(new BoxLayout(marca, BoxLayout.Y_AXIS));
        marca.setBackground(Estilos.BARRA);
        marca.setBorder(BorderFactory.createEmptyBorder(22, 18, 20, 18));
        marca.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        marca.setMaximumSize(new Dimension(235, 80));

        JLabel titulo = new JLabel("Sistema de Talleres");
        titulo.setFont(Estilos.NEGRITA);
        titulo.setForeground(Estilos.TEXTO);
        JLabel sub = new JLabel("Taller Mecanico Central");
        sub.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        sub.setForeground(Estilos.TEXTO_SUAVE);

        marca.add(titulo);
        marca.add(Box.createVerticalStrut(3));
        marca.add(sub);
        lateral.add(marca);

        JLabel seccion = new JLabel("MODULOS");
        seccion.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 10));
        seccion.setForeground(new java.awt.Color(110, 110, 110));
        seccion.setBorder(BorderFactory.createEmptyBorder(6, 20, 8, 20));
        seccion.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        lateral.add(seccion);

        agregarBotonMenu(lateral, "Resumen", "Resumen general");
        agregarBotonMenu(lateral, "Ordenes", "Ordenes de trabajo");
        agregarBotonMenu(lateral, "Clientes", "Clientes");
        agregarBotonMenu(lateral, "Vehiculos", "Vehiculos");
        agregarBotonMenu(lateral, "Empleados", "Empleados");
        agregarBotonMenu(lateral, "Talleres", "Talleres");
        agregarBotonMenu(lateral, "Inventario", "Inventario");
        agregarBotonMenu(lateral, "Requerimientos", "Requerimientos");
        agregarBotonMenu(lateral, "Catalogos", "Catalogos");
        agregarBotonMenu(lateral, "Usuarios", "Usuarios");
        agregarBotonMenu(lateral, "Reportes", "Reportes");

        lateral.add(Box.createVerticalGlue());

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        abajo.setBackground(Estilos.BARRA);
        abajo.setMaximumSize(new Dimension(235, 60));
        btnCerrarSesion = Estilos.boton("Cerrar sesion", Estilos.PANEL_CLARO);
        btnCerrarSesion.setPreferredSize(new Dimension(180, 34));
        btnCerrarSesion.addActionListener(this);
        abajo.add(btnCerrarSesion);
        lateral.add(abajo);

        return lateral;
    }

    private void agregarBotonMenu(JPanel lateral, String clave, String texto) {
        JButton boton = Estilos.botonMenu(texto);
        boton.setAlignmentX(JButton.LEFT_ALIGNMENT);
        boton.setEnabled(Sesion.tienePermiso(clave));
        if (!boton.isEnabled()) {
            boton.setForeground(new java.awt.Color(80, 80, 80));
            boton.setToolTipText("Su perfil (" + Sesion.puesto() + ") no tiene acceso a este modulo");
        }
        boton.addActionListener(this);
        botonesMenu.add(boton);
        clavesMenu.add(clave);
        lateral.add(boton);
    }

    private JPanel construirCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(Estilos.FONDO);
        cabecera.setBorder(BorderFactory.createEmptyBorder(18, 26, 14, 26));

        JPanel izquierda = new JPanel();
        izquierda.setLayout(new BoxLayout(izquierda, BoxLayout.Y_AXIS));
        izquierda.setBackground(Estilos.FONDO);
        lblTitulo = Estilos.titulo("Resumen general");
        lblSubtitulo = Estilos.subtitulo("Indicadores principales del taller");
        izquierda.add(lblTitulo);
        izquierda.add(Box.createVerticalStrut(3));
        izquierda.add(lblSubtitulo);

        JPanel derecha = new JPanel();
        derecha.setLayout(new BoxLayout(derecha, BoxLayout.Y_AXIS));
        derecha.setBackground(Estilos.FONDO);
        JLabel usuario = new JLabel(Sesion.nombreUsuario());
        usuario.setFont(Estilos.NEGRITA);
        usuario.setForeground(Estilos.TEXTO);
        usuario.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        JLabel rol = new JLabel(Sesion.puesto());
        rol.setFont(Estilos.SUBTITULO);
        rol.setForeground(Estilos.NARANJA);
        rol.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
        derecha.add(usuario);
        derecha.add(rol);

        cabecera.add(izquierda, BorderLayout.WEST);
        cabecera.add(derecha, BorderLayout.EAST);
        return cabecera;
    }

    private JPanel construirContenido() {
        tarjetas = new CardLayout();
        contenido = new JPanel(tarjetas);
        contenido.setBackground(Estilos.FONDO);
        contenido.setBorder(BorderFactory.createEmptyBorder(0, 26, 20, 26));

        panelResumen = new PanelResumen();
        contenido.add(panelResumen, "Resumen");
        contenido.add(new PanelOrdenes(), "Ordenes");
        contenido.add(new PanelClientes(), "Clientes");
        contenido.add(new PanelVehiculos(), "Vehiculos");
        contenido.add(new PanelEmpleados(), "Empleados");
        contenido.add(new PanelTalleres(), "Talleres");
        contenido.add(new PanelInventario(), "Inventario");
        contenido.add(new PanelRequerimientos(), "Requerimientos");
        contenido.add(new PanelCatalogos(), "Catalogos");
        contenido.add(new PanelUsuarios(), "Usuarios");
        contenido.add(new PanelReportes(), "Reportes");

        return contenido;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnCerrarSesion) {
            cerrarSesion();
            return;
        }
        for (int i = 0; i < botonesMenu.size(); i++) {
            if (e.getSource() == botonesMenu.get(i)) {
                String clave = clavesMenu.get(i);
                mostrarModulo(clave, botonesMenu.get(i).getText(), descripcionModulo(clave));
                return;
            }
        }
    }

    private void mostrarModulo(String clave, String titulo, String descripcion) {
        if (!Sesion.tienePermiso(clave)) {
            JOptionPane.showMessageDialog(this,
                    "Su perfil de " + Sesion.puesto() + " no tiene permiso para ingresar al modulo de " + titulo + ".",
                    "Acceso restringido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        tarjetas.show(contenido, clave);
        lblTitulo.setText(titulo);
        lblSubtitulo.setText(descripcion);

        for (int i = 0; i < botonesMenu.size(); i++) {
            Estilos.marcarMenuActivo(botonesMenu.get(i), clavesMenu.get(i).equals(clave));
            if (!botonesMenu.get(i).isEnabled()) {
                botonesMenu.get(i).setForeground(new java.awt.Color(80, 80, 80));
            }
        }
        refrescarModulo(clave);
    }

    private void refrescarModulo(String clave) {
        Component[] paneles = contenido.getComponents();
        for (int i = 0; i < paneles.length; i++) {
            if (paneles[i].isVisible() && paneles[i] instanceof Refrescable) {
                ((Refrescable) paneles[i]).refrescar();
            }
        }
    }

    private String descripcionModulo(String clave) {
        if (clave.equals("Resumen")) {
            return "Indicadores principales del taller";
        }
        if (clave.equals("Ordenes")) {
            return "Registro, asignacion y seguimiento de ordenes de trabajo";
        }
        if (clave.equals("Clientes")) {
            return "Registro de clientes del taller";
        }
        if (clave.equals("Vehiculos")) {
            return "Vehiculos registrados y su propietario";
        }
        if (clave.equals("Empleados")) {
            return "Mecanicos, jefes de taller, bodegueros, recepcion y administracion";
        }
        if (clave.equals("Talleres")) {
            return "Talleres del negocio, su jefe a cargo y sus mecanicos";
        }
        if (clave.equals("Inventario")) {
            return "Productos y repuestos de la bodega central";
        }
        if (clave.equals("Requerimientos")) {
            return "Solicitud, autorizacion y salida de producto de bodega";
        }
        if (clave.equals("Catalogos")) {
            return "Servicios de cada taller, categorias de productos y proveedores";
        }
        if (clave.equals("Usuarios")) {
            return "Usuarios del sistema y control de permisos";
        }
        return "Reportes generales del sistema";
    }

    private void cerrarSesion() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "Esta seguro que desea cerrar la sesion?", "Cerrar sesion",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            new ControlUsuarios().cerrarSesion();
            FrmLogin login = new FrmLogin();
            login.setVisible(true);
            dispose();
        }
    }

    private void salir() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "Desea salir del sistema? La informacion ya esta guardada en la base de datos.",
                "Salir del sistema", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta == JOptionPane.YES_OPTION) {
            new ControlUsuarios().cerrarSesion();
            tallermecanico.conexion.Conexion.getInstancia().cerrar();
            System.exit(0);
        }
    }
}
