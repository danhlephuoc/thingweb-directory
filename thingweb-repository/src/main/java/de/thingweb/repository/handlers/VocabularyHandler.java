package de.thingweb.repository.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;

import de.thingweb.repository.Repository;
import de.thingweb.repository.ThingDescription;
import de.thingweb.repository.ThingDescriptionUtils;
import de.thingweb.repository.rest.BadRequestException;
import de.thingweb.repository.rest.NotFoundException;
import de.thingweb.repository.rest.RESTException;
import de.thingweb.repository.rest.RESTHandler;
import de.thingweb.repository.rest.RESTResource;
import de.thingweb.repository.rest.RESTServerInstance;

public class VocabularyHandler extends RESTHandler {
	
	public VocabularyHandler(String id, List<RESTServerInstance> instances) {
		super(id, instances);
	}

	@Override
	public RESTResource get(URI uri, Map<String, String> parameters) throws RESTException {
		RESTResource resource = new RESTResource(uri.toString(),this);

		Dataset dataset = Repository.get().dataset;
		dataset.begin(ReadWrite.READ);

		try {
			Model result = dataset.getNamedModel(uri.toString());
			if (!result.isEmpty()) {
				resource.contentType = "text/turtle";
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				result.write(out, "Turtle");
				resource.content = out.toString();
			} else {
				throw new RESTException();
			}
		} finally {
			dataset.end();
		}
		
		return resource;
	}
	
	@Override
	public void delete(URI uri, Map<String, String> parameters, InputStream payload) throws RESTException {
		Dataset dataset = Repository.get().dataset;
		dataset.begin(ReadWrite.WRITE);
		
		try {
			dataset.getDefaultModel().getResource(uri.toString()).removeProperties();
			dataset.removeNamedModel(uri.toString());
			deleteToAll(uri.getPath());
			dataset.commit();
		} catch (Exception e) {
			// TODO distinguish between client and server errors
			throw new RESTException();
		} finally {
			dataset.end();
		}
	}

}