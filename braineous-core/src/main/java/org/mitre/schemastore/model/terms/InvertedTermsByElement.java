// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.terms;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Class storing an inverted list of terms
 * @author CWOLF
 */
public class InvertedTermsByElement implements Serializable
{	
	/** Stores the inverted list of terms by associated element */
	private HashMap<org.mitre.schemastore.model.terms.AssociatedElement, org.mitre.schemastore.model.terms.Terms> invertedList = new HashMap<org.mitre.schemastore.model.terms.AssociatedElement, org.mitre.schemastore.model.terms.Terms>();
	
	/** Constructs the terms */
	public InvertedTermsByElement(org.mitre.schemastore.model.terms.Terms terms)
		{ for(org.mitre.schemastore.model.terms.Term term : terms.getTerms()) addTerm(term); }
	
	/** Adds a term to the inverted list */
	public void addTerm(org.mitre.schemastore.model.terms.Term term)
	{
		for(org.mitre.schemastore.model.terms.AssociatedElement element : term.getElements())
		{
			org.mitre.schemastore.model.terms.Terms terms = invertedList.get(element);
			if(terms==null) invertedList.put(element, terms=new org.mitre.schemastore.model.terms.Terms());
			terms.addTerm(term);
		}
	}
	
	/** Updates a term in the inverted list */
	public void updateTerm(org.mitre.schemastore.model.terms.Term oldTerm, org.mitre.schemastore.model.terms.Term newTerm)
		{ removeTerm(oldTerm); addTerm(newTerm); }
	
	/** Removes a term from the inverted list */
	public void removeTerm(Term term)
	{
		for(org.mitre.schemastore.model.terms.AssociatedElement element : term.getElements())
		{
			org.mitre.schemastore.model.terms.Terms terms = invertedList.get(element);
			if(terms!=null) terms.removeTerm(term);
		}
	}
	
	/** Get the terms associated with the specified element */
	public org.mitre.schemastore.model.terms.Terms getTerms(org.mitre.schemastore.model.terms.AssociatedElement element)
	{
		org.mitre.schemastore.model.terms.Terms terms = invertedList.get(element);
		return terms==null ? new org.mitre.schemastore.model.terms.Terms() : terms;
	}
	
	/** Get the terms associated with the specified element */
	public Terms getTerms(Integer schemaID, Integer elementID)
		{ return getTerms(new AssociatedElement(schemaID,elementID,"","")); }
}