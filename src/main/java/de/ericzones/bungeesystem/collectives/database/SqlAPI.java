// Created by Eric B. 30.01.2021 23:09
package de.ericzones.bungeesystem.collectives.database;

import de.ericzones.bungeesystem.BungeeSystem;

import java.sql.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SqlAPI {

    final ExecutorService executorService = Executors.newCachedThreadPool();

    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    private final BungeeSystem instance;

    public SqlAPI(String host, int port, String database, String username, String password) {
        this.instance = BungeeSystem.getInstance();
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    private Connection connection;

    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+database+"?useJDBCCompliantTimezoneShift=true&&serverTimezone=UTC&&useUnicode=true&autoReconnect=true", username, password);
            System.out.println(instance.getPluginName()+" Connected via MySQL on "+database);
        } catch (Exception e) {
            System.err.println(e);
        }

        instance.getProxyServer().getScheduler().buildTask(instance, new Runnable() {
            @Override
            public void run() {
                getResult("SELECT 1");
            }
        }).repeat(60, TimeUnit.SECONDS).schedule();
    }

    public void disconnect() {
        try {
            if(connection != null) connection.close();
        } catch (SQLException e) {
            System.err.println(e);
        }
    }

    public void update(String sqlQuery) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sqlQuery);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            connect();
            e.printStackTrace();
        } finally {
            if(preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ResultSet getResult(String sqlQuery) {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sqlQuery);
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createTable(String tableName, List<String> objects, List<String> types) {
        String upload = "CREATE TABLE IF NOT EXISTS "+tableName+"("+objects.get(0)+" "+types.get(0);
        for(int i = 1; i < objects.size(); i++) upload = (upload + "," + objects.get(i) + " " + types.get(i));
        upload = upload + ");";
        update(upload);
    }

    public boolean lineExists(String tableName, String type, String set) {
        ResultSet resultSet = null;
        try {
            resultSet = getResult("SELECT * FROM "+tableName+" WHERE "+type+"= '"+set+"'");
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void createLine(String tableName, List<String> types, List<String> list) {
        String upload = "INSERT INTO "+tableName+"("+types.get(0);
        for(int i = 1; i < types.size(); i++) upload = upload+", "+types.get(i);
        upload = upload + ") VALUES ('"+list.get(0)+"'";
        for(int i = 1; i < list.size(); i++) upload = upload+", '"+list.get(i)+"'";
        upload = upload+");";
        update(upload);
    }

    public Connection getConnection() {
        return connection;
    }

    public String getFromTable(String tableName, String column, String value, String neededColumn) {
        String query = "SELECT * FROM "+tableName+" WHERE "+column+"= '"+value+"'";
        ResultSet resultSet = getResult(query);
        try {
            if(resultSet.next())
                return resultSet.getString(neededColumn);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public void setInt(BungeeSystem core, String database, String search, String getLine, int i, String line) {
        executorService.execute(() -> {
            update("UPDATE "+database+" SET "+line+"= '"+i+"' WHERE "+search+"= '"+getLine+"';");
        });
    }

    public void setString(BungeeSystem core, String database, String search, String getLine, String i, String line) {
        executorService.execute(() -> {
            update("UPDATE "+database+" SET "+line+"= '"+i+"' WHERE "+search+"= '"+getLine+"';");
        });
    }

    public Integer getSelectionAmount(String tableName) {
        int count = 0;
        ResultSet resultSet = getResult("SELECT * FROM "+tableName);
        try {
            while (resultSet.next())
                count++;
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

}
