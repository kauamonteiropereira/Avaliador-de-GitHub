import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
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
            HttpRequest requestRepos = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/users/" + usuario + "/repos"))
                .build();

                HttpResponse<String> responseRepos = client.send(requestRepos, HttpResponse.BodyHandlers.ofString());
                String jsonRepos = responseRepos.body();
            System.out.println("=== Perfil do GitHub ===");
            System.out.println("Usuario: " + usuario);
            System.out.println("Nome: " + nome);
            System.out.println("Bio: " + bio);
            System.out.println("Seguidores: " + seguidores);
            System.out.println("Repositorios publicos: " + repositorios);
            System.out.println("Conta criada em: " + criadoEm);
            boolean temReadme = temReadmeDePerfil(jsonRepos, usuario);
                    String linguagem = linguagemMaisUsada(jsonRepos);
                    avaliarPerfil(bio, seguidores, repositorios, criadoEm, temReadme, linguagem);
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

    public static void avaliarPerfil(String bio, String seguidores, String repositorios, String criadoEm, boolean temReadme, String linguagem) {
        System.out.println("\n=== Avaliacao do Perfil ===");

        int qtdRepositorios = Integer.parseInt(repositorios);
        int qtdSeguidores = Integer.parseInt(seguidores);

        if (bio.equals("null") || bio.isEmpty()) {
            System.out.println("- Seu perfil não tem uma bio preenchida. Escrever uma breve descrição sobre você e suas areas de interesse ajuda recrutadores a te entenderem rapidamente.");
        } else {
            System.out.println("- Você já possui uma bio preenchida. Bom trabalho!");
        }
        if (qtdRepositorios == 0) {
            System.out.println("- Você ainda não tem repositorios publicos. Considere subir pelo menos 2 ou 3 projetos para mostrar suas habilidades.");
        } else if (qtdRepositorios < 5) {
            System.out.println("- Você tem poucos repositorios publicos (" + qtdRepositorios + "). Aumentar essa quantidade fortalece seu portfolio.");
        } else {
            System.out.println("- Você tem uma boa quantidade de repositorios publicos (" + qtdRepositorios + ").");
        }
        if (qtdSeguidores < 10) {
            System.out.println("- Você tem poucos seguidores. Interagir com outros projetos (issues, pull requests) ajuda a crescer sua rede.");
        }
        if (temReadme) {
            System.out.println(" - Você já possui o README especial de perfil. Ótimo para causar uma boa primeira impressão!");
        }  else {
            System.out.println("- Voce ainda não tem o README de perfil (um repositorio com o mesmo nome do seu usuário). Crie uma apresentação personalizada na sua pagina do GitHub.");
        }
        if (!linguagem.equals("Nenhuma")) {
            System.out.println("- Sua linguagem mais utilizada e " + linguagem + ". Considere destacar isso na sua bio ou README de perfil.");
        }
    }   
    public static boolean temReadmeDePerfil(String jsonRepos, String usuario) {
        String busca = "\"name\":\"" + usuario + "\"";
        return jsonRepos.contains(busca);
    }
    public static String linguagemMaisUsada(String jsonRepos) {
            HashMap<String, Integer> contagem = new HashMap<>();
            String campo = "\"language\":";
            int pos = 0;

            while (jsonRepos.indexOf(campo, pos) != -1) {
                int inicio = jsonRepos.indexOf(campo, pos) + campo.length();
                int fim = jsonRepos.indexOf(",", inicio);

                String valor = jsonRepos.substring(inicio, fim).replace("\"", "").trim();

                if (!valor.equals("null")) {
                    contagem.put(valor, contagem.getOrDefault(valor, 0) + 1);
                }

                pos = fim;
            }

            String maisUsada = "Nenhuma";
            int maiorContagem = 0;

            for (String linguagem : contagem.keySet()) {
                if (contagem.get(linguagem) > maiorContagem) {
                    maiorContagem = contagem.get(linguagem);
                    maisUsada = linguagem;
                }
            }

            return maisUsada;
        }
    }