package nl.ordina.jobcrawler.model.assembler;

import nl.ordina.jobcrawler.controller.LocationController;
import nl.ordina.jobcrawler.model.Location;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class LocationModelAssembler implements RepresentationModelAssembler<Location, EntityModel<Location>> {

    @Override
    public EntityModel<Location> toModel(Location location) {
        return EntityModel.of(location,linkTo(methodOn(LocationController.class).getLocationByIdOrName(location.getName())).withSelfRel(),
        linkTo(methodOn(LocationController.class).getLocations()).withRel("locations"));
    }

    @Override
    public CollectionModel<EntityModel<Location>> toCollectionModel(Iterable<? extends Location> locations) {
        List<EntityModel<Location>> returnLocations = new ArrayList<>();
        locations.forEach(l -> returnLocations.add(toModel(l)));
        return CollectionModel.of(returnLocations,
                linkTo(methodOn(LocationController.class).getLocations()).withSelfRel()
        );
    }
}
