import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class AvaliadorDePerfil {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.print("\nDigite o nome de usuario do GitHub (ou 'sair' para encerrar): ");
            String usuario = sc.nextLine();

            if (usuario.equalsIgnoreCase("sair")) {
                break;
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/users/" + usuario))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String json = response.body();

            String nome = extrairCampo(json, "name");
            String bio = extrairCampo(json, "bio");
            String seguidores = extrairCampo(json, "followers");
            String repositorios = extrairCampo(json, "public_repos");
            String criadoEm = extrairCampo(json, "created_at");

            System.out.println("=== Perfil do GitHub ===");
            System.out.println("Usuario: " + usuario);
            System.out.println("Nome: " + nome);
            System.out.println("Bio: " + bio);
            System.out.println("Seguidores: " + seguidores);
            System.out.println("Repositorios publicos: " + repositorios);
            System.out.println("Conta criada em: " + criadoEm);
            avaliarPerfil(bio, seguidores, repositorios, criadoEm);
        }
    }

    public static String extrairCampo(String json, String campo) {
        String busca = "\"" + campo + "\":";
        int inicio = json.indexOf(busca) + busca.length();
        int fim = json.indexOf(",", inicio);

        String valor = json.substring(inicio, fim);
        valor = valor.replace("\"", "").trim();

        return valor;
    }

    public static void avaliarPerfil(String bio, String seguidores, String repositorios, String criadoEm) {
        System.out.println("\n=== Avaliacao do Perfil ===");

        int qtdRepositorios = Integer.parseInt(repositorios);
        int qtdSeguidores = Integer.parseInt(seguidores);

        if (bio.equals("null") || bio.isEmpty()) {
            System.out.println("- Seu perfil nao tem uma bio preenchida. Escrever uma breve descricao sobre voce e suas areas de interesse ajuda recrutadores a te entenderem rapidamente.");
        } else {
            System.out.println("- Voce ja possui uma bio preenchida. Bom trabalho!");
        }
        if (qtdRepositorios == 0) {
            System.out.println("- Voce ainda nao tem repositorios publicos. Considere subir pelo menos 2 ou 3 projetos para mostrar suas habilidades.");
        } else if (qtdRepositorios < 5) {
            System.out.println("- Voce tem poucos repositorios publicos (" + qtdRepositorios + "). Aumentar essa quantidade fortalece seu portfolio.");
        } else {
            System.out.println("- Voce tem uma boa quantidade de repositorios publicos (" + qtdRepositorios + ").");
        }

        if (qtdSeguidores < 10) {
            System.out.println("- Voce tem poucos seguidores. Interagir com outros projetos (issues, pull requests) ajuda a crescer sua rede.");
        }
    }
}