/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/
/*
 * Created on May 9, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.internal.core.parser.pst;

import java.util.List;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

/**
 * @author aniefer
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface IDerivableContainerSymbol extends IContainerSymbol {

	public void addParent( ISymbol parent );
	public void addParent( ISymbol parent, boolean virtual, ASTAccessVisibility visibility, int offset, List references );
	public List getParents();
	public boolean hasParents();
	
	public interface IParentSymbol{
		public void setParent( ISymbol parent );
		public ISymbol getParent();
		public boolean isVirtual();
		public void setVirtual( boolean virtual );
	}
}
