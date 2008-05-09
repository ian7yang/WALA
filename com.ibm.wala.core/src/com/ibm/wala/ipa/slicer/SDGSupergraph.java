/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.ipa.slicer;

import java.util.Iterator;

import com.ibm.wala.dataflow.IFDS.ISupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.util.collections.EmptyIterator;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.collections.FilterIterator;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.intset.IntSet;

/**
 * A wrapper around an SDG to make it look like a supergraph for tabulation.
 * 
 * @author sjfink
 */
class SDGSupergraph implements ISupergraph<Statement, PDG> {

  private final ISDG sdg;

  /**
   * Do a backward slice?
   */
  private final boolean backward;

  public SDGSupergraph(ISDG sdg, boolean backward) {
    this.sdg = sdg;
    this.backward = backward;
  }

  public Graph<PDG> getProcedureGraph() {
    Assertions.UNREACHABLE();
    return null;
  }

  public Object[] getEntry(Statement n) {
    Assertions.UNREACHABLE();
    return null;
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#classifyEdge(java.lang.Object, java.lang.Object)
   */
  public byte classifyEdge(Statement src, Statement dest) {
    Assertions.UNREACHABLE();
    return 0;
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#getCallSites(java.lang.Object)
   */
  public Iterator<? extends Statement> getCallSites(Statement r, PDG callee) {
    switch (r.getKind()) {
    case EXC_RET_CALLER: {
      ExceptionalReturnCaller n = (ExceptionalReturnCaller) r;
      SSAAbstractInvokeInstruction call = n.getInstruction();
      PDG pdg = getProcOf(r);
      return pdg.getCallerParamStatements(call).iterator();
    }
    case NORMAL_RET_CALLER: {
      NormalReturnCaller n = (NormalReturnCaller) r;
      SSAAbstractInvokeInstruction call = n.getInstruction();
      PDG pdg = getProcOf(r);
      return pdg.getCallerParamStatements(call).iterator();
    }
    case HEAP_RET_CALLER: {
      HeapStatement.HeapReturnCaller n = (HeapStatement.HeapReturnCaller) r;
      SSAAbstractInvokeInstruction call = n.getCall();
      PDG pdg = getProcOf(r);
      return pdg.getCallerParamStatements(call).iterator();
    }
    default:
      Assertions.UNREACHABLE(r.getKind().toString());
      return null;
    }
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#getCalledNodes(java.lang.Object)
   */
  public Iterator<? extends Statement> getCalledNodes(Statement call) {
    switch (call.getKind()) {
    case NORMAL:
      Filter f = new Filter() {
        public boolean accepts(Object o) {
          Statement s = (Statement) o;
          return isEntry(s);
        }
      };
      return new FilterIterator<Statement>(getSuccNodes(call), f);
    case PARAM_CALLER:
    case HEAP_PARAM_CALLER:
      return getSuccNodes(call);
    default:
      Assertions.UNREACHABLE(call.getKind().toString());
      return null;
    }
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#getEntriesForProcedure(java.lang.Object)
   */
  public Statement[] getEntriesForProcedure(PDG procedure) {
    Statement[] normal = procedure.getParamCalleeStatements();
    Statement[] result = new Statement[normal.length + 1];
    result[0] = new MethodEntryStatement(procedure.getCallGraphNode());
    System.arraycopy(normal, 0, result, 1, normal.length);
    return result;
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#getExitsForProcedure(java.lang.Object)
   */
  public Statement[] getExitsForProcedure(PDG procedure) {
    Statement[] normal = procedure.getReturnStatements();
    Statement[] result = new Statement[normal.length + 1];
    result[0] = new MethodExitStatement(procedure.getCallGraphNode());
    System.arraycopy(normal, 0, result, 1, normal.length);
    return result;
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#getLocalBlock(java.lang.Object, int)
   */
  public Statement getLocalBlock(PDG procedure, int i) {
    return procedure.getNode(i);
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#getLocalBlockNumber(java.lang.Object)
   */
  public int getLocalBlockNumber(Statement n) {
    PDG pdg = getProcOf(n);
    return pdg.getNumber(n);
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#getNormalSuccessors(java.lang.Object)
   */
  public Iterator<Statement> getNormalSuccessors(Statement call) {
    if (!backward) {
      return EmptyIterator.instance();
    } else {
      Assertions.UNREACHABLE();
      return null;
    }
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#getNumberOfBlocks(java.lang.Object)
   */
  public int getNumberOfBlocks(PDG procedure) {
    Assertions.UNREACHABLE();
    return 0;
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#getProcOf(java.lang.Object)
   */
  public PDG getProcOf(Statement n) {
    CGNode node = n.getNode();
    PDG result = sdg.getPDG(node);
    if (result == null) {
      Assertions.UNREACHABLE("panic: " + n + " " + node);
    }
    return result;
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#getReturnSites(java.lang.Object)
   */
  public Iterator<? extends Statement> getReturnSites(Statement call, PDG callee) {
    switch (call.getKind()) {
    case PARAM_CALLER: {
      ParamCaller n = (ParamCaller) call;
      SSAAbstractInvokeInstruction st = n.getInstruction();
      PDG pdg = getProcOf(call);
      return pdg.getCallerReturnStatements(st).iterator();
    }
    case HEAP_PARAM_CALLER: {
      HeapStatement.HeapParamCaller n = (HeapStatement.HeapParamCaller) call;
      SSAAbstractInvokeInstruction st = n.getCall();
      PDG pdg = getProcOf(call);
      return pdg.getCallerReturnStatements(st).iterator();
    }
    case NORMAL: {
      NormalStatement n = (NormalStatement) call;
      SSAAbstractInvokeInstruction st = (SSAAbstractInvokeInstruction) n.getInstruction();
      PDG pdg = getProcOf(call);
      return pdg.getCallerReturnStatements(st).iterator();
    }
    default:
      Assertions.UNREACHABLE(call.getKind().toString());
      return null;
    }
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#isCall(java.lang.Object)
   */
  public boolean isCall(Statement n) {
    switch (n.getKind()) {
    case EXC_RET_CALLEE:
    case EXC_RET_CALLER:
    case HEAP_PARAM_CALLEE:
    case NORMAL_RET_CALLEE:
    case NORMAL_RET_CALLER:
    case PARAM_CALLEE:
    case PHI:
    case HEAP_RET_CALLEE:
    case HEAP_RET_CALLER:
    case METHOD_ENTRY:
    case METHOD_EXIT:
    case CATCH:
    case PI:
      return false;
    case HEAP_PARAM_CALLER:
    case PARAM_CALLER:
      return true;
    case NORMAL:
      if (sdg.getCOptions().equals(ControlDependenceOptions.NONE)) {
        return false;
      } else {
        NormalStatement s = (NormalStatement) n;
        return s.getInstruction() instanceof SSAAbstractInvokeInstruction;
      }
    default:
      Assertions.UNREACHABLE(n.getKind() + " " + n.toString());
      return false;
    }
  }

  public boolean isEntry(Statement n) {
    switch (n.getKind()) {
    case PARAM_CALLEE:
    case HEAP_PARAM_CALLEE:
    case METHOD_ENTRY:
      return true;
    case PHI:
    case PI:
    case NORMAL_RET_CALLER:
    case PARAM_CALLER:
    case HEAP_RET_CALLER:
    case NORMAL:
    case EXC_RET_CALLEE:
    case EXC_RET_CALLER:
    case HEAP_PARAM_CALLER:
    case HEAP_RET_CALLEE:
    case NORMAL_RET_CALLEE:
    case CATCH:
      return false;
    default:
      Assertions.UNREACHABLE(n.toString());
      return false;
    }
  }

  public boolean isExit(Statement n) {
    switch (n.getKind()) {
    case PARAM_CALLEE:
    case HEAP_PARAM_CALLEE:
    case PHI:
    case PI:
    case NORMAL_RET_CALLER:
    case PARAM_CALLER:
    case HEAP_RET_CALLER:
    case NORMAL:
    case EXC_RET_CALLER:
    case METHOD_ENTRY:
    case CATCH:
      return false;
    case HEAP_RET_CALLEE:
    case EXC_RET_CALLEE:
    case NORMAL_RET_CALLEE:
    case METHOD_EXIT:
      return true;
    default:
      Assertions.UNREACHABLE(n.toString());
      return false;
    }
  }

  public boolean isReturn(Statement n) {
    switch (n.getKind()) {
    case EXC_RET_CALLER:
    case NORMAL_RET_CALLER:
    case HEAP_RET_CALLER:
      return true;
    case EXC_RET_CALLEE:
    case HEAP_PARAM_CALLEE:
    case HEAP_PARAM_CALLER:
    case HEAP_RET_CALLEE:
    case NORMAL:
    case NORMAL_RET_CALLEE:
    case PARAM_CALLEE:
    case PARAM_CALLER:
    case PHI:
    case PI:
    case METHOD_ENTRY:
    case CATCH:
      return false;
    default:
      Assertions.UNREACHABLE(n.getKind().toString());
      return false;
    }
  }

  public void removeNodeAndEdges(Statement N) {
    Assertions.UNREACHABLE();

  }

  public void addNode(Statement n) {
    Assertions.UNREACHABLE();

  }

  public boolean containsNode(Statement N) {
    return sdg.containsNode(N);
  }

  public int getNumberOfNodes() {
    Assertions.UNREACHABLE();
    return 0;
  }

  public Iterator<Statement> iterator() {
    return sdg.iterator();
  }

  public void removeNode(Statement n) {
    Assertions.UNREACHABLE();

  }

  public void addEdge(Statement src, Statement dst) {
    Assertions.UNREACHABLE();

  }

  public int getPredNodeCount(Statement N) {
    Assertions.UNREACHABLE();
    return 0;
  }

  public Iterator<? extends Statement> getPredNodes(Statement N) {
    return sdg.getPredNodes(N);
  }

  public int getSuccNodeCount(Statement N) {
    Assertions.UNREACHABLE();
    return 0;
  }

  public Iterator<? extends Statement> getSuccNodes(Statement N) {
    return sdg.getSuccNodes(N);
  }

  public boolean hasEdge(Statement src, Statement dst) {
    return sdg.hasEdge(src, dst);
  }

  public void removeAllIncidentEdges(Statement node) {
    Assertions.UNREACHABLE();

  }

  public void removeEdge(Statement src, Statement dst) {
    Assertions.UNREACHABLE();

  }

  public void removeIncomingEdges(Statement node) {
    Assertions.UNREACHABLE();

  }

  public void removeOutgoingEdges(Statement node) {
    Assertions.UNREACHABLE();

  }

  public int getMaxNumber() {
    return sdg.getMaxNumber();
  }

  public Statement getNode(int number) {
    return sdg.getNode(number);
  }

  public int getNumber(Statement N) {
    return sdg.getNumber(N);
  }

  public Iterator<Statement> iterateNodes(IntSet s) {
    Assertions.UNREACHABLE();
    return null;
  }

  public IntSet getPredNodeNumbers(Statement node) {
    return sdg.getPredNodeNumbers(node);
  }

  /*
   * @see com.ibm.wala.util.graph.NumberedEdgeManager#getSuccNodeNumbers(java.lang.Object)
   */
  public IntSet getSuccNodeNumbers(Statement node) {
    return sdg.getSuccNodeNumbers(node);
  }

}
