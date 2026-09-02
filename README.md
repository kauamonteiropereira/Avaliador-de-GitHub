# 🧑‍💻 Avaliador de Perfil GitHub

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/status-conclu%C3%ADdo-brightgreen?style=for-the-badge)
![License](https://img.shields.io/badge/licen%C3%A7a-MIT-blue?style=for-the-badge)
![Nível](https://img.shields.io/badge/n%C3%ADvel-avan%C3%A7ado-red?style=for-the-badge)

Programa em Java que consulta a API pública do GitHub, busca os dados de um perfil e de seus repositórios (incluindo arquivos e README de cada um), e gera uma avaliação automática completa, com pontuação geral e classificação do perfil.

---

## 📋 Descrição

O programa pergunta o **nome de usuário do GitHub** e faz várias requisições à API pública:

- `https://api.github.com/users/{usuario}` — dados gerais do perfil (nome, bio, seguidores, repositórios públicos, data de criação da conta)
- `https://api.github.com/users/{usuario}/repos` — lista completa dos repositórios do usuário
- `https://api.github.com/repos/{usuario}/{repo}/contents` — arquivos da raiz de cada repositório
- `https://api.github.com/repos/{usuario}/{repo}/readme` — conteúdo do README de cada repositório (decodificado de Base64)

Com base nesses dados, o programa gera:

- Uma **avaliação automática** do perfil (bio, quantidade de repositórios, seguidores, README de perfil, linguagem mais usada)
- A **categoria principal** dos projetos (Calculadoras, Jogos, IA ou Sistemas), analisando nome, descrição, arquivos e README de cada repositório
- Uma **pontuação geral** (0 a 110 pontos), somando todos os critérios avaliados
- Uma **classificação final** do perfil: Iniciante, Intermediário ou Avançado

O processo se repete para quantos usuários você quiser consultar, até que seja digitado `sair`.

## 🏆 Critérios de pontuação

| Critério                          | Pontos máximos |
|------------------------------------|----------------|
| Bio preenchida                     | 10             |
| Repositórios públicos (2 pts cada) | até 40         |
| Seguidores (1 pt cada)             | até 30         |
| README de perfil especial          | 15             |
| Linguagem mais usada identificada  | 5              |
| Categoria principal identificada   | 10             |
| **Total**                          | **110**        |

**Faixas de classificação:** 0–29 Iniciante · 30–69 Intermediário · 70–110 Avançado

## 💻 Código

```java
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

            HttpRequest request = criarRequisicao(
                    "https://api.github.com/users/" + usuario
            );

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            String json = response.body();

            if (json.contains("\"message\":\"Not Found\"")) {
                System.out.println("\nUsuario nao encontrado no GitHub. Verifique o nome digitado.");
                continue;
            }

            if (json.contains("\"message\":\"API rate limit exceeded")) {
                System.out.println("\nAtencao: limite de requisicoes da API do GitHub foi atingido. Tente novamente mais tarde.");
                continue;
            }

            String nome = extrairCampo(json, "name");
            String bio = extrairCampo(json, "bio");
            String seguidores = extrairCampo(json, "followers");
            String repositorios = extrairCampo(json, "public_repos");
            String criadoEm = extrairCampo(json, "created_at");

            HttpRequest requestRepos = criarRequisicao(
                    "https://api.github.com/users/" + usuario + "/repos"
            );

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

    public static HttpRequest criarRequisicao(String url) {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url));

        String token = System.getenv("GITHUB_TOKEN");

        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return builder.build();
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

        HttpRequest request = criarRequisicao(
                "https://api.github.com/repos/"
                        + usuario + "/" + nomeRepo + "/contents"
        );

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

        HttpRequest request = criarRequisicao(
                "https://api.github.com/repos/"
                        + usuario + "/" + nomeRepo + "/readme"
        );

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
```

## ▶️ Como executar

```bash
javac AvaliadorDePerfil.java
java AvaliadorDePerfil
```

Digite o nome de usuário do GitHub que deseja avaliar. Repita quantas vezes quiser e digite `sair` para encerrar o programa.

### Autenticação com token (recomendado)

A API do GitHub limita requisições não autenticadas a **60 por hora**. Como o programa faz várias chamadas por usuário analisado (perfil, repositórios, arquivos e README de cada repositório), esse limite pode ser atingido rapidamente. Para subir o limite para **5.000 requisições/hora**, gere um token pessoal em [github.com/settings/tokens](https://github.com/settings/tokens) (sem precisar marcar nenhuma permissão, para dados públicos) e defina a variável de ambiente antes de rodar:

```bash
export GITHUB_TOKEN=seu_token_aqui
java AvaliadorDePerfil
```

## 📤 Exemplo de saída

```
Digite o nome de usuario do GitHub (ou 'sair' para encerrar): kauamonteiropereira

=== Categorias dos projetos ===
Calculadoras: 1
Jogos: 1
IA: 12
Sistemas: 8

Categoria principal: IA
Quantidade de projetos: 12

=== Perfil do GitHub ===
Usuario: kauamonteiropereira
Nome: Kauã Monteiro Pereira
Bio: Dev Linux / Java
Seguidores: 0
Repositorios publicos: 12
Conta criada em: 2026-08-20T20:26:57Z

=== Avaliacao do Perfil ===
- Voce ja possui uma bio preenchida. Bom trabalho!
- Voce tem uma boa quantidade de repositorios publicos (12).
- Voce tem poucos seguidores.
- Voce ja possui o README especial de perfil.
- Sua linguagem mais utilizada e Java.

Pontuacao geral do perfil: 64/110
Classificacao: Perfil intermediario

Digite o nome de usuario do GitHub (ou 'sair' para encerrar): sair
```

## 🧠 Conceitos praticados

- Requisições HTTP em Java (`HttpClient`, `HttpRequest`, `HttpResponse`), incluindo cabeçalhos de autenticação (`Authorization: Bearer`)
- Consumo de múltiplos endpoints de uma API REST pública (API do GitHub), incluindo endpoints aninhados (arquivos e README por repositório)
- Extração manual de campos de um JSON usando manipulação de `String` (`indexOf`, `substring`, `replace`)
- Percorrer múltiplas ocorrências de um mesmo campo dentro de um array JSON (paginação manual de posição com `indexOf(texto, posicao)`)
- Decodificação de conteúdo Base64 (`java.util.Base64`) para ler o conteúdo real de um README
- `HashMap<String, Integer>` para contar ocorrências (linguagem mais usada, categorias dos projetos)
- Classe auxiliar (`Repositorio`) para organizar dados relacionados
- Leitura de variáveis de ambiente (`System.getenv`) para configuração opcional (token de autenticação)
- Tratamento de erros da API (usuário não encontrado, limite de requisições excedido) sem quebrar o programa
- Métodos auxiliares reutilizáveis com múltiplos parâmetros e retornos
- Estrutura de repetição com condição de parada (`while (true)` + `break`/`continue`)
- Cadeia de decisão (`if / else if / else`) para gerar recomendações e classificações personalizadas

## 🚧 Status do projeto

Este projeto está **concluído**. Principais funcionalidades implementadas ao longo do desenvolvimento:

- Avaliação textual do perfil (bio, repositórios, seguidores, README de perfil, linguagem mais usada)
- Categorização dos projetos por nome, descrição, arquivos da raiz e conteúdo do README
- Pontuação geral (0–110) e classificação (Iniciante / Intermediário / Avançado)
- Autenticação opcional via token pessoal do GitHub, para evitar o limite de requisições
- Tratamento de erros da API (usuário inexistente, rate limit) sem crashes

## 🚀 Possíveis melhorias futuras

- Analisar subpastas dos repositórios, não só a raiz (atualmente limitado para economizar requisições)
- Migrar a extração de JSON para uma biblioteca dedicada (ex: Gson ou Jackson) usando Maven/Gradle
- Interface gráfica simples em vez de terminal
- Testes automatizados para os métodos de extração e pontuação

---

<p align="center">Feito com ☕ e Java</p>
