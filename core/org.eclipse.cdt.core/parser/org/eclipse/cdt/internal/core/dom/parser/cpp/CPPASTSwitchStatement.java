/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class CPPASTSwitchStatement extends ASTNode implements
        ICPPASTSwitchStatement, IASTAmbiguityParent {

	private IScope scope;
    private IASTExpression controller;
    private IASTStatement body;
    private IASTDeclaration decl;

    
    public CPPASTSwitchStatement() {
	}

	public CPPASTSwitchStatement(IASTDeclaration controller, IASTStatement body) {
		setControllerDeclaration(controller);
		setBody(body);
	}
    
    public CPPASTSwitchStatement(IASTExpression controller, IASTStatement body) {
		setControllerExpression(controller);
		setBody(body);
	}

	public IASTExpression getControllerExpression() {
        return controller;
    }

    public void setControllerExpression(IASTExpression controller) {
        assertNotFrozen();
        this.controller = controller;
        if (controller != null) {
			controller.setParent(this);
			controller.setPropertyInParent(CONTROLLER_EXP);
		}
    }

    public IASTStatement getBody() {
        return body;
    }
    
    public void setBody(IASTStatement body) {
        assertNotFrozen();
        this.body = body;
        if (body != null) {
			body.setParent(this);
			body.setPropertyInParent(BODY);
		}
    }

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitStatements ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( controller != null ) if( !controller.accept( action ) ) return false;
        if( decl != null ) if( !decl.accept( action ) ) return false;
        if( body != null ) if( !body.accept( action ) ) return false;
        
        if( action.shouldVisitStatements ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }
    
    public void replace(IASTNode child, IASTNode other) {
        if( body == child )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            body = (IASTStatement) other;
        }
        if( child == controller )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            controller  = (IASTExpression) other;
        }
        if( child == decl )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            decl  = (IASTDeclaration) other;            
        }
            
    }

    public IASTDeclaration getControllerDeclaration() {
        return decl;
    }

    public void setControllerDeclaration(IASTDeclaration d) {
        assertNotFrozen();
        decl = d;
        if (d != null) {
			d.setParent(this);
			d.setPropertyInParent(CONTROLLER_DECLARATION);
		}
    }

	public IScope getScope() {
		if( scope == null )
            scope = new CPPBlockScope( this );
        return scope;	
    }

}
