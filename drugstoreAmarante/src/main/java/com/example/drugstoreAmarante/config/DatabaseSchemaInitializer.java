package com.example.drugstoreAmarante.config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaInitializer implements CommandLineRunner, Ordered {

    private final DataSource dataSource;

    public DatabaseSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void run(String... args) {
        try (Connection connection = dataSource.getConnection()) {
            if (!columnExists(connection, "users", "perfil")) {
                addProfileColumn(connection);
            }
            fillNullProfiles(connection);
            adjustCarrinhoItensSchema(connection);
        } catch (SQLException ex) {
            throw new IllegalStateException("Falha ao ajustar schema do banco de dados.", ex);
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(null, null, tableName, columnName)) {
            if (resultSet.next()) {
                return true;
            }
        }

        try (ResultSet resultSet = metaData.getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
            return resultSet.next();
        }
    }

    private void addProfileColumn(Connection connection) throws SQLException {
        String productName = connection.getMetaData().getDatabaseProductName();
        String sql;

        if (productName != null && productName.toLowerCase(Locale.ROOT).contains("h2")) {
            sql = "ALTER TABLE users ADD perfil VARCHAR(20) DEFAULT 'CLIENTE' NOT NULL";
        } else {
            sql = "IF COL_LENGTH('users', 'perfil') IS NULL " +
                "BEGIN " +
                "ALTER TABLE users ADD perfil NVARCHAR(20) NOT NULL CONSTRAINT DF_users_perfil DEFAULT('CLIENTE'); " +
                "END";
        }

        try (var statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void fillNullProfiles(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE users SET perfil = 'CLIENTE' WHERE perfil IS NULL");
        }
    }

    private void adjustCarrinhoItensSchema(Connection connection) throws SQLException {
        if (!columnExists(connection, "carrinho_itens", "usuario_id")) {
            return;
        }

        String productName = connection.getMetaData().getDatabaseProductName();
        String sql;
        if (productName != null && productName.toLowerCase(Locale.ROOT).contains("h2")) {
            sql = "ALTER TABLE carrinho_itens ALTER COLUMN usuario_id SET NULL";
        } else {
            sql = "ALTER TABLE carrinho_itens ALTER COLUMN usuario_id BIGINT NULL";
        }

        try (var statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception ignored) {
        }
    }
}
