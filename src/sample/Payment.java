package sample;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Payment {

        private int id;
        private LocalDateTime datePaiement;
        private double montant;
        private String methode;
        private String clientNom;
        private int commandeId;

        public Payment(int id, LocalDateTime datePaiement, double montant, String methode, String clientNom, int commandeId) {
            this.id = id;
            this.datePaiement = datePaiement;
            this.montant = montant;
            this.methode = methode;
            this.clientNom = clientNom;
            this.commandeId = commandeId;
        }

        public String getDatePaiement() {
            return datePaiement.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        public String getMontant() {
            return String.format("%.2f DH", montant);
        }

        public String getMethode() { return methode; }
        public String getStatus() { return "Confirmé"; }
        public String getRecu() { return "REC-" + id; }
        public String getClientNom() { return clientNom; }
        public int getCommandeId() { return commandeId; }
    }

