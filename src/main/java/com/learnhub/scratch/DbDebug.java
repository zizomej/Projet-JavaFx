package com.learnhub.scratch;

import com.learnhub.util.DatabaseConnection;
import java.sql.*;

public class DbDebug {
    public static void main(String[] args) throws Exception {
        Connection conn = DatabaseConnection.getInstance();

        System.out.println("--- Columns of table: candidature ---");
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet cols = meta.getColumns(null, null, "candidature", "%");
        while (cols.next()) {
            System.out.println(cols.getString("COLUMN_NAME") + " (" + cols.getString("TYPE_NAME") + ")");
        }
    }
}
