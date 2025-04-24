package org.example;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class ConexionNeo4j {
    private final Driver driver;

    public ConexionNeo4j(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public Session getSession() {
        return driver.session();
    }

    public void close() {
        driver.close();
    }
}
