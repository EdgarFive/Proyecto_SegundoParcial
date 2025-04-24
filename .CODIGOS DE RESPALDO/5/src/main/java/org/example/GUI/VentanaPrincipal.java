package org.example.GUI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private JComboBox<String> comboUsuarios;
    private String usuarioSeleccionado;

    public VentanaPrincipal(List<String> usuarios) {
        setTitle("Sistema de Recomendación");
        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout());
        
        // Centrar la ventana en la pantalla
        setLocationRelativeTo(null);

        JLabel etiqueta = new JLabel("Selecciona un usuario:");
        comboUsuarios = new JComboBox<>();

        // Verificar si hay usuarios
        if (usuarios != null && !usuarios.isEmpty()) {
            for (String usuario : usuarios) {
                comboUsuarios.addItem(usuario);
                System.out.println("Agregando usuario al combo: " + usuario);
            }
        } else {
            System.out.println("No hay usuarios para mostrar");
            comboUsuarios.addItem("No hay usuarios disponibles");
        }

        JButton botonConfirmar = new JButton("Continuar");

        botonConfirmar.addActionListener(e -> {
            usuarioSeleccionado = (String) comboUsuarios.getSelectedItem();
            JOptionPane.showMessageDialog(this,
                    "Has ingresado como: " + usuarioSeleccionado);
                    // Abre la nueva ventana y cierra la actual (opcional)
                    new VentanaCompra(usuarioSeleccionado);
                    this.dispose(); // Esto cierra la ventana actual (VentanaPrincipal)
        });

        add(etiqueta);
        add(comboUsuarios);
        add(botonConfirmar);

        setVisible(true);
    }

    public String getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }
}
