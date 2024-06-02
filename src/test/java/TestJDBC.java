import br.unipar.Main.Main;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestJDBC {
    private static final String url = "jdbc:postgresql://localhost:5432/Exemplo1";
    private static final String user = "postgres";
    private static final String password = "admin123";

    @BeforeEach
    public void setUp() {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("DROP TABLE IF EXISTS usuarios");
                Main.criarTabelaUsuario();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInserirUsuario() {
        String userInput = "testeUser\ntesteSenha\ntesteNome\n2000-01-01\n";
        simulateUserInput(userInput);
        Main.inserirUsuario(new Scanner(System.in));

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM usuarios WHERE username = 'testeUser'")) {

            Assertions.assertTrue(rs.next(), "Usuário não foi inserido corretamente no banco de dados.");
            Assertions.assertEquals("testeUser", rs.getString("username"));
            Assertions.assertEquals("testeNome", rs.getString("nome"));

        } catch (SQLException e) {
            Assertions.fail("Exceção ao testar inserirUsuario: " + e.getMessage());
        }
    }

    @Test
    public void testAtualizarUsuario() {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO usuarios (username, password, nome, nascimento) VALUES (?, ?, ?, ?)")) {

            preparedStatement.setString(1, "userToUpdate");
            preparedStatement.setString(2, "senha");
            preparedStatement.setString(3, "nomeAntigo");
            preparedStatement.setDate(4, Date.valueOf("1990-01-01"));
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String userInput = "userToUpdate\nnovaSenha\nnovoNome\n2000-01-01\n";
        simulateUserInput(userInput);
        Main.atualizarUsuario(new Scanner(System.in));

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM usuarios WHERE username = 'userToUpdate'")) {

            Assertions.assertTrue(rs.next(), "Usuário não foi atualizado corretamente no banco de dados.");
            Assertions.assertEquals("novaSenha", rs.getString("password"));
            Assertions.assertEquals("novoNome", rs.getString("nome"));

        } catch (SQLException e) {
            Assertions.fail("Exceção ao testar atualizarUsuario: " + e.getMessage());
        }
    }

    @Test
    public void testListarTodosUsuarios() {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement statement = conn.createStatement()) {

            statement.executeUpdate("INSERT INTO usuarios (username, password, nome, nascimento) VALUES ('user1', 'senha1', 'Nome1', '1990-01-01')");
            statement.executeUpdate("INSERT INTO usuarios (username, password, nome, nascimento) VALUES ('user2', 'senha2', 'Nome2', '1990-01-02')");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        Main.listarTodosUsuarios();
        String expectedOutput = "===========================\nCódigo do Usuário: 1\nUsuário: user1\nNome: Nome1\nNascimento: 1990-01-01\n===========================\n===========================\nCódigo do Usuário: 2\nUsuário: user2\nNome: Nome2\nNascimento: 1990-01-02\n===========================\n";
        Assertions.assertEquals(expectedOutput, outContent.toString(), "A listagem de usuários está incorreta.");
    }

    @Test
    public void testExcluirUsuario() {
        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = conn.prepareStatement("INSERT INTO usuarios (username, password, nome, nascimento) VALUES (?, ?, ?, ?)")) {

            preparedStatement.setString(1, "userToDelete");
            preparedStatement.setString(2, "senha");
            preparedStatement.setString(3, "Nome");
            preparedStatement.setDate(4, Date.valueOf("1990-01-01"));
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String userInput = "1\n";
        simulateUserInput(userInput);
        Main.excluirUsuario(new Scanner(System.in));

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM usuarios")) {

            Assertions.assertFalse(rs.next(), "Usuário não foi excluído corretamente do banco de dados.");

        } catch (SQLException e) {
            Assertions.fail("Exceção ao testar excluirUsuario: " + e.getMessage());
        }
    }

    private void simulateUserInput(String input) {
        InputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
    }
}