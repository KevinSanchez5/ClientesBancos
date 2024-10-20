package banco;

import banco.data.storage.BankCardStorageCsv;
import banco.domain.cards.model.BankCard;
import reactor.core.publisher.Flux;


import java.io.File;


public class Main {

    public static void main(String[] args) {
        // Ubicación del archivo CSV
        File file = new File("src/main/resources/example/bankcards.csv");
        BankCardStorageCsv bankCardStorage = new BankCardStorageCsv();

        // Importar tarjetas de crédito
        Flux<BankCard> bankCardFlux = bankCardStorage.importBankCards(file);
        bankCardFlux.collectList()
                .subscribe(
                        bankCards -> {
                            System.out.println("Tarjetas importadas:");
                            bankCards.forEach(bankCard -> System.out.println(bankCard));

                            // Exportar tarjetas de crédito a un nuevo archivo
                            File exportFile = new File("src/main/resources/example/exported_bankcards.csv");
                            bankCardStorage.exportBankCards(exportFile, bankCards)
                                    .subscribe(
                                            null,
                                            error -> System.err.println("Error al exportar: " + error),
                                            () -> System.out.println("Exportación completa.")
                                    );
                        },
                        error -> System.err.println("Error al importar: " + error)
                );
    }
}
