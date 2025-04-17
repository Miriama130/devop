package tn.esprit.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.DAO.Entities.Chambre;
import tn.esprit.spring.DAO.Entities.TypeChambre;
import tn.esprit.spring.DAO.Repositories.ChambreRepository;
import tn.esprit.spring.DAO.Repositories.BlocRepository;
import tn.esprit.spring.services.Chambre.ChambreService;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ChambreServiceMockTest {

    @Mock
    private ChambreRepository chambreRepository;

    @Mock
    private BlocRepository blocRepository;

    @InjectMocks
    private ChambreService chambreService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddOrUpdateChambre() {
        Chambre chambre = new Chambre();
        chambre.setNumeroChambre(101);
        chambre.setTypeC(TypeChambre.SIMPLE);

        when(chambreRepository.save(any(Chambre.class))).thenReturn(chambre);

        Chambre result = chambreService.addOrUpdate(chambre);

        assertNotNull(result);
        assertEquals(101, result.getNumeroChambre());
        assertEquals(TypeChambre.SIMPLE, result.getTypeC());

        verify(chambreRepository, times(1)).save(any(Chambre.class));
    }

    @Test
    void testFindAllChambres() {
        List<Chambre> chambres = new ArrayList<>();
        chambres.add(new Chambre());
        chambres.add(new Chambre());

        when(chambreRepository.findAll()).thenReturn(chambres);

        List<Chambre> result = chambreService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(chambreRepository, times(1)).findAll();
    }

    @Test
    void testFindByIdChambre() {
        Chambre chambre = new Chambre();
        chambre.setIdChambre(1L);
        chambre.setNumeroChambre(102);

        when(chambreRepository.findById(1L)).thenReturn(Optional.of(chambre));

        Chambre result = chambreService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getIdChambre());
        assertEquals(102, result.getNumeroChambre());

        verify(chambreRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteChambreById() {
        long chambreId = 1L;
        doNothing().when(chambreRepository).deleteById(chambreId);

        chambreService.deleteById(chambreId);

        verify(chambreRepository, times(1)).deleteById(chambreId);
    }

    @Test
    void testGetChambresParNomBloc() {
        List<Chambre> chambres = new ArrayList<>();
        chambres.add(new Chambre());
        chambres.add(new Chambre());

        when(chambreRepository.findByBlocNomBloc(anyString())).thenReturn(chambres);

        List<Chambre> result = chambreService.getChambresParNomBloc("Bloc A");

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(chambreRepository, times(1)).findByBlocNomBloc(anyString());
    }
}