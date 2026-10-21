package com.example.drugstoreAmarante.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProdutoImagemSchemaMigrator {

    private static final Logger log = LoggerFactory.getLogger(ProdutoImagemSchemaMigrator.class);

    @Bean
    CommandLineRunner expandImagemUrlColumn(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                String productName = metaData.getDatabaseProductName().toLowerCase(Locale.ROOT);

                migrarColunaImagem(connection, metaData, productName, "produtos", false);
                migrarColunaImagem(connection, metaData, productName, "itens_pedido", true);
            } catch (SQLException ex) {
                log.warn("Nao foi possivel ajustar a coluna produtos.imagem_url automaticamente.", ex);
            }
        };
    }

    private void migrarColunaImagem(
        Connection connection,
        DatabaseMetaData metaData,
        String productName,
        String tableName,
        boolean nullable
    ) throws SQLException {
        if (!imagemPrecisaMigracao(metaData, tableName)) {
            return;
        }

        String nullability = nullable ? " NULL" : " NOT NULL";
        try (Statement statement = connection.createStatement()) {
            if (productName.contains("sql server")) {
                statement.executeUpdate("ALTER TABLE " + tableName + " ALTER COLUMN imagem_url NVARCHAR(MAX)" + nullability);
                log.info("Coluna {}.imagem_url atualizada para NVARCHAR(MAX).", tableName);
            } else if (productName.contains("h2")) {
                statement.executeUpdate("ALTER TABLE " + tableName + " ALTER COLUMN imagem_url CLOB" + nullability);
                log.info("Coluna {}.imagem_url atualizada para CLOB.", tableName);
            }
        }
    }

    private boolean imagemPrecisaMigracao(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet columns = metaData.getColumns(null, null, tableName, "imagem_url")) {
            if (!columns.next()) {
                return false;
            }

            int columnSize = columns.getInt("COLUMN_SIZE");
            String typeName = columns.getString("TYPE_NAME");

            String normalizedType = typeName == null ? "" : typeName.toLowerCase(Locale.ROOT);
            return columnSize > 0 && columnSize <= 600
                || (normalizedType.contains("varchar") && !normalizedType.contains("max"));
        }
    }
}
