# Energy Efficient App

## Proposta do Projeto
O Energy Efficient App é um aplicativo desenvolvido para monitorar e analisar o consumo energético residencial. Ele utiliza sensores de medição para fornecer dados detalhados, como consumo, tensão e temperatura, permitindo que o usuário gerencie seu consumo de energia de forma mais eficiente e sustentável.

## Configuração do Projeto
### API Java
O aplicativo depende de uma API Java disponível no repositório https://github.com/oRonold/energy-efficient. Para configurá-la:
- Insira seus dados de conexão no arquivo de configuração `application.properties` ou `application.yml`.
- Exemplo de configuração:
  spring.datasource.url=jdbc:mysql://localhost:3306/seu_banco_de_dados
  spring.datasource.username=seu_usuario
  spring.datasource.password=sua_senha
- Execute a API localmente para que o app Android possa acessá-la em http://10.0.2.2:8080.

### Firebase
O Firebase é utilizado para autenticação e armazenamento de dados no aplicativo. Para configurá-lo:
- Baixe o arquivo google-services.json do Firebase Console e insira-o no diretório app/ do projeto Android.
- Funcionalidades implementadas:
  - Autenticação de usuários.
  - Armazenamento de informações adicionais no Firebase Realtime Database.

## Endpoints da API
GET /sensores  
Retorna uma lista de sensores cadastrados.  
Parâmetros:  
{ "page": 0, "size": 10 }

POST /sensores  
Cadastra um novo sensor.  
Exemplo de corpo:  
{ "nome": "Sensor de Teste", "proprietario": "Lucas" }

PUT /sensores/{id}  
Atualiza informações de um sensor.  
Exemplo de corpo:  
{ "nome": "Sensor Atualizado" }

GET /sensores/{id}/medidas  
Retorna as medidas de um sensor específico.  
Parâmetros:  
{ "page": 0, "size": 10, "sort": [] }

POST /sensores/{id}/medida  
Adiciona uma medida a um sensor.  
Exemplo de corpo:  
{ "idSensor": 1, "valorCorrente": 15, "valorTensao": 220, "valorTemperatura": 35 }

## Tecnologias Utilizadas
Frontend:  
- Android Studio  
- Kotlin  
- XML para layouts  

Backend:  
- Java com Spring Boot  
- Banco de Dados MySQL  
- Hibernate para ORM  

Outras tecnologias:  
- Firebase Authentication e Realtime Database  
- OkHttp3 para chamadas HTTP  
- RecyclerView para listagem de itens no app  

Extensões adicionadas:  
- Firebase Authentication SDK  
- Firebase Realtime Database SDK  
- Navigation Component para navegação entre telas  

## Telas do Aplicativo
- Home: Tela inicial com botão para login.
- Login: Tela de autenticação com opções de registrar-se e recuperar senha.
- Registro: Formulário para cadastro de novos usuários com validação de campos.
- Dashboard: Tela principal com acesso às opções de cadastro, visualização e edição de sensores.
- Visualizar Sensores: Lista os sensores cadastrados, exibindo informações detalhadas.
- Cadastrar Sensor: Permite o registro de novos sensores.
- Editar Sensor: Atualiza informações de um sensor e adiciona novas medidas.

## Membros do Projeto
- Lucas Serbato de Barros - RM 551821
- Ronald de Oliveira Farias - RM 552364
- Vitor Teixeira - RM 552228
- Phablo Isaias Silva Santos - RM 550687
- Gustavo Carvalho - RM 552466

## Nota
Certifique-se de que a API e os serviços do Firebase estejam configurados e em execução para que o aplicativo funcione corretamente.
