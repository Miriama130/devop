package tn.esprit.spring.services.Chambre;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.spring.DAO.Entities.Bloc;
import tn.esprit.spring.DAO.Entities.Chambre;
import tn.esprit.spring.DAO.Entities.Reservation;
import tn.esprit.spring.DAO.Entities.TypeChambre;
import tn.esprit.spring.DAO.Repositories.BlocRepository;
import tn.esprit.spring.DAO.Repositories.ChambreRepository;


import java.time.LocalDate;
import java.util.List;

@Service
public class ChambreService implements IChambreService {

    private static final Logger log = LoggerFactory.getLogger(ChambreService.class);


    private ChambreRepository repo;


    private BlocRepository blocRepository;

    @Override
    public Chambre addOrUpdate(Chambre c) {
        return repo.save(c);
    }

    @Override
    public List<Chambre> findAll() {
        return repo.findAll();
    }

    @Override
    public Chambre findById(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chambre with ID " + id + " not found"));
    }

    @Override
    public void deleteById(long id) {
        repo.deleteById(id);
    }

    @Override
    public void delete(Chambre c) {
        repo.delete(c);
    }

    @Override
    public List<Chambre> getChambresParNomBloc(String nomBloc) {
        return repo.findByBlocNomBloc(nomBloc);
    }

    @Override
    public long nbChambreParTypeEtBloc(TypeChambre type, long idBloc) {
        return repo.countByTypeCAndBlocIdBloc(type, idBloc);
    }

    @Override
    public List<Chambre> getChambresNonReserveParNomFoyerEtTypeChambre(String nomFoyer, TypeChambre type) {
        LocalDate[] anneeUniv = getAnneeUniversitaire();
        LocalDate dateDebutAU = anneeUniv[0];
        LocalDate dateFinAU = anneeUniv[1];

        return repo.findAll().stream()
                .filter(c -> isMatchingChambre(c, nomFoyer, type))
                .filter(c -> isChambreDisponible(c, dateDebutAU, dateFinAU))
                .toList();
    }

    private LocalDate[] getAnneeUniversitaire() {
        int year = LocalDate.now().getYear() % 100;
        if (LocalDate.now().getMonthValue() <= 7) {
            return new LocalDate[]{
                    LocalDate.of(Integer.parseInt("20" + (year - 1)), 9, 15),
                    LocalDate.of(Integer.parseInt("20" + year), 6, 30)
            };
        } else {
            return new LocalDate[]{
                    LocalDate.of(Integer.parseInt("20" + year), 9, 15),
                    LocalDate.of(Integer.parseInt("20" + (year + 1)), 6, 30)
            };
        }
    }

    private boolean isMatchingChambre(Chambre c, String nomFoyer, TypeChambre type) {
        return c.getTypeC().equals(type) && c.getBloc().getFoyer().getNomFoyer().equals(nomFoyer);
    }

    private boolean isChambreDisponible(Chambre c, LocalDate dateDebutAU, LocalDate dateFinAU) {
        long numReservation = c.getReservations().stream()
                .filter(r -> isReservationInAnneeUniversitaire(r, dateDebutAU, dateFinAU))
                .count();

        return switch (c.getTypeC()) {
            case SIMPLE -> numReservation == 0;
            case DOUBLE -> numReservation < 2;
            case TRIPLE -> numReservation < 3;
        };
    }

    private boolean isReservationInAnneeUniversitaire(Reservation r, LocalDate dateDebutAU, LocalDate dateFinAU) {
        return r.getAnneeUniversitaire().isBefore(dateFinAU) && r.getAnneeUniversitaire().isAfter(dateDebutAU);
    }

    @Override
    public void listeChambresParBloc() {
        for (Bloc b : blocRepository.findAll()) {
            log.info("Bloc => {} ayant une capacité {}", b.getNomBloc(), b.getCapaciteBloc());

            if (!b.getChambres().isEmpty()) {
                log.info("La liste des chambres pour ce bloc: ");
                for (Chambre c : b.getChambres()) {
                    log.info("NumChambre: {} type: {}", c.getNumeroChambre(), c.getTypeC());
                }
            } else {
                log.info("Pas de chambre disponible dans ce bloc");
            }

            log.info("******************");
        }
    }

    @Override
    public void pourcentageChambreParTypeChambre() {
        long totalChambre = repo.count();

        if (totalChambre == 0) { // Prevent division by zero
            log.warn("Aucune chambre disponible pour calculer le pourcentage.");
            return;
        }

        double pSimple = ((double) repo.countChambreByTypeC(TypeChambre.SIMPLE) * 100) / totalChambre;
        double pDouble = ((double) repo.countChambreByTypeC(TypeChambre.DOUBLE) * 100) / totalChambre;
        double pTriple = ((double) repo.countChambreByTypeC(TypeChambre.TRIPLE) * 100) / totalChambre;

        log.info("Nombre total des chambres: {}", totalChambre);
        log.info("Le pourcentage des chambres pour le type SIMPLE est égale à {}%", pSimple);
        log.info("Le pourcentage des chambres pour le type DOUBLE est égale à {}%", pDouble);
        log.info("Le pourcentage des chambres pour le type TRIPLE est égale à {}%", pTriple);
    }

    @Override
    public void nbPlacesDisponibleParChambreAnneeEnCours() {
        LocalDate[] anneeUniv = getAnneeUniversitaire();
        LocalDate dateDebutAU = anneeUniv[0];
        LocalDate dateFinAU = anneeUniv[1];

        repo.findAll().forEach(chambre -> logChambreDisponibilite(chambre, dateDebutAU, dateFinAU));
    }

    private void logChambreDisponibilite(Chambre chambre, LocalDate dateDebutAU, LocalDate dateFinAU) {
        long nbReservation = repo.countReservationsByIdChambreAndReservationsEstValideAndReservationsAnneeUniversitaireBetween(
                chambre.getIdChambre(), true, dateDebutAU, dateFinAU);

        int maxPlaces = getMaxPlacesByType(chambre.getTypeC());
        int placesDispo = maxPlaces - (int) nbReservation;

        if (placesDispo > 0) {
            log.info("Le nombre de places disponibles pour la chambre {} {} est {}", chambre.getTypeC(), chambre.getNumeroChambre(), placesDispo);
        } else {
            log.info("La chambre {} {} est complète", chambre.getTypeC(), chambre.getNumeroChambre());
        }
    }

    private int getMaxPlacesByType(TypeChambre type) {
        return switch (type) {
            case SIMPLE -> 1;
            case DOUBLE -> 2;
            case TRIPLE -> 3;
        };
    }
}