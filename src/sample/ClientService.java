package sample;

import java.util.ArrayList;
import java.util.List;

public class ClientService {
    private List<Clients> clients;

    public ClientService() {
        this.clients = new ArrayList<>();
        initializeDummyData();
    }

    private void initializeDummyData() {
        clients.add(new Clients(1, "ALAMI", "Ahmed", "0612345678", "ahmed.alami@email.com"));
        clients.add(new Clients(2, "BENALI", "Fatima", "0623456789", "fatima.benali@email.com"));
        clients.add(new Clients(3, "ZAHRA", "Mohamed", "0634567890", "mohamed.zahra@email.com"));
    }

    public List<Clients> getAllClients() {
        return new ArrayList<>(clients);
    }

    public Clients getClientById(int id) {
        return clients.stream()
                .filter(client -> client.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public void addClient(Clients client) {
        clients.add(client);
    }

    public void updateClient(Clients client) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getId() == client.getId()) {
                clients.set(i, client);
                break;
            }
        }
    }

    public void deleteClient(int id) {
        clients.removeIf(client -> client.getId() == id);
    }
}