# Clientes Bancos

En esta práctica, desarrollaremos un servicio bancario donde registraremos tanto a los clientes como a sus respectivas tarjetas. Cada cliente podrá poseer ninguna, una o varias tarjetas, pero cada tarjeta deberá estar siempre vinculada a un cliente.

<!--Models-->

```mermaid
classDiagram
direction LR

    class BankCard {
         -String number;
         -Long clientId;
         -LocalDate expirationDate;
         -LocalDateTime createdAt;
         -LocalDateTime updatedAt;


        CRUD()
    }

    class Client {
        -Long id;
        -String name;
        -String username;
        -String email;
        -List<BankCard> cards;
        -LocalDateTime createdAt;
        -LocalDateTime updatedAt;

        CRUD()
        
    }
    Client --> BankCard

```



## Tecnologías Utilizadas

- **Java**
- **PostgreSQL**
- **SQLite**
- **Docker**
- **TestContainers**
- **Jackson**
- **Apache Commons CSV**
- **WebFlux**
- **Lombok**
- **SLF4J con Logback**
- **JUnit y Mockito**
- **Gradle**
- **Git**
- **Postman**
## Base de Datos

Este proyecto utiliza dos bases de datos de tipo relacional para gestionar la información de clientes y tarjetas de crédito:

### *PostgreSQL*

- **Descripción**: Base de datos relacional utilizada para almacenar la información de las tarjetas de crédito.
- **Contenedor Docker**: La base de datos se gestiona a través de un contenedor Docker, facilitando su despliegue y
  configuración.
- **Configuración**: Se debe definir la configuración de conexión en el archivo `application.properties`.

### *SQLite*

- **Descripción**: Base de datos ligera utilizada para almacenar la información de los clientes de forma local.
- **Uso**: Ideal para entornos de desarrollo y pruebas, permite almacenar datos sin necesidad de un servidor de base de
  datos completo.
- **Configuración**: La conexión a la base de datos SQLite también debe ser especificada en el
  archivo `application.properties`.

```yaml
# Servicios de almacenamiento de datos
# Desarrollo
services:
  # PostgreSQL
  postgres-db:
    container_name: cards-db_postgres
    image: postgres:12.0-alpine
    restart: always
    env_file: .env
    environment:
      POSTGRES_USER: ${DATABASE_USER}
      POSTGRES_PASSWORD: ${DATABASE_PASSWORD}
      POSTGRES_DB: ${POSTGRES_DATABASE}
    ports:
      - ${POSTGRES_PORT}:5432
    volumes:
      - ./database/cards/init.sql:/docker-entrypoint-initdb.d/init.sql
      - postgres-db-data:/var/lib/postgresql/data
    networks:
      - banco-network

  # App de banco
  banco-app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: banco-app
    env_file: .env
    networks:
      - banco-network
    depends_on:
      - postgres-db

networks:
  banco-network:
    driver: bridge

volumes:
  postgres-db-data:
```

## Importación y Exportación de Datos

El sistema permite la importación y exportación de datos en varios formatos para facilitar la gestión de la información
de clientes y tarjetas. A continuación se detallan los formatos y sus respectivas funcionalidades:

### Tarjetas

- **Importación**
    - **Formato**: CSV
    - **Descripción**: Permite importar datos de tarjetas de crédito desde un archivo CSV. Se validará la información de
      cada tarjeta antes de su almacenamiento en la base de datos PostgreSQL.

- **Exportación**
    - **Formato**: CSV
    - **Descripción**: Permite exportar los datos de las tarjetas de crédito en un archivo CSV, incluyendo todos los
      atributos relevantes como número, titular y fecha de caducidad.

### Clientes

- **Importación**
    - **Formato**: CSV
    - **Descripción**: Permite importar información del cliente desde un archivo CSV utilizando la API REST. Los datos
      serán validados para asegurar que cumplan con los requisitos establecidos.

- **Exportación**
    - **Formato**: CSV
    - **Descripción**: Permite exportar los datos del cliente a un archivo CSV, facilitando su uso en otras aplicaciones
      o sistemas.

<!--Storage-->

```mermaid
classDiagram
    class Storage ~T~   {
        <<interface>>
        +Flux~T~ importFile(File file)
        +Mono~Void~ exportFile(File file, List~T~ elemts)
    }

    class StorageClient~Tarjeta~ {
        - Logger
        +Flux~Client~ importFile(File file)
        +Mono~Void~ exportFile(File file, List~Client~ clients)
    }

    class StorageCard~Usuario~ {
        - Logger
        +Flux~Card~ importFile(File file)
        +Mono~Void~ exportFile(File file, List~card~ cards)
    }

    Storage ..|> StorageCard
     Storage ..|> StorageClient

```

## Repositorio

- **Patrón Repositorio**: La abstracción del repositorio facilita el manejo de datos entre la aplicación y las bases de datos (PostgreSQL y SQLite).
- **Interfaces Genéricas**: El repositorio sigue un enfoque genérico para manejar entidades como `Cliente` y `Tarjeta`.

### BankCard

```mermaid
classDiagram
    class Repository ~ID,T~ {
        CompletableFuture ~List ~T~~ findAll()
        CompletableFuture ~T~ findById(ID id)
        CompletableFuture ~T~ save(~T~ object)
        CompletableFuture ~T~ update(ID id, ~T~ object)
        CompletableFuture ~Boolean~ delete(ID id)

    }

    Repository ..|> BankCardRepository

    class BankCardRepository ~String, BankCard~  {
         <<interface>>
            CompletableFuture ~List~BankCard~~ getBankCardsByClientId(Long client)

    }

    BankCardRepository --> BankCardRepositoryImpl

    class BankCardRepositoryImpl ~String, BankCard~ {
        - Logger
        - RemoteDatabaseManager
        - BankCardRepositoryImpl

        CompletableFuture ~List ~BankCard~~ findAll()
        CompletableFuture ~BankCard~ findById(String id)
        CompletableFuture ~BankCard~ save(~BankCard~ object)
        CompletableFuture ~BankCard~ update(String id, ~BankCard~ object)
        CompletableFuture ~Boolean~ delete(String id)
        CompletableFuture ~List ~BankCard~~ getBankCardsByClientId(Long clientId)
    }

```

### Client

```mermaid
classDiagram
    class Repository ~ID,T~ {
        CompletableFuture ~List ~T~~ findAll()
        CompletableFuture ~T~ findById(ID id)
        CompletableFuture ~T~ save(~T~ object)
        CompletableFuture ~T~ update(ID id, ~T~ object)
        CompletableFuture ~Boolean~ delete(ID id)

    }

    Repository ..|> ClientRepository

    class ClientRepository ~Long, Client~ {
        CompletableFuture ~BankCard~ saveBankCard(~BankCard~ bankCard)
        CompletableFuture ~Void~ updateBankCard(String number, ~BankCard~ bankCard)
        CompletableFuture ~Void~ deleteBankCard(String number)
    }

    ClientRepository --> ClientRepositoryImpl

    class ClientRepositoryImpl {
        - Logger logger
        - static ImplClientRepository instance
        - LocalDatabaseManager localDatabase
        - ExecutorService executorService
        + CompletableFuture~List~ findAll()
        + CompletableFuture~Client~ findById(Long id)
        + CompletableFuture~Client~ save(Client client)
        + CompletableFuture~Client~ update(Long id, Client client)
        + CompletableFuture~Void~ delete(Long id)
        + List~BankCard~ findAllCardsByClientId(Long clientId)
        + CompletableFuture~BankCard~ saveBankCard(BankCard bankCard)
        + CompletableFuture~Void~ updateBankCard(String cardNumber, BankCard updatedBankCard)
        + static ImplClientRepository getInstance(LocalDatabaseManager local)
    }
    


```

### Remote

```mermaid
classDiagram
    class Repository ~ID,T~ {
        CompletableFuture ~List ~T~~ findAll()
        CompletableFuture ~T~ findById(ID id)
        CompletableFuture ~T~ save(~T~ object)
        CompletableFuture ~T~ update(ID id, ~T~ object)
        CompletableFuture ~Boolean~ delete(ID id)

    }

    Repository ..|> ClientRepository

    class ClientRepository ~Long, Client~ {
        CompletableFuture ~BankCard~ saveBankCard(~BankCard~ bankCard)
        CompletableFuture ~Void~ updateBankCard(String number, ~BankCard~ bankCard)
        CompletableFuture ~Void~ deleteBankCard(String number)
    }

    ClientRepository --> ClientRemoteRepository

    class ClientRemoteRepository {
        - ClientApiRest clientApiRest
        - Logger logger
        + ClientRemoteRepository(ClientApiRest clientApiRest)
        + List<Client> getAll()
        + Client getById(int id)
        + Client createClient(Client client)
        + Client updateClient(Client client)
        + void deleteClient(int id)
    }
    


```

## Caché

La caché implementada por `ClientesCacheImpl` se utiliza para almacenar y gestionar datos de clientes de forma eficiente.

```mermaid
classDiagram
    direction LR
    class Cache ~K, V~ {
         <<interface>>
        + void put(K key, V value)
        + V get(K key)
        + void remove(K key)
        + void clear()
        + void shutdown()
    }

    Cache ..|> ClientesCache

    class ClientesCache ~Long, Client~ {
    }

    ClientesCache --> ClientesCacheImpl

    class ClientesCacheImpl {
        - Logger logger
        - Map<Long, Client> cache
        - ScheduledExecutorService cleaner
        + ClientesCacheImpl(int maxSize)
        + void put(Long key, Client value)
        + Client get(Long key)
        + void remove(Long key)
        + void clear()
        + void shutdown()
    }


```



## Calidad y Pruebas

El proyecto implementa diversas prácticas y herramientas para asegurar la calidad del código y la correcta funcionalidad
de la aplicación. A continuación se describen los principales enfoques utilizados:

- **Pruebas Unitarias**:
    - Se realizan pruebas unitarias para verificar el funcionamiento de componentes individuales, asegurando que cada
      unidad de código opere como se espera.

- **Pruebas de Integración**:
    - Se llevan a cabo pruebas de integración para evaluar la interacción entre diferentes módulos y servicios de la
      aplicación, garantizando que funcionen correctamente en conjunto.

- **Mockito**:
    - Se utiliza Mockito para crear simulacros de objetos en las pruebas, facilitando la comprobación de interacciones y
      comportamientos sin depender de implementaciones concretas.

- **JUnit**:
    - Se emplea JUnit como framework de pruebas para estructurar y ejecutar las pruebas unitarias y de integración de
      manera eficiente.

- **TestContainers**:
    - Se utilizan contenedores de TestContainers para probar la aplicación en un entorno similar al de producción,
      permitiendo la creación y destrucción dinámica de bases de datos durante las pruebas.

- **Cobertura de Código**:
    - Se monitorea la cobertura de código para asegurar que un porcentaje adecuado del código esté cubierto por pruebas,
      identificando áreas que necesiten más atención.

- **Revisiones de Código**:
    - Se realizan revisiones de código para asegurar que el código cumpla con los estándares de calidad y las mejores
      prácticas antes de ser fusionado.

## Autores del programa

<table align="center">
  <tr>
    <td align="center">
      <a href="https://github.com/KevinSanchez5">
        <img src="https://avatars.githubusercontent.com/u/115721589?v=4" width="70" height="70" style="border-radius: 50%;" alt="Kelvin"/>
        <br/>
        <sub><b>Kelvin</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/christiangregorio">
        <img src="https://avatars.githubusercontent.com/u/146003155?v=4" width="70" height="70" style="border-radius: 50%;" alt="Christian"/>
        <br/>
        <sub><b>Christian</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/Diegonovi">
        <img src="https://avatars.githubusercontent.com/u/119690879?v=4" width="70" height="70" style="border-radius: 50%;" alt="Diego Novillo"/>
        <br/>
        <sub><b>Diego</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/Diokar017">
        <img src="https://avatars.githubusercontent.com/u/105505594?v=4" width="70" height="70" style="border-radius: 50%;" alt="Oscar Encabo Nieto"/>
        <br/>
        <sub><b>Oscar</b></sub>
      </a>
    </td>
    <td align="center">
      <a href="https://github.com/TomasVaquerin">
        <img src="https://avatars.githubusercontent.com/u/83606915" width="70" height="70" style="border-radius: 50%;" alt="Tomas Vaquerin"/>
        <br/>
        <sub><b>Tomas</b></sub>
      </a>
    </td>
  </tr>
</table>
