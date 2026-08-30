# 🧑‍💻 Avaliador de Perfil GitHub
 
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Status](https://img.shields.io/badge/status-em%20andamento-yellow?style=for-the-badge)
![License](https://img.shields.io/badge/licen%C3%A7a-MIT-blue?style=for-the-badge)
![Nível](https://img.shields.io/badge/n%C3%ADvel-intermedi%C3%A1rio-orange?style=for-the-badge)
 
Programa em Java que consulta a API pública do GitHub, busca os dados de um perfil e gera uma avaliação automática com recomendações para deixá-lo mais profissional.
 
---
 
## 📋 Descrição
 
O programa pergunta o **nome de usuário do GitHub**, faz uma requisição para a API pública (`https://api.github.com/users/{usuario}`) e extrai informações do perfil como nome, bio, seguidores, repositórios públicos e data de criação da conta.
 
Com base nesses dados, o programa gera uma **avaliação automática**, apontando pontos de atenção e recomendações — por exemplo, se a bio está vazia, se há poucos repositórios públicos ou poucos seguidores.
 
O processo se repete para quantos usuários você quiser consultar, até que seja digitado `sair`.
 
## 💻 Código
 
```java
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
```
 
## ▶️ Como executar
 
```bash
javac AvaliadorDePerfil.java
java AvaliadorDePerfil
```
 
Digite o nome de usuário do GitHub que deseja avaliar. Repita quantas vezes quiser e digite `sair` para encerrar o programa.
 
## 📤 Exemplo de saída
 
```
Digite o nome de usuario do GitHub (ou 'sair' para encerrar): torvalds
=== Perfil do GitHub ===
Usuario: torvalds
Nome: Linus Torvalds
Bio: null
Seguidores: 319156
Repositorios publicos: 12
Conta criada em: 2011-09-03T15:26:22Z
 
=== Avaliacao do Perfil ===
- Seu perfil nao tem uma bio preenchida. Escrever uma breve descricao sobre voce e suas areas de interesse ajuda recrutadores a te entenderem rapidamente.
- Voce tem uma boa quantidade de repositorios publicos (12).
 
Digite o nome de usuario do GitHub (ou 'sair' para encerrar): sair
```
 
## 🧠 Conceitos praticados
 
- Requisições HTTP em Java (`HttpClient`, `HttpRequest`, `HttpResponse`)
- Consumo de API REST pública (API do GitHub)
- Extração manual de campos de um JSON usando manipulação de `String` (`indexOf`, `substring`, `replace`)
- Métodos auxiliares reutilizáveis (`extrairCampo`, `avaliarPerfil`)
- Estrutura de repetição com condição de parada (`while (true)` + `break`)
- Conversão de texto para número (`Integer.parseInt`)
- Cadeia de decisão (`if / else if / else`) para gerar recomendações personalizadas
## 🚧 Status do projeto
 
Este projeto está **em desenvolvimento**. Próximos passos planejados:
 
- Buscar a lista de repositórios do usuário (endpoint `/users/{usuario}/repos`).
- Verificar se existe o README especial de perfil (repositório com o mesmo nome do usuário).
- Avaliar descrição dos repositórios e linguagens mais usadas.
- Expandir os critérios de recomendação com base nesses novos dados.
## 🚀 Possíveis melhorias
 
- Tratar erros de usuário inexistente ou limite de requisições da API do GitHub excedido.
- Migrar a extração de JSON para uma biblioteca dedicada (ex: Gson ou Jackson) usando Maven/Gradle.
- Adicionar uma pontuação geral (nota) para o perfil, além das recomendações textuais.
---
 
<p align="center">Feito com ☕ e Java</p>
 
