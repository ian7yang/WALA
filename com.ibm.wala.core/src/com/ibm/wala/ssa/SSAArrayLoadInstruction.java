/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.ssa;

import java.util.Collection;

import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.Exceptions;
import com.ibm.wala.util.debug.Assertions;

/**
 * SSA instruction representing an array load.
 * 
 * @author sfink
 * 
 */
public class SSAArrayLoadInstruction extends SSAArrayReferenceInstruction {
  private final int result;

  SSAArrayLoadInstruction(int result, int arrayref, int index, TypeReference elementType) {
    super(arrayref, index, elementType);
    this.result = result;
  }

  @Override
  public SSAInstruction copyForSSA(int[] defs, int[] uses) throws IllegalArgumentException {
    if (defs != null && defs.length == 0) {
      throw new IllegalArgumentException("defs.length == 0");
    }
    if (uses != null && uses.length < 2) {
      throw new IllegalArgumentException("uses.length < 2");
    }
    return new SSAArrayLoadInstruction(defs == null ? result : defs[0], uses == null ? getArrayRef() : uses[0],
        uses == null ? getIndex() : uses[1], getElementType());
  }

  @Override
  public String toString(SymbolTable symbolTable, ValueDecorator d) {
    return getValueString(symbolTable, d, result) + " = arrayload " + getValueString(symbolTable, d, getArrayRef()) + "["
        + getValueString(symbolTable, d, getIndex()) + "]";
  }

  /**
   * @see com.ibm.wala.ssa.SSAInstruction#visit(IVisitor)
   * @throws IllegalArgumentException
   *             if v is null
   */
  @Override
  public void visit(IVisitor v) {
    if (v == null) {
      throw new IllegalArgumentException("v is null");
    }
    v.visitArrayLoad(this);
  }

  /**
   * @see com.ibm.wala.ssa.SSAInstruction#getDef()
   */
  @Override
  public boolean hasDef() {
    return true;
  }

  @Override
  public int getDef() {
    return result;
  }

  @Override
  public int getDef(int i) {
    Assertions._assert(i == 0);
    return result;
  }

  @Override
  public int getNumberOfDefs() {
    return 1;
  }

  @Override
  public int hashCode() {
    return 6311 * result ^ 2371 * getArrayRef() + getIndex();
  }

  /*
   * @see com.ibm.wala.ssa.Instruction#getExceptionTypes()
   */
  @Override
  public Collection<TypeReference> getExceptionTypes() {
    return Exceptions.getArrayAccessExceptions();
  }

}
