package org.example.GUI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private JComboBox<String> comboUsuarios;
    private String usuarioSeleccionado;

    public VentanaPrincipal(List<String> usuarios) {
        setTitle("Sistema de Recomendación");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel principal con color de fondo oscuro
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(40, 40, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Título
        JLabel titleLabel = new JLabel("Log in");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Panel para el combo
        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.Y_AXIS));
        comboPanel.setBackground(new Color(40, 40, 40));
        comboPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Estilizar el combo
        comboUsuarios = new JComboBox<>();
        comboUsuarios.setPreferredSize(new Dimension(300, 40));
        comboUsuarios.setMaximumSize(new Dimension(300, 40));
        comboUsuarios.setBackground(new Color(60, 60, 60));
        comboUsuarios.setForeground(Color.WHITE);
        comboUsuarios.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Agregar usuarios al combo
        if (usuarios != null && !usuarios.isEmpty()) {
            for (String usuario : usuarios) {
                comboUsuarios.addItem(usuario);
            }
        } else {
            comboUsuarios.addItem("No hay usuarios disponibles");
        }

        // Botón de continuar
        JButton botonConfirmar = new JButton("Log in");
        botonConfirmar.setPreferredSize(new Dimension(300, 40));
        botonConfirmar.setMaximumSize(new Dimension(300, 40));
        botonConfirmar.setBackground(new Color(255, 69, 69));
        botonConfirmar.setForeground(Color.WHITE);
        botonConfirmar.setBorderPainted(false);
        botonConfirmar.setFocusPainted(false);
        botonConfirmar.setFont(new Font("Arial", Font.BOLD, 14));
        botonConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        botonConfirmar.addActionListener(e -> {
            usuarioSeleccionado = (String) comboUsuarios.getSelectedItem();
            new VentanaCompra(usuarioSeleccionado);
            this.dispose();
        });

        // Agregar componentes al panel principal
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(comboUsuarios);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(botonConfirmar);

        // Agregar panel principal al frame
        add(mainPanel);
        setVisible(true);
    }

    public String getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }
}
