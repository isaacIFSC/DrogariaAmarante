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

                if (!produtoImagemPrecisaMigracao(metaData)) {
                    return;
                }

                try (Statement statement = connection.createStatement()) {
                    if (productName.contains("sql server")) {
                        statement.executeUpdate(
                            "ALTER TABLE produtos ALTER COLUMN imagem_url NVARCHAR(MAX) NOT NULL"
                        );
                        log.info("Coluna produtos.imagem_url atualizada para NVARCHAR(MAX).");
                    } else if (productName.contains("h2")) {
                        statement.executeUpdate(
                            "ALTER TABLE produtos ALTER COLUMN imagem_url CLOB NOT NULL"
                        );
                        log.info("Coluna produtos.imagem_url atualizada para CLOB.");
                    }
                }
            } catch (SQLException ex) {
                log.warn("Nao foi possivel ajustar a coluna produtos.imagem_url automaticamente.", ex);
            }
        };
    }

    private boolean produtoImagemPrecisaMigracao(DatabaseMetaData metaData) throws SQLException {
        try (ResultSet columns = metaData.getColumns(null, null, "produtos", "imagem_url")) {
            if (!columns.next()) {
                return false;
            }

            int columnSize = columns.getInt("COLUMN_SIZE");
            String typeName = columns.getString("TYPE_NAME");

            return columnSize > 0 && columnSize <= 600
                || (typeName != null && typeName.toLowerCase(Locale.ROOT).contains("varchar"));
        }
    }
}
