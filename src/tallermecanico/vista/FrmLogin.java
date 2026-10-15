package tallermecanico.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import tallermecanico.controlador.ControlUsuarios;
import tallermecanico.excepciones.ReglaNegocioException;
import tallermecanico.util.Estilos;

public class FrmLogin extends JFrame implements ActionListener {

    private ControlUsuarios control;
    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;
    private JButton btnSalir;

    public FrmLogin() {
        control = new ControlUsuarios();
        iniciarComponentes();
    }

    private void iniciarComponentes() {
        setTitle("Sistema de Talleres - Inicio de sesion");
        setSize(430, 380);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(Estilos.FONDO);
        setLayout(new BorderLayout());

        JPanel cabecera = new JPanel();
        cabecera.setLayout(new BoxLayoutVertical(cabecera));
        cabecera.setBackground(Estilos.BARRA);
        cabecera.setBorder(BorderFactory.createEmptyBorder(28, 30, 24, 30));

        JLabel logo = new JLabel("SISTEMA DE TALLERES");
        logo.setFont(Estilos.TITULO);
        logo.setForeground(Estilos.NARANJA);
        logo.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Taller Mecanico Central");
        sub.setFont(Estilos.SUBTITULO);
        sub.setForeground(Estilos.TEXTO_SUAVE);
        sub.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        cabecera.add(logo);
        cabecera.add(javax.swing.Box.createVerticalStrut(6));
        cabecera.add(sub);

        JPanel centro = new JPanel(new GridLayout(4, 1, 0, 10));
        centro.setBackground(Estilos.FONDO);
        centro.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

        txtUsuario = Estilos.campo();
        txtContrasena = new JPasswordField();
        txtContrasena.setFont(Estilos.NORMAL);
        txtContrasena.setBackground(Estilos.PANEL_CLARO);
        txtContrasena.setForeground(Estilos.TEXTO);
        txtContrasena.setCaretColor(Estilos.NARANJA);
        txtContrasena.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Estilos.BORDE),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        centro.add(Estilos.etiqueta("Usuario"));
        centro.add(txtUsuario);
        centro.add(Estilos.etiqueta("Contrasena"));
        centro.add(txtContrasena);

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 12));
        pie.setBackground(Estilos.FONDO);

        btnIngresar = Estilos.boton("Ingresar", Estilos.NARANJA);
        btnIngresar.setPreferredSize(new Dimension(140, 36));
        btnSalir = Estilos.boton("Salir", Estilos.PANEL_CLARO);
        btnSalir.setPreferredSize(new Dimension(110, 36));

        btnIngresar.addActionListener(this);
        btnSalir.addActionListener(this);

        pie.add(btnIngresar);
        pie.add(btnSalir);

        KeyAdapter enter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    ingresar();
                }
            }
        };
        txtUsuario.addKeyListener(enter);
        txtContrasena.addKeyListener(enter);

        add(cabecera, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnIngresar) {
            ingresar();
        } else if (e.getSource() == btnSalir) {
            System.exit(0);
        }
    }

    private void ingresar() {
        try {
            String usuario = txtUsuario.getText();
            String contrasena = new String(txtContrasena.getPassword());
            control.iniciarSesion(usuario, contrasena);

            FrmPrincipal principal = new FrmPrincipal();
            principal.setVisible(true);
            dispose();
        } catch (ReglaNegocioException ex) {
            Mensajes.error(this, ex);
            txtContrasena.setText("");
        } catch (Exception ex) {
            Mensajes.error(this, ex);
        }
    }

    private static class BoxLayoutVertical extends javax.swing.BoxLayout {

        public BoxLayoutVertical(java.awt.Container destino) {
            super(destino, javax.swing.BoxLayout.Y_AXIS);
        }
    }
}
