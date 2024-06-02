package br.unipar.Main;

import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String url = "jdbc:postgresql://localhost:5432/Exemplo1";
    private static final String user = "postgres";
    private static final String password = "admin123";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        criarTabelaUsuario();

        while (true) {
            System.out.println();
            System.out.println("Menu de Usuário");
            System.out.println("1 - Inserir");
            System.out.println("2 - Alterar");
            System.out.println("3 - Listar");
            System.out.println("4 - Excluir");
            System.out.println("5 - Sair");
            System.out.print("Digite sua opção: ");
            int escolha = scanner.nextInt();

            switch (escolha) {
                case 1:
                    System.out.println();
                    inserirUsuario(scanner);
                    System.out.println();
                    break;
                case 2:
                    System.out.println();
                    atualizarUsuario(scanner);
                    System.out.println();
                    break;
                case 3:
                    System.out.println();
                    listarTodosUsuarios();
                    System.out.println();
                    break;
                case 4:
                    System.out.println();
                    excluirUsuario(scanner);
                    System.out.println();
                    break;
                case 5:
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    public static Connection connection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static void criarTabelaUsuario() {
        try (Connection conn = connection();
             Statement statement = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS usuarios ("
                    + "codigo SERIAL PRIMARY KEY,"
                    + "username VARCHAR(50) NOT NULL UNIQUE,"
                    + "password VARCHAR(300) NOT NULL,"
                    + "nome VARCHAR(50) NOT NULL,"
                    + "nascimento DATE"
                    + ");";
            statement.executeUpdate(sql);
            System.out.println("Tabela de usuários criada com sucesso!");
        } catch (SQLException exception) {
            System.out.println("Erro ao criar tabela de usuários.");
            exception.printStackTrace();
        }
    }

    public static void inserirUsuario(Scanner scanner) {
        scanner.nextLine();
        System.out.print("Digite seu usuário: ");
        String usuario = scanner.nextLine();
        System.out.print("Digite sua senha: ");
        String senha = scanner.nextLine();
        System.out.print("Digite seu nome: ");
        String nome = scanner.nextLine();
        System.out.print("Digite sua data de nascimento (AAAA-MM-DD): ");
        String dataNascimento = scanner.nextLine();

        if (!dataNascimento.matches("\\d{4}-\\d{2}-\\d{2}")) {
            System.out.println("Formato de data inválido. Use o formato AAAA-MM-DD.");
            return;
        }

        try (Connection conn = connection()) {
            if (usuarioExiste(conn, usuario)) {
                System.out.println("Usuário já existe.");
                return;
            }

            try (PreparedStatement preparedStatement = conn.prepareStatement(
                    "INSERT INTO usuarios (username, password, nome, nascimento) VALUES (?, ?, ?, ?)")) {
                preparedStatement.setString(1, usuario);
                preparedStatement.setString(2, senha);
                preparedStatement.setString(3, nome);
                preparedStatement.setDate(4, Date.valueOf(dataNascimento));
                preparedStatement.executeUpdate();
                System.out.println("Usuário inserido com sucesso!");
            } catch (SQLException e) {
                System.out.println("Erro ao inserir usuário.");
                e.printStackTrace();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao banco de dados.");
            e.printStackTrace();
        }
    }

    private static boolean usuarioExiste(Connection conn, String usuario) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(
                "SELECT * FROM usuarios WHERE username = ?")) {
            preparedStatement.setString(1, usuario);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao verificar se o usuário existe.");
            e.printStackTrace();
            return false;
        }
    }

    public static void atualizarUsuario(Scanner scanner) {
        System.out.print("Digite o código do usuário que deseja atualizar: ");
        int codigo = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Digite o novo usuário: ");
        String novoUsuario = scanner.nextLine();
        System.out.print("Digite a nova senha: ");
        String novaSenha = scanner.nextLine();
        System.out.print("Digite o novo nome: ");
        String novoNome = scanner.nextLine();
        System.out.print("Digite a nova data de nascimento (AAAA-MM-DD): ");
        String novaDataNascimento = scanner.nextLine();

        try (Connection conn = connection();
             PreparedStatement preparedStatement = conn.prepareStatement(
                     "UPDATE usuarios SET username = ?, password = ?, nome = ?, nascimento = ? WHERE codigo = ?")) {
            preparedStatement.setString(1, novoUsuario);
            preparedStatement.setString(2, novaSenha);
            preparedStatement.setString(3, novoNome);
            preparedStatement.setDate(4, Date.valueOf(novaDataNascimento));
            preparedStatement.setInt(5, codigo);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Usuário atualizado com sucesso!");
            } else {
                System.out.println("Nenhum usuário foi encontrado com o código especificado.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar usuário.");
            e.printStackTrace();
        }
    }

    public static void listarTodosUsuarios() {
        try (Connection conn = connection();
             Statement statement = conn.createStatement();
             ResultSet result = statement.executeQuery("SELECT * FROM usuarios")) {
            while (result.next()) {
                System.out.println("===========================");
                System.out.println("Código do Usuário: " + result.getInt("codigo"));
                System.out.println("Usuário: " + result.getString("username"));
                System.out.println("Nome: " + result.getString("nome"));
                System.out.println("Nascimento: " + result.getDate("nascimento"));
                System.out.println("===========================");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar usuários.");
            e.printStackTrace();
        }
    }

    public static void excluirUsuario(Scanner scanner) {
        System.out.print("Digite o código do usuário que deseja excluir: ");
        int codigo = scanner.nextInt();

        try (Connection conn = connection();
             PreparedStatement preparedStatement = conn.prepareStatement(
                     "DELETE FROM usuarios WHERE codigo = ?")) {
            preparedStatement.setInt(1, codigo);
            preparedStatement.executeUpdate();
            System.out.println("Usuário excluído com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao excluir usuário.");
            e.printStackTrace();
        }
    }
}



