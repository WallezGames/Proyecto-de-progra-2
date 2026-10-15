package tallermecanico.vista;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import tallermecanico.util.Estilos;

public class PanelCatalogos extends JPanel implements Refrescable {

    private PanelServicios panelServicios;
    private PanelCategorias panelCategorias;
    private PanelProveedores panelProveedores;

    public PanelCatalogos() {
        setLayout(new BorderLayout());
        setBackground(Estilos.FONDO);

        panelServicios = new PanelServicios();
        panelCategorias = new PanelCategorias();
        panelProveedores = new PanelProveedores();

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(Estilos.NEGRITA);
        pestanas.setBackground(Estilos.PANEL_CLARO);
        pestanas.setForeground(Estilos.TEXTO);
        pestanas.addTab("Servicios del taller", panelServicios);
        pestanas.addTab("Categorias de productos", panelCategorias);
        pestanas.addTab("Proveedores", panelProveedores);

        add(pestanas, BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        panelServicios.refrescar();
        panelCategorias.refrescar();
        panelProveedores.refrescar();
    }
}
