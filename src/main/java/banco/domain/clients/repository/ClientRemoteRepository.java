package banco.domain.clients.repository;

import banco.domain.clients.exceptions.ClientNotFoundException;
import banco.domain.clients.mapper.ClientMapper;
import banco.domain.clients.model.Client;

import banco.domain.clients.rest.ClientApiRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class ClientRemoteRepository {
    private final ClientApiRest clientApiRest;
    private final Logger logger = LoggerFactory.getLogger(ClientRemoteRepository.class);

    public ClientRemoteRepository(ClientApiRest clientApiRest) { // Cambiar a ClientApiRest si tienes un API REST específico para clientes
        this.clientApiRest = clientApiRest;
    }

    public List<Client> getAll() {
        var call = clientApiRest.getAllSync(); // Cambiar a la llamada correspondiente si tienes una API para clientes
        try {
            var response = call.execute();
            if (!response.isSuccessful()) {
                throw new Exception("Error: " + response.code());
            }
            return response.body().getData().stream()
                    .map(ClientMapper::toClientFromCreate) // Cambié UserMapper por ClientMapper
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public Client getById(int id) throws ClientNotFoundException {
        var call = clientApiRest.getByIdSync(String.valueOf(id)); // Cambiar a la llamada correspondiente si tienes una API para clientes
        try {
            var response = call.execute();
            if (!response.isSuccessful()) {
                throw new Exception("Error: " + response.code());
            }
            return ClientMapper.toClientFromCreate(response.body().getData());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            if (e.getCause().getMessage().contains("404")) {
                throw new ClientNotFoundException("Client not found with id: " + id); // Cambié UserNotFoundException por ClientNotFoundException
            } else {
                e.printStackTrace();
                return null;
            }
        }
    }



    public Client createClient(Client client) {
        var callSync = clientApiRest.createClient(ClientMapper.toRequest(client));
        try {
            var response = callSync.get();
            return ClientMapper.toClientFromCreate(response.body());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Client updateClient(Client client) { // Cambié User por Client
        var callSync = clientApiRest.updateClient(String.valueOf(client.getId()), ClientMapper.toRequest(client)); // Cambié UserMapper por ClientMapper
        try {
            Response<Client> response = callSync.get(); // Cambié User por Client
            if (response.isSuccessful() && response.body() != null) {
                Client updatedClient = response.body();
                return updatedClient;
            } else {
                System.err.println("Error al actualizar el cliente: " + response.message()); // Cambié el mensaje
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteClient(int id) throws ClientNotFoundException { // Cambié UserNotFoundException por ClientNotFoundException
        var callSync = clientApiRest.deleteClient(String.valueOf(id)); // Cambiar a la llamada correspondiente para clientes
        try {
            callSync.get();
        } catch (Exception e) {
            if (e.getCause().getMessage().contains("404")) {
                throw new ClientNotFoundException("Client not found with id: " + id); // Cambié UserNotFoundException por ClientNotFoundException
            } else {
                e.printStackTrace();
            }
        }
    }
}
