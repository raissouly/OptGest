-- phpMyAdmin SQL Dump
-- version 4.1.4
-- http://www.phpmyadmin.net
--
-- Client :  127.0.0.1
-- Généré le :  Mar 29 Juillet 2025 à 01:45
-- Version du serveur :  5.6.15-log
-- Version de PHP :  5.4.24

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de données :  `optique`
--

DELIMITER $$
--
-- Procédures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `AjouterHistoriqueLivraison`(
    IN p_num_commande VARCHAR(50),
    IN p_date_commande DATE,
    IN p_fournisseur_id INT,
    IN p_produits TEXT,
    IN p_quantite INT,
    IN p_montant DECIMAL(10,2)
)
BEGIN
    DECLARE v_fournisseur_nom VARCHAR(255);
    
    -- Récupérer le nom du fournisseur
    SELECT nom INTO v_fournisseur_nom 
    FROM fournisseurs 
    WHERE id = p_fournisseur_id;
    
    -- Insérer dans l'historique
    INSERT INTO historique_livraisons (
        num_commande,
        date_commande,
        date_livraison,
        fournisseur_id,
        fournisseur_nom,
        produits_livres,
        quantite_livree,
        montant_livraison
    ) VALUES (
        p_num_commande,
        p_date_commande,
        CURDATE(),
        p_fournisseur_id,
        v_fournisseur_nom,
        p_produits,
        p_quantite,
        p_montant
    );
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `AjouterHistoriqueLivraisonAuto`(
    IN p_reference_produit VARCHAR(50),
    IN p_quantite_recue INT,
    IN p_type_operation VARCHAR(20)
)
BEGIN
    DECLARE v_fournisseur_id INT;
    DECLARE v_fournisseur_nom VARCHAR(255);
    DECLARE v_produit_designation VARCHAR(255);
    DECLARE v_produit_marque VARCHAR(50);
    DECLARE v_prix_unitaire DECIMAL(10,2);
    DECLARE v_num_commande VARCHAR(50);
    DECLARE v_produit_info TEXT;
    DECLARE v_montant_total DECIMAL(10,2);

    -- Récupérer les informations du produit
    SELECT 
        p.fournisseur_id,
        COALESCE(f.nom, 'Fournisseur inconnu'),
        p.designation,
        p.marque,
        p.prix_unitaire
    INTO 
        v_fournisseur_id,
        v_fournisseur_nom,
        v_produit_designation,
        v_produit_marque,
        v_prix_unitaire
    FROM produits p
    LEFT JOIN fournisseurs f ON p.fournisseur_id = f.id
    WHERE p.reference = p_reference_produit
    LIMIT 1;

    -- Vérification si le produit existe
    IF v_produit_designation IS NOT NULL THEN
        -- Générer un numéro de commande automatique
        SET v_num_commande = CONCAT('AUTO-LIV-', DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'));

        -- Créer la description du produit
        SET v_produit_info = CONCAT(
            v_produit_designation, 
            ' (', COALESCE(v_produit_marque, 'N/A'), ') - Ref: ', 
            p_reference_produit
        );

        -- Calculer le montant total
        SET v_montant_total = p_quantite_recue * COALESCE(v_prix_unitaire, 0);

        -- Insérer dans l'historique
        INSERT INTO historique_livraisons (
            num_commande,
            reference_bon_livraison,
            date_commande,
            date_livraison,
            date_livraison_prevue,
            fournisseur_id,
            fournisseur_nom,
            produits_livres,
            quantite_livree,
            montant_livraison,
            statut_livraison,
            observations
        ) VALUES (
            v_num_commande,
            CONCAT('BL-', DATE_FORMAT(NOW(), '%Y%m%d%H%i')),
            CURDATE(),
            CURDATE(),
            CURDATE(),
            v_fournisseur_id,
            v_fournisseur_nom,
            v_produit_info,
            p_quantite_recue,
            v_montant_total,
            'Livré',
            CONCAT('Réception automatique - ', p_type_operation, ' de stock le ', NOW())
        );
    END IF;
END$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `auto_generate_delivery_history`()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_commande_id INT;
    DECLARE v_numero_commande VARCHAR(50);
    DECLARE v_date_commande DATE;
    DECLARE v_date_livraison DATE;
    DECLARE v_client_id INT;
    DECLARE v_produit_id VARCHAR(10);
    DECLARE v_quantite INT;
    DECLARE v_prix_total DECIMAL(10,2);
    DECLARE v_fournisseur_id INT;
    DECLARE v_fournisseur_nom VARCHAR(255);
    
    -- Curseur pour parcourir les commandes
    DECLARE cur_commandes CURSOR FOR 
        SELECT 
            c.id,
            c.numero_commande,
            c.Date_Commande,
            c.date_livraison,
            c.ID_Client,
            c.ID_produits,
            c.quantite,
            c.total_commande,
            p.fournisseur_id,
            f.nom as fournisseur_nom
        FROM commandes c
        LEFT JOIN produits p ON c.ID_produits = p.reference
        LEFT JOIN fournisseurs f ON p.fournisseur_id = f.id
        WHERE c.etat IN ('Livré', 'Terminé')
        AND NOT EXISTS (
            SELECT 1 FROM historique_livraisons hl 
            WHERE hl.num_commande = c.numero_commande
        );
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur_commandes;
    
    read_loop: LOOP
        FETCH cur_commandes INTO 
            v_commande_id, v_numero_commande, v_date_commande, 
            v_date_livraison, v_client_id, v_produit_id, v_quantite, 
            v_prix_total, v_fournisseur_id, v_fournisseur_nom;
        
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- Insérer dans l'historique des livraisons
        INSERT INTO historique_livraisons (
            num_commande,
            date_commande,
            date_livraison,
            date_livraison_prevue,
            fournisseur_id,
            fournisseur_nom,
            produits_livres,
            quantite_livree,
            montant_livraison,
            statut_livraison,
            observations
        ) VALUES (
            v_numero_commande,
            v_date_commande,
            COALESCE(v_date_livraison, CURDATE()),
            v_date_livraison,
            COALESCE(v_fournisseur_id, 1),
            COALESCE(v_fournisseur_nom, 'Fournisseur par défaut'),
            CONCAT('Produit: ', v_produit_id),
            v_quantite,
            v_prix_total,
            'Livré',
            'Historique généré automatiquement'
        );
        
    END LOOP;
    
    CLOSE cur_commandes;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `clients`
--

CREATE TABLE IF NOT EXISTS `clients` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Nom` varchar(45) DEFAULT NULL,
  `Prenom` varchar(45) DEFAULT NULL,
  `Telephone` varchar(100) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=17 ;

--
-- Contenu de la table `clients`
--

INSERT INTO `clients` (`id`, `Nom`, `Prenom`, `Telephone`, `Email`) VALUES
(1, 'khaoula', 'daoudi', '0634267788', 'khaouladaoudi@gmil.com'),
(2, 'kholoud', 'dabdi', '0606905444', 'dabdikholoudn@gmail.com'),
(3, 'Raissouly', 'nouhaila', '0645491195', 'raissoulynouhaila@gmail.com'),
(4, 'radi', 'mohammed', '0672451900', 'radimohammed@gmail.com'),
(5, 'mohammed', 'SDF', '0644566333', 'RFSKD@gmail.com'),
(6, 'raissi', 'khadija', '06447732', 'raissikhadija@gmail.com'),
(7, 'fathi', 'mouha', '06784322', 'mouha@dmail.com'),
(8, 'baioua', 'yousra', '0645433728', 'baioua@gmail.com'),
(9, 'radi', 'sohaila', '064532111', 'radi@gmail.com'),
(10, 'Rahli', 'Rahma', '06452345', 'rahli@gmail.com'),
(11, 'zahiri', 'reda', '0645328911', 'zahiri@gmail.com'),
(12, 'fathi', 'khadija', '0634561100', 'fathikhadija@gmail.com'),
(13, 'radi', 'mohamed', '064544991100', 'radimohamed@gmail.com'),
(14, 'almaouni', 'ahmed', '06452345', 'almaouni@gmail.com'),
(15, 'radi', 'nora', '064532111', 'radi@gmail.com'),
(16, 'fadil', 'asd', '06452345', 'fadil@gmail.com');

-- --------------------------------------------------------

--
-- Structure de la table `commandes`
--

CREATE TABLE IF NOT EXISTS `commandes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `numero_commande` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ID_Client` int(11) DEFAULT NULL,
  `Date_Commande` date DEFAULT NULL,
  `ID_produits` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantite` int(11) DEFAULT '1',
  `prix_unitaire` decimal(10,2) DEFAULT NULL,
  `prix_total` decimal(10,2) DEFAULT NULL,
  `etat` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'En cours',
  `priorite` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'Normale',
  `delai_fabrication` int(11) DEFAULT '1',
  `date_livraison` date DEFAULT NULL,
  `notes` mediumtext COLLATE utf8mb4_unicode_ci,
  `total_commande` decimal(10,2) DEFAULT NULL,
  `id_facture` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero_commande` (`numero_commande`),
  KEY `ID_Client` (`ID_Client`),
  KEY `ID_produits` (`ID_produits`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=12 ;

--
-- Contenu de la table `commandes`
--

INSERT INTO `commandes` (`id`, `numero_commande`, `ID_Client`, `Date_Commande`, `ID_produits`, `quantite`, `prix_unitaire`, `prix_total`, `etat`, `priorite`, `delai_fabrication`, `date_livraison`, `notes`, `total_commande`, `id_facture`) VALUES
(1, 'CMD-2025-001', 1, '2025-06-14', 'REF001', 1, '45.70', NULL, 'Livrée', 'Normale', 1, NULL, '', '45.70', NULL),
(3, 'CMD-2025-003', 3, '2025-06-14', 'REF002', 2, '100.80', NULL, 'Livrée', 'Normale', 1, NULL, '', '201.60', NULL),
(4, 'CMD-2025-004', 13, '2025-06-14', 'REF001', 1, '45.70', NULL, 'Livrée', 'Normale', 1, NULL, '', '45.70', NULL),
(5, 'CMD-2025-005', 2, '2025-06-13', 'REF003', 1, '100.00', NULL, 'Livrée', 'Normale', 1, NULL, '', '200.00', NULL),
(6, 'CMD-2025-006', 8, '2025-07-06', 'REF002', 3, '100.80', NULL, 'Livrée', 'Normale', 1, NULL, 'ref', '302.40', NULL),
(7, 'CMD-2025-007', 16, '2025-07-19', 'REF004', 2, '100.00', NULL, 'Livrée', 'Normale', 1, NULL, '', '200.00', NULL),
(9, 'CMD-2025-009', 2, '2025-07-19', 'REF005', 2, '60.00', NULL, 'Livrée', 'Normale', 1, NULL, '', '120.00', NULL),
(10, 'CMD-2025-010', 4, '2025-07-18', 'REF002', 1, '100.80', NULL, 'Livrée', 'Normale', 1, NULL, '', '100.80', NULL),
(11, NULL, 8, '2025-07-25', 'REF002', 1, '100.80', NULL, 'Livrée', 'Normale', 1, NULL, '', NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `commande_produits`
--

CREATE TABLE IF NOT EXISTS `commande_produits` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `commande_id` int(11) NOT NULL,
  `produit_id` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantite` int(11) NOT NULL DEFAULT '1',
  `prix_unitaire` decimal(10,2) NOT NULL,
  `sous_total` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_commande_produits_commande` (`commande_id`),
  KEY `fk_commande_produits_produit` (`produit_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=13 ;

--
-- Contenu de la table `commande_produits`
--

INSERT INTO `commande_produits` (`id`, `commande_id`, `produit_id`, `quantite`, `prix_unitaire`, `sous_total`) VALUES
(8, 1, 'REF001', 1, '45.70', '45.70'),
(9, 3, 'REF002', 2, '100.80', '201.60'),
(10, 4, 'REF001', 1, '45.70', '45.70'),
(11, 5, 'REF003', 1, '200.00', '200.00'),
(12, 6, 'REF002', 3, '100.80', '302.40');

-- --------------------------------------------------------

--
-- Structure de la table `configuration`
--

CREATE TABLE IF NOT EXISTS `configuration` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `entreprise_nom` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `entreprise_siret` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `entreprise_email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `entreprise_telephone` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `entreprise_adresse` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `prefixe_facture` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `taux_tva` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `delai_paiement` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=2 ;

--
-- Contenu de la table `configuration`
--

INSERT INTO `configuration` (`id`, `entreprise_nom`, `entreprise_siret`, `entreprise_email`, `entreprise_telephone`, `entreprise_adresse`, `prefixe_facture`, `taux_tva`, `delai_paiement`) VALUES
(1, 'Nom Entreprise', '123456789', 'contact@entreprise.com', '0612345678', '123 Rue Exemple, Ville', 'FAC-', '20', '30');

-- --------------------------------------------------------

--
-- Structure de la table `doctor`
--

CREATE TABLE IF NOT EXISTS `doctor` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `Nom` varchar(45) DEFAULT NULL,
  `Prenom` varchar(45) DEFAULT NULL,
  `Specialite` varchar(45) DEFAULT NULL,
  `Telephone` varchar(45) DEFAULT NULL,
  `Email` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=4 ;

--
-- Contenu de la table `doctor`
--

INSERT INTO `doctor` (`id`, `Nom`, `Prenom`, `Specialite`, `Telephone`, `Email`) VALUES
(1, 'BENNANa', 'Youssef', 'Ophtalmologie', '0512345676', 'y.bennani@clinic.ma'),
(2, 'TAZI', 'Aicha', 'Optométrie', '0523456789', 'a.tazi@clinic.ma'),
(3, 'Alaoui', 'Hassan', 'Ophtalmologie', '0534567890', 'h.rifai@clinic.ma');

-- --------------------------------------------------------

--
-- Structure de la table `factures`
--

CREATE TABLE IF NOT EXISTS `factures` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `numero_facture` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `numero_commande` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ID_Client` int(11) NOT NULL,
  `date_facture` date NOT NULL,
  `date_echeance` date DEFAULT NULL,
  `montant_ht` decimal(10,2) NOT NULL DEFAULT '0.00',
  `taux_tva` decimal(5,2) DEFAULT '20.00',
  `montant_tva` decimal(10,2) NOT NULL DEFAULT '0.00',
  `montant_ttc` decimal(10,2) NOT NULL DEFAULT '0.00',
  `statut` enum('Brouillon','Envoyée','Payée','Partiellement payée','En retard','Annulée') COLLATE utf8mb4_unicode_ci DEFAULT 'Brouillon',
  `mode_paiement` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_paiement` date DEFAULT NULL,
  `notes` mediumtext COLLATE utf8mb4_unicode_ci,
  `date_creation` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date_modification` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `commande_id` int(11) DEFAULT NULL,
  `ID_Commande` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `numero_facture` (`numero_facture`),
  KEY `ID_Client` (`ID_Client`),
  KEY `idx_date_facture` (`date_facture`),
  KEY `idx_statut` (`statut`),
  KEY `fk_factures_commande` (`numero_commande`),
  KEY `factures_ibfk_1` (`commande_id`),
  KEY `idx_factures_commande_id` (`commande_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=57 ;

--
-- Contenu de la table `factures`
--

INSERT INTO `factures` (`id`, `numero_facture`, `numero_commande`, `ID_Client`, `date_facture`, `date_echeance`, `montant_ht`, `taux_tva`, `montant_tva`, `montant_ttc`, `statut`, `mode_paiement`, `date_paiement`, `notes`, `date_creation`, `date_modification`, `commande_id`, `ID_Commande`) VALUES
(40, 'FAC-2025-002', 'CMD-2025-006', 8, '2025-07-17', '2025-08-16', '302.40', '20.00', '60.48', '362.88', 'Brouillon', NULL, NULL, NULL, '2025-07-16 22:20:23', '2025-07-16 22:20:23', NULL, NULL),
(54, 'FAC-2025-003', 'CMD-2025-003', 3, '2025-07-25', '2025-08-24', '201.60', '20.00', '40.32', '241.92', 'Brouillon', NULL, NULL, NULL, '2025-07-25 15:33:46', '2025-07-25 15:33:46', NULL, NULL),
(55, 'FAC-2025-004', 'CMD-2025-009', 2, '2025-07-25', '2025-08-24', '120.00', '20.00', '24.00', '144.00', 'Brouillon', NULL, NULL, NULL, '2025-07-25 15:34:15', '2025-07-25 15:34:15', NULL, NULL),
(56, 'FAC-2025-005', 'CMD-2025-001', 1, '2025-07-26', '2025-08-25', '45.70', '20.00', '9.14', '54.84', 'Brouillon', NULL, NULL, NULL, '2025-07-26 12:00:38', '2025-07-26 12:00:38', NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `factures_lignes`
--

CREATE TABLE IF NOT EXISTS `factures_lignes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ID_Facture` int(11) NOT NULL,
  `reference_produit` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `designation` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantite` int(11) NOT NULL DEFAULT '1',
  `prix_unitaire` decimal(10,2) NOT NULL,
  `remise` decimal(5,2) DEFAULT '0.00',
  `montant_ligne` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ID_Facture` (`ID_Facture`),
  KEY `reference_produit` (`reference_produit`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Structure de la table `fournisseurs`
--

CREATE TABLE IF NOT EXISTS `fournisseurs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `code_fournisseur` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `telephone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fax` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `site_web` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `adresse` mediumtext COLLATE utf8mb4_unicode_ci,
  `ville` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `code_postal` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pays` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT 'Maroc',
  `statut` enum('Actif','Inactif','En attente','Suspendu') COLLATE utf8mb4_unicode_ci DEFAULT 'Actif',
  `notes` mediumtext COLLATE utf8mb4_unicode_ci,
  `date_creation` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date_modification` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_fournisseur` (`code_fournisseur`),
  KEY `idx_fournisseur_nom` (`nom`(191)),
  KEY `idx_fournisseur_statut` (`statut`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=7 ;

--
-- Contenu de la table `fournisseurs`
--

INSERT INTO `fournisseurs` (`id`, `nom`, `code_fournisseur`, `telephone`, `fax`, `email`, `site_web`, `adresse`, `ville`, `code_postal`, `pays`, `statut`, `notes`, `date_creation`, `date_modification`) VALUES
(1, 'Optique Plus', 'OPT001', '0522-123456', NULL, 'contact@optiqueplus.ma', NULL, '123 Rue Mohammed V', 'Casablanca', NULL, 'Maroc', 'Actif', NULL, '2025-06-21 22:09:16', '2025-06-21 22:09:16'),
(2, 'Vision Excellence', 'VIS002', '0537-654321', NULL, 'info@visionexcellence.ma', NULL, '45 Avenue Hassan II', 'Rabat', NULL, 'Maroc', 'Actif', NULL, '2025-06-21 22:09:16', '2025-06-21 22:09:16'),
(3, 'LensCrafters Morocco', 'LEN003', '0524-789012', NULL, 'orders@lenscraft.ma', NULL, '78 Boulevard Zerktouni', 'Marrakech', NULL, 'Maroc', 'Actif', NULL, '2025-06-21 22:09:16', '2025-06-21 22:09:16'),
(4, 'EuroVision Supplies', 'EUR004', '+33-1-23456789', 'null', 'sales@eurovision.fr', 'null', '12 Rue de la Pai', 'Paris', 'null', 'France', 'Actif', 'null', '2025-06-21 22:09:16', '2025-07-16 21:37:31'),
(6, 'optique', 'vp12333', '05 45 43 33 22', '05 45 43 33 22', 'info@optique.ma', 'info@optique.ma', '45 RUE BOUCHTA', 'Tanger', '90000', 'Maroc', 'Actif', 'fg', '2025-06-28 18:15:15', '2025-06-28 18:15:15');

-- --------------------------------------------------------

--
-- Structure de la table `historique_livraisons`
--

CREATE TABLE IF NOT EXISTS `historique_livraisons` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `num_commande` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `reference_bon_livraison` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_commande` date NOT NULL,
  `date_livraison` date DEFAULT NULL,
  `date_livraison_prevue` date DEFAULT NULL,
  `fournisseur_id` int(11) NOT NULL,
  `fournisseur_nom` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `produits_livres` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `quantite_livree` int(11) NOT NULL DEFAULT '0',
  `montant_livraison` decimal(10,2) NOT NULL DEFAULT '0.00',
  `statut_livraison` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'Livré',
  `observations` text COLLATE utf8mb4_unicode_ci,
  `date_creation` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date_modification` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_num_commande` (`num_commande`),
  KEY `idx_historique_fournisseur` (`fournisseur_id`),
  KEY `idx_historique_date` (`date_commande`),
  KEY `idx_num_commande` (`num_commande`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=38 ;

--
-- Contenu de la table `historique_livraisons`
--

INSERT INTO `historique_livraisons` (`id`, `num_commande`, `reference_bon_livraison`, `date_commande`, `date_livraison`, `date_livraison_prevue`, `fournisseur_id`, `fournisseur_nom`, `produits_livres`, `quantite_livree`, `montant_livraison`, `statut_livraison`, `observations`, `date_creation`, `date_modification`) VALUES
(20, 'CMD-2025-001', NULL, '2025-06-14', '2025-06-14', NULL, 1, 'Optique Plus', 'Lunetts Optiqur', 1, '45.70', 'Livrée', NULL, '2025-07-21 16:34:55', '2025-07-21 16:48:13'),
(21, 'CMD-2025-004', NULL, '2025-06-14', '2025-06-14', NULL, 1, 'Optique Plus', 'Lunetts Optiqur', 1, '45.70', 'Livrée', NULL, '2025-07-21 16:34:55', '2025-07-21 16:48:13'),
(22, 'CMD-2025-003', NULL, '2025-06-14', '2025-06-14', NULL, 2, 'Vision Excellence', 'kdexLuex', 2, '201.60', 'Livrée', NULL, '2025-07-21 16:34:55', '2025-07-21 16:48:13'),
(23, 'CMD-2025-006', NULL, '2025-07-06', '2025-07-06', NULL, 2, 'Vision Excellence', 'kdexLuex', 3, '302.40', 'Livrée', NULL, '2025-07-21 16:34:55', '2025-07-21 16:48:13'),
(24, 'CMD-2025-010', NULL, '2025-07-18', '2025-07-18', NULL, 2, 'Vision Excellence', 'kdexLuex', 1, '100.80', 'Livrée', NULL, '2025-07-21 16:34:55', '2025-07-21 16:48:13'),
(25, 'CMD-2025-005', NULL, '2025-06-13', '2025-06-13', NULL, 3, 'LensCrafters Morocco', 'verres optique', 1, '200.00', 'Livrée', NULL, '2025-07-21 16:34:55', '2025-07-21 16:48:13'),
(26, 'CMD-2025-007', NULL, '2025-07-19', '2025-07-19', NULL, 0, 'Inconnu', 'Accessoires', 2, '200.00', 'Livrée', NULL, '2025-07-21 16:34:55', '2025-07-21 16:48:13'),
(27, 'CMD-2025-011', NULL, '2025-07-20', '2025-07-20', NULL, 0, 'Inconnu', 'Accessoires', 1, '100.00', 'Livrée', NULL, '2025-07-21 16:34:55', '2025-07-21 16:48:13'),
(28, 'CMD-2025-009', NULL, '2025-07-19', '2025-07-19', NULL, 4, 'EuroVision Supplies', 'Lunte', 2, '120.00', 'Livrée', NULL, '2025-07-21 16:34:55', '2025-07-21 16:48:13'),
(29, 'CMD-2025-012', NULL, '2025-07-20', '2025-07-20', NULL, 4, 'EuroVision Supplies', 'Lunte', 1, '60.00', 'Livrée', NULL, '2025-07-21 16:34:55', '2025-07-21 16:48:13'),
(30, 'CMD-2025-013', NULL, '2025-07-19', '2025-07-19', NULL, 4, 'EuroVision Supplies', 'Lunte', 1, '60.00', 'Livrée', NULL, '2025-07-21 16:34:55', '2025-07-21 16:48:13'),
(35, 'CMD-2025-014', NULL, '2025-07-20', NULL, NULL, 4, 'EuroVision Supplies', 'Lunte', 1, '60.00', 'Livrée', NULL, '2025-07-21 17:54:25', '2025-07-21 17:54:25'),
(36, 'CMD-2025-015', NULL, '2025-07-19', NULL, NULL, 4, 'EuroVision Supplies', 'Lunte', 2, '120.00', 'Livrée', NULL, '2025-07-21 17:54:57', '2025-07-21 17:54:57'),
(37, 'CMD-2025-037', NULL, '2025-07-24', NULL, NULL, 0, 'Inconnu', 'Accessoires', 1, '100.00', 'Livrée', NULL, '2025-07-25 12:06:40', '2025-07-25 12:06:40');

-- --------------------------------------------------------

--
-- Structure de la table `mouvements_stock`
--

CREATE TABLE IF NOT EXISTS `mouvements_stock` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reference_produit` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type_operation` enum('ENTREE','SORTIE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantite` int(11) DEFAULT NULL,
  `date_operation` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `reference_produit` (`reference_produit`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=10 ;

--
-- Contenu de la table `mouvements_stock`
--

INSERT INTO `mouvements_stock` (`id`, `reference_produit`, `type_operation`, `quantite`, `date_operation`) VALUES
(1, 'REF001', 'SORTIE', 6, '2025-06-13 14:53:11'),
(2, 'REF003', 'ENTREE', 45, '2025-06-13 14:54:42'),
(3, 'REF003', 'SORTIE', 59, '2025-06-13 20:20:35'),
(4, 'RF001', 'ENTREE', 5, '2025-07-04 11:27:46'),
(5, 'RF002', 'ENTREE', 90, '2025-07-04 14:26:49'),
(6, 'ff', 'ENTREE', 33, '2025-07-04 17:21:42'),
(7, 'REF001', 'ENTREE', 45, '2025-07-05 09:35:19'),
(8, 'FR001', 'ENTREE', 60, '2025-07-06 22:02:57'),
(9, 'REF004', 'ENTREE', 12, '2025-07-26 11:52:50');

--
-- Déclencheurs `mouvements_stock`
--
DROP TRIGGER IF EXISTS `trigger_historique_livraison`;
DELIMITER //
CREATE TRIGGER `trigger_historique_livraison` AFTER INSERT ON `mouvements_stock`
 FOR EACH ROW BEGIN
    DECLARE v_produit_info TEXT;
    DECLARE v_fournisseur_id INT;
    DECLARE v_prix DECIMAL(10,2);
    
    -- Si c'est une entrée de stock (livraison)
    IF NEW.type_operation = 'ENTREE' THEN
        -- Récupérer les informations du produit
        SELECT 
            CONCAT(designation, ' (', marque, ') - Ref: ', reference),
            fournisseur_id,
            prix_unitaire
        INTO v_produit_info, v_fournisseur_id, v_prix
        FROM produits 
        WHERE reference = NEW.reference_produit;
        
        -- Ajouter à l'historique si on a un fournisseur
        IF v_fournisseur_id IS NOT NULL THEN
            CALL AjouterHistoriqueLivraison(
                CONCAT('AUTO-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', NEW.id),
                CURDATE(),
                v_fournisseur_id,
                v_produit_info,
                NEW.quantite,
                NEW.quantite * v_prix
            );
        END IF;
    END IF;
END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Structure de la table `ordonnances`
--

CREATE TABLE IF NOT EXISTS `ordonnances` (
  `id_ordonnance` int(11) NOT NULL AUTO_INCREMENT,
  `ID_Client` int(11) NOT NULL,
  `ID_Doctor` int(11) NOT NULL,
  `Date_Ordonnance` date NOT NULL,
  `Description` mediumtext COLLATE utf8mb4_unicode_ci,
  `scan_path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `priority_level` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'Normal',
  `created_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sphere_left` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cylindre_left` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `axe_left` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `addition_left` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sphere_right` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cylindre_right` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `axe_right` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `addition_right` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `distance_pupillaire` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hauteur_montage` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `lens_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `OD_Sph` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `OD_Cyl` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `OD_Axe` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `OD_Add` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `OG_Add` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `OG_Sph` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `OG_Axe` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `OG_Cyl` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id_ordonnance`),
  KEY `ordonnances_fk_client` (`ID_Client`),
  KEY `ordonnances_fk_doctor` (`ID_Doctor`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=3 ;

--
-- Contenu de la table `ordonnances`
--

INSERT INTO `ordonnances` (`id_ordonnance`, `ID_Client`, `ID_Doctor`, `Date_Ordonnance`, `Description`, `scan_path`, `priority_level`, `created_date`, `sphere_left`, `cylindre_left`, `axe_left`, `addition_left`, `sphere_right`, `cylindre_right`, `axe_right`, `addition_right`, `distance_pupillaire`, `hauteur_montage`, `lens_type`, `OD_Sph`, `OD_Cyl`, `OD_Axe`, `OD_Add`, `OG_Add`, `OG_Sph`, `OG_Axe`, `OG_Cyl`) VALUES
(2, 2, 3, '2025-06-30', 'hg', 'C:\\Users\\LENOVO-PRO\\Downloads\\Image .jpg', 'Normal', '2025-07-14 11:16:23', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `paiements`
--

CREATE TABLE IF NOT EXISTS `paiements` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `commande_id` int(11) NOT NULL,
  `client_id` int(11) NOT NULL,
  `date_paiement` datetime NOT NULL,
  `montant` decimal(10,2) NOT NULL,
  `methode` enum('Espece','Carte','Virement') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `commande_id` (`commande_id`),
  KEY `client_id` (`client_id`),
  KEY `idx_paiements_client_id` (`client_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=15 ;

--
-- Contenu de la table `paiements`
--

INSERT INTO `paiements` (`id`, `commande_id`, `client_id`, `date_paiement`, `montant`, `methode`) VALUES
(1, 4, 4, '2025-06-25 19:29:34', '200.00', 'Espece'),
(2, 4, 4, '2025-06-25 19:30:20', '200.00', 'Espece'),
(3, 5, 2, '2025-06-25 20:20:55', '45.00', 'Carte'),
(4, 3, 3, '2025-06-25 20:35:30', '50.00', 'Virement'),
(5, 5, 2, '2025-07-06 12:19:12', '200.00', 'Virement'),
(6, 22, 3, '2025-07-24 10:28:58', '200.00', 'Espece'),
(7, 25, 2, '2025-07-24 17:33:13', '100.00', 'Espece'),
(8, 6, 8, '2025-07-24 22:00:00', '100.00', 'Carte'),
(9, 24, 2, '2025-07-24 22:00:00', '100.00', 'Espece'),
(10, 22, 3, '2025-07-24 22:00:00', '61.00', 'Espece'),
(11, 30, 8, '2025-07-24 22:00:00', '100.00', 'Espece'),
(12, 9, 2, '2025-07-24 22:00:00', '100.00', 'Espece'),
(13, 6, 8, '2025-07-24 22:00:00', '100.00', 'Espece'),
(14, 7, 16, '2025-07-25 22:00:00', '100.00', 'Espece');

-- --------------------------------------------------------

--
-- Structure de la table `produits`
--

CREATE TABLE IF NOT EXISTS `produits` (
  `reference` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `designation` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `couleur` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `marque` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantite` int(11) DEFAULT NULL,
  `taille` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stock_minimal` int(11) DEFAULT NULL,
  `prix_unitaire` double DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `date_modification` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `fournisseur_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`reference`),
  KEY `fk_produits_fournisseurs` (`fournisseur_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Contenu de la table `produits`
--

INSERT INTO `produits` (`reference`, `designation`, `couleur`, `marque`, `type`, `quantite`, `taille`, `stock_minimal`, `prix_unitaire`, `date_creation`, `date_modification`, `fournisseur_id`) VALUES
('REF001', 'Lunetts Optiqur', 'blue', 'Essilor', 'Lunettes', 46, 'L', 9, 45.7, '2025-06-05 03:55:11', '2025-07-05 09:35:19', 1),
('REF002', 'kdexLuex', 'Noir', 'Gucci', 'Lunettes', 7, 'M', 5, 100.8, '2025-06-05 03:55:11', '2025-07-04 10:04:30', 2),
('REF003', 'verres optique', 'Noir', 'Gucci', 'Verres', 5, 'M', 9, 100, '2025-06-05 03:57:07', '2025-07-04 10:04:30', 3),
('REF004', 'Accessoires', 'NOIR', 'Prada', 'Accessoires', 66, 'M', 9, 100, '2025-07-16 05:48:36', '2025-07-26 11:52:50', NULL),
('REF005', 'Lunte', 'noir', 'cucci', 'Lunettes', 80, 'S', 9, 60, '2025-07-17 08:27:01', '2025-07-17 22:07:06', 4),
('RFE006', 'Lunetts Optiqur', 'Pink', 'Essilor', 'Verres', 60, 'M', 9, 80, '2025-07-17 08:34:14', '2025-07-17 08:34:14', 6);

-- --------------------------------------------------------

--
-- Structure de la table `produits_fournisseurs`
--

CREATE TABLE IF NOT EXISTS `produits_fournisseurs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `fournisseur_id` int(11) DEFAULT NULL,
  `produit_id` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nom_produit` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `categorie` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `prix_fournisseur` decimal(10,2) DEFAULT NULL,
  `delai_livraison` int(11) DEFAULT NULL,
  `qte_min_commande` int(11) DEFAULT '1',
  `statut` enum('Disponible','Indisponible','Discontinu') COLLATE utf8mb4_unicode_ci DEFAULT 'Disponible',
  `date_creation` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `taille` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `couleur` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `marque` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `date_modification` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_produit_fournisseur` (`fournisseur_id`),
  KEY `fk_pf_produit` (`produit_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AUTO_INCREMENT=7 ;

--
-- Contenu de la table `produits_fournisseurs`
--

INSERT INTO `produits_fournisseurs` (`id`, `fournisseur_id`, `produit_id`, `nom_produit`, `categorie`, `prix_fournisseur`, `delai_livraison`, `qte_min_commande`, `statut`, `date_creation`, `taille`, `couleur`, `marque`, `date_modification`) VALUES
(1, 1, 'REF001', 'Lunettes de Soleil', 'Accessoires', '250.00', 5, 10, 'Disponible', '2025-07-09 15:27:00', NULL, NULL, NULL, '2025-07-15 21:45:31'),
(3, 2, 'REF002', 'kdexLuex', 'Lunettes', '100.80', 5, 2, 'Disponible', '2025-07-10 12:45:17', NULL, NULL, NULL, '2025-07-15 21:45:31'),
(4, 3, 'REF003', 'verres optique', 'Verres', '100.00', 7, 10, 'Disponible', '2025-07-10 12:45:17', NULL, NULL, NULL, '2025-07-15 21:45:31'),
(5, 4, 'REF005', 'Lunte', 'Lunettes', '60.00', 5, 1, 'Disponible', '2025-07-17 08:27:01', NULL, NULL, NULL, '2025-07-17 22:07:06'),
(6, 6, 'RFE006', 'Lunetts Optiqur', 'Verres', '80.00', 5, 1, 'Disponible', '2025-07-17 08:34:14', NULL, NULL, NULL, '2025-07-17 08:34:14');

-- --------------------------------------------------------

--
-- Structure de la table `rendezvous`
--

CREATE TABLE IF NOT EXISTS `rendezvous` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `client_name` varchar(100) NOT NULL,
  `client_phone` varchar(20) NOT NULL,
  `date_rdv` date NOT NULL,
  `heure_rdv` time NOT NULL,
  `opticien` varchar(100) NOT NULL,
  `service` varchar(100) NOT NULL,
  `statut` varchar(30) DEFAULT 'En attente',
  `notes` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=7 ;

--
-- Contenu de la table `rendezvous`
--

INSERT INTO `rendezvous` (`id`, `client_name`, `client_phone`, `date_rdv`, `heure_rdv`, `opticien`, `service`, `statut`, `notes`) VALUES
(5, 'raissouly khaoula', '06 45 67 32 11', '2025-07-06', '09:00:00', 'BENNANI', 'Contrôle lunettes', 'Confirmé', 'er'),
(6, 'kholoud dabdi', '06 44 67 88 11', '2025-07-07', '09:45:00', 'TAZI', 'Examen complet', 'En attente', 'n');

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('admin','employe','opticien','assistant','docteur') DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=6 ;

--
-- Contenu de la table `user`
--

INSERT INTO `user` (`id`, `username`, `password`, `role`) VALUES
(1, 'admin', 'admin123', 'admin'),
(2, 'opticien1', 'pass123', 'opticien'),
(3, 'assistant1', 'pass456', 'assistant'),
(4, 'docteur1', 'doc123', 'docteur'),
(5, 'employe', 'emp123', 'employe');

-- --------------------------------------------------------

--
-- Structure de la table `vue_commandes_completes`
--

CREATE TABLE IF NOT EXISTS `vue_commandes_completes` (
  `id` int(11) DEFAULT NULL,
  `numero_commande` varchar(50) DEFAULT NULL,
  `Date_Commande` date DEFAULT NULL,
  `date_livraison` date DEFAULT NULL,
  `etat` varchar(20) DEFAULT NULL,
  `priorite` varchar(20) DEFAULT NULL,
  `total_commande` decimal(10,2) DEFAULT NULL,
  `client_nom` varchar(91) DEFAULT NULL,
  `client_email` varchar(100) DEFAULT NULL,
  `client_telephone` varchar(100) DEFAULT NULL,
  `nombre_produits` bigint(21) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
