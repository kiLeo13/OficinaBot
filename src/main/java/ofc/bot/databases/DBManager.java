package ofc.bot.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ofc.bot.internal.data.BotFiles;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class DBManager {
    private static final HikariConfig config;
    private static final HikariDataSource dataSource;
    
    public static DSLContext getContext() {
        return DSL.using(dataSource, SQLDialect.SQLITE);
    }

    public static DSLContext getContext(Configuration cfg) {
        return DSL.using(cfg);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static int[] executeBatch(List<? extends Query> queries) {
        return executeBatch(getContext().batch(queries));
    }

    public static int[] executeBatch(Batch batch) {

        try (Connection conn = dataSource.getConnection()) {

            conn.setAutoCommit(false);

            return batch.execute();

        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    static {
        config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + BotFiles.DATABASE);

        dataSource = new HikariDataSource(config);
    }
}