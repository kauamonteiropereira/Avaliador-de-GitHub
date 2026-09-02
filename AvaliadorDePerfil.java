import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
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

            // Requisicao do perfil
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/users/" + usuario))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            String json = response.body();

            String nome = extrairCampo(json, "name");
            String bio = extrairCampo(json, "bio");
            String seguidores = extrairCampo(json, "followers");
            String repositorios = extrairCampo(json, "public_repos");
            String criadoEm = extrairCampo(json, "created_at");

            HttpRequest requestRepos = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/users/" + usuario + "/repos"))
                    .build();

            HttpResponse<String> responseRepos = client.send(
                    requestRepos,
                    HttpResponse.BodyHandlers.ofString()
            );

            String jsonRepos = responseRepos.body();

            ArrayList<Repositorio> listaRepositorios =
                    extrairTodosRepositorios(jsonRepos);

            String categoriaPrincipal = analisarCategorias(usuario, listaRepositorios);

            System.out.println("\n=== Perfil do GitHub ===");
            System.out.println("Usuario: " + usuario);
            System.out.println("Nome: " + nome);
            System.out.println("Bio: " + bio);
            System.out.println("Seguidores: " + seguidores);
            System.out.println("Repositorios publicos: " + repositorios);
            System.out.println("Conta criada em: " + criadoEm);

            boolean temReadme = temReadmeDePerfil(jsonRepos, usuario);
            String linguagem = linguagemMaisUsada(jsonRepos);

            avaliarPerfil(
                    bio,
                    seguidores,
                    repositorios,
                    criadoEm,
                    temReadme,
                    linguagem
            );

            int pontuacaoGeral = calcularPontuacaoGeral(
                    bio,
                    Integer.parseInt(repositorios),
                    Integer.parseInt(seguidores),
                    temReadme,
                    linguagem,
                    categoriaPrincipal
            );

            System.out.println("\nPontuacao geral do perfil: " + pontuacaoGeral + "/110");

            String classificacao = classificarPerfil(pontuacaoGeral);
            System.out.println("Classificacao: " + classificacao);
        }

        sc.close();
    }

    public static String extrairCampo(String json, String campo) {
        String busca = "\"" + campo + "\":";
        int inicio = json.indexOf(busca) + busca.length();
        int fim = json.indexOf(",", inicio);

        String valor = json.substring(inicio, fim);
        valor = valor.replace("\"", "").trim();

        return valor;
    }

    public static void avaliarPerfil(
            String bio,
            String seguidores,
            String repositorios,
            String criadoEm,
            boolean temReadme,
            String linguagem) {

        System.out.println("\n=== Avaliacao do Perfil ===");

        int qtdRepositorios = Integer.parseInt(repositorios);
        int qtdSeguidores = Integer.parseInt(seguidores);

        if (bio.equals("null") || bio.isEmpty()) {
            System.out.println(
                    "- Seu perfil nao tem uma bio preenchida."
            );
        } else {
            System.out.println(
                    "- Voce ja possui uma bio preenchida. Bom trabalho!"
            );
        }

        if (qtdRepositorios == 0) {
            System.out.println(
                    "- Voce ainda nao tem repositorios publicos."
            );
        } else if (qtdRepositorios < 5) {
            System.out.println(
                    "- Voce tem poucos repositorios publicos ("
                            + qtdRepositorios + ")."
            );
        } else {
            System.out.println(
                    "- Voce tem uma boa quantidade de repositorios publicos ("
                            + qtdRepositorios + ")."
            );
        }

        if (qtdSeguidores < 10) {
            System.out.println(
                    "- Voce tem poucos seguidores."
            );
        }

        if (temReadme) {
            System.out.println(
                    "- Voce ja possui o README especial de perfil."
            );
        } else {
            System.out.println(
                    "- Voce ainda nao tem o README de perfil."
            );
        }

        if (!linguagem.equals("Nenhuma")) {
            System.out.println(
                    "- Sua linguagem mais utilizada e "
                            + linguagem + "."
            );
        }
    }

    public static boolean temReadmeDePerfil(
            String jsonRepos,
            String usuario) {

        String busca = "\"name\":\"" + usuario + "\"";

        return jsonRepos.contains(busca);
    }

    public static String linguagemMaisUsada(String jsonRepos) {

        HashMap<String, Integer> contagem = new HashMap<>();

        String campo = "\"language\":";
        int pos = 0;

        while (jsonRepos.indexOf(campo, pos) != -1) {

            int inicio =
                    jsonRepos.indexOf(campo, pos)
                            + campo.length();

            int fim = jsonRepos.indexOf(",", inicio);

            String valor =
                    jsonRepos.substring(inicio, fim)
                            .replace("\"", "")
                            .trim();

            if (!valor.equals("null")) {

                contagem.put(
                        valor,
                        contagem.getOrDefault(valor, 0) + 1
                );
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

    public static ArrayList<String> buscarArquivosDoRepositorio(
            String usuario,
            String nomeRepo) throws Exception {

        ArrayList<String> arquivos = new ArrayList<>();

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.github.com/repos/"
                                + usuario + "/" + nomeRepo + "/contents"
                ))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        String jsonConteudo = response.body();

        String campoNome = "\"name\":";
        int pos = 0;

        while (jsonConteudo.indexOf(campoNome, pos) != -1) {

            int inicio =
                    jsonConteudo.indexOf(campoNome, pos)
                            + campoNome.length();

            int fim = jsonConteudo.indexOf(",", inicio);

            String nomeArquivo =
                    jsonConteudo.substring(inicio, fim)
                            .replace("\"", "")
                            .trim();

            arquivos.add(nomeArquivo);

            pos = fim;
        }

        return arquivos;
    }

    public static String buscarConteudoReadme(
            String usuario,
            String nomeRepo) throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "https://api.github.com/repos/"
                                + usuario + "/" + nomeRepo + "/readme"
                ))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        String json = response.body();

        if (json.contains("\"message\":\"API rate limit exceeded")) {
            return "RATE_LIMIT_EXCEEDED";
        }

        if (json.contains("\"message\":\"Not Found\"")) {
            return "";
        }

        String campoContent = "\"content\":";
        int inicio = json.indexOf(campoContent);

        if (inicio == -1) {
            return "";
        }

        inicio += campoContent.length();
        int fim = json.indexOf("\"encoding\"", inicio);

        String base64 = json.substring(inicio, fim)
                .replace("\"", "")
                .replace("\\n", "")
                .replace(",", "")
                .trim();

        byte[] bytesDecodificados =
                java.util.Base64.getDecoder().decode(base64);

        String conteudo = new String(bytesDecodificados);

        return conteudo;
    }

    public static ArrayList<Repositorio> extrairTodosRepositorios(
            String jsonRepos) {

        ArrayList<Repositorio> repositorios = new ArrayList<>();

        String campoNome = "\"full_name\":";
        String campoDescricao = "\"description\":";

        int pos = 0;

        while (jsonRepos.indexOf(campoNome, pos) != -1) {

            int inicioNome =
                    jsonRepos.indexOf(campoNome, pos)
                            + campoNome.length();

            int fimNome =
                    jsonRepos.indexOf(",", inicioNome);

            String nomeCompleto =
                    jsonRepos.substring(inicioNome, fimNome)
                            .replace("\"", "")
                            .trim();

            String nome =
                    nomeCompleto.substring(nomeCompleto.indexOf("/") + 1);

            int inicioDescricao =
                    jsonRepos.indexOf(campoDescricao, fimNome);

            String descricao = "null";

            if (inicioDescricao != -1) {

                inicioDescricao += campoDescricao.length();

                int fimDescricao =
                        jsonRepos.indexOf(",", inicioDescricao);

                descricao =
                        jsonRepos.substring(
                                inicioDescricao,
                                fimDescricao
                        )
                        .replace("\"", "")
                        .trim();
            }

            Repositorio repositorio = new Repositorio(nome, descricao);

            repositorios.add(repositorio);

            pos = fimNome;
        }

        return repositorios;
    }

    public static String analisarCategorias(
            String usuario,
            ArrayList<Repositorio> repositorios) throws Exception {

        int calculadoras = 0;
        int jogos = 0;
        int ia = 0;
        int sistemas = 0;

        for (Repositorio repositorio : repositorios) {

            String nome = repositorio.nome;
            String descricao = repositorio.descricao;

            ArrayList<String> arquivos =
                    buscarArquivosDoRepositorio(usuario, nome);

            String readme =
                    buscarConteudoReadme(usuario, nome);

            if (readme.equals("RATE_LIMIT_EXCEEDED")) {

                System.out.println("\nAtencao: limite de requisicoes da API do GitHub foi atingido. Tente novamente mais tarde.");
                return "Nenhuma";
            }

            String nomesDosArquivos = String.join(" ", arquivos);

            String texto =
                    (nome + " " + descricao + " "
                            + nomesDosArquivos + " " + readme)
                            .toLowerCase();

            if (texto.contains("calculadora")
                    || texto.contains("calculator")) {

                calculadoras++;
            }

            if (texto.contains("jogo")
                    || texto.contains("game")) {

                jogos++;
            }

            if (texto.contains("ia")
                    || texto.contains("inteligencia")
                    || texto.contains("inteligência")
                    || texto.contains("artificial intelligence")) {

                ia++;
            }

            if (texto.contains("sistema")
                    || texto.contains("system")) {

                sistemas++;
            }
        }

        System.out.println("\n=== Categorias dos projetos ===");

        System.out.println("Calculadoras: " + calculadoras);
        System.out.println("Jogos: " + jogos);
        System.out.println("IA: " + ia);
        System.out.println("Sistemas: " + sistemas);

        String categoriaPrincipal = "Nenhuma";
        int maiorQuantidade = 0;

        if (calculadoras > maiorQuantidade) {
            maiorQuantidade = calculadoras;
            categoriaPrincipal = "Calculadoras";
        }

        if (jogos > maiorQuantidade) {
            maiorQuantidade = jogos;
            categoriaPrincipal = "Jogos";
        }

        if (ia > maiorQuantidade) {
            maiorQuantidade = ia;
            categoriaPrincipal = "IA";
        }

        if (sistemas > maiorQuantidade) {
            maiorQuantidade = sistemas;
            categoriaPrincipal = "Sistemas";
        }

        if (maiorQuantidade > 0) {
            System.out.println(
                    "\nCategoria principal: "
                            + categoriaPrincipal
            );

            System.out.println(
                    "Quantidade de projetos: "
                            + maiorQuantidade
            );
        } else {
            System.out.println(
                    "\nNao foi possivel identificar uma categoria principal."
            );
        }
        return categoriaPrincipal;
    }

    public static int calcularPontuacaoGeral(
            String bio,
            int qtdRepositorios,
            int qtdSeguidores,
            boolean temReadme,
            String linguagem,
            String categoriaPrincipal) {

        int pontuacao = 0;

        if (!bio.equals("null") && !bio.isEmpty()) {
            pontuacao += 10;
        }

        if (qtdRepositorios > 0) {
            int pontosRepositorios = qtdRepositorios * 2;

            if (pontosRepositorios > 40) {
                pontosRepositorios = 40;
            }

            pontuacao += pontosRepositorios;
        }

        if (qtdSeguidores > 0) {
            int pontosSeguidores = qtdSeguidores;

            if (pontosSeguidores > 30) {
                pontosSeguidores = 30;
            }

            pontuacao += pontosSeguidores;
        }

        if (temReadme) {
            pontuacao += 15;
        }

        if (!linguagem.equals("Nenhuma")) {
            pontuacao += 5;
        }

        if (!categoriaPrincipal.equals("Nenhuma")) {
            pontuacao += 10;
        }

        return pontuacao;
    }

    public static String classificarPerfil(int pontuacao) {
        String classificacao;

        if (pontuacao < 30) {
            classificacao = "Perfil iniciante";

        } else if (pontuacao < 70) {
            classificacao = "Perfil intermediario";

        } else {
            classificacao = "Perfil Avancado";

        }

        return classificacao;
    }
}

class Repositorio {

    String nome;
    String descricao;

    public Repositorio(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
}