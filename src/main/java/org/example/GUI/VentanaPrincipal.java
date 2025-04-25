package org.example.GUI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private JComboBox<String> comboUsuarios;
    private String usuarioSeleccionado;

    public VentanaPrincipal(List<String> usuarios) {
        setTitle("Sistema de Recomendación");
        setSize(300, 250);  // Tamaño más compacto
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Panel principal con color de fondo oscuro
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(40, 40, 40));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 30, 30, 30));  // Más padding arriba

        // Título centrado
        JLabel titleLabel = new JLabel("Log in");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Estilizar el combo más compacto
        comboUsuarios = new JComboBox<>();
        comboUsuarios.setPreferredSize(new Dimension(240, 35));
        comboUsuarios.setMaximumSize(new Dimension(240, 35));
        comboUsuarios.setBackground(new Color(60, 60, 60));
        comboUsuarios.setForeground(Color.WHITE);
        comboUsuarios.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        comboUsuarios.setAlignmentX(Component.CENTER_ALIGNMENT);

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
        botonConfirmar.setPreferredSize(new Dimension(240, 35));
        botonConfirmar.setMaximumSize(new Dimension(240, 35));
        botonConfirmar.setBackground(new Color(255, 69, 69));
        botonConfirmar.setForeground(Color.WHITE);
        botonConfirmar.setBorderPainted(false);
        botonConfirmar.setFocusPainted(false);
        botonConfirmar.setFont(new Font("Arial", Font.BOLD, 14));
        botonConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        botonConfirmar.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Mantener el ActionListener existente
        botonConfirmar.addActionListener(e -> {
            usuarioSeleccionado = (String) comboUsuarios.getSelectedItem();
            new VentanaCompra(usuarioSeleccionado);
            this.dispose();
        });

        // Agregar componentes al panel principal con espaciado ajustado
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        mainPanel.add(comboUsuarios);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(botonConfirmar);

        // Agregar panel principal al frame
        add(mainPanel);
        setVisible(true);
    }

    public String getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }
}
