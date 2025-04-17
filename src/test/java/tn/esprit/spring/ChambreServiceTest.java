package tn.esprit.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.DAO.Entities.Chambre;
import tn.esprit.spring.DAO.Entities.TypeChambre;
import tn.esprit.spring.DAO.Repositories.BlocRepository;
import tn.esprit.spring.DAO.Repositories.ChambreRepository;
import tn.esprit.spring.services.Chambre.ChambreService;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChambreServiceTest {

    @Mock
    private ChambreRepository chambreRepository;

    @Mock
    private BlocRepository blocRepository;

    @InjectMocks
    private ChambreService chambreService;

    private Chambre chambre;

    @BeforeEach
    void setUp() {
        chambre = new Chambre();
        chambre.setIdChambre(1L);
        chambre.setNumeroChambre(101);
        chambre.setTypeC(TypeChambre.SIMPLE);
    }

    @Test
    void testAddOrUpdate() {
        when(chambreRepository.save(chambre)).thenReturn(chambre);
        Chambre savedChambre = chambreService.addOrUpdate(chambre);
        assertNotNull(savedChambre);
        assertEquals(1L, savedChambre.getIdChambre());
    }

    @Test
    void testFindAll() {
        when(chambreRepository.findAll()).thenReturn(Arrays.asList(chambre));
        List<Chambre> chambres = chambreService.findAll();
        assertFalse(chambres.isEmpty());
        assertEquals(1, chambres.size());
    }

    @Test
    void testFindById() {
        when(chambreRepository.findById(1L)).thenReturn(Optional.of(chambre));
        Chambre foundChambre = chambreService.findById(1L);
        assertNotNull(foundChambre);
        assertEquals(101, foundChambre.getNumeroChambre());
    }

    @Test
    void testDeleteById() {
        doNothing().when(chambreRepository).deleteById(1L);
        chambreService.deleteById(1L);
        verify(chambreRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDelete() {
        doNothing().when(chambreRepository).delete(chambre);
        chambreService.delete(chambre);
        verify(chambreRepository, times(1)).delete(chambre);
    }
}