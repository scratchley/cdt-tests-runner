/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug;

import org.eclipse.dd.dsf.concurrent.GetDataDone;
import org.eclipse.dd.dsf.debug.IMemory.IAddress;
import org.eclipse.dd.dsf.model.IDataModelContext;
import org.eclipse.dd.dsf.model.IDataModelData;
import org.eclipse.dd.dsf.model.IDataModelService;

/**
 * Stack service provides access to stack information for a 
 * given execution context.
 */
public interface IStack extends IDataModelService {

    /**
     * Context for a specific stack frame.  Besides allowing access to stack
     * frame data, this context is used by other services that require a stack
     * frame for evaluation.  
     */
    public interface IFrameDMC extends IDataModelContext<IFrameData> {}

    /**
     * Stack frame information. 
     */
    public interface IFrameData extends IDataModelData {
        int getLevel();
        IAddress getAddress();
        String getFile();
        String getFunction();
        int getLine();
        int getColumn();
    }
    
    /**
     * Variable context.  This context only provides access to limited 
     * expression information.  For displaying complete information, 
     * Expressions service should be used.
     */
    public interface IVariableDMC extends IDataModelContext<IVariableData> {}

    /** 
     * Stack frame variable information.
     */
    public interface IVariableData extends IDataModelData {
        String getName();
        String getValue();
    }

    /**
     * Returns whether the stack frames can be retrieved for given thread.
     * <br>
     * TODO: I'm not sure if this method should be async.  It assumes that the 
     * implementation can determine if stack is available based on process
     * state information. 
     */
    boolean isStackAvailable(IRunControl.IExecutionDMC execContext);
    
    /**
     * Retrieves list of stack frames for the given execution context.  Request
     * will fail if the stack frame data is not available.
     */
    void getFrames(IRunControl.IExecutionDMC execContext, GetDataDone<IFrameDMC[]> done);
    
    /**
     * Retrieves the top stack frame for the given execution context.  
     * Retrieving just the top frame DMC and corresponding data can be much 
     * more efficient than just retrieving the whole stack, before the data
     * is often included in the stopped event.  Also for some UI functionality, 
     * such as setpping, only top stack frame is often needed. 
     * @param execContext
     * @param done
     */
    void getTopFrame(IRunControl.IExecutionDMC execContext, GetDataDone<IFrameDMC> done);
    
    /**
     * Retrieves variables which were arguments to the stack frame's function.
     */
    void getArguments(IFrameDMC frameCtx, GetDataDone<IVariableDMC[]> done);
    
    /**
     * Retrieves variables local to the stack frame.
     */
    void getLocals(IFrameDMC frameCtx, GetDataDone<IVariableDMC[]> done);
}
