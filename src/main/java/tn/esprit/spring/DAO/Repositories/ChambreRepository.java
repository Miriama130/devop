package tn.esprit.spring.DAO.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.spring.DAO.Entities.Chambre;
import tn.esprit.spring.DAO.Entities.TypeChambre;

import java.time.LocalDate;
import java.util.List;
public interface ChambreRepository extends JpaRepository<Chambre, Long> {
    Chambre findByNumeroChambre(long num);

    List<Chambre> findByBlocNomBloc(String nom);

    int countByTypeCAndBlocIdBloc(TypeChambre typeChambre, long idBloc);

    //********************* Ajouter Reservation *********************
    //SQL
    @Query(value = "SELECT COUNT(*) FROM reservation r " +
            "WHERE r.id_chambre = :idChambre " +
            "AND r.annee_universitaire BETWEEN :dateDebut AND :dateFin",
            nativeQuery = true)
    int listerReservationPourUneChambre(@Param("idChambre") long idChambre,
                                        @Param("dateDebut") LocalDate dateDebutAU,
                                        @Param("dateFin") LocalDate dateFinAU);


    //Keyword
    int countReservationsByIdChambreAndReservationsAnneeUniversitaireBetween(long chambreId, LocalDate dateDebutAU, LocalDate dateFinAU);
    //*****************************************************************
    Chambre findByReservationsIdReservation(String idReservation);

    long count();

    long countChambreByTypeC(TypeChambre typeChambre);

    long countReservationsByIdChambreAndReservationsEstValideAndReservationsAnneeUniversitaireBetween(long idChambre, boolean estValide, LocalDate dateDebut, LocalDate dateFin);
//    List<Chambre> findAllByNumeroChambre(List<Long> num);
}