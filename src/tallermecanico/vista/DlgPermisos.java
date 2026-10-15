package tallermecanico.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import tallermecanico.controlador.ControlUsuarios;
import tallermecanico.modelo.Rol;
import tallermecanico.util.Estilos;

public class DlgPermisos extends JDialog implements ActionListener {

    private ControlUsuarios control;
    private JComboBox<Rol> cboRol;
    private JCheckBox[] casillas;
    private JButton btnGuardar;
    private JButton btnCerrar;

    public DlgPermisos(Frame padre) {
        super(padre, "Permisos por rol", true);
        control = new ControlUsuarios();
        iniciarComponentes();
        cargarRoles();
        setSize(560, 420);
        setLocationRelativeTo(padre);
    }

    private void iniciarComponentes() {
        getContentPane().setBackground(Estilos.FONDO);
        setLayout(new BorderLayout(0, 10));

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        arriba.setBackground(Estilos.FONDO);
        cboRol = new JComboBox<>();
        Estilos.combo(cboRol);
        cboRol.setPreferredSize(new java.awt.Dimension(220, 30));
        cboRol.addActionListener(this);
        arriba.add(Estilos.etiqueta("Rol:"));
        arriba.add(cboRol);

        JPanel centro = new JPanel(new GridLayout(0, 2, 10, 6));
        centro.setBackground(Estilos.PANEL);
        centro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        casillas = new JCheckBox[Rol.MODULOS.length];
        for (int i = 0; i < Rol.MODULOS.length; i++) {
            casillas[i] = new JCheckBox(Rol.MODULOS[i]);
            casillas[i].setBackground(Estilos.PANEL);
            casillas[i].setForeground(Estilos.TEXTO);
            casillas[i].setFont(Estilos.NORMAL);
            centro.add(casillas[i]);
        }

        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        abajo.setBackground(Estilos.FONDO);
        btnGuardar = Estilos.boton("Guardar permisos", Estilos.VERDE);
        btnCerrar = Estilos.boton("Cerrar", Estilos.PANEL_CLARO);
        btnGuardar.addActionListener(this);
        btnCerrar.addActionListener(this);
        abajo.add(btnGuardar);
        abajo.add(btnCerrar);

        JPanel envoltura = new JPanel(new BorderLayout());
        envoltura.setBackground(Estilos.FONDO);
        envoltura.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        envoltura.add(centro, BorderLayout.CENTER);

        add(arriba, BorderLayout.NORTH);
        add(envoltura, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);
    }

    private void cargarRoles() {
        try {
            ArrayList<Rol> roles = control.listarRoles();
            for (int i = 0; i < roles.size(); i++) {
                cboRol.addItem(roles.get(i));
            }
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private void mostrarPermisos() {
        Rol rol = (Rol) cboRol.getSelectedItem();
        if (rol == null) {
            return;
        }
        for (int i = 0; i < casillas.length; i++) {
            casillas[i].setSelected(rol.tienePermiso(Rol.MODULOS[i]));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cboRol) {
            mostrarPermisos();
        } else if (e.getSource() == btnCerrar) {
            dispose();
        } else if (e.getSource() == btnGuardar) {
            try {
                Rol rol = (Rol) cboRol.getSelectedItem();
                ArrayList<String> modulos = new ArrayList<>();
                for (int i = 0; i < casillas.length; i++) {
                    if (casillas[i].isSelected()) {
                        modulos.add(Rol.MODULOS[i]);
                    }
                }
                control.guardarPermisos(rol, modulos);
                Mensajes.exito(this, "Los permisos del rol " + rol.getNombre() + " se guardaron en la base de datos.\n"
                        + "Se aplicaran la proxima vez que un usuario con este rol inicie sesion.", "Permisos guardados");
            } catch (Exception ex) {
                Mensajes.error(this, ex);
            }
        }
    }
}
