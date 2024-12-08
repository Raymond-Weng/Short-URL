package com.raymondweng.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.*;

@RestController
public class Controller {
    @GetMapping("/404")
    public String notFound() {
        return "hum, we didn't find that link.";
    }

    @GetMapping()
    public RedirectView home() {
        return new RedirectView("https://discord.gg/jGuGHr4f");
    }

    @GetMapping("/{id}")
    public RedirectView index(@PathVariable String id) {
        try {
            if (!id.matches("[a-zA-Z]+")) {
                return new RedirectView("/404");
            }
            boolean contain;
            Connection connection = DriverManager.getConnection("jdbc:sqlite:./database/data.db");
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) AS CNT FROM LINKS WHERE KEY = ?");
            ps.setString(1, id);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            rs.next();
            contain = rs.getInt("CNT") > 0;
            rs.close();
            ps.close();
            if (contain) {
                String link;
                ps = connection.prepareStatement("SELECT LINK FROM LINKS WHERE KEY = ?");
                ps.setString(1, id);
                ps.execute();
                rs = ps.getResultSet();
                rs.next();
                link = rs.getString("LINK");
                rs.close();
                ps.close();

                connection.close();
                return new RedirectView(link);
            } else {
                connection.close();
                return new RedirectView("/404");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
