// Copyright 2008 The MITRE Corporation. ALL RIGHTS RESERVED.

package org.mitre.schemastore.model.terms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class storing terms
 * @author CWOLF
 */
public class Terms implements Serializable
{	
	/** Stores the list of terms */
	private org.mitre.schemastore.model.terms.Term[] terms = new org.mitre.schemastore.model.terms.Term[0];
	
	/** Constructs the default list of terms */ public Terms() {}
	
	/** Constructs the terms */
	protected Terms(org.mitre.schemastore.model.terms.Term[] terms)
		{ this.terms = terms; }
	
	/** Copies the terms */
	protected org.mitre.schemastore.model.terms.Term[] copyTerms()
	{
		ArrayList<org.mitre.schemastore.model.terms.Term> copiedTerms = new ArrayList<org.mitre.schemastore.model.terms.Term>();
		for(org.mitre.schemastore.model.terms.Term term : terms) copiedTerms.add(term.copy());
		return copiedTerms.toArray(new org.mitre.schemastore.model.terms.Term[0]);
	}
	
	/** Gets the specified term */
	public org.mitre.schemastore.model.terms.Term getTerm(int termID)
	{
		if(terms==null) return null;
		for(org.mitre.schemastore.model.terms.Term term : terms)
			if(termID==term.getId()) return term;
		return null;
	}
	
	/** Gets the list of terms */
	public org.mitre.schemastore.model.terms.Term[] getTerms() { return terms; }

	/** Sets the list of terms */
	public void setTerms(org.mitre.schemastore.model.terms.Term[] terms) { this.terms = terms; }
	
	/** Adds a term to the term list */
	public void addTerm(org.mitre.schemastore.model.terms.Term newTerm)
	{
		ArrayList<org.mitre.schemastore.model.terms.Term> terms = new ArrayList<org.mitre.schemastore.model.terms.Term>(Arrays.asList(this.terms));
		if(!terms.contains(newTerm))
		{
			terms.add(newTerm);
			this.terms = terms.toArray(new org.mitre.schemastore.model.terms.Term[0]);
		}
	}
	
	/** Removes a term from the term list */
	public void removeTerm(org.mitre.schemastore.model.terms.Term oldTerm)
	{
		ArrayList<org.mitre.schemastore.model.terms.Term> terms = new ArrayList<org.mitre.schemastore.model.terms.Term>(Arrays.asList(this.terms));
		terms.remove(oldTerm);
		this.terms = terms.toArray(new Term[0]);
	}
	
	/** Removes a term from the term list */
	public void removeTerm(int termID)
		{ removeTerm(getTerm(termID)); }
}