package banco.data.storage;

import banco.domain.clients.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase para gestionar el almacenamiento de clientes en archivos CSV.
 * Proporciona métodos para importar y exportar clientes de manera reactiva.
 */
public class ClientStorageCsv {
    private final Logger logger = LoggerFactory.getLogger(ClientStorageCsv.class);

    /**
     * Importa los clientes desde un archivo CSV.
     *
     * @param file Archivo CSV desde el cual se importarán los clientes.
     * @return Un {@link Flux} que emite los clientes importados.
     */
    public Flux<Client> importClients(File file) {
        logger.debug("Importando clientes desde el archivo: {}", file.getAbsolutePath());
        return Flux.<Client>create(emitter -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                reader.lines()
                        .skip(1)
                        .forEach(line -> {
                            Client client = parseLine(List.of(line.split(",")));
                            emitter.next(client);
                        });
                emitter.complete();
            } catch (Exception e) {
                logger.error("Error al importar clientes", e);
                emitter.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Exporta los clientes a un archivo CSV.
     *
     * @param file Archivo CSV al cual se exportarán los clientes.
     * @param clients Lista de clientes a exportar.
     * @return Un {@link Mono} que representa la finalización de la operación de exportación.
     */
    public Mono<Void> exportClients(File file, List<Client> clients) {
        logger.debug("Exportando clientes al archivo: {}", file.getAbsolutePath());
        return Mono.create(emitter -> {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("id,name,username,email,createdAt,updatedAt\n");
                for (Client client : clients) {
                    writer.write(String.format("%d,%s,%s,%s,%s,%s\n",
                            client.getId(),
                            client.getName(),
                            client.getUsername(),
                            client.getEmail(),
                            client.getCreatedAt(),
                            client.getUpdatedAt()));
                }
                emitter.success();
            } catch (IOException e) {
                logger.error("Error al exportar clientes", e);
                emitter.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Convierte una línea de datos CSV en un objeto {@link Client}.
     *
     * @param parts Partes de la línea CSV separadas por comas.
     * @return Un objeto {@link Client}.
     */
    private Client parseLine(List<String> parts) {
        return Client.builder()
                .id(Long.parseLong(parts.get(0)))
                .name(parts.get(1))
                .username(parts.get(2))
                .email(parts.get(3))
                .createdAt(LocalDateTime.parse(parts.get(4)))
                .updatedAt(LocalDateTime.parse(parts.get(5)))
                .build();
    }
}