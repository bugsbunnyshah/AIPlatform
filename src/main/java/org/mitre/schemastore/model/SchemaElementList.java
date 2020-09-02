// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model;

import java.util.ArrayList;
import java.util.Arrays;

/** Class for storing a list of schema elements */
public class SchemaElementList
{
	// Holds the schema elements
	private org.mitre.schemastore.model.Entity[] entities;
	private org.mitre.schemastore.model.Attribute[] attributes;
	private org.mitre.schemastore.model.Domain[] domains;
	private org.mitre.schemastore.model.DomainValue[] domainValues;
	private org.mitre.schemastore.model.Relationship[] relationships;
	private org.mitre.schemastore.model.Containment[] containments;
	private org.mitre.schemastore.model.Subtype[] subtypes;
	private org.mitre.schemastore.model.Synonym[] synonyms;
	private org.mitre.schemastore.model.Alias[] aliases;
	
	/** Provides a default constructor for this class */
	public SchemaElementList() {}
	
	/** Constructs the list of schema elements */
	public SchemaElementList(org.mitre.schemastore.model.SchemaElement[] schemaElements)
	{
		// Create arrays for each type of schema object
		ArrayList<org.mitre.schemastore.model.Entity> entities = new ArrayList<org.mitre.schemastore.model.Entity>();
		ArrayList<org.mitre.schemastore.model.Attribute> attributes = new ArrayList<org.mitre.schemastore.model.Attribute>();
		ArrayList<org.mitre.schemastore.model.Relationship> relationships = new ArrayList<org.mitre.schemastore.model.Relationship>();
		ArrayList<org.mitre.schemastore.model.Domain> domains = new ArrayList<org.mitre.schemastore.model.Domain>();
		ArrayList<org.mitre.schemastore.model.DomainValue> domainValues = new ArrayList<org.mitre.schemastore.model.DomainValue>();
		ArrayList<org.mitre.schemastore.model.Containment> containments = new ArrayList<org.mitre.schemastore.model.Containment>();
		ArrayList<org.mitre.schemastore.model.Subtype> subtypes = new ArrayList<org.mitre.schemastore.model.Subtype>();
		ArrayList<org.mitre.schemastore.model.Synonym> synonyms = new ArrayList<org.mitre.schemastore.model.Synonym>();
		ArrayList<org.mitre.schemastore.model.Alias> aliases = new ArrayList<org.mitre.schemastore.model.Alias>();
		
		// Place the schema elements in the proper arrays
		for(org.mitre.schemastore.model.SchemaElement schemaElement : schemaElements)
		{
			if(schemaElement instanceof org.mitre.schemastore.model.Entity) entities.add((org.mitre.schemastore.model.Entity)schemaElement);
			if(schemaElement instanceof org.mitre.schemastore.model.Attribute) attributes.add((org.mitre.schemastore.model.Attribute)schemaElement);
			if(schemaElement instanceof org.mitre.schemastore.model.Domain) domains.add((org.mitre.schemastore.model.Domain)schemaElement);
			if(schemaElement instanceof org.mitre.schemastore.model.DomainValue) domainValues.add((org.mitre.schemastore.model.DomainValue)schemaElement);
			if(schemaElement instanceof org.mitre.schemastore.model.Relationship) relationships.add((org.mitre.schemastore.model.Relationship)schemaElement);
			if(schemaElement instanceof org.mitre.schemastore.model.Containment) containments.add((org.mitre.schemastore.model.Containment)schemaElement);
			if(schemaElement instanceof org.mitre.schemastore.model.Subtype) subtypes.add((org.mitre.schemastore.model.Subtype)schemaElement);
			if(schemaElement instanceof org.mitre.schemastore.model.Synonym) synonyms.add((org.mitre.schemastore.model.Synonym)schemaElement);
			if(schemaElement instanceof org.mitre.schemastore.model.Alias) aliases.add((org.mitre.schemastore.model.Alias)schemaElement);
		}
		
		// Store the schema objects lists
		this.entities = entities.toArray(new org.mitre.schemastore.model.Entity[0]);
		this.attributes = attributes.toArray(new org.mitre.schemastore.model.Attribute[0]);
		this.domains = domains.toArray(new org.mitre.schemastore.model.Domain[0]);
		this.domainValues = domainValues.toArray(new org.mitre.schemastore.model.DomainValue[0]);
		this.relationships = relationships.toArray(new org.mitre.schemastore.model.Relationship[0]);
		this.containments = containments.toArray(new org.mitre.schemastore.model.Containment[0]);
		this.subtypes = subtypes.toArray(new org.mitre.schemastore.model.Subtype[0]);
		this.synonyms = synonyms.toArray(new org.mitre.schemastore.model.Synonym[0]);
		this.aliases = aliases.toArray(new org.mitre.schemastore.model.Alias[0]);
	}
	
	// Handles all getters for this class
	public org.mitre.schemastore.model.Entity[] getEntities() { return entities; }
	public org.mitre.schemastore.model.Attribute[] getAttributes() { return attributes; }
	public org.mitre.schemastore.model.Domain[] getDomains() { return domains; }
	public org.mitre.schemastore.model.DomainValue[] getDomainValues() { return domainValues; }
	public org.mitre.schemastore.model.Relationship[] getRelationships() { return relationships; }
	public org.mitre.schemastore.model.Containment[] getContainments() { return containments; }
	public org.mitre.schemastore.model.Subtype[] getSubtypes() { return subtypes; }
	public org.mitre.schemastore.model.Synonym[] getSynonyms() { return synonyms; }
	public org.mitre.schemastore.model.Alias[] getAliases() { return aliases; }

	// Handles all setters for this class
	public void setEntities(Entity[] entities) { this.entities = entities; }
	public void setAttributes(Attribute[] attributes) { this.attributes = attributes; }
	public void setDomains(Domain[] domains) { this.domains = domains; }
	public void setDomainValues(DomainValue[] domainValues) { this.domainValues = domainValues; }
	public void setRelationships(Relationship[] relationships) { this.relationships = relationships; }
	public void setContainments(Containment[] containments) { this.containments = containments; }
	public void setSubtypes(Subtype[] subtypes) { this.subtypes = subtypes; }
	public void setSynonyms(Synonym[] synonyms) { this.synonyms = synonyms; }
	public void setAliases(Alias[] aliases) { this.aliases = aliases; }

	/** Returns the schema elements */
	public org.mitre.schemastore.model.SchemaElement[] geetSchemaElements()
	{
		ArrayList<org.mitre.schemastore.model.SchemaElement> schemaElements = new ArrayList<org.mitre.schemastore.model.SchemaElement>();
		schemaElements.addAll(Arrays.asList(entities));
		schemaElements.addAll(Arrays.asList(attributes));
		schemaElements.addAll(Arrays.asList(domains));
		schemaElements.addAll(Arrays.asList(domainValues));
		schemaElements.addAll(Arrays.asList(relationships));
		schemaElements.addAll(Arrays.asList(containments));
		schemaElements.addAll(Arrays.asList(subtypes));
		schemaElements.addAll(Arrays.asList(synonyms));
		schemaElements.addAll(Arrays.asList(aliases));
		return schemaElements.toArray(new SchemaElement[0]);
	}
}