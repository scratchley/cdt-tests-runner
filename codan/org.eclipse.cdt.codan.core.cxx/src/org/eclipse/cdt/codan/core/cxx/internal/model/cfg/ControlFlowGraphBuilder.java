/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.internal.model.cfg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.cdt.codan.internal.core.cfg.AbstractBasicBlock;
import org.eclipse.cdt.codan.internal.core.cfg.ConnectorNode;
import org.eclipse.cdt.codan.internal.core.cfg.DecisionNode;
import org.eclipse.cdt.codan.internal.core.cfg.JumpNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IBasicBlock;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IConnectorNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IExitNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IJumpNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IBranchNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IPlainNode;
import org.eclipse.cdt.codan.provisional.core.model.cfg.IStartNode;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;

/**
 * TODO: add description
 */
public class ControlFlowGraphBuilder {
	CxxStartNode start;
	Collection<IExitNode> exits;
	Collection<IBasicBlock> dead;
	CxxExitNode returnExit;
	CxxNodeFactory factory = new CxxNodeFactory();
	IConnectorNode outerBreak;
	IConnectorNode outerContinue;
	HashMap<String, IBasicBlock> labels = new HashMap<String, IBasicBlock>(0);

	/**
	 * @param def
	 * @return
	 */
	public CxxControlFlowGraph build(IASTFunctionDefinition def) {
		IASTStatement body = def.getBody();
		start = new CxxStartNode();
		exits = new ArrayList<IExitNode>();
		dead = new ArrayList<IBasicBlock>();
		IBasicBlock last = createSubGraph(start, body);
		if (!(last instanceof IExitNode)) {
			returnExit = (CxxExitNode) factory.createExitNode(null);
			returnExit.setStartNode(start);
			addOutgoing(last, returnExit);
		}
		CxxControlFlowGraph graph = new CxxControlFlowGraph(start, exits);
		graph.setUnconnectedNodes(dead);
		return graph;
	}

	/**
	 * @param start2
	 * @param body
	 */
	private IBasicBlock createSubGraph(IBasicBlock prev, IASTNode body) {
		if (body instanceof IASTCompoundStatement) {
			IASTCompoundStatement comp = (IASTCompoundStatement) body;
			IASTNode[] children = comp.getChildren();
			for (int i = 0; i < children.length; i++) {
				IASTNode node = children[i];
				IBasicBlock last = createSubGraph(prev, node);
				prev = last;
			}
		} else if (body instanceof IASTExpressionStatement
				|| body instanceof IASTDeclarationStatement
				|| body instanceof IASTNullStatement) {
			CxxPlainNode node = factory.createPlainNode(body);
			addOutgoing(prev, node);
			return node;
		} else if (body instanceof IASTIfStatement) {
			return createIf(prev, (IASTIfStatement) body);
		} else if (body instanceof IASTWhileStatement) {
			return createWhile(prev, (IASTWhileStatement) body);
		} else if (body instanceof IASTForStatement) {
			return createFor(prev, (IASTForStatement) body);
		} else if (body instanceof IASTDoStatement) {
			return createDoWhile(prev, (IASTDoStatement) body);
		} else if (body instanceof IASTReturnStatement) {
			CxxExitNode node = factory.createExitNode(body);
			node.setStartNode(start);
			addOutgoing(prev, node);
			return node;
		} else if (body instanceof IASTBreakStatement) {
			if (outerBreak != null)
				return addJump(prev, outerBreak);
			return prev;
		} else if (body instanceof IASTContinueStatement) {
			if (outerContinue != null)
				return addJump(prev, outerContinue);
			return prev;
		} else if (body instanceof IASTSwitchStatement) {
			return createSwitch(prev, (IASTSwitchStatement) body);
		} else if (body instanceof IASTLabelStatement) {
			IASTLabelStatement ast = (IASTLabelStatement) body;
			String labelName = ast.getName().toString();
			IBranchNode labNode = (IBranchNode) labels.get(labelName);
			IConnectorNode conn;
			if (labNode != null) {
				conn = (IConnectorNode) labNode.getOutgoing();
				addOutgoing(prev, labNode);
			} else {
				// labeled statement contains of connector for jumps, branch for
				// label
				// and nested statement
				conn = createLabelNodes(prev, labelName);
			}
			return createSubGraph(conn, ast.getNestedStatement());
		} else if (body instanceof IASTGotoStatement) {
			IASTGotoStatement ast = (IASTGotoStatement) body;
			String labelName = ast.getName().toString();
			IConnectorNode conn;
			IBranchNode labNode = (IBranchNode) labels.get(labelName);
			if (labNode != null) {
				conn = (IConnectorNode) labNode.getOutgoing();
			} else {
				conn = createLabelNodes(null, labelName);
			}
			IJumpNode gotoNode = factory.createJumpNode();
			((JumpNode) gotoNode).setJump(conn, labNode != null);
			addOutgoing(prev, gotoNode);
			return gotoNode;
		} else if (body instanceof IASTProblemStatement) {
			System.err.println("problem");
			CxxPlainNode node = factory.createPlainNode(body);
			addOutgoing(prev, node);
			return node;
			
		} else if (body instanceof IASTAmbiguousStatement) {
			System.err.println("amb");
		} else {
			System.err.println("unknown statement for cfg: "+body);
		}
		return prev;
	}

	/**
	 * @param prev
	 * @param labelName
	 * @return
	 */
	protected IConnectorNode createLabelNodes(IBasicBlock prev, String labelName) {
		IBranchNode branch = factory.createBranchNode(labelName);
		if (prev != null)
			addOutgoing(prev, branch);
		labels.put(labelName, branch);
		IConnectorNode conn = factory.createConnectorNode();
		addOutgoing(branch, conn);
		return conn;
	}

	/**
	 * @param prev
	 * @param body
	 * @return
	 */
	protected IBasicBlock createIf(IBasicBlock prev, IASTIfStatement body) {
		DecisionNode ifNode = factory.createDecisionNode(body
				.getConditionExpression());
		addOutgoing(prev, ifNode);
		IConnectorNode mergeNode = factory.createConnectorNode();
		ifNode.setMergeNode(mergeNode);
		IBranchNode thenNode = factory.createBranchNode(IBranchNode.THEN);
		addOutgoing(ifNode, thenNode);
		IBasicBlock then = createSubGraph(thenNode, body.getThenClause());
		addJump(then, mergeNode);
		IBranchNode elseNode = factory.createBranchNode(IBranchNode.ELSE);
		addOutgoing(ifNode, elseNode);
		IBasicBlock els = createSubGraph(elseNode, body.getElseClause());
		addJump(els, mergeNode);
		return mergeNode;
	}

	/**
	 * @param prev
	 * @param body
	 * @return
	 */
	private IBasicBlock createSwitch(IBasicBlock prev, IASTSwitchStatement body) {
		DecisionNode node = factory.createDecisionNode(body
				.getControllerExpression());
		addOutgoing(prev, node);
		ConnectorNode conn = new ConnectorNode();
		node.setMergeNode(conn);
		createSwitchBody(node, conn, body.getBody());
		return conn;
	}

	/**
	 * @param switchNode
	 * @param mergeNode
	 * @param def
	 * @param body
	 */
	private void createSwitchBody(DecisionNode switchNode,
			ConnectorNode mergeNode, IASTStatement body) {
		if (!(body instanceof IASTCompoundStatement))
			return; // bad
		IASTCompoundStatement comp = (IASTCompoundStatement) body;
		IASTNode[] children = comp.getChildren();
		IBasicBlock prev = switchNode;
		for (int i = 0; i < children.length; i++) {
			IASTNode elem = children[i];
			if (elem instanceof IASTCaseStatement) {
				IASTCaseStatement caseSt = (IASTCaseStatement) elem;
			}
			if (elem instanceof IASTDefaultStatement) {
				IBranchNode lbl = factory.createBranchNode(IBranchNode.DEFAULT);
				if (!(prev instanceof IExitNode) && prev != switchNode)
					addOutgoing(prev, lbl);
				addOutgoing(switchNode, lbl);
				prev = lbl;
				continue;
			}
			if (elem instanceof IASTCaseStatement
					|| elem instanceof IASTDefaultStatement) {
				IBranchNode lbl = null;
				if (elem instanceof IASTCaseStatement) {
					IASTCaseStatement caseSt = (IASTCaseStatement) elem;
					lbl = factory.createBranchNode(caseSt);
				} else if (elem instanceof IASTDefaultStatement) {
					lbl = factory.createBranchNode(IBranchNode.DEFAULT);
				}
				if (!(prev instanceof IExitNode) && prev != switchNode) {
					IConnectorNode here = factory.createConnectorNode();
					addJump(prev, here);
					addOutgoing(lbl, here);
					prev = here;
				} else {
					prev = lbl;
				}
				addOutgoing(switchNode, lbl);
				continue;
			}
			if (elem instanceof IASTBreakStatement) {
				prev = addJump(prev, mergeNode);
				continue;
			}
			IBasicBlock last = createSubGraph(prev, elem);
			prev = last;
		}
	}

	/**
	 * @param prev
	 * @param forNode
	 * @return
	 */
	private IBasicBlock createFor(IBasicBlock prev, IASTForStatement forNode) {
		// add initializer
		IPlainNode init = factory.createPlainNode(forNode
				.getInitializerStatement());
		addOutgoing(prev, init);
		prev = init;
		// add continue connector
		IConnectorNode beforeCheck = factory.createConnectorNode();
		addOutgoing(prev, beforeCheck);
		// decision node
		CxxDecisionNode decision = factory.createDecisionNode(forNode
				.getConditionExpression());
		addOutgoing(beforeCheck, decision);
		// add break connector
		IConnectorNode nBreak = factory.createConnectorNode();
		decision.setMergeNode(nBreak);
		// create body and jump to continue node
		IBranchNode loopStart = factory.createBranchNode(IBranchNode.THEN);
		addOutgoing(decision, loopStart);
		// set break/continue
		IConnectorNode nContinue = factory.createConnectorNode();
		IConnectorNode savedContinue = outerContinue;
		IConnectorNode savedBreak = outerBreak;
		outerContinue = nContinue;
		outerBreak = nBreak;
		IBasicBlock endBody = createSubGraph(loopStart, forNode.getBody());
		outerContinue = savedContinue;
		outerBreak = savedBreak;
		// inc
		IPlainNode inc = factory.createPlainNode(forNode
				.getIterationExpression());
		addOutgoing(endBody, nContinue);
		addOutgoing(nContinue, inc);
		// connect with backward link
		addJump(inc, beforeCheck, true);
		// add "else" branch
		IBranchNode loopEnd = factory.createBranchNode(IBranchNode.ELSE);
		addOutgoing(decision, loopEnd);
		addJump(loopEnd, nBreak);
		return nBreak;
	}

	/**
	 * @param prev
	 * @param body
	 * @return
	 */
	protected IBasicBlock createWhile(IBasicBlock prev, IASTWhileStatement body) {
		// add continue connector
		IConnectorNode nContinue = factory.createConnectorNode();
		addOutgoing(prev, nContinue);
		// decision node
		CxxDecisionNode decision = factory.createDecisionNode(body
				.getCondition());
		addOutgoing(nContinue, decision);
		// add break connector
		IConnectorNode nBreak = factory.createConnectorNode();
		decision.setMergeNode(nBreak);
		// create body and jump to continue node
		IBranchNode loopStart = factory.createBranchNode(IBranchNode.THEN);
		addOutgoing(decision, loopStart);
		// set break/continue
		IConnectorNode savedContinue = outerContinue;
		IConnectorNode savedBreak = outerBreak;
		outerContinue = nContinue;
		outerBreak = nBreak;
		IBasicBlock endBody = createSubGraph(loopStart, body.getBody());
		// restore
		outerContinue = savedContinue;
		outerBreak = savedBreak;
		// backward jump
		addJump(endBody, nContinue, true);
		// connect with else branch
		IBranchNode loopEnd = factory.createBranchNode(IBranchNode.ELSE);
		addOutgoing(decision, loopEnd);
		addJump(loopEnd, nBreak);
		return nBreak;
	}

	protected IBasicBlock createDoWhile(IBasicBlock prev, IASTDoStatement body) {
		// create body and jump to continue node
		IConnectorNode loopStart = factory.createConnectorNode();
		addOutgoing(prev, loopStart);
		// continue/break
		IConnectorNode nContinue = factory.createConnectorNode();
		IConnectorNode nBreak = factory.createConnectorNode();
		IConnectorNode savedContinue = outerContinue;
		IConnectorNode savedBreak = outerBreak;
		outerContinue = nContinue;
		outerBreak = nBreak;
		IBasicBlock endBody = createSubGraph(loopStart, body.getBody());
		// restore
		outerContinue = savedContinue;
		outerBreak = savedBreak;
		// add continue connector
		addOutgoing(endBody, nContinue);
		// decision node
		CxxDecisionNode decision = factory.createDecisionNode(body
				.getCondition());
		addOutgoing(nContinue, decision);
		// then branch
		IBranchNode thenNode = factory.createBranchNode(IBranchNode.THEN);
		addOutgoing(decision, thenNode);
		IJumpNode jumpToStart = factory.createJumpNode();
		addOutgoing(thenNode, jumpToStart);
		((JumpNode) jumpToStart).setBackward(true);
		// connect with backward link
		addOutgoing(jumpToStart, loopStart);
		// connect with else branch
		IBranchNode loopEnd = factory.createBranchNode(IBranchNode.ELSE);
		addOutgoing(decision, loopEnd);
		// add break connector
		decision.setMergeNode(nBreak);
		addJump(loopEnd, nBreak);
		return nBreak;
	}

	private IJumpNode addJump(IBasicBlock prev, IConnectorNode conn) {
		return addJump(prev, conn, false);
	}

	private IJumpNode addJump(IBasicBlock prev, IConnectorNode conn,
			boolean backward) {
		if (prev instanceof IJumpNode)
			return (IJumpNode) prev;
		if (prev instanceof IExitNode)
			return null;
		IJumpNode jump = factory.createJumpNode();
		addOutgoing(prev, jump);
		addOutgoing(jump, conn);
		((JumpNode) jump).setBackward(backward);
		return jump;
	}

	/**
	 * @param prev
	 * @param node
	 */
	private void addOutgoing(IBasicBlock prev, IBasicBlock node) {
		if (prev instanceof IExitNode || prev == null) {
			dead.add(node);
			return;
		} else if (prev instanceof AbstractBasicBlock) {
			((AbstractBasicBlock) prev).addOutgoing(node);
		}
		if (!(node instanceof IStartNode))
			((AbstractBasicBlock) node).addIncoming(prev);
	}
}