package banco.domain.clients.services.clientes;

import banco.domain.clients.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Implementa la interfaz {@link ClientesCache} para proporcionar funcionalidad de caché para datos de clientes.
 * La caché se implementa utilizando un LinkedHashMap con un tamaño máximo y la eliminación automática de entradas expiradas.
 * El hilo limpiador se ejecuta cada minuto para eliminar entradas expiradas.
 */
public class ClientesCacheImpl implements ClientesCache {
    private final Logger logger = LoggerFactory.getLogger(ClientesCacheImpl.class);
    private final Map<Long, Client> cache;
    private final ScheduledExecutorService cleaner;

    /**
     * Construye una nueva instancia de ClientesCacheImpl con el tamaño máximo especificado para la caché.
     *
     * @param maxSize el número máximo de entradas que la caché puede contener
     */
    public ClientesCacheImpl(int maxSize) {
        this.cache = new LinkedHashMap<Long, Client>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Long, Client> eldest) {
                return size() > maxSize;
            }
        };
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        this.cleaner.scheduleAtFixedRate(this::clear, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public void put(Long key, Client value) {
        logger.debug("Añadiendo cliente a la caché con id: " + key + " y valor: " + value);
        cache.put(key, value);
    }

    /**
     * Obtiene un cliente de la caché utilizando la clave proporcionada.
     *
     * @param key La clave única del cliente en la caché. No puede ser nula.
     * @return El cliente asociado con la clave proporcionada, o null si no se encuentra en la caché.
     */
    @Override
    public Client get(Long key) {
        logger.debug("Obteniendo cliente de la caché con id: " + key);
        return cache.get(key);
    }

    /**
     * Elimina un cliente de la caché utilizando la clave proporcionada.
     *
     * @param key La clave única del cliente en la caché. No puede ser nula.
     * @return Nada. Este método solo elimina el cliente asociado con la clave proporcionada de la caché.
     */
    @Override
    public void remove(Long key) {
        logger.debug("Eliminando cliente de la caché con id: " + key);
        cache.remove(key);
    }

    /**
     * Limpia la caché de clientes eliminando las entradas expiradas.
     * Las entradas expiradas se determinan comparando la hora de actualización del cliente con la hora actual,
     * y si la diferencia es de más de un minuto, se considera expirada.
     *
     * @return Nada. Este método solo elimina las entradas expiradas de la caché.
     */
    @Override
    public void clear() {
        cache.entrySet().removeIf(entry -> {
            boolean shouldRemove = entry.getValue().getUpdatedAt().plusMinutes(1).isBefore(LocalDateTime.now());
            if (shouldRemove) {
                logger.debug("Autoeliminando por caducidad cliente de la caché con id: " + entry.getKey());
            }
            return shouldRemove;
        });
    }

    /**
     * Apaga el hilo limpiador de la caché de clientes, deteniendo la ejecución programada de limpieza.
     * Este método debe ser llamado cuando se desee finalizar el uso de la caché para liberar recursos.
     *
     * @return Nada. Este método solo detiene el hilo limpiador de la caché.
     */
    @Override
    public void shutdown() {
        cleaner.shutdown();
    }
}
