package ofc.bot.handlers;

import ofc.bot.domain.sqlite.DB;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class ConsoleQueryHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleQueryHandler.class);
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final DSLContext CTX = DB.getContext();

    public static void init() {

        try {
            EXECUTOR.execute(ConsoleQueryHandler::run);
            LOGGER.info("Successfully initialized ConsoleQueryHandler thread");
        } catch (RejectedExecutionException e) {
            LOGGER.error("Executor rejected the ConsoleQueryHandler operation", e);
        }
    }

    private static void run() {

        while (true) {

            String sql = SCANNER.nextLine();

            if (sql == null || sql.isBlank()) {
                LOGGER.warn("SQL queries may not be empty");
                continue;
            }

            executeAndPrettyPrint(sql);
        }
    }

    private static void executeAndPrettyPrint(String inputSQL) {

        String upperSQL = inputSQL.toUpperCase();

        if (upperSQL.contains("DELETE") && !upperSQL.contains("WHERE")) {
            LOGGER.warn("The console command sender is not allwed to DELETE without a WHERE condition");
            return;
        }

        if (upperSQL.contains("DROP") && !upperSQL.startsWith("!")) {
            LOGGER.warn("The console command sender is not allowed to perform DROP operations without a validation");
            return;
        }

        try {

            String SQL = inputSQL.startsWith("!")
                    ? inputSQL.substring(1)
                    : inputSQL;

            if (SQL.trim().startsWith("?")) {

                Result<?> result = CTX.fetch(SQL.substring(1));
                LOGGER.info("Fetch Output:\n{}", result.format());
            } else {

                int affectedRows = CTX.execute(SQL);
                LOGGER.info("Operation successful, {} rows affected.", affectedRows);
            }

        } catch (Exception e) {
            LOGGER.error("Could not execute SQL command because: {}", e.getMessage());
        }
    }
}