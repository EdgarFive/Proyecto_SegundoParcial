package org.example;

import org.example.GUI.VentanaPrincipal;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;


public class Main {
    public static void main(String[] args) {
        ConexionNeo4j conexion = ConexionNeo4j.getInstance();
        List<String> usuarios = new ArrayList<>();

        try (var session = conexion.getSession()) {
            var result = session.run("MATCH (u:Usuario) RETURN u.nombre AS nombre");
            while (result.hasNext()) {
                var record = result.next();
                usuarios.add(record.get("nombre").asString());
                // Añade esta línea para debug
                System.out.println("Usuario encontrado: " + record.get("nombre").asString());
            }
            // Añade esta línea para debug
            System.out.println("Total usuarios encontrados: " + usuarios.size());
        }

        conexion.close();

        // Iniciar interfaz gráfica
        SwingUtilities.invokeLater(() -> new VentanaPrincipal(usuarios));
    }
}