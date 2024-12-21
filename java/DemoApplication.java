package com.raymondweng.demo;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@SpringBootApplication
public class DemoApplication {

    public static JDA jda;

    public static void main(String[] args) {
        boolean databaseExists = new File("./database/data.db").exists();
        if (!databaseExists) {
            try {
                Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
                Statement statement = connection.createStatement();
                statement.executeUpdate("CREATE TABLE LINKS" +
                        "(KEY TEXT NOT NULL," +
                        "LINK TEXT NOT NULL)");
                statement.close();
                statement = connection.createStatement();
                statement.execute("CREATE UNIQUE INDEX key_index ON LINKS (KEY)");
                statement.close();
                connection.close();

                connection = DriverManager.getConnection("jdbc:sqlite:./database/keys.db");
                statement = connection.createStatement();
                statement.executeUpdate("CREATE TABLE KEYS" +
                        "(ID INTEGER PRIMARY KEY AUTOINCREMENT ," +
                        "KEY TEXT NOT NULL)");
                statement.close();
                statement = connection.createStatement();
                statement.execute("INSERT INTO KEYS (KEY) VALUES ('AAA')");
                statement.close();
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.print("Input the token: ");
        String token;
        try {
            token = new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new EventReceive())
                .build();

        SpringApplication.run(DemoApplication.class, args);
    }

}
