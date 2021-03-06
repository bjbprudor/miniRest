package fr.m2miage.miniRest.controller;

import fr.m2miage.miniRest.model.*;
import fr.m2miage.miniRest.repository.CartePostaleRepository;
import fr.m2miage.miniRest.repository.CarteUtilisateurRepository;
import fr.m2miage.miniRest.repository.UtilisateurRepository;
import fr.m2miage.miniRest.repository.VarianteCarteRepository;
import fr.m2miage.miniRest.requestobjects.CarteUtilisateurBody;
import fr.m2miage.miniRest.services.CarteUtilisateurService;
import fr.m2miage.miniRest.util.CustomErrorType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
public class CarteUtilisateurController
{
    public static final Logger log = Logger.getLogger(CarteUtilisateurController.class);

    @Autowired
    private CarteUtilisateurService carteUtilisateurService;

    @Autowired
    private CarteUtilisateurRepository repo;

    @Autowired
    private UtilisateurRepository userRepo;

    @Autowired
    private VarianteCarteRepository varRepo;

    @Autowired
    private CartePostaleRepository cpRepo;


    // -------------------Recupere tous les CarteUtilisateurs---------------------------------------------
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/carteUtilisateur/", method = RequestMethod.GET)
    public ResponseEntity<List<CarteUtilisateur>> listAllCarteUtilisateur()
    {
        List<CarteUtilisateur> list = repo.findAll();
        if (list.isEmpty())
        {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
            // You many decide to return HttpStatus.NOT_FOUND
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // -------------------Recupere tous les CarteUtilisateurs---------------------------------------------
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/carteUtilisateur/{user_id}", method = RequestMethod.GET)
    public ResponseEntity<List<CarteUtilisateur>> listAllCarteUtilisateur(@PathVariable("user_id") int user)
    {
        List<CarteUtilisateur> list = repo.findAllById_Utilisateur_Id(user);
        if (list.isEmpty())
        {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
            // You many decide to return HttpStatus.NOT_FOUND
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    // -------------------Recupere un CarteUtilisateur------------------------------------------
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/carteUtilisateur/{user_id}/{variante_id}/{carte_id}", method = RequestMethod.GET)
    public ResponseEntity<?> getCarteUtilisateur(@PathVariable("user_id") int user, @PathVariable("variante_id") int variante, @PathVariable("carte_id") int carte)
    {

        Utilisateur util = userRepo.findOne(user);
        CartePostale cp = cpRepo.findOne(carte);
        VarianteCarteId vid = new VarianteCarteId(variante,cp);
        VarianteCarte var = varRepo.findOne(vid);
        CarteUtilisateurId cid = new CarteUtilisateurId(var,util);

        String msg = String.format("Fetching CarteUtilisateur with id {%s}",cid);
        log.info(msg);

        CarteUtilisateur current = repo.findOne(cid);
        if (current == null)
        {
            msg = String.format("CarteUtilisateur with id {%s} not found.",cid);
            log.error(msg);
            return new ResponseEntity(new CustomErrorType(msg), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(current, HttpStatus.OK);
    }

    // -------------------Create a CarteUtilisateur-------------------------------------------
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/carteUtilisateur/", method = RequestMethod.POST)
    public ResponseEntity<?> createCarteUtilisateur(@RequestBody CarteUtilisateurBody target, UriComponentsBuilder ucBuilder)
    {
        try
        {
            String msg = String.format("Creating CarteUtilisateur : {%s}", target);
            log.info(msg);

            CartePostale cp = cpRepo.findOne(target.getIdCarte());
            VarianteCarteId vid = new VarianteCarteId(target.getIdVariante(),cp);
            VarianteCarte vc = varRepo.findOne(vid);
            Utilisateur u = userRepo.getOne(target.getIdUtilisateur());
            CarteUtilisateurId cuid = new CarteUtilisateurId(vc,u);
            CarteUtilisateur current = repo.findOne(cuid);
            if (current != null)
            {
                msg = String.format("Unable to create. A Editeur with id {%s} already exist", target.getIdCarte());
                log.error(msg);
                return new ResponseEntity(new CustomErrorType(msg),HttpStatus.CONFLICT);
            }
            CarteUtilisateur cut = new CarteUtilisateur(cuid,target.getNombreExemplaire());
            repo.save(cut);
            //CarteUtilisateur current = carteUtilisateurService.create(target);

            //HttpHeaders headers = new HttpHeaders();
            //headers.setLocation(ucBuilder.path(String.format("/carteUtilisateur/{?s}/{%s}/{%s}",target.getIdUtilisateur(),target.getIdVariante(),target.getIdCarte())));
            return new ResponseEntity<>(current, HttpStatus.CREATED);
        }
        catch (Exception ex)
        {
            return new ResponseEntity<Object>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ------------------- Update a CarteUtilisateur ------------------------------------------------
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/carteUtilisateur/", method = RequestMethod.PUT)
    public ResponseEntity<?> updateCarteUtilisateur(@RequestBody CarteUtilisateurBody target)
    {


            String msg = String.format("Updating CarteUtilisateur with id {%s,%s,%s}",target.getIdCarte(),target.getIdVariante(),target.getIdUtilisateur());
            log.info(msg);

        CartePostale cp = cpRepo.findOne(target.getIdCarte());
        VarianteCarteId vid = new VarianteCarteId(target.getIdVariante(),cp);
        VarianteCarte vc = varRepo.findOne(vid);
        Utilisateur u = userRepo.getOne(target.getIdUtilisateur());
        CarteUtilisateurId cuid = new CarteUtilisateurId(vc,u);
        CarteUtilisateur current = repo.findOne(cuid);

        if (current == null)
        {
            msg = String.format("Unable to update. Utilisateur with id {%s} not found.");
            log.error(msg);
            return new ResponseEntity(new CustomErrorType(msg),HttpStatus.NOT_FOUND);
        }
        current.setNombreExemplaires(target.getNombreExemplaire());
        repo.save(current);

            /*CarteUtilisateur current = carteUtilisateurService.update(target);
            if (current == null)
            {
                msg = String.format("Unable to update. CarteUtilisateur with id {%s,%s,%s} not found.", target.getIdCarte(),target.getIdVariante(),target.getIdUtilisateur());
                log.error(msg);
                return new ResponseEntity(new CustomErrorType(msg),HttpStatus.NOT_FOUND);
            }*/
            return new ResponseEntity<>(current, HttpStatus.OK);

    }

    // ------------------- Delete a CarteUtilisateur-----------------------------------------
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/carteUtilisateur/{user_id}/{variante_id}/{carte_id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteCarteUtilisateur(@PathVariable("user_id") int user, @PathVariable("variante_id") int variante, @PathVariable("carte_id") int carte)
    {
        Utilisateur util = userRepo.findOne(user);
        CartePostale cp = cpRepo.findOne(carte);
        VarianteCarteId vid = new VarianteCarteId(variante,cp);
        VarianteCarte var = varRepo.findOne(vid);
        CarteUtilisateurId cid = new CarteUtilisateurId(var,util);

        String msg = String.format("Fetching & Deleting CarteUtilisateur with id {%s}",cid);
        log.info(msg);

        CarteUtilisateur current = repo.findOne(cid);
        if (current == null)
        {
            msg = String.format("Unable to delete. CarteUtilisateur with id {%s} not found.",cid);
            return new ResponseEntity(new CustomErrorType(msg), HttpStatus.NOT_FOUND);
        }
        repo.delete(cid);
        return new ResponseEntity<CarteUtilisateur>(HttpStatus.NO_CONTENT);
    }

    // ------------------- Delete All CarteUtilisateur-----------------------------
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/carteUtilisateur/", method = RequestMethod.DELETE)
    public ResponseEntity<CarteUtilisateur> deleteAllCarteUtilisateur()
    {
        log.info("Deleting All CarteUtilisateur");
        repo.deleteAll();
        return new ResponseEntity<CarteUtilisateur>(HttpStatus.NO_CONTENT);
    }

    // -------------------Recupere tous les noms de communes ayant des cartes commence par le paramètre ---------------------------------------------
    @CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/usersId/{carteId}", method= RequestMethod.GET)
    public ResponseEntity<List<Integer>> getUsersIdByCp(@PathVariable(value = "carteId") Integer carteId)
    {
        List<Integer> list = carteUtilisateurService.getUsersIdByCp(carteId);
        if (list.isEmpty())
        {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
            // You many decide to return HttpStatus.NOT_FOUND
        }
        return new ResponseEntity<>(list, HttpStatus.OK);
    }


}