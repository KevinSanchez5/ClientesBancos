package banco.data.storage;

import banco.domain.cards.model.BankCard;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Clase para gestionar el almacenamiento de tarjetas de crédito en archivos CSV.
 * Proporciona métodos para importar y exportar tarjetas de crédito de manera reactiva.
 */
public class BankCardStorageCsv {
    private final Logger logger = LoggerFactory.getLogger(BankCardStorageCsv.class);
    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");


    /**
     * Importa las tarjetas de crédito desde un archivo CSV.
     *
     * @param file Archivo CSV desde el cual se importarán las tarjetas de crédito.
     * @return Un {@link Flux} que emite las tarjetas de crédito importadas.
     */
    public Flux<BankCard> importBankCards(File file) {
        logger.debug("Importando tarjetas de crédito desde el archivo: {}", file.getAbsolutePath());
        return Flux.<BankCard>create(emitter -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                reader.lines()
                        .skip(1)
                        .forEach(line -> {
                            logger.debug("Línea leída: {}", line);
                            BankCard bankCard = parseLine(List.of(line.split(",")));
                            if (bankCard != null){
                            emitter.next(bankCard);
                            }
                            else{
                                logger.error("Error al convertir la línea CSV en BankCard");
                            }

                        });
                emitter.complete();
            } catch (Exception e) {
                logger.error("Error al importar tarjetas de crédito", e);
                emitter.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Exporta las tarjetas de crédito a un archivo CSV.
     *
     * @param file Archivo CSV al cual se exportarán las tarjetas de crédito.
     * @param bankCards Lista de tarjetas de crédito a exportar.
     * @return Un {@link Mono} que representa la finalización de la operación de exportación.
     */
    public Mono<Void> exportBankCards(File file, List<BankCard> bankCards) {
        logger.debug("Exportando tarjetas de crédito al archivo: {}", file.getAbsolutePath());
        return Mono.<Void>create(emitter -> {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("number,clientId,expirationDate,createdAt,updatedAt\n");
                for (BankCard bankCard : bankCards) {
                    writer.write(String.format("%s,%d,%s,%s,%s\n",
                            bankCard.getNumber(),
                            bankCard.getClientId(),
                            bankCard.getExpirationDate().format(dateFormatter),
                            bankCard.getCreatedAt().format(dateTimeFormatter),
                            bankCard.getUpdatedAt().format(dateTimeFormatter)));
                }
                emitter.success();
            } catch (IOException e) {
                logger.error("Error al exportar tarjetas de crédito", e);
                emitter.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Convierte una línea de datos CSV en un objeto {@link BankCard}.
     *
     * @param parts Partes de la línea CSV separadas por comas.
     * @return Un objeto {@link BankCard}.
     */
    private BankCard parseLine(List<String> parts) {
        try {
            return  BankCard.builder()
                    .number(parts.get(0))
                    .clientId(Long.parseLong(parts.get(1)))
                    .expirationDate(LocalDate.parse(parts.get(2), dateFormatter))
                    .createdAt(LocalDateTime.parse(parts.get(3), dateTimeFormatter))
                    .updatedAt(LocalDateTime.parse(parts.get(4), dateTimeFormatter))
                    .build();
        } catch (Exception e) {
            logger.error("Error al convertir la línea CSV en BankCard", e);
            return null;
        }
    }
}
