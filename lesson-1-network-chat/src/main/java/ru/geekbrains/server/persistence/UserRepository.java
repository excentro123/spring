package ru.geekbrains.server.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.geekbrains.server.User;

@Repository
public class UserRepository {

  private final Connection conn;

  @Autowired
  public UserRepository(DataSource dataSource) throws SQLException {
    this(dataSource.getConnection());
  }

  public UserRepository(Connection conn) throws SQLException {
    this.conn = conn;
    createTableIfNotExists(conn);
  }

  private void createTableIfNotExists(Connection conn) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.execute("create table if not exists users (\n" + "\tid int auto_increment primary key,\n"
          + "    login varchar(25),\n" + "    password varchar(25),\n"
          + "    unique index uq_login(login)\n" + ");");
    }
  }

  public void insert(User user) throws SQLException {
    try (PreparedStatement stmt =
        conn.prepareStatement("insert into users(login, password) values (?, ?);")) {
      stmt.setString(1, user.getLogin());
      stmt.setString(2, user.getPassword());
      stmt.execute();
    }
  }

  public User findByLogin(String login) throws SQLException {
    try (PreparedStatement stmt =
        conn.prepareStatement("select id, login, password from users where login = ?")) {
      stmt.setString(1, login);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return new User(rs.getInt(1), rs.getString(2), rs.getString(3));
        }
      }
    }
    return new User(-1, "", "");
  }

  public List<User> getAllUsers() throws SQLException {
    List<User> res = new ArrayList<>();
    try (Statement stmt = conn.createStatement()) {
      try (ResultSet rs = stmt.executeQuery("select id, login, password from users")) {
        while (rs.next()) {
          res.add(new User(rs.getInt(1), rs.getString(2), rs.getString(3)));
        }
      }
    }
    return res;
  }
}
